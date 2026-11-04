package com.example.dopamenu.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * One funnel step on a shared link. The token is denormalized on purpose: a
 * revoked share must not erase the events it produced.
 */
@Entity
@Table(name = "menu_event")
public class MenuEvent {

    /**
     * The funnel vocabulary. Codes are what the database and the experiment
     * queries use; the enum keeps call sites honest.
     */
    public enum Kind {

        SHARE_VIEW("share_view"),
        MENU_COPIED("menu_copied"),
        ORDER_STARTED("order_started"),
        ORDER_COMPLETED("order_completed");

        private final String code;

        Kind(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }

        static Kind fromCode(String code) {
            for (Kind kind : values()) {
                if (kind.code.equals(code)) {
                    return kind;
                }
            }
            throw new IllegalArgumentException("Unknown menu event kind: " + code);
        }
    }

    @Converter
    public static final class KindConverter implements AttributeConverter<Kind, String> {

        @Override
        public String convertToDatabaseColumn(Kind attribute) {
            return attribute == null ? null : attribute.code();
        }

        @Override
        public Kind convertToEntityAttribute(String dbData) {
            return dbData == null ? null : Kind.fromCode(dbData);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Convert(converter = KindConverter.class)
    @Column(nullable = false, length = 32)
    private Kind kind;

    @Column(name = "owner_sub", length = 128)
    private String ownerSub;

    @Column(length = 22)
    private String token;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(length = 512)
    private String referrer;

    protected MenuEvent() {
        // for JPA
    }

    public MenuEvent(Kind kind, String ownerSub, String token, UUID orderId, String referrer) {
        this.kind = kind;
        this.ownerSub = ownerSub;
        this.token = token;
        this.orderId = orderId;
        this.referrer = referrer;
        this.occurredAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Kind getKind() {
        return kind;
    }

    public String getOwnerSub() {
        return ownerSub;
    }

    public String getToken() {
        return token;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getReferrer() {
        return referrer;
    }
}
