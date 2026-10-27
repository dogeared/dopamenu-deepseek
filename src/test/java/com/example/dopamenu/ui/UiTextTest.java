package com.example.dopamenu.ui;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class UiTextTest {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM");

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
        assertThat(UiText.mood(2)).isEqualTo("Low");
        assertThat(UiText.mood(3)).isEqualTo("Okay");
        assertThat(UiText.mood(4)).isEqualTo("Good");
        assertThat(UiText.mood(5)).isEqualTo("Great");
        assertThat(UiText.mood(null)).isEqualTo("—");
        assertThat(UiText.mood(0)).isEqualTo("—");
        assertThat(UiText.sign(2)).isEqualTo("+2");
        assertThat(UiText.sign(-1)).isEqualTo("-1");
        assertThat(UiText.sign(0)).isEqualTo("0");
    }

    @Test
    void describesMoodsWithEmoji() {
        assertThat(UiText.moodEmoji(1)).isEqualTo("😖");
        assertThat(UiText.moodEmoji(2)).isEqualTo("😕");
        assertThat(UiText.moodEmoji(3)).isEqualTo("😐");
        assertThat(UiText.moodEmoji(4)).isEqualTo("🙂");
        assertThat(UiText.moodEmoji(5)).isEqualTo("😄");
        assertThat(UiText.moodEmoji(null)).isEqualTo("·");
        assertThat(UiText.moodEmoji(9)).isEqualTo("·");
    }

    @Test
    void labelsTodayYesterdayAndOlderDays() {
        Instant tenDaysAgo = Instant.now().minus(10, ChronoUnit.DAYS);
        String expected = LocalDate.now().minusDays(10).format(DAY);

        assertThat(UiText.day(Instant.now())).isEqualTo("Today");
        assertThat(UiText.day(Instant.now().minus(1, ChronoUnit.DAYS)))
                .isEqualTo("Yesterday");
        assertThat(UiText.day(tenDaysAgo)).isEqualTo(expected);
    }

    @Test
    void formatsTimeInTheLocalZone() {
        Instant instant = Instant.parse("2026-10-27T14:05:00Z");
        String expected = instant.atZone(ZoneId.systemDefault()).format(TIME);

        assertThat(UiText.time(instant)).isEqualTo(expected);
    }

    @Test
    void describesRelativeTimesFromJustNowToOlderDays() {
        assertThat(UiText.relative(Instant.now())).isEqualTo("just now");
        assertThat(UiText.relative(Instant.now().minus(5, ChronoUnit.MINUTES)
                .minusSeconds(5))).isEqualTo("5 min ago");
        assertThat(UiText.relative(Instant.now().minus(61, ChronoUnit.MINUTES)))
                .isEqualTo("1 hr ago");
        assertThat(UiText.relative(Instant.now().minus(3, ChronoUnit.HOURS)
                .minusSeconds(5))).isEqualTo("3 hrs ago");
        assertThat(UiText.relative(Instant.now().minus(25, ChronoUnit.HOURS)))
                .isEqualTo("1 day ago");
        assertThat(UiText.relative(Instant.now().minus(3, ChronoUnit.DAYS)
                .minusSeconds(5))).isEqualTo("3 days ago");

        Instant tenDaysAgo = Instant.now().minus(10, ChronoUnit.DAYS);
        assertThat(UiText.relative(tenDaysAgo)).isEqualTo(UiText.day(tenDaysAgo));
    }
}
