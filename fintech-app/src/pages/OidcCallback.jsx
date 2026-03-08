import React, { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";

const API_BASE = import.meta.env.VITE_API_BASE_URL;
const FINTECH_BASE = import.meta.env.VITE_FINTECH_BASE_URL;
function decodeJwtPayload(token) {
  const parts = (token || "").split(".");
  if (parts.length < 2) return null;

  const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
  const padded = base64 + "===".slice((base64.length + 3) % 4);

  try {
    // UTF-8 safe decode
    const bytes = Uint8Array.from(atob(padded), (c) => c.charCodeAt(0));
    const json = new TextDecoder("utf-8").decode(bytes);
    return JSON.parse(json);
  } catch {
    return null;
  }
}

function safeReturnTo(value) {
  // chỉ cho phép internal route dạng "/..."
  if (typeof value !== "string") return "/dashboard";
  if (!value.startsWith("/")) return "/dashboard";
  return value;
}

export default function OidcCallback() {
  const navigate = useNavigate();
  const ranRef = useRef(false);

  useEffect(() => {
    if (ranRef.current) return; // ✅ tránh chạy 2 lần (StrictMode dev)
    ranRef.current = true;

    (async () => {
      const url = new URL(window.location.href);
      const code = url.searchParams.get("code");
      const state = url.searchParams.get("state");
      const error = url.searchParams.get("error");
      const errorDesc = url.searchParams.get("error_description");

      if (error) {
        console.error("OIDC error", { error, errorDesc });
        alert("CIAM trả về lỗi xác thực. Vui lòng thử lại.");
        navigate("/login", { replace: true });
        return;
      }

      const expectedState = sessionStorage.getItem("oidc_state");
      const codeVerifier = sessionStorage.getItem("pkce_verifier");
      const returnTo = safeReturnTo(sessionStorage.getItem("return_to") || "/dashboard");
      const flow = sessionStorage.getItem("oidc_flow") || "AIS_LINK";

      // ===== validate callback =====
      if (!code || !state || state !== expectedState || !codeVerifier) {
        console.error("OIDC callback invalid", { code, state, expectedState, hasVerifier: !!codeVerifier });
        alert("OIDC callback không hợp lệ. Vui lòng đăng nhập lại.");
        navigate("/login", { replace: true });
        return;
      }

      const redirectUri = `${FINTECH_BASE}/oidc/callback`;

      try {
        const endpoint = "/api/auth/ciam/callback";

        const res = await fetch(endpoint, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ code, codeVerifier, redirectUri }),
        });

        if (!res.ok) {
          const text = await res.text().catch(() => "");
          throw new Error(`Token exchange failed: ${res.status} ${text}`);
        }

        const tokenResponse = await res.json();
        const accessToken = tokenResponse.access_token;

        // ===== lưu session FE (PoC) =====
        // Production: KHÔNG nên lưu access token ở localStorage
        localStorage.setItem(
          "fintech_session",
          JSON.stringify({
            loggedInAt: Date.now(),
            flow,
            // PoC debug:
            accessToken,
          })
        );

        // ===== AIS_LINK: lưu trạng thái liên kết + expire =====
        if (flow === "AIS_LINK") {
          const payload = decodeJwtPayload(accessToken);
          const expSec = payload?.exp;
          const expiresAtMs = expSec
            ? expSec * 1000
            : Date.now() + (tokenResponse.expires_in || 3600) * 1000;

          localStorage.setItem(
            "finwallet_linked_bank",
            JSON.stringify({
              name: "Maritime Bank",
              bankCode: "MSB",
              masked: "**** **** **** 4590",
              linked: true,
              linkedAt: Date.now(),
              exp: expiresAtMs,
            })
          );
        }

        // ===== PIS_PAY flow =====
        if (flow === "PIS_PAY") {
          localStorage.setItem(
            "last_pis_result",
            JSON.stringify({
              status: "PENDING",
              message: "Xác thực CIAM thành công. Đang xử lý thanh toán...",
              at: Date.now(),
            })
          );
        }

        // ===== cleanup =====
        sessionStorage.removeItem("oidc_state");
        sessionStorage.removeItem("pkce_verifier");
        sessionStorage.removeItem("oidc_flow");
        sessionStorage.removeItem("return_to");

        navigate(returnTo, { replace: true });
      } catch (err) {
        console.error("OIDC callback error", err);
        alert("Đăng nhập CIAM thất bại. Vui lòng thử lại.");
        navigate("/login", { replace: true });
      }
    })();
  }, [navigate]);

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontFamily: "system-ui",
        color: "#0f172a",
      }}
    >
      <div style={{ textAlign: "center" }}>
        <h2>Đang xử lý xác thực CIAM…</h2>
        <p>Vui lòng không đóng trình duyệt.</p>
      </div>
    </div>
  );
}