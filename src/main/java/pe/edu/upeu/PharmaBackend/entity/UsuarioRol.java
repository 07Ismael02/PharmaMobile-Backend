package pe.edu.upeu.PharmaBackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuario_roles")
@IdClass(UsuarioRolId.class)
@Getter
@Setter
public class UsuarioRol {
    @Id
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Id
    @Column(name = "rol_id")
    private Long rolId;
}
