import { randomString, makeCodeChallenge } from "./pkce";

const ISSUER = import.meta.env.VITE_CIAM_ISSUER; // .../realms/<realm>
const CLIENT_ID = import.meta.env.VITE_CIAM_CLIENT_ID;
const FINTECH_BASE = import.meta.env.VITE_FINTECH_BASE_URL;

export async function startCiamLogin(returnTo = "/dashboard") {
  const authorize = `${ISSUER}/protocol/openid-connect/auth`;

  const redirectUri = `${FINTECH_BASE}/oidc/callback`;
  const state = randomString(32);
  const verifier = randomString(64);
  const challenge = await makeCodeChallenge(verifier);

  // store to validate callback
  sessionStorage.setItem("pkce_verifier", verifier);
  sessionStorage.setItem("oidc_state", state);
  sessionStorage.setItem("return_to", returnTo);

  const params = new URLSearchParams({
    client_id: CLIENT_ID,
    response_type: "code",
    scope: "openid profile email accounts.read",
    redirect_uri: redirectUri,
    state,
    code_challenge: challenge,
    code_challenge_method: "S256"
  });

  window.location.href = `${authorize}?${params.toString()}`;
}
