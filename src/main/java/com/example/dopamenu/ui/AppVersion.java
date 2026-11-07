package com.example.dopamenu.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The running application version, read from the build metadata that Maven
 * generates with Spring Boot's {@code build-info} goal.
 */
public final class AppVersion {

    static final String BUILD_INFO = "/META-INF/build-info.properties";
    static final String VERSION_KEY = "build.version";
    static final String FALLBACK = "dev";

    private AppVersion() {
    }

    /**
     * Returns the build version, or {@code dev} when the app runs from a source
     * tree that has not been built by Maven.
     */
    public static String current() {
        try (InputStream buildInfo = AppVersion.class.getResourceAsStream(BUILD_INFO)) {
            return from(buildInfo);
        } catch (IOException e) {
            return FALLBACK;
        }
    }

    static String from(InputStream buildInfo) throws IOException {
        if (buildInfo == null) {
            return FALLBACK;
        }
        Properties properties = new Properties();
        properties.load(buildInfo);
        return properties.getProperty(VERSION_KEY, FALLBACK);
    }
}
