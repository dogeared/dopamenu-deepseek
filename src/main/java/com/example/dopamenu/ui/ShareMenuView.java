package com.example.dopamenu.ui;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuShare;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuEventService;
import com.example.dopamenu.service.MenuItemService;
import com.example.dopamenu.service.MenuShareService;
import com.example.dopamenu.service.ShareIntent;
import com.example.dopamenu.ui.component.CourseSection;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * The public, anonymous view of one shared menu. It is the traffic source, the
 * acquisition path and the measuring instrument for the share experiment.
 *
 * <p>Deindexing comes from {@code X-Robots-Tag}, set by
 * {@link com.example.dopamenu.config.RobotsHeaderFilter}. Vaadin only accepts
 * app-shell annotations such as {@code @Meta} on an {@code AppShellConfigurator},
 * so a per-view meta tag is not an option here.</p>
 */
@Route(value = ShareRoutes.PREFIX, layout = PublicLayout.class)
@PageTitle("A dopa menu")
@AnonymousAllowed
public class ShareMenuView extends VerticalLayout implements HasUrlParameter<String> {

    private static final Logger LOG = LoggerFactory.getLogger(ShareMenuView.class);

    private final MenuShareService shareService;
    private final MenuItemService menuItemService;
    private final MenuEventService events;
    private final CurrentUser currentUser;

    public ShareMenuView(MenuShareService shareService, MenuItemService menuItemService,
            MenuEventService events, CurrentUser currentUser) {
        this.shareService = shareService;
        this.menuItemService = menuItemService;
        this.events = events;
        this.currentUser = currentUser;
        addClassName("share-view");
        setPadding(false);
        setSpacing(false);
    }

    @Override
    public void setParameter(BeforeEvent event, String token) {
        render(token);
    }

    void render(String token) {
        removeAll();
        Optional<MenuShare> live = shareService.live(token);
        if (live.isEmpty()) {
            add(deadPage());
            setTitle("Link not active · Dopa Menu");
            return;
        }

        MenuShare share = live.get();
        recordView(share);
        setTitle(share.getDisplayName() + "’s menu · Dopa Menu");

        add(hero(share));
        List<MenuItem> items = menuItemService.menuFor(share.getOwnerSub());
        for (Course course : Course.values()) {
            List<MenuItem> courseItems = items.stream()
                    .filter(item -> item.getCourse() == course)
                    .toList();
            add(new CourseSection(course, courseItems,
                    item -> order(share.getToken(), item), false));
        }
        add(new SiteFooter());
    }

    private Div hero(MenuShare share) {
        Div hero = new Div();
        Css.add(hero, "hero", "share-hero");

        Div copy = new Div();
        copy.addClassName("hero-copy");
        Span eyebrow = new Span("A dopa menu");
        eyebrow.addClassName("eyebrow");
        H1 heading = new H1(share.getDisplayName() + "’s menu");
        heading.addClassName("hero-title");
        Paragraph lede = new Paragraph(
                "Pick the thing that fits the time and energy you actually have. "
                        + "Ordering one takes you into the app, where you can make it yours.");
        lede.addClassName("hero-lede");
        copy.add(eyebrow, heading, lede);
        hero.add(copy);

        Div action = new Div();
        action.addClassName("share-action");
        if (isOwner(share)) {
            Span note = new Span("That’s already your menu.");
            note.addClassName("share-owner-note");
            action.add(note);
        } else {
            Span label = new Span("Want the whole thing?");
            label.addClassName("share-action-label");
            NativeButton copyButton = new NativeButton();
            Css.add(copyButton, "btn", "btn-ghost", "share-copy");
            copyButton.add(VaadinIcon.COPY.create());
            copyButton.add(new Span("Copy this menu"));
            copyButton.addClickListener(event -> copy(share.getToken()));
            action.add(label, copyButton);
        }
        hero.add(action);
        return hero;
    }

    private Div deadPage() {
        Div page = new Div();
        page.addClassName("share-dead");

        Span eyebrow = new Span("Off the menu");
        eyebrow.addClassName("eyebrow");
        H1 heading = new H1("This link is no longer active.");
        heading.addClassName("share-dead-title");
        Paragraph copy = new Paragraph(
                "The person who shared this menu may have replaced the link. "
                        + "Ask them for a fresh one.");
        copy.addClassName("share-dead-copy");
        RouterLink home = new RouterLink("See what a dopa menu is", LandingView.class);
        home.addClassName("inline-link");

        page.add(eyebrow, heading, copy, home);
        return page;
    }

    private void order(String token, MenuItem item) {
        shareService.storeIntent(ShareIntent.order(token, item.getId()));
        continueAfterSignIn();
    }

    private void copy(String token) {
        shareService.storeIntent(ShareIntent.copy(token));
        continueAfterSignIn();
    }

    private void continueAfterSignIn() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }
        if (currentUser.isAuthenticated()) {
            ui.navigate(MenuView.class);
        } else {
            ui.getPage().setLocation(LoginView.KEYCLOAK_LOGIN_URL);
        }
    }

    private boolean isOwner(MenuShare share) {
        return currentUser.isAuthenticated() && currentUser.id().equals(share.getOwnerSub());
    }

    private void recordView(MenuShare share) {
        if (isOwner(share)) {
            return;
        }
        VaadinRequest request = VaadinRequest.getCurrent();
        String userAgent = request == null ? null : request.getHeader("User-Agent");
        String referrer = request == null ? null : request.getHeader("Referer");
        try {
            events.recordShareView(share.getToken(), userAgent, referrer);
        } catch (RuntimeException error) {
            // D7: a failed measurement must never take the menu down.
            LOG.warn("Could not record share_view for token {}", share.getToken(), error);
        }
    }

    private static void setTitle(String title) {
        UI ui = UI.getCurrent();
        if (ui != null && ui.getSession() != null) {
            ui.getPage().setTitle(title);
        }
    }
}
