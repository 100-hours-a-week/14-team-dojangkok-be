package com.dojangkok.backend.auth.config;

import com.dojangkok.backend.auth.filter.InternalApiKeyFilter;
import com.dojangkok.backend.auth.jwt.JwtAuthenticationFilter;
import com.dojangkok.backend.auth.oauth.cookie.CookieOAuth2AuthorizationRequestRepository;
import com.dojangkok.backend.auth.oauth.handler.OAuth2LoginSuccessHandler;
import com.dojangkok.backend.auth.oauth.service.CustomOAuth2UserService;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final InternalApiKeyFilter internalApiKeyFilter;
    private final ObjectMapper objectMapper;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ASYNC).permitAll()
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v2/property-posts", "/api/v2/property-posts/{propertyPostId}").permitAll()
                        .requestMatchers("/api/v1/auth/token", "/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/test/**").permitAll()
                        .requestMatchers("/api/internal/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedEntryPoint())
                )

                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(auth -> auth
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository)
                        )
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                )

                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



    @Bean
    public AuthenticationEntryPoint unauthorizedEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> body = Map.of(
                    "code", "UNAUTHORIZED",
                    "message", "Access Token이 만료되었습니다."
            );

            objectMapper.writeValue(response.getOutputStream(), body);
        };
    }
}
