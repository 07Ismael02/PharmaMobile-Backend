. "$PSScriptRoot\entorno.ps1"
$previousJavaOptions=$env:JAVA_TOOL_OPTIONS
# El contexto MSIX de Codex requiere fallback TCP; solo en este proceso.
$env:JAVA_TOOL_OPTIONS=($previousJavaOptions+' -Djdk.net.unixdomain.tmpdir=C:\PharmaMobile-Nio-TcpFallback').Trim()
Push-Location $Repo
try { & .\mvnw.cmd -B -ntp spring-boot:run '-Dspring-boot.run.jvmArguments=-Xms128m -Xmx512m'; exit $LASTEXITCODE }
finally { Pop-Location; $env:JAVA_TOOL_OPTIONS=$previousJavaOptions }
