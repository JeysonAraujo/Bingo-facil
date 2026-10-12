package com.bingoai.backend.admin;

import com.bingoai.backend.common.config.SecurityConfig;
import com.bingoai.backend.common.mail.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminManagementController.class)
@Import(SecurityConfig.class)
@SuppressWarnings("null")
public class AdminManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AdminRepository adminRepository;

    @MockBean
    private AdminSessionRepository adminSessionRepository;

    @MockBean
    private EmailService emailService;

    private Admin superAdmin;
    private Admin targetAdmin;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        superAdmin = new Admin();
        superAdmin.setId(UUID.randomUUID());
        superAdmin.setUsername("superadmin");
        superAdmin.setEmail("superadmin@test.com");
        superAdmin.setRole("ROLE_SUPER_ADMIN");
        superAdmin.setActive(true);

        targetAdmin = new Admin();
        targetAdmin.setId(UUID.randomUUID());
        targetAdmin.setUsername("targetadmin");
        targetAdmin.setEmail("target@test.com");
        targetAdmin.setRole("ROLE_ADMIN");
        targetAdmin.setActive(true);

        auth = new UsernamePasswordAuthenticationToken(
                superAdmin,
                null,
                Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
        );
    }

    @Test
    void sendResetCode_Success_HashesCodeAndSendsEmailWithoutExposingInResponse() throws Exception {
        when(adminRepository.findById(targetAdmin.getId())).thenReturn(Optional.of(targetAdmin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/v1/admin/management/admins/{id}/send-reset-code", targetAdmin.getId())
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Código de recuperação enviado com sucesso para o e-mail cadastrado."));

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(1)).sendPasswordResetEmail(eq("target@test.com"), codeCaptor.capture());

        String plainCode = codeCaptor.getValue();
        assertNotNull(plainCode);
        assertEquals(6, plainCode.length());

        // Ensure database stores HASH and NOT plain text
        assertNotNull(targetAdmin.getResetCode());
        assertNotEquals(plainCode, targetAdmin.getResetCode());
        assertTrue(passwordEncoder.matches(plainCode, targetAdmin.getResetCode()));
        assertTrue(targetAdmin.getResetCodeExpiresAt().isAfter(OffsetDateTime.now()));
    }
}
