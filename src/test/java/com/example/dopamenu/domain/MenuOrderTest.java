package com.example.dopamenu.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MenuOrderTest {

    private MenuItem item() {
        return new MenuItem("user-1", Course.ENTREE, "A long walk", "Outside",
                45, DopamineLevel.BRIGHT, false);
    }

    @Test
    void startingAnOrderSnapshotsTheItem() {
        MenuItem item = item();

        MenuOrder order = new MenuOrder("user-1", item, 2);

        assertThat(order.getId()).isNull();
        assertThat(order.getOwnerSub()).isEqualTo("user-1");
        assertThat(order.getItem()).isSameAs(item);
        assertThat(order.getItemTitle()).isEqualTo("A long walk");
        assertThat(order.getCourse()).isEqualTo(Course.ENTREE);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ACTIVE);
        assertThat(order.getPlannedMinutes()).isEqualTo(45);
        assertThat(order.getStartedAt()).isNotNull();
        assertThat(order.getEndedAt()).isNull();
        assertThat(order.getMoodBefore()).isEqualTo(2);
        assertThat(order.getMoodAfter()).isNull();
        assertThat(order.getNote()).isNull();
    }

    @Test
    void completingRecordsMoodNoteAndEndTime() {
        MenuOrder order = new MenuOrder("user-1", item(), 2);

        order.complete(5, "Worth it");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getEndedAt()).isNotNull();
        assertThat(order.getMoodAfter()).isEqualTo(5);
        assertThat(order.getNote()).isEqualTo("Worth it");
        assertThat(order.moodLift()).isEqualTo(3);
    }

    @Test
    void abandoningEndsTheOrderWithoutAMoodAfter() {
        MenuOrder order = new MenuOrder("user-1", item(), 3);

        order.abandon();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.ABANDONED);
        assertThat(order.getEndedAt()).isNotNull();
        assertThat(order.getMoodAfter()).isNull();
        assertThat(order.moodLift()).isNull();
    }

    @Test
    void moodLiftNeedsBothReadings() {
        MenuOrder order = new MenuOrder("user-1", item(), 2);

        assertThat(order.moodLift()).isNull();
    }

    @Test
    void runningOrdersUseNowAsTheEndOfTheElapsedWindow() {
        MenuOrder order = new MenuOrder("user-1", item(), 3);

        assertThat(order.minutesElapsed()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void loggingAPastOrderKeepsTheMinutesTheUserReported() {
        MenuOrder order = new MenuOrder("user-1", item(), 2);

        order.logPast(40, 4, "Walked the loop");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.isRetroactive()).isTrue();
        assertThat(order.getActualMinutes()).isEqualTo(40);
        assertThat(order.minutesElapsed()).isEqualTo(40);
        assertThat(order.getEndedAt()).isNotNull();
        assertThat(order.getMoodAfter()).isEqualTo(4);
        assertThat(order.getNote()).isEqualTo("Walked the loop");
        assertThat(order.moodLift()).isEqualTo(2);
    }

    @Test
    void completingWithACorrectedDurationKeepsTheReportedMinutes() {
        MenuOrder order = new MenuOrder("user-1", item(), 2);

        order.complete(5, "Took longer", 52);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getActualMinutes()).isEqualTo(52);
        assertThat(order.minutesElapsed()).isEqualTo(52);
        assertThat(order.isRetroactive()).isFalse();
    }

    @Test
    void aCorrectedDurationNeedsAtLeastOneMinute() {
        MenuOrder order = new MenuOrder("user-1", item(), 2);

        assertThatThrownBy(() -> order.complete(5, null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Minutes must be at least one");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ACTIVE);
    }

    @Test
    void aTimedOrderIsNotRetroactiveAndDerivesItsMinutes() {
        MenuOrder order = new MenuOrder("user-1", item(), 2);

        order.complete(5, null);

        assertThat(order.isRetroactive()).isFalse();
        assertThat(order.getActualMinutes()).isNull();
        assertThat(order.minutesElapsed()).isLessThanOrEqualTo(1);
    }

    @Test
    void loggingAPastOrderNeedsAtLeastOneMinute() {
        MenuOrder order = new MenuOrder("user-1", item(), 2);

        assertThatThrownBy(() -> order.logPast(0, 4, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Minutes must be at least one");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ACTIVE);
        assertThat(order.isRetroactive()).isFalse();
    }
}
