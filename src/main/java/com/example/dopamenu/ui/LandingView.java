package com.example.dopamenu.ui;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.ui.component.CourseSection;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Public front door. Explains the dopa-menu idea and stays reachable once the
 * user has signed in.
 */
@Route(value = "", layout = PublicLayout.class)
@PageTitle("Dopa Menu")
@AnonymousAllowed
public class LandingView extends VerticalLayout {

    public static final String ORIGINAL_VIDEO_URL =
            "https://www.youtube.com/watch?v=-6WCkTwW6xg";

    public LandingView() {
        addClassName("landing-view");
        setPadding(false);
        setSpacing(false);

        add(hero());
        add(whatIsIt());
        add(courses());
        add(howItWorks());
        add(origin());
        add(new SiteFooter());
    }

    private Div hero() {
        Div hero = new Div();
        hero.addClassName("landing-hero");

        Span eyebrow = new Span("The dopa-menu");
        eyebrow.addClassName("eyebrow");
        H1 title = new H1("Choose the dopamine you actually want.");
        title.addClassName("landing-title");
        Paragraph lede = new Paragraph(
                "A dopa-menu is a short, personal menu of things that reliably make you "
                        + "feel better — organised by the time and energy they ask for, "
                        + "not by whatever is loudest on your screen.");
        lede.addClassName("landing-lede");

        Div actions = new Div();
        actions.addClassName("landing-actions");
        RouterLink open = new RouterLink("Open the menu", MenuView.class);
        Css.add(open, "btn", "btn-primary");
        Anchor video = new Anchor(ORIGINAL_VIDEO_URL, "Watch the original idea");
        Css.add(video, "btn", "btn-ghost");
        video.setTarget("_blank");
        actions.add(open, video);

        hero.add(eyebrow, title, lede, actions);
        return hero;
    }

    private Div whatIsIt() {
        Div section = section("A menu, not a to-do list");
        Paragraph first = new Paragraph(
                "Instead of reaching for the nearest high-dopamine thing when you are "
                        + "tired or stuck, you order something from a menu you wrote when "
                        + "you were thinking clearly. No streaks, no scores, no guilt.");
        first.addClassName("landing-copy");
        Paragraph second = new Paragraph(
                "The menu has four courses: quick boosts, longer activities, background "
                        + "companions, and treats that are always time-boxed.");
        second.addClassName("landing-copy");
        section.add(first, second);
        return section;
    }

    private Div courses() {
        Div section = section("Four courses");
        Div grid = new Div();
        grid.addClassName("landing-grid");
        for (Course course : Course.values()) {
            Div card = new Div();
            card.addClassName("landing-card");
            card.addClassName("course-" + course.cssName());

            Div head = new Div();
            head.addClassName("landing-card-head");
            Div iconWrap = new Div();
            iconWrap.addClassName("course-icon-small");
            Icon icon = CourseSection.iconFor(course).create();
            iconWrap.add(icon);
            H3 title = new H3(course.label());
            Span window = new Span(course.timeWindow());
            window.addClassName("landing-window");
            head.add(iconWrap, title, window);

            Span tagline = new Span(course.tagline());
            tagline.addClassName("landing-card-tagline");
            Paragraph description = new Paragraph(course.description());
            description.addClassName("landing-card-copy");

            card.add(head, tagline, description);
            grid.add(card);
        }
        section.add(grid);
        return section;
    }

    private Div howItWorks() {
        Div section = section("How it works");
        Div grid = new Div();
        grid.addClassName("landing-grid");
        grid.add(step("1", "Pick a course",
                "Start with the time and energy you actually have, not the mood you wish you were in."));
        grid.add(step("2", "Order something",
                "Choose one concrete thing from your menu. Small and specific beats ambitious and vague."));
        grid.add(step("3", "Run the timer",
                "Desserts and longer activities get a timer, so the treat stays a treat."));
        grid.add(step("4", "Check in",
                "Note how you felt before and after. Keep what worked; retire what did not."));
        section.add(grid);
        return section;
    }

    private Div origin() {
        Div section = section("Where the idea comes from");
        Div card = new Div();
        card.addClassName("landing-origin");

        Paragraph copy = new Paragraph();
        copy.addClassName("landing-copy");
        copy.add(new Span("This app was inspired by "));
        Anchor video = new Anchor(ORIGINAL_VIDEO_URL,
                "How to ADHD's video “How to Give Your Brain the Stimulation It Needs”");
        video.setTarget("_blank");
        video.addClassName("inline-link");
        copy.add(video);
        copy.add(new Span(". Thank you to Jessica McCabe and the How to ADHD team for "
                + "the idea, and for making it so understandable."));

        Anchor watch = new Anchor(ORIGINAL_VIDEO_URL, "Watch the original video");
        Css.add(watch, "btn", "btn-ghost");
        watch.setTarget("_blank");

        card.add(copy, watch);
        section.add(card);
        return section;
    }

    private static Div section(String title) {
        Div section = new Div();
        section.addClassName("landing-section");
        H2 heading = new H2(title);
        heading.addClassName("landing-section-title");
        section.add(heading);
        return section;
    }

    private static Div step(String number, String title, String copy) {
        Div step = new Div();
        step.addClassName("landing-card");
        Span numberSpan = new Span(number);
        numberSpan.addClassName("landing-step-number");
        H3 titleSpan = new H3(title);
        titleSpan.addClassName("landing-step-title");
        Paragraph body = new Paragraph(copy);
        body.addClassName("landing-card-copy");
        step.add(numberSpan, titleSpan, body);
        return step;
    }
}
