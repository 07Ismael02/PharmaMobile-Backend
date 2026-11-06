. "$PSScriptRoot\entorno.ps1"
$classpathFile = Join-Path $Repo 'target\s13-classpath.txt'
if (-not (Test-Path $classpathFile)) { throw 'Ejecutar Maven dependency:build-classpath -Dmdep.outputFile=target/s13-classpath.txt' }
$env:WRITER_PASSWORD=$Secrets.WRITER_PASSWORD
$env:READER_PASSWORD=$Secrets.READER_PASSWORD
try {
    $hashes = & "$env:JAVA_HOME\bin\java.exe" --class-path (Get-Content $classpathFile -Raw).Trim() "$PSScriptRoot\HashPasswords.java"
    if ($LASTEXITCODE -ne 0 -or $hashes.Count -ne 2) { throw 'No se generaron hashes válidos' }
} finally { Remove-Item Env:WRITER_PASSWORD,Env:READER_PASSWORD }
$writerHash=$hashes[0]; $readerHash=$hashes[1]
# Solo inserciones de fixtures propios ausentes. No modifica contraseñas de cuentas existentes.
Invoke-S13Sql @"
insert into PHARMADB.empleados (dni,nombres,apellidos,cargo,email,estado,fecha_creacion)
select '90000001','S13','Escritura Sintetica','Prueba','s13.writer@example.invalid',true,systimestamp from dual
where not exists(select 1 from PHARMADB.empleados where dni='90000001');
insert into PHARMADB.empleados (dni,nombres,apellidos,cargo,email,estado,fecha_creacion)
select '90000002','S13','Lectura Sintetica','Prueba','s13.reader@example.invalid',true,systimestamp from dual
where not exists(select 1 from PHARMADB.empleados where dni='90000002');
insert into PHARMADB.usuarios (username,password,estado,empleado_id,fecha_creacion)
select 's13_writer','$writerHash',true,e.id,systimestamp from PHARMADB.empleados e where e.dni='90000001'
and not exists(select 1 from PHARMADB.usuarios where username='s13_writer');
insert into PHARMADB.usuarios (username,password,estado,empleado_id,fecha_creacion)
select 's13_reader','$readerHash',true,e.id,systimestamp from PHARMADB.empleados e where e.dni='90000002'
and not exists(select 1 from PHARMADB.usuarios where username='s13_reader');
insert into PHARMADB.usuario_roles (usuario_id,rol_id)
select u.id,r.id from PHARMADB.usuarios u cross join PHARMADB.roles r
where ((u.username='s13_writer' and r.nombre='ALMACENERO') or (u.username='s13_reader' and r.nombre='VENDEDOR'))
and not exists(select 1 from PHARMADB.usuario_roles ur where ur.usuario_id=u.id and ur.rol_id=r.id);
insert into PHARMADB.categorias (nombre,descripcion,estado,fecha_creacion)
select 'S13 Sintetica','Fixture local de integracion',true,systimestamp from dual
where not exists(select 1 from PHARMADB.categorias where nombre='S13 Sintetica');
insert into PHARMADB.productos (nombre,precio,stock,estado,categoria_id,fecha_creacion)
select 'S13 Producto Base',4.50,10,true,c.id,systimestamp from PHARMADB.categorias c where c.nombre='S13 Sintetica'
and not exists(select 1 from PHARMADB.productos where nombre='S13 Producto Base');
commit;
select 'FIXTURE_USER='||username from PHARMADB.usuarios where username in ('s13_writer','s13_reader');
"@
