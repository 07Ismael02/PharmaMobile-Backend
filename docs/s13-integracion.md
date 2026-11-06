# S13: Oracle y JWT en desarrollo aislado

## Alcance

Rama feature/s13-autonoma-integracion basada en feature/security-jwt-logacho. No reemplaza main. Frontend sin cambios. Esta misión habilita servicios; no acredita los diez escenarios visuales S13 ni genera el PDF.

Oracle oficial Free Lite 23.26.0.0 (26ai), fijado también por digest en compose.yaml. Usa FREEPDB1, volumen exclusivo pharmamobile-s13-oracle-data y 127.0.0.1:1522. Fuente: https://container-registry.oracle.com/ords/ocr/ba/database/free . No usar otro proyecto Compose ni volúmenes ajenos.

## Secretos

PowerShell 7, desde la raíz del backend:

```powershell
. .\scripts\s13\entorno.ps1 -Initialize
```

Genera una sola vez secretos aleatorios en %LOCALAPPDATA%\PharmaMobile\S13\secrets.json, con ACL de usuario y fuera del repositorio. No imprime valores. Contiene ORACLE_PWD, SPRING_DATASOURCE_PASSWORD, JWT_SECRET_BASE64, WRITER_PASSWORD y READER_PASSWORD. Las contraseñas de los usuarios están disponibles allí para el propietario: consultarlas solo localmente, nunca pegarlas en chat, logs ni capturas. No borrar/regenerar el archivo si ya existe un volumen: sus contraseñas deben seguir coincidiendo.

Las variables se cargan solo en el proceso. Perfil dev, Hibernate validate y SPRING_FLYWAY_BASELINE_ON_MIGRATE=false. JWT_EXPIRACION_MINUTOS puede fijarse para una demostración de vencimiento; si no, 60 minutos. No mostrar docker compose config sin --quiet ni docker inspect completo: pueden revelar variables.

## Oracle y esquema

```powershell
. .\scripts\s13\entorno.ps1
docker compose up -d oracle
docker compose ps
```

Esperar estado healthy. Solo sobre esta base nueva:

```powershell
.\scripts\s13\crear-esquema.ps1
```

Comprueba que PHARMADB no exista; si ya existe se detiene sin modificarlo. Crea usuario con CREATE SESSION/TABLE/SEQUENCE y cuota 200M en el tablespace exclusivo PHARMAS13 (Oracle Lite no incluye USERS). Verifica cero objetos y SQL BOOLEAN. No volver a ejecutar para un esquema existente.

V1 crea catálogo/ventas, V2 seguridad/roles y V3 fixtures históricos. Las tres son migraciones existentes, intactas y autorizadas para este esquema nuevo en la Misión 4. Oracle DDL hace commits implícitos: si falla una migración, detenerse y registrar estado; no clean, repair, DROP ni eliminación de volúmenes. No usar baseline para omitir V1. No conectar a una base productiva.

## Backend y datos sintéticos

```powershell
.\scripts\s13\iniciar-backend.ps1
```

El primer arranque aplica V1-V3 y valida JPA. Para detenerlo en consola: Ctrl+C. Si se inició oculto durante la integración, sus logs y PID del lanzador están en el directorio privado. scripts/s13/detener-backend.ps1 verifica el Java de este repositorio que escucha en 8080 y lo termina; usarlo solo sin operaciones activas. No termina otros procesos Java.

En otra consola:

```powershell
. .\scripts\s13\entorno.ps1
.\mvnw.cmd -B -ntp dependency:build-classpath '-Dmdep.outputFile=target/s13-classpath.txt'
.\scripts\s13\preparar-datos.ps1
```

El aprovisionamiento usa BCrypt con la dependencia del proyecto. Inserta fixtures propios ausentes sin cambiar contraseñas de cuentas existentes: s13_writer/ALMACENERO (lectura y escritura), s13_reader/VENDEDOR (lectura de catálogo sin escritura), empleados sintéticos con correo example.invalid, categoría S13 Sintetica y producto S13 Producto Base. No reutilizar como credenciales conocidas los hashes históricos de V3. No publicar secretos, bases ni volúmenes.

## Validación

```powershell
. .\scripts\s13\entorno.ps1
.\mvnw.cmd -B -ntp test
.\scripts\s13\validar-http.ps1
```

El verificador HTTP realiza login real de ambas cuentas, /auth/me, GET de catálogo/categorías, POST autorizado 201, POST restringido 403, contraseña incorrecta 401 y token inválido/ausente 401. Nunca imprime tokens. Cada ejecución crea un producto sintético con nombre único: no es una consulta de solo lectura. La expiración está cubierta además por la prueba JwtExpiradoWebTest; distinguirla del token inválido probado contra el servidor vivo.

El script preparar-datos puede repetirse con los mismos secretos sin sobrescribir fixtures. Para un entorno completamente nuevo, elegir explícitamente otro volumen/proyecto y otros secretos; no borrar el volumen existente ni cambiar su contraseña automáticamente.

## Android y cierre

El frontend ya usa http://10.0.2.2:8080/api/v1/; localhost en el emulador no es el anfitrión. No cambiar esa URL. Comprobar /api/health desde el emulador antes de preparar capturas. Dos cuentas requieren instalaciones/perfiles Android aislados por la vinculación de cuenta existente; no borrar datos históricos.

Para detener solo Oracle conservando datos:

```powershell
. .\scripts\s13\entorno.ps1
docker compose stop oracle
```

No usar down -v. Recursos ajustados: Oracle tiene límite de 2 GiB y backend JVM máximo 512 MiB; evitar herramientas pesadas simultáneas. Firebase/JSON auténtico sigue pendiente, pero no bloquea autenticación REST. No cambiar migraciones históricas ni desactivar validación para conseguir un arranque aparente.

## Resultados de esta ejecución

Verificado el 06/11/2026:

- Oracle 23.26.0.0.0 healthy; PHARMADB inicialmente con cero objetos; SQL BOOLEAN correcto. Flyway V1/V2/V3 con success=1, sin baseline. Hibernate validate correcto.
- 38 pruebas unitarias/web (incluye JWT vencido) y 1 prueba contextLoads contra Oracle real: 39 aprobadas, cero fallos/errores/omitidas, en dos ejecuciones complementarias.
- HTTP real: health 200, login de ambas cuentas 200, me 200, catálogo/categorías 200, creación con writer 201 (ID 21), escritura con reader 403, contraseña incorrecta 401, token inválido 401 y sin token 401. No se simuló ni imprimió un JWT.
- Pixel 9a emulator-5554 consultó http://10.0.2.2:8080/api/health y recibió HTTP 200 con el texto real del endpoint. Frontend intacto; no se capturaron los diez pasos.
- Primer intento de esquema falló antes de migrar porque Lite no incluye USERS: se creó el tablespace exclusivo PHARMAS13, sin borrar nada. Flyway no falló.
- Primer arranque Spring aplicó migraciones correctamente pero Tomcat falló por sockets Unix en el contexto MSIX de Codex. El lanzador utiliza solo para su proceso -Djdk.net.unixdomain.tmpdir=C:\PharmaMobile-Nio-TcpFallback (ruta inexistente para activar fallback TCP). No se modifica código funcional ni configuración global. El segundo arranque fue correcto.
- No se cambiaron V1/V2/V3 ni se usaron clean, repair, DROP o eliminación de volúmenes. Firebase y evidencias visuales Android/iOS quedan para fases posteriores.
