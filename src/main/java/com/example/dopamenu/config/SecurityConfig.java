package com.example.dopamenu.config;

import com.example.dopamenu.ui.ShareRoutes;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            ClientRegistrationRepository clientRegistrations,
            KeycloakProperties keycloakProperties) throws Exception {
        // Health checks, the public share page and static assets must stay
        // reachable without a session. Vaadin serves this app's stylesheet and
        // favicon from the classpath, and the browser requests them
        // before/while logging in.
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers("/", "/" + ShareRoutes.PREFIX + "/**",
                        "/actuator/health", "/actuator/health/**",
                        "/favicon.ico", "/styles/**", "/images/**", "/VAADIN/**")
                .permitAll());

        // The /register shim asks for registration by adding action=register;
        // the wrapper sends only that request to Keycloak's registration
        // endpoint instead of its login endpoint.
        OAuth2AuthorizationRequestResolver authorizationRequests = new RegistrationResolver(
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrations,
                        "/oauth2/authorization"),
                keycloakProperties.registrationsUri());
        http.oauth2Login(login -> login.authorizationEndpoint(endpoint ->
                endpoint.authorizationRequestResolver(authorizationRequests)));

        // Keycloak handles the login form. Vaadin's configurer wires up CSRF,
        // logout, request caching and route access control for the Flow UI.
        http.with(VaadinSecurityConfigurer.vaadin(), vaadin -> vaadin
                .oauth2LoginPage("/oauth2/authorization/keycloak", "{baseUrl}")
                .defaultSuccessUrl("/menu", false)
                .anyRequest(requests -> requests.authenticated()));

        return http.build();
    }
}
