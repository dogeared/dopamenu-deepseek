package com.example.dopamenu.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegistrationResolverTest {

    private static final String AUTHORIZATION_URI =
            "https://auth.example/realms/dopamenu/protocol/openid-connect/auth";
    private static final String REGISTRATIONS_URI =
            "https://auth.example/realms/dopamenu/protocol/openid-connect/registrations";

    private final OAuth2AuthorizationRequestResolver delegate =
            mock(OAuth2AuthorizationRequestResolver.class);
    private final RegistrationResolver resolver =
            new RegistrationResolver(delegate, REGISTRATIONS_URI);
    private final OAuth2AuthorizationRequest request = OAuth2AuthorizationRequest
            .authorizationCode()
            .clientId("dopamenu-app")
            .authorizationUri(AUTHORIZATION_URI)
            .redirectUri("http://localhost:8080/login/oauth2/code/keycloak")
            .state("state")
            .additionalParameters(Map.of(
                    "code_challenge", "challenge",
                    "code_challenge_method", "S256"))
            .build();

    @Test
    void sendsSignUpToTheRegistrationEndpoint() {
        MockHttpServletRequest http = new MockHttpServletRequest();
        http.setParameter(RegistrationResolver.ACTION_PARAMETER,
                RegistrationResolver.REGISTER_ACTION);
        when(delegate.resolve(http)).thenReturn(request);

        OAuth2AuthorizationRequest customized = resolver.resolve(http);

        assertThat(customized.getAuthorizationUri()).isEqualTo(REGISTRATIONS_URI);
        assertThat(customized.getState()).isEqualTo("state");
        assertThat(customized.getClientId()).isEqualTo("dopamenu-app");
        assertThat(customized.getRedirectUri())
                .isEqualTo("http://localhost:8080/login/oauth2/code/keycloak");
        assertThat(customized.getAdditionalParameters())
                .containsEntry("code_challenge", "challenge")
                .containsEntry("code_challenge_method", "S256");
    }

    @Test
    void leavesNormalSignInOnTheAuthorizationEndpoint() {
        MockHttpServletRequest http = new MockHttpServletRequest();
        when(delegate.resolve(http)).thenReturn(request);

        assertThat(resolver.resolve(http)).isSameAs(request);
    }

    @Test
    void leavesTheRegistrationScopedResolveUntouchedWithoutTheParameter() {
        MockHttpServletRequest http = new MockHttpServletRequest();
        when(delegate.resolve(http, "keycloak")).thenReturn(request);

        assertThat(resolver.resolve(http, "keycloak")).isSameAs(request);
    }

    @Test
    void aMissingDelegateResultStaysMissing() {
        MockHttpServletRequest http = new MockHttpServletRequest();
        http.setParameter(RegistrationResolver.ACTION_PARAMETER,
                RegistrationResolver.REGISTER_ACTION);
        when(delegate.resolve(http)).thenReturn(null);

        assertThat(resolver.resolve(http)).isNull();
    }
}
