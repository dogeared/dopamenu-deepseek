-- Orders that are logged after the fact carry the minutes the user actually
-- spent, instead of deriving them from a start and end timestamp.
ALTER TABLE menu_order ADD COLUMN actual_minutes INTEGER;

-- Distinguishes "I did this earlier and logged it" from a timed order.
ALTER TABLE menu_order ADD COLUMN retroactive BOOLEAN NOT NULL DEFAULT FALSE;
