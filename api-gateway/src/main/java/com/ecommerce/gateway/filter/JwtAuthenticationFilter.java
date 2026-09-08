package com.ecommerce.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final Key signingKey;
    private final AntPathMatcher matcher = new AntPathMatcher();
    private static final List<String> OPEN_PATHS = List.of(
            "/api/auth/**",
            "/api/products/**",
            "/api/categories/**",
            "/actuator/**",
            "/v3/api-docs/**", "/swagger-ui/**"
    );

    public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // CORS preflight must never require auth
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethod().name())) {
            return chain.filter(exchange);
        }
        String path = exchange.getRequest().getPath().value();
        boolean open = OPEN_PATHS.stream().anyMatch(p -> matcher.match(p, path));
        // Allow GET on products/categories without auth; mutate requires auth (checked downstream too)
        if (open && isPublicRead(exchange, path)) {
            return chain.filter(exchange);
        }
        if (open && path.startsWith("/api/auth")) {
            return chain.filter(exchange);
        }
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build()
                    .parseClaimsJws(token).getBody();
            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Email", String.valueOf(claims.get("email", String.class)))
                    .header("X-User-Role", String.valueOf(claims.get("role", String.class)))
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicRead(ServerWebExchange exchange, String path) {
        String method = exchange.getRequest().getMethod().name();
        return "GET".equalsIgnoreCase(method)
                && (path.startsWith("/api/products") || path.startsWith("/api/categories"));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
