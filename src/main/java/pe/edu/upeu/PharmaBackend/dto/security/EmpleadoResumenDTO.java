package pe.edu.upeu.PharmaBackend.dto.security;

public record EmpleadoResumenDTO(Long id, String dni, String nombres, String apellidos,
                                 String cargo, String email, String telefono, Boolean estado) { }
