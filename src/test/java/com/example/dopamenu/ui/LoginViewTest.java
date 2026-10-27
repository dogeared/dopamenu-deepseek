package com.example.dopamenu.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginViewTest {

    @Test
    void isAnAnonymousRouteAtLogin() {
        Route route = LoginView.class.getAnnotation(Route.class);

        assertThat(route).isNotNull();
        assertThat(route.value()).isEqualTo("login");
        assertThat(LoginView.class.isAnnotationPresent(AnonymousAllowed.class)).isTrue();
        assertThat(LoginView.KEYCLOAK_LOGIN_URL)
                .isEqualTo("/oauth2/authorization/keycloak");
    }

    @Test
    void enteringTheRouteRedirectsToTheKeycloakAuthorizationEndpoint() {
        UI ui = mock(UI.class);
        Page page = mock(Page.class);
        when(ui.getPage()).thenReturn(page);

        try (MockedStatic<UI> mockedUi = mockStatic(UI.class)) {
            mockedUi.when(UI::getCurrent).thenReturn(ui);

            new LoginView().beforeEnter(mock(BeforeEnterEvent.class));

            verify(page).setLocation(LoginView.KEYCLOAK_LOGIN_URL);
        }
    }
}
