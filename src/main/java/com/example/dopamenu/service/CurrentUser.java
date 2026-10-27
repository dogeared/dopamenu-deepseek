package com.example.dopamenu.service;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

/**
 * Resolves the signed-in Keycloak user. The immutable Keycloak subject is used
 * as the ownership key, while the display name is only used for presentation.
 */
@Component
public class CurrentUser {

    private final AuthenticationContext authenticationContext;

    public CurrentUser(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public String id() {
        return oidcUser().map(OidcUser::getSubject)
                .orElseThrow(() -> new IllegalStateException("No authenticated user"));
    }

    public boolean isAuthenticated() {
        return oidcUser().isPresent();
    }

    public String displayName() {
        return oidcUser().map(user -> {
            String fullName = user.getFullName();
            if (fullName != null && !fullName.isBlank()) {
                return fullName;
            }
            return user.getPreferredUsername();
        }).orElse("Guest");
    }

    public String username() {
        return oidcUser().map(OidcUser::getPreferredUsername).orElse("");
    }

    public String initials() {
        String name = displayName().trim();
        if (name.isEmpty()) {
            return "?";
        }
        String[] parts = name.split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1))
                .toUpperCase();
    }

    private java.util.Optional<OidcUser> oidcUser() {
        return authenticationContext.getAuthenticatedUser(OidcUser.class);
    }
}
