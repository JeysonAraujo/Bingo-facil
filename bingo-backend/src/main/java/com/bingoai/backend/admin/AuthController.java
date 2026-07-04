package com.bingoai.backend.admin;

import com.bingoai.backend.common.dto.ApiResponse;
import com.bingoai.backend.admin.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Transactional
public class AuthController {

    private final AdminRepository adminRepository;
    private final AdminSessionRepository adminSessionRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AdminRepository adminRepository,
                          AdminSessionRepository adminSessionRepository,
                          PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.adminSessionRepository = adminSessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        Admin admin = adminRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos."));

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

        // Generate 6-digit code
        String code = String.format("%06d", new Random().nextInt(999999));
        admin.setResetCode(code);
        admin.setResetCodeExpiresAt(OffsetDateTime.now().plusMinutes(15));
        adminRepository.save(admin);

        // Print mock email to console
        System.out.println("==================================================");
        System.out.println("[EMAIL MOCK] Para: " + admin.getEmail());
        System.out.println("[EMAIL MOCK] Assunto: Código de Recuperação de Senha");
        System.out.println("[EMAIL MOCK] Corpo: Seu código de recuperação é: " + code);
        System.out.println("==================================================");

        return ResponseEntity.ok(ApiResponse.success("Código de recuperação enviado. Verifique o console do servidor."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        Admin admin = adminRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados inválidos."));

        if (admin.getResetCode() == null || !admin.getResetCode().equals(request.code())) {
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
