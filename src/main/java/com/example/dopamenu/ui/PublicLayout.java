package com.example.dopamenu.ui;

import com.example.dopamenu.service.CurrentUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;

/**
 * Layout for the public part of the site. The page itself is reachable both
 * before and after signing in.
 */
@AnonymousAllowed
public class PublicLayout extends AppLayout {

    public PublicLayout(CurrentUser currentUser,
            AuthenticationContext authenticationContext) {
        addClassName("app-shell");
        addClassName("public-shell");

        Header header = new Header();
        header.addClassName("app-header");
        header.add(brand(), nav(currentUser));
        Div spacer = new Div();
        spacer.addClassName("header-spacer");
        header.add(spacer, account(currentUser, authenticationContext));
        addToNavbar(header);
    }

    private static RouterLink brand() {
        RouterLink brand = new RouterLink();
        brand.setRoute(LandingView.class);
        brand.addClassName("brand");
        brand.getElement().setAttribute("aria-label", "Dopa Menu — home");
        Span dopa = new Span("dopa");
        dopa.addClassName("brand-strong");
        Span menu = new Span("menu");
        menu.addClassName("brand-light");
        brand.add(dopa, menu);
        return brand;
    }

    private static Nav nav(CurrentUser currentUser) {
        Nav nav = new Nav();
        nav.addClassName("app-nav");
        nav.add(link("Home", VaadinIcon.HOME, LandingView.class, "nav-link"));
        if (currentUser.isAuthenticated()) {
            nav.add(link("Menu", VaadinIcon.CUTLERY, MenuView.class, "nav-link"));
            nav.add(link("History", VaadinIcon.CHART, HistoryView.class, "nav-link"));
        }
        return nav;
    }

    private static Component account(CurrentUser currentUser,
            AuthenticationContext authenticationContext) {
        if (!currentUser.isAuthenticated()) {
            Anchor login = new Anchor("/oauth2/authorization/keycloak", "Log in");
            Css.add(login, "btn", "btn-primary", "nav-login");
            login.getElement().setAttribute("aria-label", "Log in with Keycloak");
            return login;
        }

        Div account = new Div();
        account.addClassName("nav-account");

        Div chip = new Div();
        chip.addClassName("user-chip");
        Span avatar = new Span(currentUser.initials());
        avatar.addClassName("avatar");
        Div text = new Div();
        text.addClassName("user-text");
        Span name = new Span(currentUser.displayName());
        name.addClassName("user-name");
        Span username = new Span("@" + currentUser.username());
        username.addClassName("user-handle");
        text.add(name, username);
        chip.add(avatar, text);

        NativeButton signOut = new NativeButton("Sign out");
        Css.add(signOut, "btn", "btn-ghost", "nav-signout");
        signOut.addClickListener(event -> authenticationContext.logout());

        account.add(chip, signOut);
        return account;
    }

    private static RouterLink link(String text, VaadinIcon icon,
            Class<? extends Component> route, String className) {
        RouterLink link = new RouterLink();
        link.setRoute(route);
        link.addClassName(className);
        Icon graphic = icon.create();
        graphic.addClassName("nav-icon");
        link.add(graphic, new Span(text));
        return link;
    }
}
