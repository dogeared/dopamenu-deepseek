package com.example.dopamenu.ui;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.service.MenuItemDraft;
import com.example.dopamenu.service.MenuItemService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Create/edit form for a single menu item.
 */
public class MenuItemFormDialog extends Dialog {

    private final MenuItemService menuItemService;
    private final String ownerSub;
    private final MenuItem existing;
    private final Runnable onSaved;

    private final TextField title = new TextField("Name");
    private final TextArea description = new TextArea("Description");
    private final Select<Course> course = new Select<>();
    private final IntegerField minutes = new IntegerField("Minutes");
    private final Select<DopamineLevel> level = new Select<>();
    private final Checkbox timer = new Checkbox("Pair with a timer");

    public MenuItemFormDialog(MenuItemService menuItemService, String ownerSub,
            MenuItem existing, Runnable onSaved) {
        this.menuItemService = menuItemService;
        this.ownerSub = ownerSub;
        this.existing = existing;
        this.onSaved = onSaved;

        addClassName("form-dialog");
        configureShell();
        build();
        populate();
    }

    private void configureShell() {
        setCloseOnOutsideClick(false);
        setCloseOnEsc(true);
    }

    private void build() {
        Div shell = new Div();
        shell.addClassName("dialog-shell");

        Div head = new Div();
        head.addClassName("dialog-head");
        Span eyebrow = new Span(existing == null ? "New dish" : "Editing a dish");
        eyebrow.addClassName("eyebrow");
        H2 heading = new H2(existing == null ? "Add to the menu" : existing.getTitle());
        heading.addClassName("dialog-title");
        Span hint = new Span("Keep it concrete and small enough to actually choose.");
        hint.addClassName("dialog-subtitle");
        head.add(eyebrow, heading, hint);

        title.setPlaceholder("e.g. A ten minute walk");
        title.setMaxLength(160);
        title.addClassName("field-full");
        description.setPlaceholder("Why does this one work for you?");
        description.setMaxLength(600);
        description.setMinRows(3);
        description.addClassName("field-full");
        timer.addClassName("field-full");

        course.setLabel("Course");
        course.setItems(Course.values());
        course.setItemLabelGenerator(Course::label);

        minutes.setMin(1);
        minutes.setMax(600);
        minutes.setValue(10);
        minutes.setStepButtonsVisible(true);

        level.setLabel("Dopamine");
        level.setItems(DopamineLevel.values());
        level.setItemLabelGenerator(DopamineLevel::label);

        Div grid = new Div();
        grid.addClassName("form-grid");
        grid.add(title);
        grid.add(course);
        grid.add(minutes);
        grid.add(level);
        grid.add(description);
        grid.add(timer);

        course.addValueChangeListener(event -> {
            if (event.getValue() != null && event.getValue().caution()) {
                timer.setValue(true);
                timer.setHelperText("Desserts are always time-boxed.");
            } else {
                timer.setHelperText(null);
            }
        });

        Div actions = new Div();
        actions.addClassName("dialog-actions");
        NativeButton cancel = new NativeButton("Cancel");
        Css.add(cancel, "btn", "btn-ghost");
        cancel.addClickListener(event -> close());
        NativeButton save = new NativeButton();
        Css.add(save, "btn", "btn-primary");
        save.add(new Span(existing == null ? "Add to menu" : "Save changes"));
        save.add(VaadinIcon.CHECK.create());
        save.addClickListener(event -> save());
        actions.add(cancel, save);

        shell.add(head, grid, actions);
        add(shell);
    }

    private void populate() {
        if (existing == null) {
            course.setValue(Course.APPETIZER);
            level.setValue(DopamineLevel.STEADY);
            return;
        }
        title.setValue(existing.getTitle());
        description.setValue(existing.getDescription() == null ? "" : existing.getDescription());
        course.setValue(existing.getCourse());
        minutes.setValue(existing.getDurationMinutes());
        level.setValue(existing.getDopamineLevel());
        timer.setValue(existing.isTimerRequired());
    }

    private void save() {
        boolean valid = true;
        if (title.getValue() == null || title.getValue().isBlank()) {
            title.setInvalid(true);
            title.setErrorMessage("Give it a name");
            valid = false;
        } else {
            title.setInvalid(false);
        }
        Integer value = minutes.getValue();
        if (value == null || value < 1) {
            minutes.setInvalid(true);
            minutes.setErrorMessage("At least one minute");
            valid = false;
        } else {
            minutes.setInvalid(false);
        }
        if (course.getValue() == null) {
            course.setInvalid(true);
            valid = false;
        }
        if (level.getValue() == null) {
            level.setInvalid(true);
            valid = false;
        }
        if (!valid) {
            return;
        }

        MenuItemDraft draft = new MenuItemDraft(title.getValue(), description.getValue(),
                course.getValue(), value, level.getValue(), timer.getValue());
        if (existing == null) {
            menuItemService.create(ownerSub, draft);
            Notification.show("Added to your menu.");
        } else {
            menuItemService.update(ownerSub, existing.getId(), draft);
            Notification.show("Menu updated.");
        }
        onSaved.run();
        close();
    }
}
