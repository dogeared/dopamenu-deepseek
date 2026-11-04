package com.example.dopamenu.repository;

import com.example.dopamenu.domain.MenuShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MenuShareRepository extends JpaRepository<MenuShare, UUID> {

    Optional<MenuShare> findByTokenAndRevokedAtIsNull(String token);

    Optional<MenuShare> findFirstByOwnerSubAndRevokedAtIsNullOrderByCreatedAtDesc(String ownerSub);
}
