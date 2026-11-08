package com.example.dopamenu.config;

import com.example.dopamenu.ui.ShareRoutes;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The one HTTP-level test: it proves the public share route is reachable
 * without a session and carries the noindex header, and that the app routes
 * are not. Runs against a real Postgres so Flyway and Hibernate validation are
 * exercised too.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PublicRouteHttpTest {

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void theShareRouteIsAnonymousAndNoindexed() throws Exception {
        HttpResponse<String> response = get("/" + ShareRoutes.PREFIX + "/unknown-token");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("X-Robots-Tag")).contains("noindex");
    }

    @Test
    void theMenuRouteIsNotAnonymous() throws Exception {
        HttpResponse<String> response = get("/menu");

        assertThat(response.statusCode()).isBetween(300, 399);
    }

    @Test
    void theLandingPageIsAnonymousAndStartsASession() throws Exception {
        HttpResponse<String> response = get("/");

        // The landing page must render before the visitor signs in, and the
        // first response must carry a session cookie. Without it Vaadin's
        // client reports "Cookies disabled" instead of rendering the page.
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type").orElse(""))
                .contains("text/html");

        String cookies = String.join("; ",
                response.headers().allValues("set-cookie"));
        assertThat(cookies).contains("JSESSIONID=");
        assertThat(cookies).contains("Max-Age=2592000");
        assertThat(cookies).contains("HttpOnly");
    }

    @Test
    void sessionsKeepTheirCookieForThirtyDays() throws Exception {
        HttpResponse<String> response = get("/menu");

        String cookies = String.join("; ",
                response.headers().allValues("set-cookie"));

        assertThat(cookies).contains("JSESSIONID=");
        assertThat(cookies).contains("Max-Age=2592000");
        assertThat(cookies).contains("HttpOnly");
    }
}
