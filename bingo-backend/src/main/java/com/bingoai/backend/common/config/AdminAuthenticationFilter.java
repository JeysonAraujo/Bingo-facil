package com.bingoai.backend.common.config;

import com.bingoai.backend.admin.Admin;
import com.bingoai.backend.admin.AdminSession;
import com.bingoai.backend.admin.AdminSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;

@Component
public class AdminAuthenticationFilter extends OncePerRequestFilter {

    private final AdminSessionRepository adminSessionRepository;

    public AdminAuthenticationFilter(AdminSessionRepository adminSessionRepository) {
        this.adminSessionRepository = adminSessionRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            Optional<AdminSession> sessionOpt = adminSessionRepository.findByToken(token);
            if (sessionOpt.isPresent()) {
                AdminSession session = sessionOpt.get();

                if (session.getExpiresAt().isAfter(OffsetDateTime.now())) {
                    Admin admin = session.getAdmin();

                    if (!admin.isActive()) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Conta desativada.");
                        return;
                    }

                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(admin.getRole());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            admin,
                            null,
                            Collections.singletonList(authority)
                    );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
