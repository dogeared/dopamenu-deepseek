package com.example.dopamenu.service;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuOrder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatsTest {

    private MenuItem dessert() {
        return new MenuItem("user-1", Course.DESSERT, "One video", "Just one.",
                10, DopamineLevel.INTENSE, true);
    }

    @Test
    void emptyHistoryProducesEmptySnapshot() {
        OrderStats.Snapshot snapshot = OrderStats.from(List.of());

        assertThat(snapshot.completedOrders()).isZero();
        assertThat(snapshot.minutesInvested()).isZero();
        assertThat(snapshot.completionRatePercent()).isZero();
        assertThat(snapshot.favouriteCourse()).isNull();
        assertThat(snapshot.averageMoodLift()).isNull();
    }

    @Test
    void countsCompletionAndMoodLift() {
        MenuOrder completed = new MenuOrder("user-1", dessert(), 2);
        completed.complete(5, "Worth it");

        MenuOrder abandoned = new MenuOrder("user-1", dessert(), 3);
        abandoned.abandon();

        OrderStats.Snapshot snapshot = OrderStats.from(List.of(completed, abandoned));

        assertThat(snapshot.completedOrders()).isEqualTo(1);
        assertThat(snapshot.completionRatePercent()).isEqualTo(50);
        assertThat(snapshot.favouriteCourse()).isEqualTo(Course.DESSERT);
        assertThat(snapshot.averageMoodLift()).isEqualTo(3.0);
    }

    @Test
    void activeOrdersCountTowardsFavouriteButNotCompletion() {
        MenuItem appetizer = new MenuItem("user-1", Course.APPETIZER, "Water", null,
                2, DopamineLevel.GENTLE, false);
        MenuOrder active = new MenuOrder("user-1", appetizer, 3);

        OrderStats.Snapshot snapshot = OrderStats.from(List.of(active));

        assertThat(snapshot.completedOrders()).isZero();
        assertThat(snapshot.completionRatePercent()).isZero();
        assertThat(snapshot.favouriteCourse()).isEqualTo(Course.APPETIZER);
        assertThat(snapshot.averageMoodLift()).isNull();
    }
}
