package com.example.dopamenu.ui;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UiTextTest {

    @Test
    void formatsDurations() {
        assertThat(UiText.minutes(0)).isEqualTo("0 min");
        assertThat(UiText.minutes(45)).isEqualTo("45 min");
        assertThat(UiText.minutes(60)).isEqualTo("1 hr");
        assertThat(UiText.minutes(95)).isEqualTo("1 hr 35 min");
    }

    @Test
    void formatsClockWithNoNegativeValues() {
        assertThat(UiText.clock(-4)).isEqualTo("0:00");
        assertThat(UiText.clock(9)).isEqualTo("0:09");
        assertThat(UiText.clock(605)).isEqualTo("10:05");
    }

    @Test
    void describesMoods() {
        assertThat(UiText.mood(1)).isEqualTo("Rough");
        assertThat(UiText.mood(5)).isEqualTo("Great");
        assertThat(UiText.mood(null)).isEqualTo("—");
        assertThat(UiText.sign(2)).isEqualTo("+2");
        assertThat(UiText.sign(-1)).isEqualTo("-1");
    }
}
