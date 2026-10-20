package pe.edu.upeu.PharmaBackend.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackend.dto.security.*;
import pe.edu.upeu.PharmaBackend.entity.Rol;
import pe.edu.upeu.PharmaBackend.entity.Usuario;
import pe.edu.upeu.PharmaBackend.repository.UsuarioRepository;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO iniciarSesion(LoginRequestDTO request) {
        Usuario usuario = usuarios.findByUsernameIgnoreCase(request.username())
                .orElseThrow(CredencialesInvalidasException::new);
        if (!Boolean.TRUE.equals(usuario.getEstado())
                || !Boolean.TRUE.equals(usuario.getEmpleado().getEstado())
                || !passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }
        Set<String> roles = new TreeSet<>();
        for (Rol rol : usuario.getRoles()) roles.add(rol.getNombre());
        List<String> permisos = PermisosPorRol.deRoles(roles).stream().sorted().toList();
        String token = jwtService.emitir(usuario.getUsername(), List.copyOf(roles), permisos);
        String nombre = usuario.getEmpleado().getNombres() + " " + usuario.getEmpleado().getApellidos();
        return new LoginResponseDTO(token, "Bearer", jwtService.expiracionSegundos(),
                usuario.getUsername(), nombre, List.copyOf(roles), permisos);
    }

    @Transactional(readOnly = true)
    public IdentidadDTO identidad(String username) {
        Usuario usuario = usuarios.findByUsernameIgnoreCase(username)
                .filter(u -> Boolean.TRUE.equals(u.getEstado()) && Boolean.TRUE.equals(u.getEmpleado().getEstado()))
                .orElseThrow(CredencialesInvalidasException::new);
        Set<String> roles = new TreeSet<>();
        for (Rol rol : usuario.getRoles()) roles.add(rol.getNombre());
        String nombre = usuario.getEmpleado().getNombres() + " " + usuario.getEmpleado().getApellidos();
        return new IdentidadDTO(usuario.getUsername(), nombre, List.copyOf(roles),
                PermisosPorRol.deRoles(roles).stream().sorted().toList());
    }
}
