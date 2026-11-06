. "$PSScriptRoot\entorno.ps1"
$connections=@(Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue)
$serverIds=@($connections.OwningProcess | Select-Object -Unique)
foreach($serverId in $serverIds) {
    $process=Get-CimInstance Win32_Process -Filter "ProcessId=$serverId"
    if($process.Name -ne 'java.exe' -or -not $process.CommandLine.Contains($Repo)) { throw 'El puerto 8080 pertenece a otro proceso; no se detiene.' }
    # Solo desarrollo y sin operaciones activas. En consola, preferir Ctrl+C.
    Stop-Process -Id $serverId
    Write-Output "Backend de desarrollo detenido, PID=$serverId"
}
