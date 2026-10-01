# PharmaMobile-Backend

Backend REST de PharmaMobile. Usa Spring Boot, Java 21, Oracle Database Free y Flyway.

## Desarrollo local

Requiere Oracle disponible en `localhost:1522/FREEPDB1` y el esquema `PHARMADB` existente. La contraseña se proporciona exclusivamente mediante la variable de entorno `SPRING_DATASOURCE_PASSWORD`; nunca se guarda en Git.

Con el perfil `dev`, el backend escucha en el puerto `8080`. Sus rutas principales son `GET /api/health`, `GET /api/v1/categorias` y el CRUD de `/api/v1/productos`. Swagger UI está disponible en `/swagger-ui.html`.

La migración inicial está en `src/main/resources/db/migration`. No edites una migración ya aplicada ni recrees el esquema existente. Hibernate utiliza `ddl-auto: validate`.

Este proyecto reutiliza y adapta como referencia académica componentes técnicos de [pharmaSoft](https://github.com/dreyna/pharmaSoft). El repositorio de entrega y su historial Git son independientes.
