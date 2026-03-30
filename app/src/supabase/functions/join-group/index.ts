Deno.serve((req) => {
  const url = new URL(req.url);
  const groupId = url.searchParams.get("groupId") ?? "";
  const groupName = url.searchParams.get("groupName") ?? "";

  const deepLink = `divvy://join/${encodeURIComponent(groupId)}` +
    (groupName ? `?groupName=${encodeURIComponent(groupName)}` : "");

  return new Response(null, {
    status: 302,
    headers: { "Location": deepLink },
  });
});
