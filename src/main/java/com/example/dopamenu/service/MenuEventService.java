package com.example.dopamenu.service;

import com.example.dopamenu.domain.MenuEvent;
import com.example.dopamenu.repository.MenuEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The only writer of {@code menu_event}. Keeping the bot predicate next to the
 * write means a view cannot disagree with the service about what counts.
 */
@Service
public class MenuEventService {

    private static final int REFERRER_LIMIT = 512;

    /**
     * Deliberately small and literal: this is a curiosity filter, not a bot
     * defence. Anything it misses still lands in the table.
     */
    private static final Pattern BOT = Pattern.compile(
            "bot|crawler|crawl|spider|slurp|headless|facebookexternalhit|preview",
            Pattern.CASE_INSENSITIVE);

    private final MenuEventRepository repository;

    public MenuEventService(MenuEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(MenuEvent.Kind kind, String ownerSub, String token, UUID orderId,
            String referrer) {
        repository.save(new MenuEvent(kind, ownerSub, token, orderId, trim(referrer)));
    }

    @Transactional
    public void recordShareView(String token, String userAgent, String referrer) {
        if (token == null || token.isBlank() || isBot(userAgent)) {
            return;
        }
        record(MenuEvent.Kind.SHARE_VIEW, null, token, null, referrer);
    }

    public boolean isBot(String userAgent) {
        return userAgent != null && BOT.matcher(userAgent).find();
    }

    private static String trim(String referrer) {
        if (referrer == null) {
            return null;
        }
        String value = referrer.trim();
        return value.length() <= REFERRER_LIMIT ? value : value.substring(0, REFERRER_LIMIT);
    }
}
