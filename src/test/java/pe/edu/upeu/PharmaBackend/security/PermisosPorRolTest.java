package pe.edu.upeu.PharmaBackend.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PermisosPorRolTest {
    @Test
    void adminTieneLosOchoPermisos() {
        Set<String> p = PermisosPorRol.deRoles(Set.of("ADMIN"));
        assertEquals(8, p.size());
        assertTrue(p.contains("USUARIOS_ADMINISTRAR"));
        assertTrue(p.contains("REPORTES_VER"));
    }

    @Test
    void vendedorNoPuedeEscribirCatalogoNiVerReportes() {
        Set<String> p = PermisosPorRol.deRoles(Set.of("VENDEDOR"));
        assertTrue(p.containsAll(Set.of("CATALOGO_LEER", "CLIENTES_ESCRIBIR", "VENTAS_REGISTRAR")));
        assertFalse(p.contains("CATALOGO_ESCRIBIR"));
        assertFalse(p.contains("REPORTES_VER"));
    }

    @Test
    void almaceneroSoloPuedeLeerYEscribirCatalogo() {
        assertEquals(Set.of("CATALOGO_LEER", "CATALOGO_ESCRIBIR"),
                PermisosPorRol.deRoles(Set.of("ALMACENERO")));
    }

    @Test
    void rolDesconocidoNoConcedePermisos() {
        assertTrue(PermisosPorRol.deRoles(Set.of("OTRO")).isEmpty());
    }
}
