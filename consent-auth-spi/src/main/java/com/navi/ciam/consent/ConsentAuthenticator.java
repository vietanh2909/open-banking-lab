package com.navi.ciam.consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navi.ciam.consent.dto.AccountConsent;
import com.navi.ciam.consent.dto.ApproveConsentRequest;
import com.navi.ciam.consent.dto.Permission;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConsentAuthenticator implements Authenticator {
    private static final ServicesLogger LOG = ServicesLogger.LOGGER;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        // ====== 1. GUARD: chưa có user thì bỏ qua step này ======
        UserModel user = context.getUser();
        if (user == null) {
            LOG.debug("CONSENT|authenticate|user is null → skip (attempted)");
            context.attempted();
            return;
        }

        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        // ====== 2. Chỉ skip nếu ĐÃ approve trong session hiện tại ======
        String consentApproved = authSession.getAuthNote("consent_approved");
        if ("true".equals(consentApproved)) {
            LOG.info("CONSENT|authenticate|already approved in this session → success");
            context.success();
            return;
        }

        try {
            String psuId = user.getId();
            String clientUUId = authSession.getClient().getId();
            String clientId = authSession.getClient().getClientId();
            String realmName = context.getRealm().getName();
            String scopes = authSession.getClientNote("scope");
            String transactionId = UUID.randomUUID().toString();

            LOG.info("CONSENT|authenticate|user= " + user.getUsername());
            LOG.info("CONSENT|authenticate|client= "+ clientId);
            LOG.info("CONSENT|authenticate|scopes= " + scopes);

            // ====== 3. Normalize & decide whether consent is needed ======
            if (scopes == null || scopes.isBlank()) {
                scopes = "";
            }

            // 1) Phân loại profile theo scope
            String obProfile = resolveObProfile(scopes); // "AIS" | "PIS" | "NONE"

            // 2) (optional) chặn scope không hợp lệ
            if ("INVALID".equals(obProfile)) {
                context.failureChallenge(
                        AuthenticationFlowError.INVALID_CLIENT_SESSION,
                        context.form().setError("Invalid scope combination").createErrorPage(Response.Status.BAD_REQUEST)
                );
                return;
            }

            // 3) Lưu profile vào session để bước /token đọc lại
            authSession.setAuthNote("ob_profile", obProfile);

            // Ví dụ: chỉ consent nếu có scope nghiệp vụ
            if ("NONE".equals(obProfile)) {
                LOG.info("CONSENT|authenticate|no business scope → skip consent");
                context.success();
                return;
            }

            String consentServiceBaseUrl =
                    getConfig(context, "consentServiceBaseUrl", "http://consent-service:8082");

            // ====== 4. Create consent ======
            Map<String, Object> payload = Map.of(
                    "tppId", clientId,
                    "providerId", realmName,
                    "clientId", clientId,
                    "scopeText", scopes,
                    "purpose", obProfile

            );
            //"expiresAt", Instant.now().plusSeconds(90L * 24 * 3600).toString()

            String body = MAPPER.writeValueAsString(payload);

            LOG.info("REQUEST BODY: " + body);
            LOG.info("REQUEST URL: " + consentServiceBaseUrl + "/internal/v1/ais/consents/init");

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(consentServiceBaseUrl + "/internal/v1/ais/consents/init"))
                    .header("Content-Type", "application/json")
                    .header("Request-ID", transactionId)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> resp = HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());
            LOG.info("RESPONSE STATUS INIT CONSENT: " + resp.statusCode());
            if (resp.statusCode() >= 300) {
                LOG.warnf("Consent create failed. status=%s body=%s",
                        resp.statusCode(), resp.body());
                context.failureChallenge(
                        AuthenticationFlowError.INTERNAL_ERROR,
                        context.form()
                                .setError("Cannot create consent")
                                .createErrorPage(Response.Status.BAD_GATEWAY)
                );
                return;
            }

            String createdConsentId =
                    MAPPER.readTree(resp.body()).get("consentId").asText();

            authSession.setAuthNote("consent_id", createdConsentId);  // dùng trong flow hiện tại
            authSession.setUserSessionNote("consent_id", createdConsentId); // dùng để mapper nhét vào token

            // ====== 5. Render consent UI ======
            Response challenge = context.form()
                    .setAttribute("consentId", createdConsentId)
                    .setAttribute("clientId", clientId)
                    .setAttribute("scopes", scopes)
                    .createForm("consent.ftl");

            LOG.info("CONSENT|authenticate|render consent UI");
            context.challenge(challenge);

        } catch (Exception e) {
            LOG.error("ConsentAuthenticator error", e);
            context.failureChallenge(
                    AuthenticationFlowError.INTERNAL_ERROR,
                    context.form()
                            .setError("Unexpected error")
                            .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR)
            );
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> form = context.getHttpRequest().getDecodedFormParameters();
        String decision = form.getFirst("decision"); // "approve" hoặc "reject"
        String consentId = context.getAuthenticationSession().getAuthNote("consent_id");
        String consentServiceBaseUrl = getConfig(context, "consentServiceBaseUrl", "http://consent-service:8082");
        LOG.info("Decision: " + decision);
        LOG.info("Consent ID: " + consentId);
        try {
            if ("approve".equals(decision)) {

                // ====== psuId lấy từ user đã authenticate ======
                UserModel user = context.getUser();
                if (user == null) {
                    context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                            context.form().setError("User not found").createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
                    return;
                }
                String psuId = user.getUsername();
                Permission permission = new Permission();
                permission.setBalances(true);
                permission.setTransactions(true);

                AccountConsent account = new AccountConsent();
                account.setAccountId("01234567890123456789012345678901");
                account.setPermissions(permission);

                ApproveConsentRequest request = new ApproveConsentRequest();
                request.setPsuId(psuId);
                request.setAccounts(List.of(account));

                String jsonBody = MAPPER.writeValueAsString(request);
                LOG.info("REQUEST BODY: " + jsonBody);
                LOG.info("REQUEST URL: " + consentServiceBaseUrl + "/internal/v1/ais/consents/" + consentId + "/approve");

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(consentServiceBaseUrl + "/internal/v1/ais/consents/" + consentId + "/approve"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();
                HttpClient http = HttpClient.newHttpClient();

                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                LOG.info("RESPONSE STATUS APPROVED CONSENT: " + resp.statusCode());
                if (resp.statusCode() >= 300) {
                    context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                            context.form().setError("Approve consent failed").createErrorPage(Response.Status.BAD_GATEWAY));
                    return;
                }

                context.getAuthenticationSession().setAuthNote("consent_approved", "true");
                context.getAuthenticationSession().setUserSessionNote("consent_approved", "true");
                context.success();
                return;
            }

            // reject
            context.cancelLogin();

        } catch (Exception e) {
            LOG.error("Consent action error", e);
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                    context.form().setError("Unexpected error").createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    private String getConfig(AuthenticationFlowContext context, String key, String def) {
        AuthenticatorConfigModel cfg = context.getAuthenticatorConfig();
        if (cfg == null || cfg.getConfig() == null) return def;
        return cfg.getConfig().getOrDefault(key, def);
    }

    private String resolveObProfile(String scopes) {
        // normalize: split theo space
        String s = " " + scopes.trim().toLowerCase() + " ";
        LOG.info("CONSENT|resolveObProfile|scope: " + s);
        boolean hasAis = s.contains("ais") || s.contains("accounts.read"); // tùy bạn map scope nào là AIS
        boolean hasPis = s.contains("pis") || s.contains("payments");      // tùy bạn map scope nào là PIS

        if (hasAis && hasPis) return "INVALID";   // nếu policy không cho mix
        if (hasAis) return "AIS";
        if (hasPis) return "PIS";
        return "NONE";
    }

    @Override public boolean requiresUser() { return false; }
    @Override public boolean configuredFor(org.keycloak.models.KeycloakSession session, org.keycloak.models.RealmModel realm, UserModel user) { return true; }
    @Override public void setRequiredActions(org.keycloak.models.KeycloakSession session, org.keycloak.models.RealmModel realm, UserModel user) {}
    @Override public void close() {}
}
