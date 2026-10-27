package com.example.dopamenu.service;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.MenuOrder;
import com.example.dopamenu.domain.OrderStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Pure calculation of the numbers shown on the history screen. Kept free of
 * Spring and persistence concerns so it is easy to test.
 */
public final class OrderStats {

    private OrderStats() {
    }

    public record Snapshot(long completedOrders, long minutesInvested,
            int completionRatePercent, Course favouriteCourse,
            Double averageMoodLift) {
    }

    public static Snapshot from(List<MenuOrder> orders) {
        List<MenuOrder> finished = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.ACTIVE)
                .toList();
        long completed = finished.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .count();
        long minutes = finished.stream().mapToLong(MenuOrder::minutesElapsed).sum();

        int rate = finished.isEmpty() ? 0
                : (int) Math.round(completed * 100.0 / finished.size());

        Course favourite = orders.stream()
                .collect(Collectors.groupingBy(MenuOrder::getCourse, Collectors.counting()))
                .entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry<Course, Long>::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);

        OptionalDouble moodLift = orders.stream()
                .map(MenuOrder::moodLift)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average();

        return new Snapshot(completed, minutes, rate, favourite,
                moodLift.isPresent() ? moodLift.getAsDouble() : null);
    }
}
