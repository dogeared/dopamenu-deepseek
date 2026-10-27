package com.example.dopamenu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Keycloak connection details.
 * <p>
 * The public URL is what the browser sees (it must be reachable from the user's
 * machine). The internal URL is used for back-channel calls (token exchange and
 * JWKS) and may be a different hostname when the app runs in Docker.
 */
@ConfigurationProperties(prefix = "app.keycloak")
public record KeycloakProperties(
        String publicUrl,
        String internalUrl,
        String realm,
        String clientId,
        String clientSecret) {

    public KeycloakProperties {
        publicUrl = trimTrailingSlash(publicUrl);
        internalUrl = trimTrailingSlash(internalUrl);
    }

    public String issuer() {
        return publicUrl + "/realms/" + realm;
    }

    public String authorizationUri() {
        return issuer() + "/protocol/openid-connect/auth";
    }

    /**
     * Browser-facing endpoint that ends the Keycloak SSO session. Spring uses
     * this metadata entry for RP-initiated logout.
     */
    public String endSessionEndpoint() {
        return issuer() + "/protocol/openid-connect/logout";
    }

    public String tokenUri() {
        return internalUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }

    public String jwkSetUri() {
        return internalUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
    }

    public String userInfoUri() {
        return internalUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";
    }

    private static String trimTrailingSlash(String value) {
        if (value == null) {
            return null;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
