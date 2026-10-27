package com.example.dopamenu.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MenuItemTest {

    @Test
    void createsAnActiveItemWithTimestamps() {
        MenuItem item = new MenuItem("user-1", Course.APPETIZER, "Water", "Hydrate",
                2, DopamineLevel.GENTLE, false);

        assertThat(item.getId()).isNull();
        assertThat(item.getOwnerSub()).isEqualTo("user-1");
        assertThat(item.getCourse()).isEqualTo(Course.APPETIZER);
        assertThat(item.getTitle()).isEqualTo("Water");
        assertThat(item.getDescription()).isEqualTo("Hydrate");
        assertThat(item.getDurationMinutes()).isEqualTo(2);
        assertThat(item.getDopamineLevel()).isEqualTo(DopamineLevel.GENTLE);
        assertThat(item.isTimerRequired()).isFalse();
        assertThat(item.getCreatedAt()).isNotNull();
        assertThat(item.isArchived()).isFalse();
    }

    @Test
    void updatesEveryEditableField() {
        MenuItem item = new MenuItem("user-1", Course.APPETIZER, "Water", null,
                2, DopamineLevel.GENTLE, false);

        item.setCourse(Course.DESSERT);
        item.setTitle("One video");
        item.setDescription("Just one");
        item.setDurationMinutes(10);
        item.setDopamineLevel(DopamineLevel.INTENSE);
        item.setTimerRequired(true);

        assertThat(item.getCourse()).isEqualTo(Course.DESSERT);
        assertThat(item.getTitle()).isEqualTo("One video");
        assertThat(item.getDescription()).isEqualTo("Just one");
        assertThat(item.getDurationMinutes()).isEqualTo(10);
        assertThat(item.getDopamineLevel()).isEqualTo(DopamineLevel.INTENSE);
        assertThat(item.isTimerRequired()).isTrue();
    }

    @Test
    void archiveIsOneWay() {
        MenuItem item = new MenuItem("user-1", Course.SIDE, "Podcast", null,
                25, DopamineLevel.STEADY, false);

        item.archive();

        assertThat(item.isArchived()).isTrue();
    }
}
