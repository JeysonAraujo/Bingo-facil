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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@SuppressWarnings("null")
public class AuthControllerTest {

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

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = new Admin();
        admin.setId(UUID.randomUUID());
        admin.setUsername("testadmin");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("oldPassword123"));
        admin.setRole("ROLE_ADMIN");
        admin.setActive(true);
    }

    @Test
    void forgotPassword_Success_HashesCodeAndDispatchesEmail() throws Exception {
        when(adminRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String requestBody = """
            {
                "email": "admin@test.com"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Código de recuperação enviado com sucesso para o e-mail cadastrado."));

        // Capture plain code passed to EmailService
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(1)).sendPasswordResetEmail(eq("admin@test.com"), codeCaptor.capture());

        String plainCode = codeCaptor.getValue();
        assertNotNull(plainCode);
        assertEquals(6, plainCode.length());

        // Verify the code saved in Admin DB is HASHED and NOT stored in plaintext
        assertNotNull(admin.getResetCode());
        assertNotEquals(plainCode, admin.getResetCode());
        assertTrue(passwordEncoder.matches(plainCode, admin.getResetCode()));

        // Verify expiration is set in the future (~15 mins)
        assertNotNull(admin.getResetCodeExpiresAt());
        assertTrue(admin.getResetCodeExpiresAt().isAfter(OffsetDateTime.now()));
    }

    @Test
    void forgotPassword_EmailNotFound_ReturnsNotFound() throws Exception {
        when(adminRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        String requestBody = """
            {
                "email": "unknown@test.com"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());

        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void resetPassword_Success_VerifiesHashUpdatesPasswordAndClearsCode() throws Exception {
        String plainCode = "123456";
        admin.setResetCode(passwordEncoder.encode(plainCode));
        admin.setResetCodeExpiresAt(OffsetDateTime.now().plusMinutes(10));

        when(adminRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String requestBody = """
            {
                "email": "admin@test.com",
                "code": "123456",
                "newPassword": "newSecretPassword123"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Senha alterada com sucesso."));

        // Verify password was updated
        assertTrue(passwordEncoder.matches("newSecretPassword123", admin.getPassword()));

        // Verify resetCode and resetCodeExpiresAt are wiped clean (no trace left in DB)
        assertNull(admin.getResetCode());
        assertNull(admin.getResetCodeExpiresAt());

        // Verify sessions were purged for security
        verify(adminSessionRepository, times(1)).deleteByAdminId(admin.getId());
    }

    @Test
    void resetPassword_InvalidCode_ReturnsBadRequest() throws Exception {
        String correctCode = "654321";
        admin.setResetCode(passwordEncoder.encode(correctCode));
        admin.setResetCodeExpiresAt(OffsetDateTime.now().plusMinutes(10));

        when(adminRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        String requestBody = """
            {
                "email": "admin@test.com",
                "code": "000000",
                "newPassword": "newSecretPassword123"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        // Verify password was NOT changed
        assertFalse(passwordEncoder.matches("newSecretPassword123", admin.getPassword()));
        verify(adminSessionRepository, never()).deleteByAdminId(any());
    }

    @Test
    void resetPassword_ExpiredCode_ReturnsBadRequest() throws Exception {
        String plainCode = "123456";
        admin.setResetCode(passwordEncoder.encode(plainCode));
        admin.setResetCodeExpiresAt(OffsetDateTime.now().minusMinutes(1)); // expired

        when(adminRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        String requestBody = """
            {
                "email": "admin@test.com",
                "code": "123456",
                "newPassword": "newSecretPassword123"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
