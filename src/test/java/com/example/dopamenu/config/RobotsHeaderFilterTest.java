package com.example.dopamenu.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RobotsHeaderFilterTest {

    @Test
    void marksTheSharePathNoindex() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/m/token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new RobotsHeaderFilter().doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(RobotsHeaderFilter.HEADER))
                .isEqualTo(RobotsHeaderFilter.VALUE);
    }

    @Test
    void isMappedToTheSharePathOnly() {
        FilterRegistrationBean<RobotsHeaderFilter> registration =
                new RobotsHeaderConfig().robotsHeaderFilter();

        assertThat(registration.getUrlPatterns()).containsExactly("/m/*");
    }
}
