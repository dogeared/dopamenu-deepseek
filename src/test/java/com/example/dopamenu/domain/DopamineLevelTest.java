package com.example.dopamenu.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DopamineLevelTest {

    @Test
    void exposesLabelAndStrength() {
        assertThat(DopamineLevel.GENTLE.label()).isEqualTo("Gentle");
        assertThat(DopamineLevel.GENTLE.strength()).isEqualTo(1);
        assertThat(DopamineLevel.STEADY.label()).isEqualTo("Steady");
        assertThat(DopamineLevel.STEADY.strength()).isEqualTo(2);
        assertThat(DopamineLevel.BRIGHT.label()).isEqualTo("Bright");
        assertThat(DopamineLevel.BRIGHT.strength()).isEqualTo(3);
        assertThat(DopamineLevel.INTENSE.label()).isEqualTo("Intense");
        assertThat(DopamineLevel.INTENSE.strength()).isEqualTo(4);
    }
}
