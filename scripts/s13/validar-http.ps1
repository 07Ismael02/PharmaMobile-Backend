. "$PSScriptRoot\entorno.ps1"
$baseUrl='http://localhost:8080'
function Request([string]$Method,[string]$Path,[int]$Expected,[object]$Body=$null,[string]$Token='') {
    $parameters=@{Uri="$baseUrl$Path";Method=$Method;SkipHttpErrorCheck=$true;TimeoutSec=20}
    if($Token){$parameters.Headers=@{Authorization="Bearer $Token"}}
    if($null -ne $Body){$parameters.Body=($Body|ConvertTo-Json -Compress);$parameters.ContentType='application/json'}
    $response=Invoke-WebRequest @parameters
    if([int]$response.StatusCode -ne $Expected){throw "$Method $Path expected=$Expected actual=$($response.StatusCode)"}
    Write-Host "$Method $Path -> $Expected"
    if($response.Content -and $response.Headers["Content-Type"] -match "json"){return ($response.Content|ConvertFrom-Json)}
}
$null=Request GET '/api/health' 200
$writer=Request POST '/api/v1/auth/login' 200 @{username='s13_writer';password=$Secrets.WRITER_PASSWORD}
$reader=Request POST '/api/v1/auth/login' 200 @{username='s13_reader';password=$Secrets.READER_PASSWORD}
if($writer.tipo -ne 'Bearer' -or $writer.permisos -notcontains 'CATALOGO_ESCRIBIR'){throw 'Contrato writer incorrecto'}
if($reader.permisos -contains 'CATALOGO_ESCRIBIR'){throw 'Reader tiene escritura'}
$identity=Request GET '/api/v1/auth/me' 200 $null $writer.token
if($identity.username -ne 's13_writer'){throw 'Identidad incorrecta'}
$null=Request GET '/api/v1/productos?pagina=0&tamanio=20' 200 $null $writer.token
$categories=Request GET '/api/v1/categorias' 200 $null $writer.token
$category=@($categories | Where-Object {$_.nombre -eq 'S13 Sintetica'})[0]
if(-not $category.id){throw 'Falta categoria sintetica'}
$product=@{nombre=('S13 HTTP '+[guid]::NewGuid().ToString('N'));precio=5.25;stock=7;estado=$true;categoriaId=$category.id}
$created=Request POST '/api/v1/productos' 201 $product $writer.token
if($created.id -le 0){throw 'ID creado invalido'}
$null=Request POST '/api/v1/productos' 403 $product $reader.token
$null=Request POST '/api/v1/auth/login' 401 @{username='s13_writer';password='invalid-'+[guid]::NewGuid().ToString('N')}
$null=Request GET '/api/v1/auth/me' 401 $null 'invalid.jwt.token'
$null=Request GET '/api/v1/auth/me' 401
Write-Output "HTTP_CHECKS_OK CreatedProductId=$($created.id). Tokens no mostrados."
