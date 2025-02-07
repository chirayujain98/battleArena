package com.gocomet.battleArena.rateLimitter;

import io.github.bucket4j.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response,
                                    jakarta.servlet.FilterChain filterChain) throws IOException, jakarta.servlet.ServletException {

        String clientIp = request.getRemoteAddr(); // Get user IP for rate limiting

        Bucket bucket = buckets.computeIfAbsent(clientIp, key -> createNewBucket());

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response); // Proceed if request is allowed
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests! Slow down.");
        }
    }

    private Bucket createNewBucket() {
        return Bucket4j.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofSeconds(10)))) // 5 requests per 10 sec
                .build();
    }
}

