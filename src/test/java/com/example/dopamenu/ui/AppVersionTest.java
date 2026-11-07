package com.example.dopamenu.ui;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class AppVersionTest {

    @Test
    void readsTheVersionFromBuildInfo() throws IOException {
        assertThat(AppVersion.from(streamOf("build.version=1.2.3\n")))
                .isEqualTo("1.2.3");
    }

    @Test
    void fallsBackWhenBuildInfoIsMissing() throws IOException {
        assertThat(AppVersion.from(null)).isEqualTo(AppVersion.FALLBACK);
    }

    @Test
    void fallsBackWhenBuildInfoHasNoVersion() throws IOException {
        assertThat(AppVersion.from(streamOf("build.time=2026-11-06\n")))
                .isEqualTo(AppVersion.FALLBACK);
    }

    @Test
    void currentUsesTheGeneratedVersionOrTheDevFallback() {
        assertThat(AppVersion.current()).matches("\\d+\\.\\d+\\.\\d+|dev");
    }

    private static InputStream streamOf(String contents) {
        return new ByteArrayInputStream(contents.getBytes(StandardCharsets.ISO_8859_1));
    }
}
