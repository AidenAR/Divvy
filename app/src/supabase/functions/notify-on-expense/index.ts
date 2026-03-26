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
  const isSettlement = record.split_method === "SETTLEMENT";

  // Get FCM tokens for all group members except the payer
  const { data: members } = await supabase
    .from("group_members")
    .select("user_id")
    .eq("group_id", groupId)
    .neq("user_id", paidBy);

  if (!members?.length) return new Response("no recipients", { status: 200 });

  const userIds = members.map((m) => m.user_id as string);

  const { data: tokens } = await supabase
    .from("push_tokens")
    .select("token")
    .in("user_id", userIds);

  if (!tokens?.length) return new Response("no tokens", { status: 200 });

  // Get an OAuth2 access token from the service account
  const serviceAccount = JSON.parse(Deno.env.get("FCM_SERVICE_ACCOUNT_KEY")!);
  const auth = new GoogleAuth({
    credentials: serviceAccount,
    scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
  });
  const accessToken = await auth.getAccessToken();
  const projectId = serviceAccount.project_id;

  const title = isSettlement ? "Settlement recorded" : `New expense: ${merchant}`;
  const body  = isSettlement
    ? "A settlement was recorded in your group."
    : `Someone added "${merchant}" — check your share.`;

  // Send to each token
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
          data: { type: isSettlement ? "settlement" : "expense" },
        },
      }),
    })
  ));

  return new Response("ok", { status: 200 });
});