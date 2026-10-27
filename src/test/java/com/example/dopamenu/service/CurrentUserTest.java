package com.example.dopamenu.service;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentUserTest {

    private final AuthenticationContext context = mock(AuthenticationContext.class);
    private final CurrentUser currentUser = new CurrentUser(context);

    private OidcUser user(String subject, String fullName, String username) {
        OidcUser user = mock(OidcUser.class);
        when(user.getSubject()).thenReturn(subject);
        when(user.getFullName()).thenReturn(fullName);
        when(user.getPreferredUsername()).thenReturn(username);
        return user;
    }

    private void signedInAs(OidcUser user) {
        when(context.getAuthenticatedUser(OidcUser.class)).thenReturn(Optional.of(user));
    }

    @Test
    void idIsTheKeycloakSubject() {
        signedInAs(user("subject-1", "Ada Lovelace", "ada"));

        assertThat(currentUser.id()).isEqualTo("subject-1");
    }

    @Test
    void idThrowsWithoutAnAuthenticatedUser() {
        when(context.getAuthenticatedUser(OidcUser.class)).thenReturn(Optional.empty());

        assertThatThrownBy(currentUser::id)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No authenticated user");
    }

    @Test
    void displayNamePrefersFullName() {
        signedInAs(user("subject-1", "Ada Lovelace", "ada"));

        assertThat(currentUser.displayName()).isEqualTo("Ada Lovelace");
    }

    @Test
    void displayNameFallsBackToUsernameWhenFullNameIsMissingOrBlank() {
        signedInAs(user("subject-1", null, "ada"));
        assertThat(currentUser.displayName()).isEqualTo("ada");

        signedInAs(user("subject-1", "   ", "ada"));
        assertThat(currentUser.displayName()).isEqualTo("ada");
    }

    @Test
    void displayNameIsGuestWhenSignedOut() {
        when(context.getAuthenticatedUser(OidcUser.class)).thenReturn(Optional.empty());

        assertThat(currentUser.displayName()).isEqualTo("Guest");
    }

    @Test
    void usernameComesFromThePreferredUsername() {
        signedInAs(user("subject-1", "Ada Lovelace", "ada"));
        assertThat(currentUser.username()).isEqualTo("ada");

        when(context.getAuthenticatedUser(OidcUser.class)).thenReturn(Optional.empty());
        assertThat(currentUser.username()).isEmpty();
    }

    @Test
    void initialsUseFirstAndLastName() {
        signedInAs(user("subject-1", "Ada King Lovelace", "ada"));
        assertThat(currentUser.initials()).isEqualTo("AL");
    }

    @Test
    void initialsUseTheOnlyNameWhenThereIsOneWord() {
        signedInAs(user("subject-1", "Ada", "ada"));
        assertThat(currentUser.initials()).isEqualTo("A");
    }

    @Test
    void initialsFallBackToQuestionMarkWhenThereIsNoName() {
        signedInAs(user("subject-1", "", ""));
        assertThat(currentUser.initials()).isEqualTo("?");
    }

    @Test
    void initialsTrimExtraWhitespace() {
        signedInAs(user("subject-1", "  Ada   Lovelace  ", "ada"));
        assertThat(currentUser.initials()).isEqualTo("AL");
    }
}
