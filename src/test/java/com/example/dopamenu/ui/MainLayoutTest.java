package com.example.dopamenu.ui;

import com.example.dopamenu.service.CurrentUser;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.dopamenu.ui.UiTestSupport.button;
import static com.example.dopamenu.ui.UiTestSupport.buttonByAriaLabel;
import static com.example.dopamenu.ui.UiTestSupport.click;
import static com.example.dopamenu.ui.UiTestSupport.hasText;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MainLayoutTest {

    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final AuthenticationContext authenticationContext = mock(
            AuthenticationContext.class);

    @BeforeEach
    void setUp() {
        UiTestSupport.installUi();
        when(currentUser.displayName()).thenReturn("Ada Lovelace");
        when(currentUser.username()).thenReturn("ada");
        when(currentUser.initials()).thenReturn("AL");
    }

    @AfterEach
    void tearDown() {
        UiTestSupport.removeUi();
    }

    @Test
    void rendersTheBrandNavigationAndUserChip() {
        MainLayout layout = new MainLayout(currentUser, authenticationContext);

        assertThat(hasText(layout, "dopa")).isTrue();
        assertThat(hasText(layout, "menu")).isTrue();
        assertThat(hasText(layout, "Home")).isTrue();
        assertThat(hasText(layout, "Menu")).isTrue();
        assertThat(hasText(layout, "History")).isTrue();
        assertThat(hasText(layout, "Edit menu")).isTrue();
        assertThat(hasText(layout, "Ada Lovelace")).isTrue();
        assertThat(hasText(layout, "@ada")).isTrue();
        assertThat(hasText(layout, "AL")).isTrue();
        assertThat(hasText(layout, "Your menu")).isTrue();
        assertThat(hasText(layout, "Take what you need. Leave the rest.")).isTrue();
        assertThat(hasText(layout, "Sign out")).isTrue();
    }

    @Test
    void theDrawerToggleOpensAndClosesTheDrawer() {
        MainLayout layout = new MainLayout(currentUser, authenticationContext);
        assertThat(layout.isDrawerOpened()).isFalse();

        click(buttonByAriaLabel(layout, "Toggle navigation"));
        assertThat(layout.isDrawerOpened()).isTrue();

        click(buttonByAriaLabel(layout, "Toggle navigation"));
        assertThat(layout.isDrawerOpened()).isFalse();
    }

    @Test
    void signingOutDelegatesToTheAuthenticationContext() {
        MainLayout layout = new MainLayout(currentUser, authenticationContext);

        click(button(layout, "Sign out"));

        verify(authenticationContext).logout();
    }
}
