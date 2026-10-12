package com.bingoai.backend.admin;

import com.bingoai.backend.common.dto.ApiResponse;
import com.bingoai.backend.admin.dto.*;
import com.bingoai.backend.common.mail.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Transactional
public class AuthController {

    private final AdminRepository adminRepository;
    private final AdminSessionRepository adminSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthController(AdminRepository adminRepository,
                          AdminSessionRepository adminSessionRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.adminRepository = adminRepository;
        this.adminSessionRepository = adminSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        Admin admin = adminRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos."));

        if (!admin.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Esta conta foi desativada.");
        }

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos.");
        }

        // Generate session token
        String token = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        AdminSession session = new AdminSession(admin, token, OffsetDateTime.now().plusDays(7));
        adminSessionRepository.save(session);

        AuthResponseDto response = new AuthResponseDto(token, admin.getUsername(), admin.getRole());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signup(@Valid @RequestBody SignupRequestDto request) {
        if (adminRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username já está cadastrado.");
        }
        if (adminRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail já está cadastrado.");
        }

        Admin newAdmin = new Admin();
        newAdmin.setUsername(request.username());
        newAdmin.setEmail(request.email());
        newAdmin.setPassword(passwordEncoder.encode(request.password()));
        newAdmin.setRole(request.role().toUpperCase());
        adminRepository.save(newAdmin);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Administrador cadastrado com sucesso."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        // Find admin by email.
        Admin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "E-mail não encontrado."));

        // Generate 6-digit code securely
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        admin.setResetCode(passwordEncoder.encode(code));
        admin.setResetCodeExpiresAt(OffsetDateTime.now().plusMinutes(15));
        adminRepository.save(admin);

        // Send via EmailService
        emailService.sendPasswordResetEmail(admin.getEmail(), code);

        return ResponseEntity.ok(ApiResponse.success("Código de recuperação enviado com sucesso para o e-mail cadastrado."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        Admin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados inválidos."));

        if (admin.getResetCode() == null || !passwordEncoder.matches(request.code(), admin.getResetCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de recuperação inválido.");
        }

        if (admin.getResetCodeExpiresAt() == null || admin.getResetCodeExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de recuperação expirado.");
        }

        // Reset password
        admin.setPassword(passwordEncoder.encode(request.newPassword()));
        admin.setResetCode(null);
        admin.setResetCodeExpiresAt(null);
        adminRepository.save(admin);

        // Invalidate active sessions
        adminSessionRepository.deleteByAdminId(admin.getId());

        return ResponseEntity.ok(ApiResponse.success("Senha alterada com sucesso."));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            adminSessionRepository.deleteByToken(token);
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
