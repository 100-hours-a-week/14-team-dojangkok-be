package com.dojangkok.backend.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private static final String INTERNAL_API_PATH_PREFIX = "/api/internal/";
    private static final String INTERNAL_CALLBACK_PATH_PREFIX = "/api/internal/callbacks/";
    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final ObjectMapper objectMapper;

    @Value("${app.internal.api-key}")
    private String internalApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // /api/internal/** 중 콜백(AI서버)은 제외, 나머지만 API Key 검증
        if (!uri.startsWith(INTERNAL_API_PATH_PREFIX) || uri.startsWith(INTERNAL_CALLBACK_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String providedKey = request.getHeader(API_KEY_HEADER);

        if (providedKey == null || !providedKey.equals(internalApiKey)) {
            log.warn("내부 API 인증 실패 - URI: {}, IP: {}", uri, request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> body = Map.of(
                    "code", "UNAUTHORIZED",
                    "message", "유효하지 않은 내부 API 키입니다."
            );
            objectMapper.writeValue(response.getOutputStream(), body);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
