package com.example.dopamenu.ui;

import com.example.dopamenu.domain.MenuOrder;
import com.example.dopamenu.domain.OrderStatus;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuOrderService;
import com.example.dopamenu.service.OrderStats;
import com.example.dopamenu.ui.component.OrderDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Locale;

@Route(value = "history", layout = MainLayout.class)
@PageTitle("History · Dopa Menu")
@PermitAll
public class HistoryView extends VerticalLayout {

    private final MenuOrderService orderService;
    private final CurrentUser currentUser;

    public HistoryView(MenuOrderService orderService, CurrentUser currentUser) {
        this.orderService = orderService;
        this.currentUser = currentUser;
        addClassName("history-view");
        setPadding(false);
        setSpacing(false);
        render();
    }

    private void render() {
        removeAll();
        String owner = currentUser.id();
        OrderStats.Snapshot stats = orderService.stats(owner, 7);
        List<MenuOrder> orders = orderService.recentOrders(owner);

        add(head());
        add(statGrid(stats));
        add(recent(orders));
        add(new SiteFooter());
    }

    private Div head() {
        Div head = new Div();
        head.addClassName("page-head");

        Div copy = new Div();
        copy.addClassName("page-head-copy");
        Span eyebrow = new Span("The receipts");
        eyebrow.addClassName("eyebrow");
        H1 title = new H1("What you actually did.");
        title.addClassName("page-title");
        Paragraph lede = new Paragraph(
                "No streaks to protect, no scores to chase. Just a record of what you "
                        + "reached for and how it landed.");
        lede.addClassName("page-lede");
        copy.add(eyebrow, title, lede);
        head.add(copy);
        return head;
    }

    private Div statGrid(OrderStats.Snapshot stats) {
        Div grid = new Div();
        grid.addClassName("stat-grid");
        grid.add(stat("Finished", Long.toString(stats.completedOrders()), "orders, last 7 days"));
        grid.add(stat("Invested", UiText.minutes(stats.minutesInvested()), "deliberate minutes"));
        grid.add(stat("Completion", stats.completionRatePercent() + "%", "of orders finished"));
        grid.add(stat("Mood lift",
                stats.averageMoodLift() == null ? "—"
                        : String.format(Locale.US, "%+.1f", stats.averageMoodLift()),
                "average after − before"));
        return grid;
    }

    private static Div stat(String label, String value, String hint) {
        Div tile = new Div();
        tile.addClassName("stat");
        Span labelSpan = new Span(label);
        labelSpan.addClassName("stat-label");
        Span valueSpan = new Span(value);
        valueSpan.addClassName("stat-value");
        Span hintSpan = new Span(hint);
        hintSpan.addClassName("stat-hint");
        tile.add(labelSpan, valueSpan, hintSpan);
        return tile;
    }

    private Div recent(List<MenuOrder> orders) {
        Div section = new Div();
        section.addClassName("recent");

        Div heading = new Div();
        heading.addClassName("section-head");
        H2 title = new H2("Recent orders");
        title.addClassName("section-title");
        Span count = new Span(orders.size() + (orders.size() == 1 ? " entry" : " entries"));
        count.addClassName("section-count");
        heading.add(title, count);
        section.add(heading);

        if (orders.isEmpty()) {
            Div empty = new Div();
            empty.addClassName("empty-state");
            Span titleText = new Span("Nothing on the tab yet.");
            titleText.addClassName("empty-title");
            Span body = new Span("Order something from the menu and it will show up here.");
            body.addClassName("empty-body");
            empty.add(titleText, body);
            section.add(empty);
            return section;
        }

        for (MenuOrder order : orders) {
            section.add(orderRow(order));
        }
        return section;
    }

    private Div orderRow(MenuOrder order) {
        Div row = new Div();
        row.addClassName("order-row");
        row.addClassName("order-" + order.getCourse().cssName());

        Div marker = new Div();
        marker.addClassName("order-marker");

        Div body = new Div();
        body.addClassName("order-body");
        Div line = new Div();
        line.addClassName("order-line");
        Span title = new Span(order.getItemTitle());
        title.addClassName("order-title");
        Span status = new Span(statusLabel(order.getStatus()));
        Css.add(status, "status-pill", "status-" + order.getStatus().name().toLowerCase());
        line.add(title, status);

        Div meta = new Div();
        meta.addClassName("order-meta");
        meta.add(new Span(UiText.day(order.getStartedAt()) + " · "
                + UiText.time(order.getStartedAt())));
        meta.add(new Span(order.getCourse().label()));
        meta.add(new Span(UiText.minutes(order.minutesElapsed()) + " spent"));

        body.add(line, meta);

        if (order.getNote() != null && !order.getNote().isBlank()) {
            Paragraph note = new Paragraph(order.getNote());
            note.addClassName("order-note");
            body.add(note);
        }

        row.add(marker, body);

        if (order.getStatus() == OrderStatus.ACTIVE) {
            NativeButton resume = new NativeButton("Resume");
            Css.add(resume, "btn", "btn-ghost", "order-resume");
            resume.addClickListener(event -> resume(order));
            row.add(resume);
        } else {
            row.add(mood(order));
        }
        return row;
    }

    private static Div mood(MenuOrder order) {
        Div mood = new Div();
        mood.addClassName("mood-summary");
        Span before = new Span(UiText.moodEmoji(order.getMoodBefore()));
        before.addClassName("mood-emoji-small");
        before.setTitle("Before: " + UiText.mood(order.getMoodBefore()));
        Span arrow = new Span("→");
        arrow.addClassName("mood-arrow");
        Span after = new Span(UiText.moodEmoji(order.getMoodAfter()));
        after.addClassName("mood-emoji-small");
        after.setTitle("After: " + UiText.mood(order.getMoodAfter()));
        mood.add(before, arrow, after);
        Integer lift = order.moodLift();
        if (lift != null && lift != 0) {
            Span delta = new Span(UiText.sign(lift));
            delta.addClassName("mood-delta");
            delta.getElement().getClassList().set("is-positive", lift > 0);
            mood.add(delta);
        }
        return mood;
    }

    private static String statusLabel(OrderStatus status) {
        return switch (status) {
            case ACTIVE -> "Running";
            case COMPLETED -> "Finished";
            case ABANDONED -> "Let go";
        };
    }

    private void resume(MenuOrder order) {
        Dialogs.open(new OrderDialog(order, orderService, currentUser, this::render));
    }
}
