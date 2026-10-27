package com.example.dopamenu.ui;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuOrder;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuItemService;
import com.example.dopamenu.service.MenuOrderService;
import com.example.dopamenu.service.OrderStats;
import com.example.dopamenu.ui.component.CourseSection;
import com.example.dopamenu.ui.component.OrderDialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dopa Menu")
@PermitAll
public class MenuView extends VerticalLayout {

    private final MenuItemService menuItemService;
    private final MenuOrderService orderService;
    private final CurrentUser currentUser;

    public MenuView(MenuItemService menuItemService, MenuOrderService orderService,
            CurrentUser currentUser) {
        this.menuItemService = menuItemService;
        this.orderService = orderService;
        this.currentUser = currentUser;
        addClassName("menu-view");
        setPadding(false);
        setSpacing(false);
        render();
    }

    private void render() {
        removeAll();
        String owner = currentUser.id();
        List<MenuItem> items = menuItemService.ensureSeeded(owner);
        OrderStats.Snapshot stats = orderService.stats(owner, 7);

        add(hero(stats, items.size()));
        orderService.activeOrder(owner).ifPresent(order -> add(activeBanner(order)));

        for (Course course : Course.values()) {
            List<MenuItem> courseItems = items.stream()
                    .filter(item -> item.getCourse() == course)
                    .toList();
            add(new CourseSection(course, courseItems, this::order));
        }

        add(footer());
    }

    private Div hero(OrderStats.Snapshot stats, int itemCount) {
        Div hero = new Div();
        hero.addClassName("hero");

        Div copy = new Div();
        copy.addClassName("hero-copy");
        Span eyebrow = new Span("The dopa menu");
        eyebrow.addClassName("eyebrow");
        H1 heading = new H1("Feed the need, not the scroll.");
        heading.addClassName("hero-title");
        Paragraph lede = new Paragraph(
                "Pick a course based on the time and energy you actually have. "
                        + "Appetizers to start, entrées to sink into, sides to carry you, "
                        + "and desserts with a timer attached.");
        lede.addClassName("hero-lede");
        copy.add(eyebrow, heading, lede);
        hero.add(copy);

        Div statRow = new Div();
        statRow.addClassName("hero-stats");
        statRow.add(statTile(Long.toString(stats.completedOrders()), "orders logged",
                "in the last 7 days"));
        statRow.add(statTile(UiText.minutes(stats.minutesInvested()), "time invested",
                "on purpose"));
        statRow.add(statTile(Integer.toString(itemCount), "dishes on your menu",
                "ready when you are"));
        hero.add(statRow);

        return hero;
    }

    private static Div statTile(String value, String label, String hint) {
        Div tile = new Div();
        tile.addClassName("hero-stat");
        Span number = new Span(value);
        number.addClassName("hero-stat-value");
        Span name = new Span(label);
        name.addClassName("hero-stat-label");
        Span note = new Span(hint);
        note.addClassName("hero-stat-hint");
        tile.add(number, name, note);
        return tile;
    }

    private Div activeBanner(MenuOrder order) {
        Div banner = new Div();
        banner.addClassName("active-order");
        Div pulse = new Div();
        pulse.addClassName("active-pulse");

        Div copy = new Div();
        copy.addClassName("active-copy");
        Span eyebrow = new Span("On the table");
        eyebrow.addClassName("active-eyebrow");
        Span title = new Span(order.getItemTitle());
        title.addClassName("active-title");
        Span since = new Span("Started " + UiText.relative(order.getStartedAt()));
        since.addClassName("active-since");
        copy.add(eyebrow, title, since);

        NativeButton resume = new NativeButton();
        Css.add(resume, "btn", "btn-cream");
        resume.add(new Span("Resume"));
        Icon arrow = VaadinIcon.ARROW_RIGHT.create();
        resume.add(arrow);
        resume.addClickListener(event -> resume(order));

        banner.add(pulse, copy, resume);
        return banner;
    }

    private static Div footer() {
        Div footer = new Div();
        footer.addClassName("view-footer");
        Span text = new Span("Courses, not chores. ");
        Anchor source = new Anchor("https://www.youtube.com/watch?v=-6WCkTwW6xg",
                "The original dopa-menu idea");
        source.setTarget("_blank");
        source.addClassName("inline-link");
        footer.add(text, source);
        return footer;
    }

    private void order(MenuItem item) {
        Dialogs.open(new OrderDialog(item, orderService, currentUser, this::render));
    }

    private void resume(MenuOrder order) {
        Dialogs.open(new OrderDialog(order, orderService, currentUser, this::render));
    }
}
