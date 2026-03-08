package com.navi.ciam.consent;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class ConsentAuthenticatorFactory implements AuthenticatorFactory {
    public static final String ID = "ciam-consent-authenticator";

    @Override public String getId() { return ID; }
    @Override public String getDisplayType() { return "CIAM Consent (PoC)"; }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override public String getHelpText() { return "Shows consent page and calls consent-service to approve/reject."; }
    @Override public Authenticator create(KeycloakSession session) { return new ConsentAuthenticator(); }
    @Override public void init(org.keycloak.Config.Scope config) {}
    @Override public void postInit(KeycloakSessionFactory factory) {}
    @Override public void close() {}

    @Override
    public boolean isConfigurable() { return true; }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty p = new ProviderConfigProperty();
        p.setName("consentServiceBaseUrl");
        p.setLabel("Consent Service Base URL");
        p.setHelpText("Example: http://consent-service:8082");
        p.setType(ProviderConfigProperty.STRING_TYPE);
        p.setDefaultValue("http://consent-service:8082");
        return List.of(p);
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }
}
