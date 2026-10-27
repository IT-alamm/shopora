package com.ecommerce.auth.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String userId = null;
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtil.parse(header.substring(7));
                userId = claims.getSubject();
                String role = claims.get("role", String.class);
                var authorities = role == null ? Collections.<SimpleGrantedAuthority>emptyList()
                        : List.of(new SimpleGrantedAuthority(role));
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(userId, null, authorities));
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        final String resolvedUserId = userId;
        HttpServletRequest wrapped = new HttpServletRequestWrapper(request) {
            @Override public String getHeader(String name) {
                if ("X-User-Id".equalsIgnoreCase(name) && resolvedUserId != null) return resolvedUserId;
                return super.getHeader(name);
            }
            @Override public Enumeration<String> getHeaders(String name) {
                if ("X-User-Id".equalsIgnoreCase(name) && resolvedUserId != null)
                    return Collections.enumeration(List.of(resolvedUserId));
                return super.getHeaders(name);
            }
        };
        chain.doFilter(wrapped, response);
    }
}