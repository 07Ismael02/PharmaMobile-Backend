. "$PSScriptRoot\entorno.ps1"
$connections=@(Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue)
$serverIds=@($connections.OwningProcess | Select-Object -Unique)
foreach($serverId in $serverIds) {
    $process=Get-CimInstance Win32_Process -Filter "ProcessId=$serverId"
    $belongsToRepo = $process.CommandLine.Contains($Repo)
    $argumentFile = [regex]::Match($process.CommandLine, '@([^"\s]+\.argfile)')
    if (-not $belongsToRepo -and $argumentFile.Success -and (Test-Path -LiteralPath $argumentFile.Groups[1].Value)) {
        $arguments = Get-Content -LiteralPath $argumentFile.Groups[1].Value -Raw
        $belongsToRepo = $arguments.Replace('\\','\').Contains($Repo, [StringComparison]::OrdinalIgnoreCase)
    }
    if($process.Name -ne 'java.exe' -or -not $belongsToRepo) { throw 'El puerto 8080 pertenece a otro proceso; no se detiene.' }
    # Solo desarrollo y sin operaciones activas. En consola, preferir Ctrl+C.
    Stop-Process -Id $serverId
    Write-Output "Backend de desarrollo detenido, PID=$serverId"
}
