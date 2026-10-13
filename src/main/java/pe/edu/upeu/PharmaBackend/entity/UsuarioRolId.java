package pe.edu.upeu.PharmaBackend.entity;

import java.io.Serializable;
import java.util.Objects;

public class UsuarioRolId implements Serializable {
    private Long usuarioId;
    private Long rolId;

    public UsuarioRolId() { }

    public UsuarioRolId(Long usuarioId, Long rolId) {
        this.usuarioId = usuarioId;
        this.rolId = rolId;
    }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public Long getRolId() { return rolId; }
    public void setRolId(Long rolId) { this.rolId = rolId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof UsuarioRolId that)) return false;
        return Objects.equals(usuarioId, that.usuarioId) && Objects.equals(rolId, that.rolId);
    }

    @Override
    public int hashCode() { return Objects.hash(usuarioId, rolId); }
}
