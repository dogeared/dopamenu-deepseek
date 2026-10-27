package com.example.dopamenu.ui;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.service.CurrentUser;
import com.example.dopamenu.service.MenuItemService;
import com.example.dopamenu.ui.component.CourseSection;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "manage", layout = MainLayout.class)
@PageTitle("Edit menu · Dopa Menu")
@PermitAll
public class ManageMenuView extends VerticalLayout {

    private final MenuItemService menuItemService;
    private final CurrentUser currentUser;

    public ManageMenuView(MenuItemService menuItemService, CurrentUser currentUser) {
        this.menuItemService = menuItemService;
        this.currentUser = currentUser;
        addClassName("manage-view");
        setPadding(false);
        setSpacing(false);
        render();
    }

    private void render() {
        removeAll();
        List<MenuItem> items = menuItemService.ensureSeeded(currentUser.id());

        add(head());
        for (Course course : Course.values()) {
            add(group(course, items.stream()
                    .filter(item -> item.getCourse() == course)
                    .toList()));
        }
    }

    private Div head() {
        Div head = new Div();
        Css.add(head, "page-head", "manage-head");

        Div copy = new Div();
        copy.addClassName("page-head-copy");
        Span eyebrow = new Span("Your kitchen");
        eyebrow.addClassName("eyebrow");
        H1 title = new H1("Edit the menu.");
        title.addClassName("page-title");
        Paragraph lede = new Paragraph(
                "A good menu is specific. Add the things that reliably work for you "
                        + "and retire the ones that only look good on paper.");
        lede.addClassName("page-lede");
        copy.add(eyebrow, title, lede);

        NativeButton add = new NativeButton();
        Css.add(add, "btn", "btn-primary", "manage-add");
        add.add(VaadinIcon.PLUS.create());
        add.add(new Span("New dish"));
        add.addClickListener(event -> Dialogs.open(new MenuItemFormDialog(menuItemService,
                currentUser.id(), null, this::render)));

        head.add(copy, add);
        return head;
    }

    private Div group(Course course, List<MenuItem> items) {
        Div group = new Div();
        group.addClassName("manage-group");
        group.addClassName("course-" + course.cssName());

        Div head = new Div();
        head.addClassName("manage-group-head");
        Div iconWrap = new Div();
        Css.add(iconWrap, "course-icon", "course-icon-small");
        Icon icon = CourseSection.iconFor(course).create();
        iconWrap.add(icon);
        H2 title = new H2(course.label());
        title.addClassName("manage-group-title");
        Span count = new Span(items.size() + (items.size() == 1 ? " item" : " items"));
        count.addClassName("manage-count");
        head.add(iconWrap, title, count);
        group.add(head);

        if (items.isEmpty()) {
            Div empty = new Div();
            empty.addClassName("manage-empty");
            empty.add(new Span("Nothing on this course yet."));
            group.add(empty);
            return group;
        }

        for (MenuItem item : items) {
            group.add(row(item));
        }
        return group;
    }

    private Div row(MenuItem item) {
        Div row = new Div();
        row.addClassName("manage-row");

        Div main = new Div();
        main.addClassName("manage-main");
        Span title = new Span(item.getTitle());
        title.addClassName("manage-title");
        main.add(title);
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            Span description = new Span(item.getDescription());
            description.addClassName("manage-description");
            main.add(description);
        }

        Div meta = new Div();
        meta.addClassName("manage-meta");
        meta.add(new Span(UiText.minutes(item.getDurationMinutes())));
        meta.add(new Span(item.getDopamineLevel().label()));
        if (item.isTimerRequired()) {
            Span timer = new Span("Timer");
            timer.addClassName("mini-pill");
            meta.add(timer);
        }

        Div actions = new Div();
        actions.addClassName("manage-actions");
        NativeButton edit = iconButton(VaadinIcon.PENCIL, "Edit " + item.getTitle());
        edit.addClickListener(event -> Dialogs.open(new MenuItemFormDialog(menuItemService,
                currentUser.id(), item, this::render)));
        NativeButton remove = iconButton(VaadinIcon.TRASH, "Remove " + item.getTitle());
        remove.addClassName("is-danger");
        remove.addClickListener(event -> {
            menuItemService.archive(currentUser.id(), item.getId());
            Notification.show("Removed “" + item.getTitle() + "” from your menu.");
            render();
        });
        actions.add(edit, remove);

        row.add(main, meta, actions);
        return row;
    }

    private static NativeButton iconButton(VaadinIcon icon, String label) {
        NativeButton button = new NativeButton();
        button.addClassName("icon-button");
        button.setAriaLabel(label);
        button.setTitle(label);
        Icon graphic = icon.create();
        button.add(graphic);
        return button;
    }
}
