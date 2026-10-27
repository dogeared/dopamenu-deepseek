package com.example.dopamenu.service;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuOrder;
import com.example.dopamenu.domain.OrderStatus;
import com.example.dopamenu.repository.MenuItemRepository;
import com.example.dopamenu.repository.MenuOrderRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuOrderServiceTest {

    private final MenuOrderRepository orderRepository = mock(MenuOrderRepository.class);
    private final MenuItemRepository itemRepository = mock(MenuItemRepository.class);
    private final MenuOrderService service = new MenuOrderService(orderRepository,
            itemRepository);

    private MenuItem item() {
        return new MenuItem("user-1", Course.ENTREE, "A long walk", null,
                45, DopamineLevel.BRIGHT, false);
    }

    @Test
    void activeOrderIsTheMostRecentRunningOne() {
        MenuOrder active = new MenuOrder("user-1", item(), 3);
        when(orderRepository.findFirstByOwnerSubAndStatusOrderByStartedAtDesc("user-1",
                OrderStatus.ACTIVE)).thenReturn(Optional.of(active));

        assertThat(service.activeOrder("user-1")).contains(active);
    }

    @Test
    void startCreatesAnOrderFromTheItem() {
        UUID itemId = UUID.randomUUID();
        MenuItem item = item();
        when(orderRepository.findFirstByOwnerSubAndStatusOrderByStartedAtDesc("user-1",
                OrderStatus.ACTIVE)).thenReturn(Optional.empty());
        when(itemRepository.findByIdAndOwnerSub(itemId, "user-1"))
                .thenReturn(Optional.of(item));
        when(orderRepository.save(any(MenuOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MenuOrder order = service.start("user-1", itemId, 4);

        assertThat(order.getItem()).isSameAs(item);
        assertThat(order.getMoodBefore()).isEqualTo(4);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ACTIVE);
        verify(orderRepository).save(order);
    }

    @Test
    void startRefusesWhenAnOrderIsAlreadyRunning() {
        MenuOrder active = new MenuOrder("user-1", item(), 3);
        when(orderRepository.findFirstByOwnerSubAndStatusOrderByStartedAtDesc("user-1",
                OrderStatus.ACTIVE)).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> service.start("user-1", UUID.randomUUID(), 3))
                .isInstanceOf(MenuOrderService.ActiveOrderException.class)
                .satisfies(error -> assertThat(
                        ((MenuOrderService.ActiveOrderException) error).getOrder())
                        .isSameAs(active))
                .hasMessageContaining("A long walk");
        verify(orderRepository, never()).save(any());
    }

    @Test
    void startFailsWhenTheItemDoesNotBelongToTheUser() {
        when(orderRepository.findFirstByOwnerSubAndStatusOrderByStartedAtDesc("user-1",
                OrderStatus.ACTIVE)).thenReturn(Optional.empty());
        when(itemRepository.findByIdAndOwnerSub(any(), eq("user-1")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.start("user-1", UUID.randomUUID(), 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Menu item not found");
    }

    @Test
    void completeTrimsTheNote() {
        MenuOrder order = new MenuOrder("user-1", item(), 2);
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdAndOwnerSub(orderId, "user-1"))
                .thenReturn(Optional.of(order));

        MenuOrder completed = service.complete("user-1", orderId, 5, "  worth it  ");

        assertThat(completed).isSameAs(order);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getNote()).isEqualTo("worth it");
        assertThat(order.getMoodAfter()).isEqualTo(5);
    }

    @Test
    void completeStoresNullForBlankNotes() {
        MenuOrder order = new MenuOrder("user-1", item(), 2);
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdAndOwnerSub(orderId, "user-1"))
                .thenReturn(Optional.of(order));

        service.complete("user-1", orderId, 5, "   ");

        assertThat(order.getNote()).isNull();
    }

    @Test
    void completeLeavesAlreadyFinishedOrdersUntouched() {
        MenuOrder order = new MenuOrder("user-1", item(), 2);
        order.complete(4, "first");
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findByIdAndOwnerSub(orderId, "user-1"))
                .thenReturn(Optional.of(order));

        MenuOrder result = service.complete("user-1", orderId, 5, "second");

        assertThat(result).isSameAs(order);
        assertThat(order.getMoodAfter()).isEqualTo(4);
        assertThat(order.getNote()).isEqualTo("first");
    }

    @Test
    void completeFailsForAnUnknownOrder() {
        when(orderRepository.findByIdAndOwnerSub(any(), eq("user-1")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.complete("user-1", UUID.randomUUID(), 3, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order not found");
    }

    @Test
    void abandonOnlyAffectsRunningOrders() {
        MenuOrder running = new MenuOrder("user-1", item(), 3);
        UUID runningId = UUID.randomUUID();
        when(orderRepository.findByIdAndOwnerSub(runningId, "user-1"))
                .thenReturn(Optional.of(running));

        service.abandon("user-1", runningId);

        assertThat(running.getStatus()).isEqualTo(OrderStatus.ABANDONED);

        MenuOrder finished = new MenuOrder("user-1", item(), 3);
        finished.complete(4, null);
        UUID finishedId = UUID.randomUUID();
        when(orderRepository.findByIdAndOwnerSub(finishedId, "user-1"))
                .thenReturn(Optional.of(finished));

        service.abandon("user-1", finishedId);

        assertThat(finished.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void abandonIgnoresUnknownOrders() {
        when(orderRepository.findByIdAndOwnerSub(any(), eq("user-1")))
                .thenReturn(Optional.empty());

        service.abandon("user-1", UUID.randomUUID());

        verify(orderRepository).findByIdAndOwnerSub(any(), eq("user-1"));
    }

    @Test
    void recentOrdersAreDelegated() {
        MenuOrder order = new MenuOrder("user-1", item(), 3);
        when(orderRepository.findTop50ByOwnerSubOrderByStartedAtDesc("user-1"))
                .thenReturn(List.of(order));

        assertThat(service.recentOrders("user-1")).containsExactly(order);
    }

    @Test
    void statsUseOrdersStartedWithinTheWindow() {
        MenuOrder order = new MenuOrder("user-1", item(), 2);
        order.complete(5, null);
        when(orderRepository.findByOwnerSubAndStartedAtAfterOrderByStartedAtDesc(eq("user-1"),
                any(Instant.class))).thenReturn(List.of(order));

        OrderStats.Snapshot snapshot = service.stats("user-1", 7);

        assertThat(snapshot.completedOrders()).isEqualTo(1);
        assertThat(snapshot.completionRatePercent()).isEqualTo(100);
        assertThat(snapshot.favouriteCourse()).isEqualTo(Course.ENTREE);
        assertThat(snapshot.averageMoodLift()).isEqualTo(3.0);
    }
}
