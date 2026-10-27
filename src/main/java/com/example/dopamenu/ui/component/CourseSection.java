package com.example.dopamenu.ui.component;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.ui.ManageMenuView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.RouterLink;

import java.util.List;
import java.util.function.Consumer;

public class CourseSection extends Div {

    public CourseSection(Course course, List<MenuItem> items, Consumer<MenuItem> onOrder) {
        addClassName("course-section");
        addClassName("course-" + course.cssName());

        Div head = new Div();
        head.addClassName("course-head");

        Div iconWrap = new Div();
        iconWrap.addClassName("course-icon");
        Icon icon = iconFor(course).create();
        iconWrap.add(icon);

        Div heading = new Div();
        heading.addClassName("course-heading");
        Span tagline = new Span(course.tagline());
        tagline.addClassName("course-tagline");
        H2 title = new H2(course.label());
        title.addClassName("course-title");
        Paragraph description = new Paragraph(course.description());
        description.addClassName("course-description");
        heading.add(tagline, title, description);

        Span window = new Span(course.timeWindow());
        window.addClassName("course-window");

        head.add(iconWrap, heading, window);
        add(head);

        Div grid = new Div();
        grid.addClassName("card-grid");
        if (items.isEmpty()) {
            Div empty = new Div();
            empty.addClassName("course-empty");
            Span text = new Span("Nothing on this course yet. ");
            RouterLink add = new RouterLink("Add something", ManageMenuView.class);
            add.addClassName("inline-link");
            empty.add(text, add);
            grid.add(empty);
        } else {
            items.forEach(item -> grid.add(new MenuItemCard(item, onOrder)));
        }
        add(grid);
    }

    public static VaadinIcon iconFor(Course course) {
        return switch (course) {
            case APPETIZER -> VaadinIcon.COFFEE;
            case ENTREE -> VaadinIcon.ROCKET;
            case SIDE -> VaadinIcon.HEADPHONES;
            case DESSERT -> VaadinIcon.GAMEPAD;
        };
    }
}
