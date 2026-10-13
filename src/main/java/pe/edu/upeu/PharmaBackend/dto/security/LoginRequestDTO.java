package pe.edu.upeu.PharmaBackend.dto.security;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(@NotBlank String username, @NotBlank String password) {
    @Override
    public String toString() { return "LoginRequestDTO[username=" + username + ", password=[REDACTED]]"; }
}
