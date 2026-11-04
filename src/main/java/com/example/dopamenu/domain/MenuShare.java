package com.example.dopamenu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * One shareable link to an owner's menu. At most one row per owner is active at
 * a time; revoking frees the slot for rotation.
 */
@Entity
@Table(name = "menu_share")
public class MenuShare {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_sub", nullable = false, length = 128)
    private String ownerSub;

    @Column(nullable = false, length = 22, unique = true)
    private String token;

    @Column(name = "display_name", nullable = false, length = 80)
    private String displayName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected MenuShare() {
        // for JPA
    }

    public MenuShare(String ownerSub, String token, String displayName) {
        this.ownerSub = ownerSub;
        this.token = token;
        this.displayName = displayName;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getOwnerSub() {
        return ownerSub;
    }

    public String getToken() {
        return token;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public boolean isActive() {
        return revokedAt == null;
    }

    public void revoke() {
        if (revokedAt == null) {
            revokedAt = Instant.now();
        }
    }
}
