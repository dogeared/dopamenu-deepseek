CREATE TABLE menu_share (
    id           UUID PRIMARY KEY,
    owner_sub    VARCHAR(128) NOT NULL,
    token        VARCHAR(22)  NOT NULL,
    display_name VARCHAR(80)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    revoked_at   TIMESTAMPTZ
);

CREATE UNIQUE INDEX uq_menu_share_token ON menu_share (token);

-- One active link per owner. A revoked row frees the slot, so rotation is
-- revoke-then-create and the cap is enforced by the index rather than by
-- service logic.
CREATE UNIQUE INDEX uq_menu_share_active_owner ON menu_share (owner_sub)
    WHERE revoked_at IS NULL;
