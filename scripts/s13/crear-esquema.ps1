. "$PSScriptRoot\entorno.ps1"
$count = (Invoke-S13Sql "select count(*) from dba_users where username='PHARMADB';" | Out-String).Trim()
if ($count -ne '0') { throw 'PHARMADB ya existe. No se modifica: revisar su estado antes de continuar.' }
$password = $Secrets.SPRING_DATASOURCE_PASSWORD
Invoke-S13Sql @"
create tablespace PHARMAS13 datafile '/opt/oracle/oradata/pharmas13.dbf' size 50M autoextend on next 10M maxsize 500M;
create user PHARMADB identified by "$password" default tablespace PHARMAS13 temporary tablespace TEMP quota 200M on PHARMAS13;
grant create session, create table, create sequence to PHARMADB;
select 'PHARMADB_OBJECTS='||count(*) from dba_objects where owner='PHARMADB';
select banner_full from v`$version where banner_full like 'Oracle%';
select case when true then 'SQL_BOOLEAN_OK' else 'ERROR' end;
"@
