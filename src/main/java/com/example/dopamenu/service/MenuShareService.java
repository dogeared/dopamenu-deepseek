package com.example.dopamenu.service;

import com.example.dopamenu.domain.MenuEvent;
import com.example.dopamenu.domain.MenuItem;
import com.example.dopamenu.domain.MenuShare;
import com.example.dopamenu.repository.MenuShareRepository;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Share lifecycle (create, rotate, revoke, resolve) plus the single-use session
 * intent that carries a visitor's choice across the sign-in redirect. Menu item
 * writes stay in {@link MenuItemService}.
 */
@Service
public class MenuShareService {

    static final String INTENT_KEY = "dopamenu.share.intent";
    public static final int DISPLAY_NAME_LIMIT = 80;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final MenuShareRepository repository;
    private final MenuItemService menuItems;
    private final MenuEventService events;

    public MenuShareService(MenuShareRepository repository, MenuItemService menuItems,
            MenuEventService events) {
        this.repository = repository;
        this.menuItems = menuItems;
        this.events = events;
    }

    @Transactional
    public MenuShare create(String ownerSub, String displayName) {
        String name = validName(displayName);
        repository.findFirstByOwnerSubAndRevokedAtIsNullOrderByCreatedAtDesc(ownerSub)
                .ifPresent(active -> {
                    throw new ActiveShareException(active);
                });
        return repository.save(new MenuShare(ownerSub, newToken(), name));
    }

    @Transactional
    public MenuShare rotate(String ownerSub, String displayName) {
        String name = validName(displayName);
        repository.findFirstByOwnerSubAndRevokedAtIsNullOrderByCreatedAtDesc(ownerSub)
                .ifPresent(active -> {
                    active.revoke();
                    // Hibernate flushes inserts before updates, so the revoked
                    // row must reach the database before the new one is inserted
                    // or the partial unique index rejects it.
                    repository.saveAndFlush(active);
                });
        return repository.save(new MenuShare(ownerSub, newToken(), name));
    }

    @Transactional
    public void revoke(String ownerSub, UUID shareId) {
        repository.findById(shareId)
                .filter(share -> share.getOwnerSub().equals(ownerSub))
                .ifPresent(MenuShare::revoke);
    }

    @Transactional(readOnly = true)
    public Optional<MenuShare> live(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return repository.findByTokenAndRevokedAtIsNull(token);
    }

    @Transactional(readOnly = true)
    public Optional<MenuShare> activeFor(String ownerSub) {
        return repository.findFirstByOwnerSubAndRevokedAtIsNullOrderByCreatedAtDesc(ownerSub);
    }

    /**
     * Copies every item of the shared menu that the visitor does not already
     * have, and records one {@code menu_copied} event when anything was copied.
     */
    @Transactional
    public List<MenuItem> copyAll(String ownerSub, String token) {
        MenuShare share = live(token)
                .orElseThrow(() -> new IllegalArgumentException("Share not found"));
        List<MenuItem> source = menuItems.menuFor(share.getOwnerSub());
        return copy(ownerSub, token, source);
    }

    /**
     * Copies exactly one item from the shared menu, for a visitor who tapped
     * "Order" on it.
     *
     * @return the item the visitor should order: the new copy, or the existing
     *         dish when the visitor already owned it
     */
    @Transactional
    public Optional<MenuItem> copyOne(String ownerSub, String token, UUID itemId) {
        MenuShare share = live(token)
                .orElseThrow(() -> new IllegalArgumentException("Share not found"));
        Optional<MenuItem> source = menuItems.menuFor(share.getOwnerSub()).stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst();
        if (source.isEmpty()) {
            return Optional.empty();
        }
        List<MenuItem> created = copy(ownerSub, token, List.of(source.get()));
        if (!created.isEmpty()) {
            return Optional.of(created.getFirst());
        }
        return menuItems.menuFor(ownerSub).stream()
                .filter(item -> sameDish(item, source.get()))
                .findFirst();
    }

    private static boolean sameDish(MenuItem left, MenuItem right) {
        return left.getCourse() == right.getCourse()
                && left.getTitle().trim().equalsIgnoreCase(right.getTitle().trim());
    }

    private List<MenuItem> copy(String ownerSub, String token, List<MenuItem> source) {
        List<MenuItem> created = menuItems.copyFrom(ownerSub, source, token);
        if (!created.isEmpty()) {
            events.record(MenuEvent.Kind.MENU_COPIED, ownerSub, token, null, null);
        }
        return created;
    }

    /**
     * Stores an intent for the current browser session. The no-argument
     * variants keep servlet plumbing out of the views; the session is already
     * established by the time either action runs.
     */
    public void storeIntent(ShareIntent intent) {
        HttpSession session = currentSession();
        if (session != null) {
            storeIntent(session, intent);
        }
    }

    public Optional<ShareIntent> consumeIntent() {
        HttpSession session = currentSession();
        return session == null ? Optional.empty() : consumeIntent(session);
    }

    public void storeIntent(HttpSession session, ShareIntent intent) {
        session.setAttribute(INTENT_KEY, intent);
    }

    public Optional<ShareIntent> consumeIntent(HttpSession session) {
        Object value = session.getAttribute(INTENT_KEY);
        session.removeAttribute(INTENT_KEY);
        if (value instanceof ShareIntent intent && !intent.expired()) {
            return Optional.of(intent);
        }
        return Optional.empty();
    }

    private static HttpSession currentSession() {
        if (VaadinRequest.getCurrent() instanceof VaadinServletRequest request) {
            return request.getHttpServletRequest().getSession();
        }
        return null;
    }

    static String newToken() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String validName(String displayName) {
        String name = displayName == null ? "" : displayName.trim();
        if (name.isEmpty() || name.length() > DISPLAY_NAME_LIMIT) {
            throw new IllegalArgumentException(
                    "Give the link an author name of 80 characters or fewer");
        }
        return name;
    }

    /**
     * Thrown when an owner already has a live link, so the UI can offer to
     * rotate instead of failing silently.
     */
    public static final class ActiveShareException extends RuntimeException {

        private final transient MenuShare share;

        ActiveShareException(MenuShare share) {
            super("A link is already active");
            this.share = share;
        }

        public MenuShare getShare() {
            return share;
        }
    }
}
