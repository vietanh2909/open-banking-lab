import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";

// Ở phương án chuẩn: callback gọi BFF/backend để exchange code -> token
async function exchangeCodeWithBackend({ code, codeVerifier, redirectUri }) {
  // Ví dụ endpoint BFF bạn sẽ tạo: /api/auth/ciam/callback
  const res = await fetch("/api/auth/ciam/callback", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ code, codeVerifier, redirectUri })
  });

  if (!res.ok) throw new Error("Exchange failed");
  return res.json(); // { access_token, refresh_token, id_token, ... } hoặc session
}

export default function OidcCallback() {
  const nav = useNavigate();

  useEffect(() => {
    (async () => {
      const url = new URL(window.location.href);
      const code = url.searchParams.get("code");
      const state = url.searchParams.get("state");

      const expectedState = sessionStorage.getItem("oidc_state");
      const verifier = sessionStorage.getItem("pkce_verifier");
      const returnTo = sessionStorage.getItem("return_to") || "/dashboard";

      if (!code || !state || state !== expectedState || !verifier) {
        alert("OIDC callback invalid (missing code/state or state mismatch)");
        nav("/login", { replace: true });
        return;
      }

      const redirectUri = `${import.meta.env.VITE_FINTECH_BASE_URL}/oidc/callback`;

      try {
        const session = await exchangeCodeWithBackend({ code, codeVerifier: verifier, redirectUri });

        // Lưu session/token (demo)
        localStorage.setItem("fintech_session", JSON.stringify({
          ...session,
          loggedInAt: Date.now()
        }));

        // cleanup
        sessionStorage.removeItem("oidc_state");
        sessionStorage.removeItem("pkce_verifier");

        nav(returnTo, { replace: true });
      } catch (e) {
        console.error(e);
        alert("Login failed");
        nav("/login", { replace: true });
      }
    })();
  }, [nav]);

  return (
    <div style={{ padding: 24, fontFamily: "system-ui" }}>
      Đang xử lý đăng nhập CIAM...
    </div>
  );
}
