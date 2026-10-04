package com.bingoai.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.activation-base-url:http://localhost:3000}")
    private String allowedOrigin;

    private final AdminAuthenticationFilter adminAuthenticationFilter;

    public SecurityConfig(AdminAuthenticationFilter adminAuthenticationFilter) {
        this.adminAuthenticationFilter = adminAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public WebSocket handshake and polling
                        .requestMatchers("/ws/bingo/**", "/ws/**").permitAll()

                        // Public Auth and Player routes
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/forgot-password",
                                "/api/v1/auth/reset-password")
                        .permitAll()
                        .requestMatchers("/api/v1/tickets/activate", "/api/v1/tickets/validate").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/claims").permitAll() // Players
                                                                                                                 // claim
                                                                                                                 // bingo

                        // Public views of events for players
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/events/{id}").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/events/{id}/draws")
                        .permitAll()

                        // Test endpoints
                        .requestMatchers("/api/test-scenario/**").permitAll()

                        // Health checks (Cloud Run probes)
                        .requestMatchers("/actuator/health/**").permitAll()

                        // SuperAdmin-only routes
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/signup")
                        .hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/v1/admin/management/**").hasRole("SUPER_ADMIN")

                        // Protected Admin-only routes
                        .requestMatchers("/api/v1/events/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/claims/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/tickets/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/v1/rounds/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .anyRequest().authenticated())
                .addFilterBefore(adminAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
