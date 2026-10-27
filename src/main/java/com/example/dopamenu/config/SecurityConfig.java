package com.example.dopamenu.config;

import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Health checks and static assets must stay reachable without a
        // session. Vaadin serves this app's stylesheet and favicon from the
        // classpath, and the browser requests them before/while logging in.
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers("/", "/actuator/health", "/actuator/health/**",
                        "/favicon.ico", "/styles/**", "/images/**", "/VAADIN/**")
                .permitAll());

        // Keycloak handles the login form. Vaadin's configurer wires up CSRF,
        // logout, request caching and route access control for the Flow UI.
        http.with(VaadinSecurityConfigurer.vaadin(), vaadin -> vaadin
                .oauth2LoginPage("/oauth2/authorization/keycloak", "{baseUrl}")
                .defaultSuccessUrl("/menu", true)
                .anyRequest(requests -> requests.authenticated()));

        return http.build();
    }
}
