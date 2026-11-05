package com.example.dopamenu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * A single "order" from the menu, with an optional mood check before and after.
 */
@Entity
@Table(name = "menu_order")
public class MenuOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_sub", nullable = false, length = 128)
    private String ownerSub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private MenuItem item;

    @Column(name = "item_title", nullable = false, length = 160)
    private String itemTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(name = "planned_minutes", nullable = false)
    private int plannedMinutes;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    /**
     * The minutes the user reported when logging an order that already
     * happened. Null for orders that ran on the timer.
     */
    @Column(name = "actual_minutes")
    private Integer actualMinutes;

    /**
     * True when the order was recorded after the fact rather than started and
     * timed in the app.
     */
    @Column(nullable = false)
    private boolean retroactive;

    @Column(name = "mood_before")
    private Integer moodBefore;

    @Column(name = "mood_after")
    private Integer moodAfter;

    @Column(length = 600)
    private String note;

    /**
     * The share token that led to this order, copied from the item at start
     * time so attribution survives revocation.
     */
    @Column(name = "share_token", length = 22)
    private String shareToken;

    protected MenuOrder() {
        // for JPA
    }

    public MenuOrder(String ownerSub, MenuItem item, int moodBefore) {
        this.ownerSub = ownerSub;
        this.item = item;
        this.itemTitle = item.getTitle();
        this.course = item.getCourse();
        this.status = OrderStatus.ACTIVE;
        this.plannedMinutes = item.getDurationMinutes();
        this.startedAt = Instant.now();
        this.moodBefore = moodBefore;
        this.shareToken = item.getSourceShareToken();
    }

    public UUID getId() {
        return id;
    }

    public String getOwnerSub() {
        return ownerSub;
    }

    public MenuItem getItem() {
        return item;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public Course getCourse() {
        return course;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getPlannedMinutes() {
        return plannedMinutes;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public Integer getActualMinutes() {
        return actualMinutes;
    }

    public boolean isRetroactive() {
        return retroactive;
    }

    public Integer getMoodBefore() {
        return moodBefore;
    }

    public Integer getMoodAfter() {
        return moodAfter;
    }

    public String getNote() {
        return note;
    }

    public String getShareToken() {
        return shareToken;
    }

    public void complete(int moodAfter, String note) {
        complete(moodAfter, note, null);
    }

    /**
     * @param actualMinutes the minutes the user corrected after the timer ran,
     *                      or null to keep the clock's own measurement.
     */
    public void complete(int moodAfter, String note, Integer actualMinutes) {
        if (actualMinutes != null && actualMinutes < 1) {
            throw new IllegalArgumentException("Minutes must be at least one");
        }
        this.status = OrderStatus.COMPLETED;
        this.endedAt = Instant.now();
        this.actualMinutes = actualMinutes;
        this.moodAfter = moodAfter;
        this.note = note;
    }

    /**
     * Records an order that already happened. The timer never ran, so the
     * user's reported minutes stand in for the elapsed time.
     */
    public void logPast(int minutes, int moodAfter, String note) {
        if (minutes < 1) {
            throw new IllegalArgumentException("Minutes must be at least one");
        }
        this.status = OrderStatus.COMPLETED;
        this.retroactive = true;
        this.actualMinutes = minutes;
        this.endedAt = Instant.now();
        this.moodAfter = moodAfter;
        this.note = note;
    }

    public void abandon() {
        this.status = OrderStatus.ABANDONED;
        this.endedAt = Instant.now();
    }

    public long minutesElapsed() {
        if (actualMinutes != null) {
            return actualMinutes;
        }
        Instant end = endedAt != null ? endedAt : Instant.now();
        return Math.max(0, Duration.between(startedAt, end).toMinutes());
    }

    public Integer moodLift() {
        if (moodBefore == null || moodAfter == null) {
            return null;
        }
        return moodAfter - moodBefore;
    }
}
