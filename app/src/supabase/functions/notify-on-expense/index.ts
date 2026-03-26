import { createClient } from "https://esm.sh/@supabase/supabase-js@2";
import { GoogleAuth } from "https://esm.sh/google-auth-library@9";

Deno.serve(async (req) => {
  const { record } = await req.json();

  const supabase = createClient(
    Deno.env.get("SUPABASE_URL")!,
    Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!
  );

  const paidBy       = record.paid_by_user_id as string;
  const merchant     = (record.merchant as string) ?? "an expense";
  const groupId      = record.group_id as string;
  const amountCents  = record.amount_cents as number;
  const currency     = (record.currency as string) ?? "USD";
  const isSettlement = record.split_method === "SETTLEMENT";

  // Look up payer name, group name, and members in parallel
  const [payerResult, groupResult, membersResult] = await Promise.all([
    supabase
      .from("profiles")
      .select("first_name, last_name")
      .eq("id", paidBy)
      .single(),
    supabase
      .from("groups")
      .select("name")
      .eq("id", groupId)
      .single(),
    supabase
      .from("group_members")
      .select("user_id")
      .eq("group_id", groupId)
      .neq("user_id", paidBy),
  ]);

  if (!membersResult.data?.length) return new Response("no recipients", { status: 200 });

  const payerName  = payerResult.data
    ? `${payerResult.data.first_name ?? ""} ${payerResult.data.last_name ?? ""}`.trim()
    : "Someone";
  const groupName  = groupResult.data?.name ?? "your group";

  // Format amount e.g. $12.50 or ₹1,000.00
  const formatted = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
  }).format(amountCents / 100);

  const title = isSettlement
    ? `${payerName} settled up`
    : `${payerName} added an expense`;

  const body = isSettlement
    ? `${payerName} recorded a ${formatted} settlement in ${groupName}.`
    : `${payerName} paid ${formatted} for "${merchant}" in ${groupName}.`;

  // Get FCM tokens for all group members except the payer
  const userIds = membersResult.data.map((m) => m.user_id as string);
  const { data: tokens } = await supabase
    .from("push_tokens")
    .select("token")
    .in("user_id", userIds);

  if (!tokens?.length) return new Response("no tokens", { status: 200 });

  // Get OAuth2 access token from service account
  const serviceAccount = JSON.parse(Deno.env.get("FCM_SERVICE_ACCOUNT_KEY")!);
  const auth = new GoogleAuth({
    credentials: serviceAccount,
    scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
  });
  const accessToken = await auth.getAccessToken();
  const projectId = serviceAccount.project_id;

  await Promise.all(tokens.map(({ token }) =>
    fetch(`https://fcm.googleapis.com/v1/projects/${projectId}/messages:send`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        message: {
          token,
          notification: { title, body },
          data: {
            type:     isSettlement ? "settlement" : "expense",
            groupId,
            merchant,
          },
        },
      }),
    })
  ));

  return new Response("ok", { status: 200 });
});
