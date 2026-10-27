package com.example.dopamenu.ui;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Small formatting helpers shared by the views.
 */
public final class UiText {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("h:mm a");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEE d MMM");

    private UiText() {
    }

    public static String minutes(long minutes) {
        if (minutes < 60) {
            return minutes + " min";
        }
        long hours = minutes / 60;
        long rest = minutes % 60;
        return rest == 0 ? hours + " hr" : hours + " hr " + rest + " min";
    }

    public static String clock(long totalSeconds) {
        long seconds = Math.max(0, totalSeconds);
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    public static String day(Instant instant) {
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();
        if (date.equals(today)) {
            return "Today";
        }
        if (date.equals(today.minusDays(1))) {
            return "Yesterday";
        }
        return date.format(DAY);
    }

    public static String time(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).format(TIME);
    }

    public static String relative(Instant instant) {
        Duration ago = Duration.between(instant, Instant.now());
        long minutes = Math.max(0, ago.toMinutes());
        if (minutes < 1) {
            return "just now";
        }
        if (minutes < 60) {
            return minutes + " min ago";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + (hours == 1 ? " hr ago" : " hrs ago");
        }
        long days = hours / 24;
        if (days < 7) {
            return days + (days == 1 ? " day ago" : " days ago");
        }
        return day(instant);
    }

    public static String mood(Integer value) {
        if (value == null) {
            return "—";
        }
        return switch (value) {
            case 1 -> "Rough";
            case 2 -> "Low";
            case 3 -> "Okay";
            case 4 -> "Good";
            case 5 -> "Great";
            default -> "—";
        };
    }

    public static String moodEmoji(Integer value) {
        if (value == null) {
            return "·";
        }
        return switch (value) {
            case 1 -> "😖";
            case 2 -> "😕";
            case 3 -> "😐";
            case 4 -> "🙂";
            case 5 -> "😄";
            default -> "·";
        };
    }

    public static String sign(int value) {
        return value > 0 ? "+" + value : Integer.toString(value);
    }
}
