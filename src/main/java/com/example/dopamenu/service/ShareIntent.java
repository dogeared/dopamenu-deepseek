package com.example.dopamenu.service;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * A single-use action a visitor asked for before signing in. It is kept in the
 * HTTP session, never in a cookie, and expires so a stale tab cannot replay it.
 *
 * <p>Serializable because it lives in the servlet session, which a container
 * may persist and restore.</p>
 */
public record ShareIntent(String token, UUID itemId, Action action, Instant expiresAt)
        implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Action {
        ORDER,
        COPY
    }

    public static final Duration TTL = Duration.ofMinutes(30);

    public static ShareIntent order(String token, UUID itemId) {
        return new ShareIntent(token, itemId, Action.ORDER, Instant.now().plus(TTL));
    }

    public static ShareIntent copy(String token) {
        return new ShareIntent(token, null, Action.COPY, Instant.now().plus(TTL));
    }

    public boolean expired() {
        return Instant.now().isAfter(expiresAt);
    }
}
