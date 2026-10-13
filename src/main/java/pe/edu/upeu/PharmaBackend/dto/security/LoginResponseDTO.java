package pe.edu.upeu.PharmaBackend.dto.security;

import java.util.List;

public record LoginResponseDTO(
        String token,
        String tipo,
        long expiraEnSegundos,
        String username,
        String nombreCompleto,
        List<String> roles,
        List<String> permisos) {
    @Override
    public String toString() { return "LoginResponseDTO[token=[REDACTED], username=" + username + "]"; }
}
