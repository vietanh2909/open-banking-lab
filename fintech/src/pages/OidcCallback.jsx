import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";

const API_BASE = import.meta.env.VITE_API_BASE_URL;
const FINTECH_BASE = import.meta.env.VITE_FINTECH_BASE_URL;

function decodeJwtPayload(token) {
  // token: header.payload.signature
  const parts = (token || "").split(".");
  if (parts.length < 2) return null;
  const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
  const padded = base64 + "===".slice((base64.length + 3) % 4);
  try {
    return JSON.parse(decodeURIComponent(escape(atob(padded))));
  } catch {
    return null;
  }
}

export default function OidcCallback() {
  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      const url = new URL(window.location.href);
      const code = url.searchParams.get("code");
      const state = url.searchParams.get("state");

      const expectedState = sessionStorage.getItem("oidc_state");
      const codeVerifier = sessionStorage.getItem("pkce_verifier");
      const returnTo = sessionStorage.getItem("return_to") || "/dashboard";
      const flow = sessionStorage.getItem("oidc_flow") || "AIS_LINK";

      // ===== validate callback =====
      if (!code || !state || state !== expectedState || !codeVerifier) {
        console.error("OIDC callback invalid", { code, state, expectedState });
        alert("OIDC callback không hợp lệ. Vui lòng đăng nhập lại.");
        navigate("/login", { replace: true });
        return;
      }

      const redirectUri = `${FINTECH_BASE}/oidc/callback`;

      try {
        // ===== exchange code -> token via fintech-service =====
        const res = await fetch("/api/auth/ciam/callback", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            code,
            codeVerifier,
            redirectUri
          })
        });

        if (!res.ok) {
          const text = await res.text();
          throw new Error(`Token exchange failed: ${res.status} ${text}`);
        }

        const tokenResponse = await res.json();
        const accessToken = tokenResponse.access_token;

        // ===== lưu session FE (PoC) =====
        localStorage.setItem(
          "fintech_session",
          JSON.stringify({
            loggedInAt: Date.now(),
            flow,
            // demo: lưu access_token để debug, production KHÔNG nên
            accessToken: tokenResponse.access_token
          })
        );

        // ===== ⭐ AIS_LINK: lưu trạng thái liên kết + expire time (exp) =====
        if (flow === "AIS_LINK") {
          const payload = decodeJwtPayload(accessToken);
          const expSec = payload?.exp; // seconds
          const expiresAtMs = expSec ? expSec * 1000 : (Date.now() + (tokenResponse.expires_in || 3600) * 1000);

          localStorage.setItem("bank_link_state", JSON.stringify({
            linked: true,
            bankCode: "MSB",                 // PoC: bạn có thể thay bằng bank user chọn
            bankName: "Ngân hàng MSB",
            linkedAt: Date.now(),
            expiresAt: expiresAtMs
          }));
        }

        // ===== phân nhánh theo flow =====
        if (flow === "PIS_PAY") {
          localStorage.setItem(
            "last_pis_result",
            JSON.stringify({
              status: "PENDING",
              message: "Xác thực CIAM thành công. Đang xử lý thanh toán...",
              at: Date.now()
            })
          );
        }

        // ===== cleanup =====
        sessionStorage.removeItem("oidc_state");
        sessionStorage.removeItem("pkce_verifier");
        sessionStorage.removeItem("oidc_flow");
        sessionStorage.removeItem("return_to");

        // ===== back to dashboard =====
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
        color: "#0f172a"
      }}
    >
      <div style={{ textAlign: "center" }}>
        <h2>Đang xử lý xác thực CIAM…</h2>
        <p>Vui lòng không đóng trình duyệt.</p>
      </div>
    </div>
  );
}