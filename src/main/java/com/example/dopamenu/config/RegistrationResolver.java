package com.example.dopamenu.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * Sends the sign-in request to Keycloak's registration endpoint when the user
 * asked to create an account. Every other authorization request is returned
 * untouched, so PKCE, state, nonce and the normal login path are unchanged.
 *
 * <p>Keycloak ignores {@code kc_action=register} on its authorization endpoint,
 * which is why the request targets {@code /protocol/openid-connect/registrations}
 * instead. That endpoint renders the sign-up form directly and keeps the PKCE
 * challenge, so the code exchange still completes through the public client.</p>
 */
public class RegistrationResolver implements OAuth2AuthorizationRequestResolver {

    static final String ACTION_PARAMETER = "action";
    static final String REGISTER_ACTION = "register";

    private final OAuth2AuthorizationRequestResolver delegate;
    private final String registrationUri;

    public RegistrationResolver(OAuth2AuthorizationRequestResolver delegate,
            String registrationUri) {
        this.delegate = delegate;
        this.registrationUri = registrationUri;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return customize(delegate.resolve(request), request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request,
            String registrationId) {
        return customize(delegate.resolve(request, registrationId), request);
    }

    private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest request,
            HttpServletRequest httpRequest) {
        if (request == null
                || !REGISTER_ACTION.equals(httpRequest.getParameter(ACTION_PARAMETER))) {
            return request;
        }
        return OAuth2AuthorizationRequest.from(request)
                .authorizationUri(registrationUri)
                .build();
    }
}
