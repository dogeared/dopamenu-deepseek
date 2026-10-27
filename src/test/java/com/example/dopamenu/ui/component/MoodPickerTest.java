package com.example.dopamenu.ui.component;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.ui.UiTestSupport;
import com.vaadin.flow.component.html.NativeButton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.example.dopamenu.ui.UiTestSupport.click;
import static com.example.dopamenu.ui.UiTestSupport.findAll;
import static com.example.dopamenu.ui.UiTestSupport.hasText;
import static org.assertj.core.api.Assertions.assertThat;

class MoodPickerTest {

    @BeforeEach
    void setUp() {
        UiTestSupport.installUi();
    }

    @AfterEach
    void tearDown() {
        UiTestSupport.removeUi();
    }

    @Test
    void rendersFiveOptionsAndSelectsTheInitialValue() {
        MoodPicker picker = new MoodPicker("How are you feeling?", 3);

        assertThat(hasText(picker, "How are you feeling?")).isTrue();
        assertThat(findAll(picker, NativeButton.class)).hasSize(5);
        assertThat(picker.getValue()).isEqualTo(3);

        List<NativeButton> buttons = findAll(picker, NativeButton.class);
        assertThat(buttons.get(2).getElement().getAttribute("aria-pressed"))
                .isEqualTo("true");
        assertThat(buttons.get(2).getClassNames()).contains("is-selected");
        assertThat(buttons.get(0).getElement().getAttribute("aria-pressed"))
                .isEqualTo("false");
    }

    @Test
    void clickingAnOptionChangesTheValue() {
        MoodPicker picker = new MoodPicker("Mood", 1);

        click(findAll(picker, NativeButton.class).get(4));

        assertThat(picker.getValue()).isEqualTo(5);
        assertThat(findAll(picker, NativeButton.class).get(4).getClassNames())
                .contains("is-selected");
        assertThat(findAll(picker, NativeButton.class).get(0).getClassNames())
                .doesNotContain("is-selected");
    }

    @Test
    void valuesAreClampedToTheFivePointScale() {
        MoodPicker picker = new MoodPicker("Mood", 0);
        assertThat(picker.getValue()).isEqualTo(1);

        picker.setValue(99);
        assertThat(picker.getValue()).isEqualTo(5);
    }

    @Test
    void everyOptionHasAnAccessibleName() {
        MoodPicker picker = new MoodPicker("Mood", 3);

        assertThat(findAll(picker, NativeButton.class))
                .extracting(button -> button.getAriaLabel().orElse(null))
                .containsExactly("Rough", "Low", "Okay", "Good", "Great");
    }

    @Test
    void orderingAnItemInvokesTheCallback() {
        MenuItem item = new MenuItem("user-1", Course.APPETIZER, "Water", null,
                2, DopamineLevel.GENTLE, false);
        List<MenuItem> ordered = new ArrayList<>();
        MenuItemCard card = new MenuItemCard(item, ordered::add);

        click(UiTestSupport.button(card, "Order"));

        assertThat(ordered).containsExactly(item);
    }

    @Test
    void cardShowsDescriptionTimerAndStrength() {
        MenuItem item = new MenuItem("user-1", Course.DESSERT, "One video",
                "Not the next one", 10, DopamineLevel.INTENSE, true);
        MenuItemCard card = new MenuItemCard(item, ignored -> {
        });

        assertThat(hasText(card, "One video")).isTrue();
        assertThat(hasText(card, "Not the next one")).isTrue();
        assertThat(hasText(card, "10 min")).isTrue();
        assertThat(hasText(card, "Timer")).isTrue();
        assertThat(hasText(card, "Intense")).isTrue();
        assertThat(hasText(card, "Order One video")).isFalse();
        assertThat(card.getClassNames()).contains("menu-card", "strength-intense");
        assertThat(findAll(card, com.vaadin.flow.component.html.Span.class))
                .filteredOn(span -> span.getClassNames().contains("dopamine-dot"))
                .hasSize(4)
                .filteredOn(span -> span.getClassNames().contains("is-on"))
                .hasSize(4);
    }

    @Test
    void cardOmitsDescriptionWhenThereIsNone() {
        MenuItem item = new MenuItem("user-1", Course.APPETIZER, "Water", "  ",
                2, DopamineLevel.GENTLE, false);
        MenuItemCard card = new MenuItemCard(item, ignored -> {
        });

        assertThat(findAll(card, com.vaadin.flow.component.html.Paragraph.class))
                .isEmpty();
        assertThat(findAll(card, com.vaadin.flow.component.html.Span.class))
                .filteredOn(span -> span.getClassNames().contains("dopamine-dot"))
                .filteredOn(span -> span.getClassNames().contains("is-on"))
                .hasSize(1);
    }

    @Test
    void courseSectionShowsItsCopyAndCards() {
        MenuItem item = new MenuItem("user-1", Course.APPETIZER, "Water", null,
                2, DopamineLevel.GENTLE, false);

        CourseSection section = new CourseSection(Course.APPETIZER, List.of(item),
                ignored -> {
                });

        assertThat(hasText(section, "Appetizers")).isTrue();
        assertThat(hasText(section, "Quick boosts")).isTrue();
        assertThat(hasText(section, "1–10 min")).isTrue();
        assertThat(hasText(section, "Fast, low-effort")).isTrue();
        assertThat(hasText(section, "Water")).isTrue();
        assertThat(section.getClassNames()).contains("course-section", "course-appetizer");
        assertThat(CourseSection.iconFor(Course.APPETIZER))
                .isEqualTo(com.vaadin.flow.component.icon.VaadinIcon.COFFEE);
        assertThat(CourseSection.iconFor(Course.ENTREE))
                .isEqualTo(com.vaadin.flow.component.icon.VaadinIcon.ROCKET);
        assertThat(CourseSection.iconFor(Course.SIDE))
                .isEqualTo(com.vaadin.flow.component.icon.VaadinIcon.HEADPHONES);
        assertThat(CourseSection.iconFor(Course.DESSERT))
                .isEqualTo(com.vaadin.flow.component.icon.VaadinIcon.GAMEPAD);
    }

    @Test
    void emptyCourseSectionInvitesTheUserToAddSomething() {
        CourseSection section = new CourseSection(Course.SIDE, List.of(), ignored -> {
        });

        assertThat(hasText(section, "Nothing on this course yet.")).isTrue();
        assertThat(hasText(section, "Add something")).isTrue();
    }
}
