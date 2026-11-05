package com.example.dopamenu.ui.component;

import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuOrderService;
import com.example.dopamenu.ui.Css;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;

/**
 * Logs an order that already happened, such as a walk taken an hour ago. The
 * user reports the minutes spent, so no timer is involved.
 */
public class LogPastDialog extends Dialog {

    private final MenuOrderService orderService;
    private final CurrentUser currentUser;
    private final MenuItem item;
    private final Runnable onChanged;

    private final IntegerField minutes = new IntegerField("Minutes spent");
    private final MoodPicker moodBefore = new MoodPicker("How were you feeling before?", 3);
    private final MoodPicker moodAfter = new MoodPicker("How do you feel now?", 4);
    private final TextArea note = new TextArea("What did it give you?");

    public LogPastDialog(MenuItem item, MenuOrderService orderService,
            CurrentUser currentUser, Runnable onChanged) {
        this.item = item;
        this.orderService = orderService;
        this.currentUser = currentUser;
        this.onChanged = onChanged;

        addClassName("form-dialog");
        addClassName("log-past-dialog");
        setCloseOnOutsideClick(false);
        setCloseOnEsc(true);
        build();
    }

    private void build() {
        Div shell = new Div();
        shell.addClassName("dialog-shell");

        Div head = new Div();
        head.addClassName("dialog-head");
        Span eyebrow = new Span("Already did this one?");
        eyebrow.addClassName("eyebrow");
        H2 heading = new H2(item.getTitle());
        heading.addClassName("dialog-title");
        Span hint = new Span("Log it after the fact. No timer — just the minutes you "
                + "actually spent.");
        hint.addClassName("dialog-subtitle");
        head.add(eyebrow, heading, hint);

        minutes.setMin(1);
        minutes.setMax(600);
        minutes.setStepButtonsVisible(true);
        minutes.setValue(Math.max(1, item.getDurationMinutes()));
        minutes.setHelperText("How long did it really take?");
        minutes.addClassName("field-full");

        note.setPlaceholder("Optional — one line is plenty.");
        note.setMaxLength(600);
        note.setMinRows(2);
        note.addClassName("dialog-note");

        Div actions = new Div();
        actions.addClassName("dialog-actions");
        NativeButton cancel = new NativeButton("Cancel");
        Css.add(cancel, "btn", "btn-ghost");
        cancel.addClickListener(event -> close());
        NativeButton log = new NativeButton();
        Css.add(log, "btn", "btn-primary");
        log.add(new Span("Add to history"));
        log.add(VaadinIcon.CHECK.create());
        log.addClickListener(event -> save());
        actions.add(cancel, log);

        shell.add(head, minutes, moodBefore, moodAfter, note, actions);
        add(shell);
    }

    private void save() {
        Integer value = minutes.getValue();
        if (value == null || value < 1) {
            minutes.setInvalid(true);
            minutes.setErrorMessage("At least one minute");
            return;
        }
        minutes.setInvalid(false);
        orderService.logPast(currentUser.id(), item.getId(), value, moodBefore.getValue(),
                moodAfter.getValue(), note.getValue());
        Notification.show("Logged. Nice work.");
        onChanged.run();
        close();
    }
}
