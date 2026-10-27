package com.example.dopamenu.domain;

/**
 * The four courses of a dopa-menu. Each course carries the copy used to
 * introduce it in the UI, so the menu structure lives in one place.
 */
public enum Course {

    APPETIZER("Appetizers", "Quick boosts", "1–10 min",
            "Fast, low-effort hits to break inertia and get started.", 1, false),

    ENTREE("Entrées", "High-impact", "30+ min",
            "Immersive activities that reward a longer, deeper investment.", 2, false),

    SIDE("Sides", "Task companions", "Any length",
            "Background stimuli that make boring or mandatory tasks tolerable.", 3, false),

    DESSERT("Desserts", "Use with caution", "Time-boxed",
            "High-dopamine treats. Delicious, easy to overdo — always pair with a timer.",
            4, true);

    private final String label;
    private final String tagline;
    private final String timeWindow;
    private final String description;
    private final int order;
    private final boolean caution;

    Course(String label, String tagline, String timeWindow, String description,
            int order, boolean caution) {
        this.label = label;
        this.tagline = tagline;
        this.timeWindow = timeWindow;
        this.description = description;
        this.order = order;
        this.caution = caution;
    }

    public String label() {
        return label;
    }

    public String tagline() {
        return tagline;
    }

    public String timeWindow() {
        return timeWindow;
    }

    public String description() {
        return description;
    }

    public int order() {
        return order;
    }

    public boolean caution() {
        return caution;
    }

    public String cssName() {
        return name().toLowerCase();
    }
}
