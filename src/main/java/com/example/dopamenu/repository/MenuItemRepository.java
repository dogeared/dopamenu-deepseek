package com.example.dopamenu.repository;

import com.example.dopamenu.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    List<MenuItem> findByOwnerSubAndArchivedFalse(String ownerSub);

    Optional<MenuItem> findByIdAndOwnerSub(UUID id, String ownerSub);

    long countByOwnerSub(String ownerSub);
}
