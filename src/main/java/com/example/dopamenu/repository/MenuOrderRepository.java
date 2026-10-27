package com.example.dopamenu.repository;

import com.example.dopamenu.domain.MenuOrder;
import com.example.dopamenu.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuOrderRepository extends JpaRepository<MenuOrder, UUID> {

    Optional<MenuOrder> findFirstByOwnerSubAndStatusOrderByStartedAtDesc(String ownerSub,
            OrderStatus status);

    Optional<MenuOrder> findByIdAndOwnerSub(UUID id, String ownerSub);

    List<MenuOrder> findTop50ByOwnerSubOrderByStartedAtDesc(String ownerSub);

    List<MenuOrder> findByOwnerSubAndStartedAtAfterOrderByStartedAtDesc(String ownerSub,
            Instant after);
}
