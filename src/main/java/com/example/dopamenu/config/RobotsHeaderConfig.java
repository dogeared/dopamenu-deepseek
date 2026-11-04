package com.example.dopamenu.config;

import com.example.dopamenu.ui.ShareRoutes;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RobotsHeaderConfig {

    /**
     * The noindex filter is registered against the share path only, so every
     * other route keeps its normal headers. The constant in {@link ShareRoutes}
     * is the same one the route and the security permit use.
     */
    @Bean
    FilterRegistrationBean<RobotsHeaderFilter> robotsHeaderFilter() {
        FilterRegistrationBean<RobotsHeaderFilter> registration =
                new FilterRegistrationBean<>(new RobotsHeaderFilter());
        registration.addUrlPatterns("/" + ShareRoutes.PREFIX + "/*");
        return registration;
    }
}
