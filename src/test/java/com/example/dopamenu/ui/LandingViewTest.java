package com.example.dopamenu.ui;

import com.example.dopamenu.domain.Course;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.router.RouterLink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.dopamenu.ui.UiTestSupport.findAll;
import static com.example.dopamenu.ui.UiTestSupport.hasText;
import static org.assertj.core.api.Assertions.assertThat;

class LandingViewTest {

    @BeforeEach
    void setUp() {
        UiTestSupport.installUi();
    }

    @AfterEach
    void tearDown() {
        UiTestSupport.removeUi();
    }

    @Test
    void explainsWhatADopaMenuIs() {
        LandingView view = new LandingView();

        assertThat(hasText(view, "The dopa-menu")).isTrue();
        assertThat(hasText(view, "Choose the dopamine you actually want.")).isTrue();
        assertThat(hasText(view, "A menu, not a to-do list")).isTrue();
        assertThat(hasText(view, "No streaks, no scores, no guilt.")).isTrue();
        assertThat(hasText(view, "How it works")).isTrue();
        assertThat(hasText(view, "Pick a course")).isTrue();
        assertThat(hasText(view, "Run the timer")).isTrue();
        assertThat(hasText(view, "Check in")).isTrue();
    }

    @Test
    void listsEveryCourseWithItsTimeWindow() {
        LandingView view = new LandingView();

        for (Course course : Course.values()) {
            assertThat(hasText(view, course.label())).isTrue();
            assertThat(hasText(view, course.timeWindow())).isTrue();
            assertThat(hasText(view, course.tagline())).isTrue();
            assertThat(hasText(view, course.description())).isTrue();
        }
    }

    @Test
    void linksToTheOriginalVideoAndThanksTheAuthor() {
        LandingView view = new LandingView();

        assertThat(findAll(view, Anchor.class))
                .anyMatch(anchor -> LandingView.ORIGINAL_VIDEO_URL.equals(anchor.getHref()));
        assertThat(findAll(view, Anchor.class))
                .filteredOn(anchor -> LandingView.ORIGINAL_VIDEO_URL.equals(anchor.getHref()))
                .allMatch(anchor -> anchor.getTarget().filter("_blank"::equals).isPresent());

        assertThat(hasText(view, "How to ADHD")).isTrue();
        assertThat(hasText(view, "Jessica McCabe")).isTrue();
        assertThat(hasText(view, "Thank you")).isTrue();
        assertThat(hasText(view, "Where the idea comes from")).isTrue();
    }

    @Test
    void offersAWayIntoTheMenu() {
        LandingView view = new LandingView();

        assertThat(findAll(view, RouterLink.class))
                .anyMatch(link -> hasText(link, "Open the menu"));
    }

    @Test
    void endsWithThePersistentFooter() {
        LandingView view = new LandingView();

        assertThat(findAll(view, SiteFooter.class)).hasSize(1);
        assertThat(hasText(view, "made with ❤️ by dogeared")).isTrue();
    }
}
