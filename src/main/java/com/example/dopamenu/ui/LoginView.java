package com.example.dopamenu.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Friendly {@code /login} entry point.
 *
 * <p>Spring Security's OAuth2 client listens on
 * {@code /oauth2/authorization/keycloak}; this route gives the public site a
 * stable {@code /login} link and forwards the browser there.</p>
 */
@Route("login")
@PageTitle("Log in")
@AnonymousAllowed
public class LoginView extends Div implements BeforeEnterObserver {

    public static final String KEYCLOAK_LOGIN_URL = "/oauth2/authorization/keycloak";

    public LoginView() {
        addClassName("login-view");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        UI.getCurrent().getPage().setLocation(KEYCLOAK_LOGIN_URL);
    }
}
