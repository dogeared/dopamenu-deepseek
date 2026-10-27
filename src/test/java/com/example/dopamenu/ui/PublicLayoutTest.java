package com.example.dopamenu.ui;

import com.example.dopamenu.service.CurrentUser;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.dopamenu.ui.UiTestSupport.button;
import static com.example.dopamenu.ui.UiTestSupport.click;
import static com.example.dopamenu.ui.UiTestSupport.findAll;
import static com.example.dopamenu.ui.UiTestSupport.hasText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicLayoutTest {

    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final AuthenticationContext authenticationContext = mock(
            AuthenticationContext.class);

    @BeforeEach
    void setUp() {
        UiTestSupport.installUi();
    }

    @AfterEach
    void tearDown() {
        UiTestSupport.removeUi();
    }

    @Test
    void signedOutVisitorsSeeHomeAndALoginButton() {
        when(currentUser.isAuthenticated()).thenReturn(false);

        PublicLayout layout = new PublicLayout(currentUser, authenticationContext);

        assertThat(hasText(layout, "Home")).isTrue();
        assertThat(hasText(layout, "Menu")).isFalse();
        assertThat(hasText(layout, "History")).isFalse();
        assertThat(hasText(layout, "Sign out")).isFalse();

        Anchor login = findAll(layout, Anchor.class).stream()
                .filter(anchor -> "Log in".equals(anchor.getText()))
                .findFirst()
                .orElseThrow();
        assertThat(login.getHref()).isEqualTo("/login");
        assertThat(login.getClassNames()).contains("btn", "btn-primary", "nav-login");
    }

    @Test
    void signedInUsersKeepHomeAndAlsoSeeTheAppNav() {
        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.displayName()).thenReturn("Ada Lovelace");
        when(currentUser.username()).thenReturn("ada");
        when(currentUser.initials()).thenReturn("AL");

        PublicLayout layout = new PublicLayout(currentUser, authenticationContext);

        assertThat(hasText(layout, "Home")).isTrue();
        assertThat(hasText(layout, "Menu")).isTrue();
        assertThat(hasText(layout, "History")).isTrue();
        assertThat(hasText(layout, "Ada Lovelace")).isTrue();
        assertThat(hasText(layout, "@ada")).isTrue();
        assertThat(hasText(layout, "AL")).isTrue();
        assertThat(findAll(layout, Anchor.class))
                .noneMatch(anchor -> "Log in".equals(anchor.getText()));
    }

    @Test
    void signingOutDelegatesToTheAuthenticationContext() {
        when(currentUser.isAuthenticated()).thenReturn(true);
        when(currentUser.displayName()).thenReturn("Ada Lovelace");
        when(currentUser.username()).thenReturn("ada");
        when(currentUser.initials()).thenReturn("AL");

        PublicLayout layout = new PublicLayout(currentUser, authenticationContext);

        click(button(layout, "Sign out"));

        verify(authenticationContext).logout();
    }
}
