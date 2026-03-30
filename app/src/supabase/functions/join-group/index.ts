Deno.serve((req) => {
  const url = new URL(req.url);
  const groupId = url.searchParams.get("groupId") ?? "";
  const groupName = url.searchParams.get("groupName") ?? "";

  const deepLink = `divvy://join/${encodeURIComponent(groupId)}` +
    (groupName ? `?groupName=${encodeURIComponent(groupName)}` : "");

  const displayName = groupName || "a group";

  const html = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Join ${displayName} on Divvy</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      display: flex; align-items: center; justify-content: center;
      min-height: 100vh; background: #f5f5f5; color: #1a1a1a;
    }
    .card {
      background: white; border-radius: 16px; padding: 32px;
      max-width: 400px; width: 90%; text-align: center;
      box-shadow: 0 2px 12px rgba(0,0,0,0.08);
    }
    h1 { font-size: 22px; margin-bottom: 8px; }
    p  { font-size: 15px; color: #666; margin-bottom: 24px; }
    .btn {
      display: inline-block; background: #6750A4; color: white;
      padding: 14px 32px; border-radius: 12px; text-decoration: none;
      font-size: 16px; font-weight: 600;
    }
  </style>
</head>
<body>
  <div class="card">
    <h1>Join ${displayName} on Divvy</h1>
    <p>Tap the button below to open the Divvy app and join this group.</p>
    <a class="btn" href="${deepLink}">Open in Divvy</a>
  </div>
  <script>
    if (/Android/i.test(navigator.userAgent)) {
      window.location.href = "${deepLink}";
    }
  </script>
</body>
</html>`;

  return new Response(html, {
    status: 200,
    headers: {
      "Content-Type": "text/html; charset=utf-8",
      "X-Content-Type-Options": "nosniff",
    },
  });
});
