package com.example.dopamenu.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Marks the public share path {@code noindex}. The mapping is deliberately
 * narrow: {@code PublicLayout} also serves the landing page, so a page-level
 * header would deindex the front door along with the shared menus.
 */
public class RobotsHeaderFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Robots-Tag";
    public static final String VALUE = "noindex";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        response.setHeader(HEADER, VALUE);
        filterChain.doFilter(request, response);
    }
}
