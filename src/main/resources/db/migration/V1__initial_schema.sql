CREATE TABLE menu_item (
    id                UUID PRIMARY KEY,
    owner_sub         VARCHAR(128) NOT NULL,
    course            VARCHAR(32)  NOT NULL,
    title             VARCHAR(160) NOT NULL,
    description       VARCHAR(600),
    duration_minutes  INTEGER      NOT NULL,
    dopamine_level    VARCHAR(32)  NOT NULL,
    timer_required    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    archived          BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_menu_item_owner ON menu_item (owner_sub) WHERE archived = FALSE;

CREATE TABLE menu_order (
    id              UUID PRIMARY KEY,
    owner_sub       VARCHAR(128) NOT NULL,
    item_id         UUID REFERENCES menu_item (id) ON DELETE SET NULL,
    item_title      VARCHAR(160) NOT NULL,
    course          VARCHAR(32)  NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    planned_minutes INTEGER      NOT NULL,
    started_at      TIMESTAMPTZ  NOT NULL,
    ended_at        TIMESTAMPTZ,
    mood_before     INTEGER,
    mood_after      INTEGER,
    note            VARCHAR(600)
);

CREATE INDEX idx_menu_order_owner_started ON menu_order (owner_sub, started_at DESC);
CREATE INDEX idx_menu_order_active ON menu_order (owner_sub) WHERE status = 'ACTIVE';
