# PharmaMobile-Backend

Backend REST de PharmaMobile. Usa Spring Boot, Java 21, Oracle Database Free y Flyway.

## Desarrollo local

Requiere Oracle disponible en `localhost:1522/FREEPDB1` y el esquema `PHARMADB` existente. La contraseña se proporciona exclusivamente mediante la variable de entorno `SPRING_DATASOURCE_PASSWORD`; nunca se guarda en Git.

Con el perfil `dev`, el backend escucha en el puerto `8080`. Sus rutas principales son `GET /api/health`, `GET /api/v1/categorias` y el CRUD de `/api/v1/productos`. Swagger UI está disponible en `/swagger-ui.html`.

La migración inicial está en `src/main/resources/db/migration`. No edites una migración ya aplicada ni recrees el esquema existente. Hibernate utiliza `ddl-auto: validate`.

## Seguridad JWT (S10, pendiente de migraciones)

El código de seguridad requiere `JWT_SECRET_BASE64`: una clave aleatoria de al menos 32 bytes codificada en Base64, suministrada como variable de entorno fuera de Git. No reutilices contraseñas de base de datos como clave de firma. El perfil `dev` sigue requiriendo `SPRING_DATASOURCE_PASSWORD`; el perfil `prod` utiliza además `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y `FRONTEND_ORIGINS`, con `SERVER_PORT` opcional. Ningún valor secreto debe incluirse en archivos versionados o logs.

Las migraciones V2 y V3 están preparadas pero **no autorizadas para ejecución**. Con Flyway habilitado, el próximo arranque podría aplicarlas automáticamente: no reinicies el backend hasta comprobarlas y autorizarlo expresamente. La validación HTTP y Postman de S10 aún está pendiente.

Este proyecto reutiliza y adapta como referencia académica componentes técnicos de [pharmaSoft](https://github.com/dreyna/pharmaSoft). El repositorio de entrega y su historial Git son independientes.
