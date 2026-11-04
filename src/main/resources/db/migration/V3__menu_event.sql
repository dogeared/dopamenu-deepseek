CREATE TABLE menu_event (
    id          UUID PRIMARY KEY,
    occurred_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    kind        VARCHAR(32)  NOT NULL,
    owner_sub   VARCHAR(128),
    token       VARCHAR(22),
    order_id    UUID,
    referrer    VARCHAR(512)
);

-- Every funnel query groups by these two columns.
CREATE INDEX idx_menu_event_token_kind ON menu_event (token, kind);
