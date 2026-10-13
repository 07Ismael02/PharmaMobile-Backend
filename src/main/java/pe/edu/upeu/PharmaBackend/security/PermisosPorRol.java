package pe.edu.upeu.PharmaBackend.security;

import java.util.Set;
import java.util.TreeSet;

public final class PermisosPorRol {
    private static final Set<String> CATALOGO = Set.of("CATALOGO_LEER", "CATALOGO_ESCRIBIR");
    private static final Set<String> VENTAS = Set.of("VENTAS_LEER", "VENTAS_REGISTRAR");
    private static final Set<String> CLIENTES = Set.of("CLIENTES_LEER", "CLIENTES_ESCRIBIR");
    private static final Set<String> ADMIN = Set.of(
            "CATALOGO_LEER", "CATALOGO_ESCRIBIR", "CLIENTES_LEER", "CLIENTES_ESCRIBIR",
            "VENTAS_LEER", "VENTAS_REGISTRAR", "REPORTES_VER", "USUARIOS_ADMINISTRAR");

    private PermisosPorRol() { }

    public static Set<String> deRoles(Set<String> roles) {
        Set<String> permisos = new TreeSet<>();
        for (String rol : roles) {
            switch (rol) {
                case "ADMIN" -> permisos.addAll(ADMIN);
                case "VENDEDOR" -> {
                    permisos.add("CATALOGO_LEER");
                    permisos.addAll(CLIENTES);
                    permisos.addAll(VENTAS);
                }
                case "ALMACENERO" -> permisos.addAll(CATALOGO);
                default -> { /* Un rol desconocido no concede permisos. */ }
            }
        }
        return Set.copyOf(permisos);
    }
}
