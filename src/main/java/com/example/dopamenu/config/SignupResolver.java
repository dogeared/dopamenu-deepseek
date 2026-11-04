package com.example.dopamenu.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adds Keycloak's {@code kc_action=register} parameter when the sign-in request
 * asked for registration. Every other authorization request is returned
 * untouched, so PKCE, state and the normal login path are unchanged.
 */
public class SignupResolver implements OAuth2AuthorizationRequestResolver {

    static final String ACTION_PARAMETER = "action";
    static final String REGISTER_ACTION = "register";
    static final String KC_ACTION_PARAMETER = "kc_action";

    private final OAuth2AuthorizationRequestResolver delegate;

    public SignupResolver(OAuth2AuthorizationRequestResolver delegate) {
        this.delegate = delegate;
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

    private static OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest request,
            HttpServletRequest httpRequest) {
        if (request == null
                || !REGISTER_ACTION.equals(httpRequest.getParameter(ACTION_PARAMETER))) {
            return request;
        }
        Map<String, Object> parameters = new LinkedHashMap<>(request.getAdditionalParameters());
        parameters.put(KC_ACTION_PARAMETER, REGISTER_ACTION);
        return OAuth2AuthorizationRequest.from(request)
                .additionalParameters(parameters)
                .build();
    }
}
