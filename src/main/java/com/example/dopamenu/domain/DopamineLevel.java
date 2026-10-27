package com.example.dopamenu.domain;

/**
 * A rough, intentionally non-scientific label for how much punch an item packs.
 */
public enum DopamineLevel {

    GENTLE("Gentle", 1),
    STEADY("Steady", 2),
    BRIGHT("Bright", 3),
    INTENSE("Intense", 4);

    private final String label;
    private final int strength;

    DopamineLevel(String label, int strength) {
        this.label = label;
        this.strength = strength;
    }

    public String label() {
        return label;
    }

    public int strength() {
        return strength;
    }
}
