package com.example.dopamenu.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CourseTest {

    @Test
    void exposesCopyOrderingAndCssName() {
        assertThat(Course.APPETIZER.label()).isEqualTo("Appetizers");
        assertThat(Course.APPETIZER.tagline()).isEqualTo("Quick boosts");
        assertThat(Course.APPETIZER.timeWindow()).isEqualTo("1–10 min");
        assertThat(Course.APPETIZER.description()).startsWith("Fast, low-effort");
        assertThat(Course.APPETIZER.order()).isEqualTo(1);
        assertThat(Course.APPETIZER.caution()).isFalse();
        assertThat(Course.APPETIZER.cssName()).isEqualTo("appetizer");

        assertThat(Course.ENTREE.order()).isEqualTo(2);
        assertThat(Course.ENTREE.cssName()).isEqualTo("entree");

        assertThat(Course.SIDE.order()).isEqualTo(3);
        assertThat(Course.SIDE.cssName()).isEqualTo("side");

        assertThat(Course.DESSERT.order()).isEqualTo(4);
        assertThat(Course.DESSERT.caution()).isTrue();
        assertThat(Course.DESSERT.cssName()).isEqualTo("dessert");
    }

    @Test
    void everyCourseHasAllCopy() {
        for (Course course : Course.values()) {
            assertThat(course.label()).isNotBlank();
            assertThat(course.tagline()).isNotBlank();
            assertThat(course.timeWindow()).isNotBlank();
            assertThat(course.description()).isNotBlank();
        }
    }
}
