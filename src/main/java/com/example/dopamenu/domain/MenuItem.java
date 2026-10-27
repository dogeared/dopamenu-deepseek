package com.example.dopamenu.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "menu_item")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_sub", nullable = false, length = 128)
    private String ownerSub;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Course course;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 600)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "dopamine_level", nullable = false, length = 32)
    private DopamineLevel dopamineLevel;

    @Column(name = "timer_required", nullable = false)
    private boolean timerRequired;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean archived;

    protected MenuItem() {
        // for JPA
    }

    public MenuItem(String ownerSub, Course course, String title, String description,
            int durationMinutes, DopamineLevel dopamineLevel, boolean timerRequired) {
        this.ownerSub = ownerSub;
        this.course = course;
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.dopamineLevel = dopamineLevel;
        this.timerRequired = timerRequired;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getOwnerSub() {
        return ownerSub;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public DopamineLevel getDopamineLevel() {
        return dopamineLevel;
    }

    public void setDopamineLevel(DopamineLevel dopamineLevel) {
        this.dopamineLevel = dopamineLevel;
    }

    public boolean isTimerRequired() {
        return timerRequired;
    }

    public void setTimerRequired(boolean timerRequired) {
        this.timerRequired = timerRequired;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isArchived() {
        return archived;
    }

    public void archive() {
        this.archived = true;
    }
}
