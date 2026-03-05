package com.collabhub.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // Prevent MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        //Prevent clickjacking

        response.setHeader("X-Frame-Options","DENY");
        // Force HTTPS(1 year)
        response.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains");
        //Disable referrer info leaking
        response.setHeader("Referrer-Policy", "no-referrer");
        //Restrict browser features
        response.setHeader("Permissions-Policy",
                "camera=(),microphone=(), geolocation=()");
        filterChain.doFilter(request,response);

    }
}
