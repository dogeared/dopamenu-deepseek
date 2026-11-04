package com.example.dopamenu.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignupResolverTest {

    private final OAuth2AuthorizationRequestResolver delegate =
            mock(OAuth2AuthorizationRequestResolver.class);
    private final SignupResolver resolver = new SignupResolver(delegate);
    private final OAuth2AuthorizationRequest request = OAuth2AuthorizationRequest
            .authorizationCode()
            .clientId("dopamenu-app")
            .authorizationUri("https://auth.example/realms/dopamenu/protocol/openid-connect/auth")
            .redirectUri("http://localhost:8080/login/oauth2/code/keycloak")
            .state("state")
            .build();

    @Test
    void addsKcActionOnlyWhenTheRequestAsksForRegistration() {
        MockHttpServletRequest http = new MockHttpServletRequest();
        when(delegate.resolve(http)).thenReturn(request);

        assertThat(resolver.resolve(http).getAdditionalParameters())
                .doesNotContainKey(SignupResolver.KC_ACTION_PARAMETER);

        http.setParameter(SignupResolver.ACTION_PARAMETER,
                SignupResolver.REGISTER_ACTION);
        OAuth2AuthorizationRequest customized = resolver.resolve(http);

        assertThat(customized.getAdditionalParameters())
                .containsEntry(SignupResolver.KC_ACTION_PARAMETER,
                        SignupResolver.REGISTER_ACTION);
        assertThat(customized.getState()).isEqualTo("state");
        assertThat(customized.getClientId()).isEqualTo("dopamenu-app");
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
        http.setParameter(SignupResolver.ACTION_PARAMETER,
                SignupResolver.REGISTER_ACTION);
        when(delegate.resolve(http)).thenReturn(null);

        assertThat(resolver.resolve(http)).isNull();
    }
}
