package com.example.dopamenu.ui.component;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuOrder;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuOrderService;
import com.example.dopamenu.ui.Css;
import com.example.dopamenu.ui.UiText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.shared.Registration;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * The order flow: check in, run the timer, check out. The timer is a plain
 * server-side countdown driven by Vaadin polling — no JavaScript involved.
 */
public class OrderDialog extends Dialog {

    private enum Phase {
        BEFORE, RUNNING, AFTER
    }

    private final MenuOrderService orderService;
    private final CurrentUser currentUser;
    private final Runnable onChanged;

    private final UUID itemId;
    private final int moodBeforeDefault;

    private String title;
    private Course course;
    private int plannedMinutes;
    private MenuOrder order;
    private Phase phase = Phase.BEFORE;
    private int extraMinutes;
    private Registration pollRegistration;
    private Span timeLabel;
    private ProgressBar progress;
    private Div timerWrap;

    public OrderDialog(MenuItem item, MenuOrderService orderService,
            CurrentUser currentUser, Runnable onChanged) {
        this.title = item.getTitle();
        this.course = item.getCourse();
        this.plannedMinutes = Math.max(1, item.getDurationMinutes());
        this.itemId = item.getId();
        this.moodBeforeDefault = 3;
        this.orderService = orderService;
        this.currentUser = currentUser;
        this.onChanged = onChanged;

        configureShell();
        showBefore();
    }

    public OrderDialog(MenuOrder order, MenuOrderService orderService,
            CurrentUser currentUser, Runnable onChanged) {
        this.title = order.getItemTitle();
        this.course = order.getCourse();
        this.plannedMinutes = Math.max(1, order.getPlannedMinutes());
        this.itemId = null;
        this.moodBeforeDefault = order.getMoodBefore() != null ? order.getMoodBefore() : 3;
        this.order = order;
        this.orderService = orderService;
        this.currentUser = currentUser;
        this.onChanged = onChanged;

        configureShell();
        showRunning();
    }

    private void configureShell() {
        addClassName("order-dialog");
        setCloseOnOutsideClick(false);
        setCloseOnEsc(true);
        addOpenedChangeListener(event -> {
            if (event.isOpened() && phase == Phase.RUNNING) {
                startPolling();
            } else if (!event.isOpened()) {
                stopPolling();
            }
        });
        addDetachListener(event -> stopPolling());
        addClosedListener(event -> stopPolling());
    }

    // ---------------------------------------------------------------- phases

    private void showBefore() {
        phase = Phase.BEFORE;
        Div shell = shell();

        Div head = new Div();
        head.addClassName("dialog-head");
        Span eyebrow = new Span("From the " + course.label() + " course");
        eyebrow.addClassName("eyebrow");
        H2 heading = new H2(title);
        heading.addClassName("dialog-title");
        head.add(eyebrow, heading, metaRow());
        shell.add(head);

        if (course.caution()) {
            shell.add(callout("Time-boxed by design. The timer starts when you do."));
        }

        MoodPicker mood = new MoodPicker("How are you feeling right now?", moodBeforeDefault);
        shell.add(mood);

        Div actions = new Div();
        actions.addClassName("dialog-actions");
        NativeButton cancel = new NativeButton("Not now");
        Css.add(cancel, "btn", "btn-ghost");
        cancel.addClickListener(event -> close());
        NativeButton start = new NativeButton();
        Css.add(start, "btn", "btn-primary");
        start.add(new Span("Start — " + UiText.minutes(plannedMinutes)));
        start.add(VaadinIcon.ARROW_RIGHT.create());
        start.addClickListener(event -> {
            try {
                order = orderService.start(currentUser.id(), itemId, mood.getValue());
            } catch (MenuOrderService.ActiveOrderException alreadyRunning) {
                order = alreadyRunning.getOrder();
                title = order.getItemTitle();
                course = order.getCourse();
                plannedMinutes = Math.max(1, order.getPlannedMinutes());
                Notification.show("You already had something running — picking it back up.");
            }
            onChanged.run();
            showRunning();
        });
        actions.add(cancel, start);
        shell.add(actions);
    }

    private void showRunning() {
        if (order == null) {
            return;
        }
        phase = Phase.RUNNING;
        Div shell = shell();

        Div head = new Div();
        head.addClassName("dialog-head");
        Span eyebrow = new Span("In progress · " + course.label());
        eyebrow.addClassName("eyebrow");
        H2 heading = new H2(title);
        heading.addClassName("dialog-title");
        head.add(eyebrow, heading);
        shell.add(head);

        timerWrap = new Div();
        timerWrap.addClassName("timer-wrap");
        timeLabel = new Span();
        timeLabel.addClassName("timer-display");
        Span caption = new Span("left on the clock");
        caption.addClassName("timer-caption");
        progress = new ProgressBar(0, 1, 0);
        progress.addClassName("timer-progress");
        timerWrap.add(timeLabel, caption, progress);
        shell.add(timerWrap);

        Div meta = new Div();
        meta.addClassName("timer-meta");
        meta.add(metaItem(VaadinIcon.CLOCK,
                "Planned " + UiText.minutes(plannedMinutes)));
        if (course.caution()) {
            meta.add(metaItem(VaadinIcon.TIMER, "Dessert — stop when it rings"));
        }
        shell.add(meta);

        Div actions = new Div();
        Css.add(actions, "dialog-actions", "dialog-actions-wrap");
        NativeButton more = new NativeButton("+5 min");
        Css.add(more, "btn", "btn-ghost");
        more.addClickListener(event -> {
            extraMinutes += 5;
            tick();
        });
        NativeButton done = new NativeButton("I'm done");
        Css.add(done, "btn", "btn-primary");
        done.addClickListener(event -> showAfter());
        NativeButton walkAway = new NativeButton("End without logging");
        Css.add(walkAway, "btn", "btn-quiet");
        walkAway.addClickListener(event -> {
            orderService.abandon(currentUser.id(), order.getId());
            onChanged.run();
            close();
        });
        actions.add(walkAway, more, done);
        shell.add(actions);

        tick();
        startPolling();
    }

    private void showAfter() {
        phase = Phase.AFTER;
        stopPolling();
        Div shell = shell();

        Div head = new Div();
        head.addClassName("dialog-head");
        Span eyebrow = new Span("Almost served");
        eyebrow.addClassName("eyebrow");
        H2 heading = new H2("How did that land?");
        heading.addClassName("dialog-title");
        Span spent = new Span("You gave it " + UiText.minutes(order.minutesElapsed())
                + ". Before: " + UiText.mood(order.getMoodBefore()) + ".");
        spent.addClassName("dialog-subtitle");
        head.add(eyebrow, heading, spent);
        shell.add(head);

        MoodPicker mood = new MoodPicker("How do you feel now?",
                order.getMoodBefore() != null ? order.getMoodBefore() : 3);
        shell.add(mood);

        TextArea note = new TextArea("What did it give you?");
        note.setPlaceholder("Optional — one line is plenty.");
        note.addClassName("dialog-note");
        note.setMaxLength(600);
        shell.add(note);

        Div actions = new Div();
        actions.addClassName("dialog-actions");
        NativeButton back = new NativeButton("Back to timer");
        Css.add(back, "btn", "btn-ghost");
        back.addClickListener(event -> showRunning());
        NativeButton log = new NativeButton();
        Css.add(log, "btn", "btn-primary");
        log.add(new Span("Log it"));
        log.add(VaadinIcon.CHECK.create());
        log.addClickListener(event -> {
            orderService.complete(currentUser.id(), order.getId(), mood.getValue(),
                    note.getValue());
            Notification.show("Logged. Nice work.");
            onChanged.run();
            close();
        });
        actions.add(back, log);
        shell.add(actions);
    }

    // --------------------------------------------------------------- helpers

    private Div shell() {
        removeAll();
        Div shell = new Div();
        shell.addClassName("dialog-shell");
        add(shell);
        return shell;
    }

    private Div metaRow() {
        Div meta = new Div();
        meta.addClassName("dialog-meta");
        meta.add(metaItem(VaadinIcon.CLOCK, UiText.minutes(plannedMinutes)));
        meta.add(metaItem(VaadinIcon.BOLT,
                course.caution() ? "Timer required" : "No timer needed"));
        return meta;
    }

    private static Div metaItem(VaadinIcon icon, String text) {
        Div item = new Div();
        item.addClassName("meta-item");
        Icon graphic = icon.create();
        graphic.addClassName("meta-icon");
        item.add(graphic, new Span(text));
        return item;
    }

    private static Div callout(String text) {
        Div callout = new Div();
        Css.add(callout, "callout", "callout-warn");
        Icon icon = VaadinIcon.TIMER.create();
        icon.addClassName("callout-icon");
        callout.add(icon, new Span(text));
        return callout;
    }

    private void tick() {
        if (order == null || timeLabel == null) {
            return;
        }
        long elapsed = Math.max(0,
                Duration.between(order.getStartedAt(), Instant.now()).getSeconds());
        long total = (plannedMinutes + extraMinutes) * 60L;
        long remaining = total - elapsed;
        boolean over = remaining < 0;
        timeLabel.setText(over ? "+" + UiText.clock(-remaining) : UiText.clock(remaining));
        timeLabel.getElement().getClassList().set("is-over", over);
        if (timerWrap != null) {
            timerWrap.getElement().getClassList().set("is-over", over);
        }
        progress.setValue(total == 0 ? 1 : Math.min(1.0, elapsed / (double) total));
    }

    private void startPolling() {
        UI ui = UI.getCurrent();
        if (ui == null || pollRegistration != null) {
            return;
        }
        ui.setPollInterval(1000);
        pollRegistration = ui.addPollListener(event -> {
            if (phase == Phase.RUNNING) {
                tick();
            }
        });
    }

    private void stopPolling() {
        if (pollRegistration != null) {
            pollRegistration.remove();
            pollRegistration = null;
        }
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.setPollInterval(-1);
        }
    }
}
