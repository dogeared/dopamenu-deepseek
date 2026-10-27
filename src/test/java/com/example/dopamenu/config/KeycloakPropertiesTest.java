package com.example.dopamenu.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakPropertiesTest {

    @Test
    void trimsTrailingSlashesAndBuildsEveryEndpoint() {
        KeycloakProperties properties = new KeycloakProperties(
                "http://localhost:8180/", "http://keycloak:8180/", "dopamenu",
                "dopamenu-app", "secret");

        assertThat(properties.publicUrl()).isEqualTo("http://localhost:8180");
        assertThat(properties.internalUrl()).isEqualTo("http://keycloak:8180");
        assertThat(properties.realm()).isEqualTo("dopamenu");
        assertThat(properties.clientId()).isEqualTo("dopamenu-app");
        assertThat(properties.clientSecret()).isEqualTo("secret");

        assertThat(properties.issuer()).isEqualTo("http://localhost:8180/realms/dopamenu");
        assertThat(properties.authorizationUri())
                .isEqualTo("http://localhost:8180/realms/dopamenu/protocol/openid-connect/auth");
        assertThat(properties.endSessionEndpoint())
                .isEqualTo("http://localhost:8180/realms/dopamenu/protocol/openid-connect/logout");
        assertThat(properties.tokenUri())
                .isEqualTo("http://keycloak:8180/realms/dopamenu/protocol/openid-connect/token");
        assertThat(properties.jwkSetUri())
                .isEqualTo("http://keycloak:8180/realms/dopamenu/protocol/openid-connect/certs");
        assertThat(properties.userInfoUri())
                .isEqualTo("http://keycloak:8180/realms/dopamenu/protocol/openid-connect/userinfo");
    }

    @Test
    void leavesUrlsWithoutTrailingSlashesAlone() {
        KeycloakProperties properties = new KeycloakProperties(
                "http://localhost:8180", "http://keycloak:8180", "dopamenu",
                "dopamenu-app", "secret");

        assertThat(properties.publicUrl()).isEqualTo("http://localhost:8180");
        assertThat(properties.internalUrl()).isEqualTo("http://keycloak:8180");
    }

    @Test
    void toleratesMissingUrls() {
        KeycloakProperties properties = new KeycloakProperties(
                null, null, "dopamenu", "dopamenu-app", "secret");

        assertThat(properties.publicUrl()).isNull();
        assertThat(properties.internalUrl()).isNull();
    }
}
