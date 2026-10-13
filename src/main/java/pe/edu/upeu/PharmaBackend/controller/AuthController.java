package pe.edu.upeu.PharmaBackend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.PharmaBackend.dto.security.*;
import pe.edu.upeu.PharmaBackend.security.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.iniciarSesion(request));
    }

    @GetMapping("/me")
    public ResponseEntity<IdentidadDTO> me(Authentication authentication) {
        return ResponseEntity.ok(authService.identidad(authentication.getName()));
    }
}
