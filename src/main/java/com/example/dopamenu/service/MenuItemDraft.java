package com.example.dopamenu.service;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Form data for creating or editing a menu item.
 */
public record MenuItemDraft(
        @NotBlank(message = "Give it a name") @Size(max = 160) String title,

        @Size(max = 600) String description,

        @NotNull(message = "Pick a course") Course course,

        @Min(value = 1, message = "At least one minute") @Max(value = 600, message = "Keep it under 10 hours") int durationMinutes,

        @NotNull(message = "Pick a strength") DopamineLevel dopamineLevel,

        boolean timerRequired) {
}
