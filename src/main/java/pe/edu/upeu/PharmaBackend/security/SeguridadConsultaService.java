package pe.edu.upeu.PharmaBackend.security;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.PharmaBackend.dto.security.*;
import pe.edu.upeu.PharmaBackend.entity.Rol;
import pe.edu.upeu.PharmaBackend.repository.*;

import java.util.List;

@Service
public class SeguridadConsultaService {
    private final UsuarioRepository usuarios;
    private final EmpleadoRepository empleados;
    private final RolRepository roles;

    public SeguridadConsultaService(UsuarioRepository usuarios, EmpleadoRepository empleados, RolRepository roles) {
        this.usuarios = usuarios;
        this.empleados = empleados;
        this.roles = roles;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResumenDTO> usuarios() {
        return usuarios.findAll().stream().map(u -> new UsuarioResumenDTO(
                u.getId(), u.getUsername(), u.getEstado(), u.getEmpleado().getId(),
                u.getRoles().stream().map(Rol::getNombre).sorted().toList())).toList();
    }

    @Transactional(readOnly = true)
    public List<EmpleadoResumenDTO> empleados() {
        return empleados.findAll().stream().map(e -> new EmpleadoResumenDTO(
                e.getId(), e.getDni(), e.getNombres(), e.getApellidos(), e.getCargo(),
                e.getEmail(), e.getTelefono(), e.getEstado())).toList();
    }

    @Transactional(readOnly = true)
    public List<RolResumenDTO> roles() {
        return roles.findAll().stream().map(r -> new RolResumenDTO(
                r.getId(), r.getNombre(), r.getDescripcion())).toList();
    }
}
