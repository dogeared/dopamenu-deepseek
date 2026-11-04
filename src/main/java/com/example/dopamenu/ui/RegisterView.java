package com.example.dopamenu.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Friendly {@code /register} entry point. It forwards to the same OAuth2
 * authorization endpoint as {@code /login}, with the parameter
 * {@link com.example.dopamenu.config.SignupResolver} turns into Keycloak's
 * {@code kc_action=register}.
 */
@Route("register")
@PageTitle("Create account")
@AnonymousAllowed
public class RegisterView extends Div implements BeforeEnterObserver {

    public static final String KEYCLOAK_REGISTER_URL =
            LoginView.KEYCLOAK_LOGIN_URL + "?action=register";

    public RegisterView() {
        addClassName("register-view");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        UI.getCurrent().getPage().setLocation(KEYCLOAK_REGISTER_URL);
    }
}
