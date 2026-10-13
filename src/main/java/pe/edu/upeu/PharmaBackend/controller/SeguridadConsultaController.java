package pe.edu.upeu.PharmaBackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.PharmaBackend.dto.security.*;
import pe.edu.upeu.PharmaBackend.security.SeguridadConsultaService;

import java.util.List;

@RestController
public class SeguridadConsultaController {
    private final SeguridadConsultaService consultas;

    public SeguridadConsultaController(SeguridadConsultaService consultas) { this.consultas = consultas; }

    @GetMapping("/api/v1/usuarios")
    public List<UsuarioResumenDTO> usuarios() { return consultas.usuarios(); }

    @GetMapping("/api/v1/empleados")
    public List<EmpleadoResumenDTO> empleados() { return consultas.empleados(); }

    @GetMapping("/api/v1/roles")
    public List<RolResumenDTO> roles() { return consultas.roles(); }
}
