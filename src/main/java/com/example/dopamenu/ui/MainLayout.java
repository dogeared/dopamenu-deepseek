package com.example.dopamenu.ui;

import com.example.dopamenu.service.CurrentUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

@PermitAll
public class MainLayout extends AppLayout {

    public MainLayout(CurrentUser currentUser, AuthenticationContext authenticationContext) {
        addClassName("app-shell");

        NativeButton drawerToggle = new NativeButton();
        drawerToggle.addClassName("drawer-toggle");
        drawerToggle.setAriaLabel("Toggle navigation");
        drawerToggle.setTitle("Menu");
        drawerToggle.add(VaadinIcon.MENU.create());
        drawerToggle.addClickListener(event -> setDrawerOpened(!isDrawerOpened()));

        Header header = new Header();
        header.addClassName("app-header");
        header.add(drawerToggle, brand(), desktopNav());
        Div spacer = new Div();
        spacer.addClassName("header-spacer");
        header.add(spacer, userChip(currentUser));
        addToNavbar(header);

        addToDrawer(drawer(authenticationContext));
        setDrawerOpened(false);
    }

    private static RouterLink brand() {
        RouterLink brand = new RouterLink();
        brand.setRoute(MenuView.class);
        brand.addClassName("brand");
        brand.getElement().setAttribute("aria-label", "Dopa Menu — home");
        Span dopa = new Span("dopa");
        dopa.addClassName("brand-strong");
        Span menu = new Span("menu");
        menu.addClassName("brand-light");
        brand.add(dopa, menu);
        return brand;
    }

    private static Nav desktopNav() {
        Nav nav = new Nav();
        nav.addClassName("app-nav");
        nav.add(link("Menu", VaadinIcon.CUTLERY, MenuView.class, "nav-link"),
                link("History", VaadinIcon.CHART, HistoryView.class, "nav-link"),
                link("Edit menu", VaadinIcon.EDIT, ManageMenuView.class, "nav-link"));
        return nav;
    }

    private static Div userChip(CurrentUser currentUser) {
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
        return chip;
    }

    private static Div drawer(AuthenticationContext authenticationContext) {
        Div drawer = new Div();
        drawer.addClassName("drawer");

        Div head = new Div();
        head.addClassName("drawer-head");
        Span title = new Span("Your menu");
        title.addClassName("drawer-title");
        Span hint = new Span("Take what you need. Leave the rest.");
        hint.addClassName("drawer-hint");
        head.add(title, hint);

        Nav nav = new Nav();
        nav.addClassName("drawer-nav");
        nav.add(link("Menu", VaadinIcon.CUTLERY, MenuView.class, "drawer-link"),
                link("History", VaadinIcon.CHART, HistoryView.class, "drawer-link"),
                link("Edit menu", VaadinIcon.EDIT, ManageMenuView.class, "drawer-link"));

        NativeButton signOut = new NativeButton("Sign out");
        Css.add(signOut, "btn", "btn-ghost", "drawer-signout");
        signOut.addClickListener(event -> authenticationContext.logout());

        drawer.add(head, nav, signOut);
        return drawer;
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
