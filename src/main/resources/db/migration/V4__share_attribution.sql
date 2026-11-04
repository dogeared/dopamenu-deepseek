-- Plain text rather than a foreign key: a revoked share row must not erase the
-- evidence of the traffic it produced.
ALTER TABLE menu_item ADD COLUMN source_share_token VARCHAR(22);
ALTER TABLE menu_order ADD COLUMN share_token VARCHAR(22);
