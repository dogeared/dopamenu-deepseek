package com.example.dopamenu.ui.component;

import com.vaadin.flow.component.html.Article;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.ui.Css;
import com.example.dopamenu.ui.UiText;

import java.util.function.Consumer;

public class MenuItemCard extends Article {

    public MenuItemCard(MenuItem item, Consumer<MenuItem> onOrder) {
        this(item, onOrder, null);
    }

    /**
     * @param onLog opens the after-the-fact logging flow for timer dishes.
     *              {@code null} hides that action, which is what the public
     *              share page wants. Untimed dishes hide it too: their Order
     *              action already opens the minutes form.
     */
    public MenuItemCard(MenuItem item, Consumer<MenuItem> onOrder, Consumer<MenuItem> onLog) {
        addClassName("menu-card");
        addClassName("strength-" + item.getDopamineLevel().name().toLowerCase());

        Div top = new Div();
        top.addClassName("card-top");
        Span strength = new Span(item.getDopamineLevel().label());
        strength.addClassName("strength-pill");
        top.add(strength, dopamineMeter(item));
        add(top);

        H3 title = new H3(item.getTitle());
        title.addClassName("card-title");
        add(title);

        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            Paragraph description = new Paragraph(item.getDescription());
            description.addClassName("card-description");
            add(description);
        }

        Div meta = new Div();
        meta.addClassName("card-meta");
        meta.add(metaItem(VaadinIcon.CLOCK, UiText.minutes(item.getDurationMinutes())));
        if (item.isTimerRequired()) {
            meta.add(metaItem(VaadinIcon.TIMER, "Timer"));
        }
        add(meta);

        Div footer = new Div();
        footer.addClassName("card-footer");
        // Without a timer, Order already opens the minutes form, so a second
        // "Log it" action would just repeat it.
        if (onLog != null && item.isTimerRequired()) {
            NativeButton log = new NativeButton("Log it");
            Css.add(log, "btn", "btn-quiet", "card-log");
            log.setAriaLabel("Log " + item.getTitle() + " after the fact");
            log.setTitle("Already did this one? Add it to your history.");
            log.addClickListener(event -> onLog.accept(item));
            footer.add(log);
        }
        NativeButton order = new NativeButton();
        order.addClassName("card-action");
        order.add(new Span("Order"));
        Icon arrow = VaadinIcon.ARROW_RIGHT.create();
        arrow.addClassName("action-icon");
        order.add(arrow);
        order.setAriaLabel("Order " + item.getTitle());
        order.addClickListener(event -> onOrder.accept(item));
        footer.add(order);
        add(footer);
    }

    private static Div dopamineMeter(MenuItem item) {
        Div meter = new Div();
        meter.addClassName("dopamine-meter");
        meter.setTitle("Dopamine: " + item.getDopamineLevel().label());
        int strength = item.getDopamineLevel().strength();
        for (int i = 0; i < 4; i++) {
            Span dot = new Span();
            dot.addClassName("dopamine-dot");
            if (i < strength) {
                dot.addClassName("is-on");
            }
            meter.add(dot);
        }
        return meter;
    }

    private static Div metaItem(VaadinIcon icon, String text) {
        Div item = new Div();
        item.addClassName("meta-item");
        Icon graphic = icon.create();
        graphic.addClassName("meta-icon");
        item.add(graphic, new Span(text));
        return item;
    }
}
