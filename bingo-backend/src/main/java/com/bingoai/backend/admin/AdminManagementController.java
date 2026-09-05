package com.bingoai.backend.admin;

import com.bingoai.backend.admin.dto.AdminDto;
import com.bingoai.backend.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/management")
@Transactional
public class AdminManagementController {

    private final AdminRepository adminRepository;
    private final AdminSessionRepository adminSessionRepository;

    public AdminManagementController(AdminRepository adminRepository,
                                     AdminSessionRepository adminSessionRepository) {
        this.adminRepository = adminRepository;
        this.adminSessionRepository = adminSessionRepository;
    }

    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<List<AdminDto>>> getAllAdmins() {
        List<AdminDto> admins = adminRepository.findAll().stream()
                .map(a -> new AdminDto(a.getId(), a.getUsername(), a.getEmail(), a.getRole(), a.isActive()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(admins));
    }

    @PutMapping("/admins/{id}/toggle-active")
    public ResponseEntity<ApiResponse<AdminDto>> toggleActive(
            @PathVariable UUID id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Admin currentAdmin) {
        
        if (currentAdmin != null && currentAdmin.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você não pode alterar o status da sua própria conta.");
        }

        Admin admin = adminRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado."));

        admin.setActive(!admin.isActive());
        Admin saved = adminRepository.save(admin);

        // If deactivated, delete their session tokens immediately
        if (!saved.isActive()) {
            adminSessionRepository.deleteByAdminId(Objects.requireNonNull(id));
            System.out.println("[AdminManagement] Terminated all active sessions for deactivated admin: " + id);
        }
        
        AdminDto dto = new AdminDto(saved.getId(), saved.getUsername(), saved.getEmail(), saved.getRole(), saved.isActive());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping("/admins/{id}/send-reset-code")
    public ResponseEntity<ApiResponse<String>> sendResetCode(@PathVariable UUID id) {
        Admin admin = adminRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado."));

        // Generate 6-digit code
        String code = String.format("%06d", new Random().nextInt(999999));
        admin.setResetCode(code);
        admin.setResetCodeExpiresAt(OffsetDateTime.now().plusMinutes(15));
        adminRepository.save(admin);

        // Print mock email to console
        System.out.println("==================================================");
        System.out.println("[EMAIL MOCK - SUPERADMIN INITIATED] Para: " + admin.getEmail());
        System.out.println("[EMAIL MOCK - SUPERADMIN INITIATED] Assunto: Código de Recuperação de Senha");
        System.out.println("[EMAIL MOCK - SUPERADMIN INITIATED] Corpo: Seu código de recuperação é: " + code);
        System.out.println("==================================================");

        return ResponseEntity.ok(ApiResponse.success("Código de recuperação enviado. Verifique o console do servidor. Código gerado: " + code));
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAdmin(
            @PathVariable UUID id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal Admin currentAdmin) {
        
        if (currentAdmin != null && currentAdmin.getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você não pode excluir sua própria conta.");
        }

        Admin admin = adminRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Administrador não encontrado."));

        // Clear active sessions before deletion
        adminSessionRepository.deleteByAdminId(Objects.requireNonNull(id));
        
        adminRepository.delete(Objects.requireNonNull(admin));
        return ResponseEntity.ok(ApiResponse.success("Administrador excluído com sucesso."));
    }
}
