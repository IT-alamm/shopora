package com.ecommerce.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory per-IP token-bucket rate limiter. Runs before JWT validation
 * so abusive traffic is rejected early with 429.
 *
 * <p>Limits (per minute, per client IP):
 * <ul>
 *   <li>{@code /api/auth/**} — strict (login/register brute-force protection)</li>
 *   <li>everything else — default budget</li>
 * </ul>
 * Tune via {@code ratelimit.default-capacity} / {@code ratelimit.auth-capacity}.
 * For multi-instance deployments replace with RedisRateLimiter + Redis.
 */
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private final int defaultCapacity;
    private final int authCapacity;
    private final long refillNanos;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static class Bucket {
        double tokens;
        long lastRefillNanos;
        long lastSeenNanos;
    }

    public RateLimitingFilter(
            @Value("${ratelimit.default-capacity:120}") int defaultCapacity,
            @Value("${ratelimit.auth-capacity:20}") int authCapacity,
            @Value("${ratelimit.refill-seconds:60}") long refillSeconds) {
        this.defaultCapacity = defaultCapacity;
        this.authCapacity = authCapacity;
        this.refillNanos = refillSeconds * 1_000_000_000L;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        boolean isAuth = path.startsWith("/api/auth");
        int capacity = isAuth ? authCapacity : defaultCapacity;

        String key = (isAuth ? "auth:" : "api:") + clientIp(exchange);
        Bucket b = buckets.computeIfAbsent(key, k -> {
            Bucket nb = new Bucket();
            nb.tokens = capacity;
            nb.lastRefillNanos = System.nanoTime();
            return nb;
        });

        boolean allowed;
        synchronized (b) {
            long now = System.nanoTime();
            double elapsed = (double) (now - b.lastRefillNanos) / refillNanos;
            if (elapsed > 0) {
                b.tokens = Math.min(capacity, b.tokens + elapsed * capacity);
                b.lastRefillNanos = now;
            }
            b.lastSeenNanos = now;
            allowed = b.tokens >= 1.0;
            if (allowed) b.tokens -= 1.0;
        }

        evictIdle();
        if (!allowed) return tooManyRequests(exchange, path);
        return chain.filter(exchange);
    }

    private Mono<Void> tooManyRequests(ServerWebExchange exchange, String path) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("Retry-After", "60");
        String body = String.format(
                "{\"timestamp\":\"%s\",\"status\":429,\"message\":\"Too many requests — slow down and retry.\",\"path\":\"%s\"}",
                Instant.now(), path);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private String clientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return String.valueOf(exchange.getRequest().getRemoteAddress());
    }

    /** Prevent unbounded map growth from IP rotation. */
    private void evictIdle() {
        if (buckets.size() < 5000) return;
        long cutoff = System.nanoTime() - 10L * 60 * 1_000_000_000L;
        buckets.entrySet().removeIf(e -> e.getValue().lastSeenNanos < cutoff);
    }

    @Override
    public int getOrder() {
        return -200; // before JwtAuthenticationFilter (-100)
    }
}
