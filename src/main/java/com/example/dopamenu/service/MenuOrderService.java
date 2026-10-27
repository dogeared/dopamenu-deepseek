package com.example.dopamenu.service;

import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuOrder;
import com.example.dopamenu.domain.OrderStatus;
import com.example.dopamenu.repository.MenuItemRepository;
import com.example.dopamenu.repository.MenuOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MenuOrderService {

    private final MenuOrderRepository orderRepository;
    private final MenuItemRepository itemRepository;

    public MenuOrderService(MenuOrderRepository orderRepository,
            MenuItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public Optional<MenuOrder> activeOrder(String ownerSub) {
        return orderRepository.findFirstByOwnerSubAndStatusOrderByStartedAtDesc(ownerSub,
                OrderStatus.ACTIVE);
    }

    @Transactional
    public MenuOrder start(String ownerSub, UUID itemId, int moodBefore) {
        activeOrder(ownerSub).ifPresent(order -> {
            throw new ActiveOrderException(order);
        });
        MenuItem item = itemRepository.findByIdAndOwnerSub(itemId, ownerSub)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));
        return orderRepository.save(new MenuOrder(ownerSub, item, moodBefore));
    }

    @Transactional
    public MenuOrder complete(String ownerSub, UUID orderId, int moodAfter, String note) {
        MenuOrder order = orderRepository.findByIdAndOwnerSub(orderId, ownerSub)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getStatus() != OrderStatus.ACTIVE) {
            return order;
        }
        order.complete(moodAfter, note == null || note.isBlank() ? null : note.trim());
        return order;
    }

    @Transactional
    public void abandon(String ownerSub, UUID orderId) {
        orderRepository.findByIdAndOwnerSub(orderId, ownerSub)
                .filter(order -> order.getStatus() == OrderStatus.ACTIVE)
                .ifPresent(MenuOrder::abandon);
    }

    @Transactional(readOnly = true)
    public List<MenuOrder> recentOrders(String ownerSub) {
        return orderRepository.findTop50ByOwnerSubOrderByStartedAtDesc(ownerSub);
    }

    @Transactional(readOnly = true)
    public OrderStats.Snapshot stats(String ownerSub, int days) {
        Instant after = Instant.now().minus(days, ChronoUnit.DAYS);
        return OrderStats.from(orderRepository
                .findByOwnerSubAndStartedAtAfterOrderByStartedAtDesc(ownerSub, after));
    }

    /**
     * Thrown when a second order is started while another is still running.
     */
    public static final class ActiveOrderException extends RuntimeException {

        private final transient MenuOrder order;

        ActiveOrderException(MenuOrder order) {
            super("An order is already running: " + order.getItemTitle());
            this.order = order;
        }

        public MenuOrder getOrder() {
            return order;
        }
    }
}
