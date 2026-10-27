package com.example.dopamenu.service;

import com.example.dopamenu.domain.Course;
import com.example.dopamenu.domain.DopamineLevel;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MenuItemDraftTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void acceptsAValidDraft() {
        MenuItemDraft draft = new MenuItemDraft("A walk", "Around the block",
                Course.ENTREE, 30, DopamineLevel.BRIGHT, false);

        assertThat(validator.validate(draft)).isEmpty();
    }

    @Test
    void rejectsBlankTitleAndShortDuration() {
        MenuItemDraft draft = new MenuItemDraft("  ", null, Course.APPETIZER,
                0, DopamineLevel.GENTLE, false);

        assertThat(validator.validate(draft))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("title", "durationMinutes");
    }

    @Test
    void dessertCourseIsAlwaysMarkedAsCaution() {
        assertThat(Course.DESSERT.caution()).isTrue();
        assertThat(Course.DESSERT.timeWindow()).isEqualTo("Time-boxed");
        assertThat(Course.APPETIZER.caution()).isFalse();
    }
}
