package org.maxbot.back.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${app.chat-key}")
    private String chatApiKey;

    @Value("${app.embedding-key}")
    private String embeddingApiKey;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException, ServletException {
        String providedApiKey = request.getHeader("X-API-KEY");
        String requestPath = request.getRequestURI();

        // Determine which API key to validate based on the request path
        String expectedApiKey = requestPath.startsWith("/api/embedding") ? embeddingApiKey : chatApiKey;

        if (providedApiKey == null || !providedApiKey.equals(expectedApiKey)) {
            log.warn("Request blocked due to missing or invalid API key. Path: {}", requestPath);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            return;
        }

        filterChain.doFilter(request, response);
    }
}
