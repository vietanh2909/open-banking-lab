package com.navi.ciam.token;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenResponseMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.services.ServicesLogger;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Keycloak 26.3.3 - Protocol Mapper
 *
 * Rule:
 * - AIS  -> access token TTL = 3600s, keep refresh_token
 * - PIS  -> access token TTL = 300s, remove refresh_token
 *
 * Data source:
 * - primary: AuthenticationSession auth note "ob_profile" (set by ConsentAuthenticator in /authorize flow)
 * - fallback: derive from granted scope string
 */
public class ObTtlByScopeMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCAccessTokenResponseMapper {

    public static final String PROVIDER_ID = "ob-ttl-by-scope-mapper";

    // Optional: if you want to use different keywords
    private static final int AIS_TTL_SECONDS = 3600;
    private static final int PIS_TTL_SECONDS = 300;

    private static final ServicesLogger LOG = ServicesLogger.LOGGER;

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayCategory() {
        return "OpenBanking";
    }

    @Override
    public String getDisplayType() {
        return "OB - TTL by Scope (AIS/PIS)";
    }

    @Override
    public String getHelpText() {
        return "Set access token TTL by profile derived from auth session note (ob_profile) or granted scopes. "
                + "AIS=3600s (with refresh), PIS=300s (no refresh).";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public AccessToken transformAccessToken(
            AccessToken token,
            ProtocolMapperModel mappingModel,
            KeycloakSession session,
            UserSessionModel userSession,
            ClientSessionContext clientSessionCtx) {

        String grantedScope = clientSessionCtx != null ? clientSessionCtx.getScopeString() : null; // official API
        LOG.info("TOKEN-LISTENER-SPI|transformAccessToken|grantedScope: " + grantedScope);
        String profile = deriveProfileFromScope(grantedScope);
        LOG.info("TOKEN-LISTENER-SPI|transformAccessToken|profile: " + profile);
        long now = Instant.now().getEpochSecond();
        if ("PIS".equals(profile)) {
            token.exp(now + PIS_TTL_SECONDS);
        } else if ("AIS".equals(profile)) {
            token.exp(now + AIS_TTL_SECONDS);
        }

        // ===== ADD consent_id claim =====
        // nguồn: UserSession note (do ConsentAuthenticator setUserSessionNote)
        String consentId = userSession != null ? userSession.getNote("consent_id") : null;

        // optional: chỉ add khi đã approve
        String approved = userSession != null ? userSession.getNote("consent_approved") : null;

        if (consentId != null && !consentId.isBlank() && "true".equals(approved)) {
            token.getOtherClaims().put("consent_id", consentId);
            LOG.info("TOKEN-LISTENER-SPI|transformAccessToken|added consent_id=" + consentId);
        } else {
            LOG.info("TOKEN-LISTENER-SPI|transformAccessToken|consent_id not added (missing or not approved)");
        }

        return token;
    }

    @Override
    protected void setClaim(
            AccessTokenResponse accessTokenResponse,
            ProtocolMapperModel mappingModel,
            UserSessionModel userSession,
            KeycloakSession keycloakSession,
            ClientSessionContext clientSessionCtx) {

        String grantedScope = clientSessionCtx != null ? clientSessionCtx.getScopeString() : null;
        String profile = deriveProfileFromScope(grantedScope);

        LOG.info("TOKEN-LISTENER-SPI|setClaim|profile: " + profile);
        // PIS: no refresh token
        if ("PIS".equals(profile)) {
            accessTokenResponse.setRefreshToken(null);
        }
    }

    private String deriveProfileFromScope(String scopeString) {
        String s = normalize(scopeString);

        // TODO: chỉnh theo naming scope thật của bạn
        boolean isAis = containsAny(s,
                " ais ",
                " accounts.read ",
                " balances.read ",
                " transactions.read ",
                " accounts "
        );

        boolean isPis = containsAny(s,
                " pis ",
                " payments ",
                " payment.initiation "
        );

        // Fail-safe nếu bị mix: chọn PIS (TTL ngắn + không refresh)
        if (isAis && isPis) return "PIS";
        if (isPis) return "PIS";
        if (isAis) return "AIS";
        return "NONE";
    }

    private String normalize(String s) {
        if (s == null) return " ";
        return " " + s.toLowerCase().trim() + " ";
    }

    private boolean containsAny(String haystack, String... needles) {
        for (String n : needles) {
            if (haystack.contains(n)) return true;
        }
        return false;
    }
}