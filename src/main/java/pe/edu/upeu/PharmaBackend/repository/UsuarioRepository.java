package pe.edu.upeu.PharmaBackend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.PharmaBackend.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @EntityGraph(attributePaths = {"empleado", "roles"})
    Optional<Usuario> findByUsernameIgnoreCase(String username);

    @Override
    @EntityGraph(attributePaths = {"empleado", "roles"})
    List<Usuario> findAll();
}
