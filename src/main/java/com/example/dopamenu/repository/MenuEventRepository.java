package com.example.dopamenu.repository;

import com.example.dopamenu.domain.MenuEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuEventRepository extends JpaRepository<MenuEvent, UUID> {
}
