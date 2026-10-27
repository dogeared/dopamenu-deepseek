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

    @Column(name = "mood_before")
    private Integer moodBefore;

    @Column(name = "mood_after")
    private Integer moodAfter;

    @Column(length = 600)
    private String note;

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

    public Integer getMoodBefore() {
        return moodBefore;
    }

    public Integer getMoodAfter() {
        return moodAfter;
    }

    public String getNote() {
        return note;
    }

    public void complete(int moodAfter, String note) {
        this.status = OrderStatus.COMPLETED;
        this.endedAt = Instant.now();
        this.moodAfter = moodAfter;
        this.note = note;
    }

    public void abandon() {
        this.status = OrderStatus.ABANDONED;
        this.endedAt = Instant.now();
    }

    public long minutesElapsed() {
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
