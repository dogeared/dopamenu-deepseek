package com.example.dopamenu.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakClientConfigTest {

    private final KeycloakProperties properties = new KeycloakProperties(
            "http://localhost:8180", "http://keycloak:8180", "dopamenu",
            "dopamenu-app", "dopamenu-secret");

    @Test
    void buildsAKeycloakClientRegistrationWithoutOidcDiscovery() {
        ClientRegistrationRepository repository = new KeycloakClientConfig()
                .clientRegistrationRepository(properties);

        ClientRegistration registration = repository.findByRegistrationId("keycloak");

        assertThat(registration).isNotNull();
        assertThat(registration.getClientName()).isEqualTo("Keycloak");
        assertThat(registration.getClientId()).isEqualTo("dopamenu-app");
        assertThat(registration.getClientSecret()).isEqualTo("dopamenu-secret");
        assertThat(registration.getClientAuthenticationMethod())
                .isEqualTo(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        assertThat(registration.getAuthorizationGrantType())
                .isEqualTo(AuthorizationGrantType.AUTHORIZATION_CODE);
        assertThat(registration.getRedirectUri())
                .isEqualTo("{baseUrl}/login/oauth2/code/{registrationId}");
        assertThat(registration.getScopes()).containsExactly("openid", "profile", "email");
        assertThat(registration.getProviderDetails().getAuthorizationUri())
                .isEqualTo("http://localhost:8180/realms/dopamenu/protocol/openid-connect/auth");
        assertThat(registration.getProviderDetails().getTokenUri())
                .isEqualTo("http://keycloak:8180/realms/dopamenu/protocol/openid-connect/token");
        assertThat(registration.getProviderDetails().getJwkSetUri())
                .isEqualTo("http://keycloak:8180/realms/dopamenu/protocol/openid-connect/certs");
        assertThat(registration.getProviderDetails().getUserInfoEndpoint().getUri())
                .isEqualTo("http://keycloak:8180/realms/dopamenu/protocol/openid-connect/userinfo");
        assertThat(registration.getProviderDetails().getIssuerUri())
                .isEqualTo("http://localhost:8180/realms/dopamenu");
        assertThat(registration.getProviderDetails().getConfigurationMetadata())
                .containsEntry("end_session_endpoint",
                        "http://localhost:8180/realms/dopamenu/protocol/openid-connect/logout");
        assertThat(registration.getProviderDetails().getUserInfoEndpoint()
                .getUserNameAttributeName()).isEqualTo("preferred_username");
    }
}
