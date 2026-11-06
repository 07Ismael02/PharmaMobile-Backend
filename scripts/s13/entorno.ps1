param([switch]$Initialize)
$ErrorActionPreference = 'Stop'
$script:Repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$script:PrivateDir = Join-Path $env:LOCALAPPDATA 'PharmaMobile\S13'
$script:SecretsFile = Join-Path $PrivateDir 'secrets.json'
if ($Initialize -and -not (Test-Path $SecretsFile)) {
    New-Item -ItemType Directory -Path $PrivateDir -Force | Out-Null
    $acl = Get-Acl $PrivateDir
    $acl.SetAccessRuleProtection($true, $false)
    $identity = [Security.Principal.WindowsIdentity]::GetCurrent().Name
    $rule = [Security.AccessControl.FileSystemAccessRule]::new($identity,'FullControl','ContainerInherit,ObjectInherit','None','Allow')
    $acl.AddAccessRule($rule)
    Set-Acl -LiteralPath $PrivateDir -AclObject $acl
    function New-Password { 'S13aA9'+[Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(20)) }
    [ordered]@{
        ORACLE_PWD = New-Password
        SPRING_DATASOURCE_PASSWORD = New-Password
        JWT_SECRET_BASE64 = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
        WRITER_PASSWORD = New-Password
        READER_PASSWORD = New-Password
    } | ConvertTo-Json | Set-Content -LiteralPath $SecretsFile -Encoding utf8
}
if (-not (Test-Path $SecretsFile)) { throw 'Primero ejecutar entorno.ps1 -Initialize' }
$script:Secrets = Get-Content -LiteralPath $SecretsFile -Raw | ConvertFrom-Json
$env:ORACLE_PWD = $Secrets.ORACLE_PWD
$env:SPRING_DATASOURCE_PASSWORD = $Secrets.SPRING_DATASOURCE_PASSWORD
$env:JWT_SECRET_BASE64 = $Secrets.JWT_SECRET_BASE64
$env:SPRING_PROFILES_ACTIVE = 'dev'
$env:SPRING_FLYWAY_BASELINE_ON_MIGRATE = 'false'
$env:JAVA_HOME = 'C:\Program Files\Zulu\zulu-21'
$env:Path = "$env:JAVA_HOME\bin;C:\Program Files\Docker\Docker\resources\bin;"+$env:Path
function Invoke-S13Sql([string]$Sql) {
    $container = (docker compose -f "$Repo\compose.yaml" ps -q oracle | Select-Object -Unique | Select-Object -First 1)
    if (-not $container) { throw 'Oracle no está iniciado' }
    $inputSql = "set echo off feedback off verify off heading off pagesize 0`nwhenever sqlerror exit sql.sqlcode rollback`nalter session set container=FREEPDB1;`n$Sql`nexit;"
    $result = $inputSql | docker exec -i $container sqlplus -s '/ as sysdba'
    if ($LASTEXITCODE -ne 0) { $safeMessage = $result | Out-String; foreach ($item in $Secrets.PSObject.Properties) { $safeMessage = $safeMessage.Replace([string]$item.Value, "[REDACTED]") }; throw "Consulta Oracle falló. No usar clean/repair. $safeMessage" }
    $result
}
