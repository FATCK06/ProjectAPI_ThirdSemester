param(
    [string]$BaseUrl = "http://localhost:8082/api"
)

$ErrorActionPreference = "Stop"
$BaseUrl = $BaseUrl.TrimEnd("/")
$temporaryFiles = [System.Collections.Generic.List[string]]::new()
$failures = 0

$internalDetailPatterns = @(
    "at [\w.$]+\(",
    "Exception",
    "StackTrace",
    "org\.springframework",
    "org\.hibernate",
    "java\.sql",
    "jdbc:",
    "SELECT\s+.+\s+FROM",
    "INSERT\s+INTO",
    "UPDATE\s+.+\s+SET"
)

function Invoke-ValidationCheck {
    param(
        [string]$Name,
        [ValidateSet("GET", "POST")]
        [string]$Method,
        [string]$Path,
        [string]$Body
    )

    $responsePath = Join-Path $env:TEMP ("frota-validation-" + [guid]::NewGuid().ToString("N") + ".tmp")
    $temporaryFiles.Add($responsePath)

    $curlArguments = @(
        "--silent",
        "--show-error",
        "--output", $responsePath,
        "--write-out", "%{http_code}",
        "--request", $Method
    )

    if ($Method -eq "POST") {
        $requestPath = Join-Path $env:TEMP ("frota-validation-request-" + [guid]::NewGuid().ToString("N") + ".json")
        $temporaryFiles.Add($requestPath)
        [System.IO.File]::WriteAllText($requestPath, $Body, [System.Text.Encoding]::UTF8)
        $curlArguments += @(
            "--header", "Content-Type: application/json",
            "--data-binary", "@$requestPath"
        )
    }

    $curlArguments += "$BaseUrl$Path"
    $status = & curl.exe @curlArguments

    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "FAIL: $Name"
        Write-Host "curl.exe terminou com código $LASTEXITCODE"
        $script:failures++
        return
    }

    $responseBody = [System.IO.File]::ReadAllText($responsePath)
    Write-Host ""
    Write-Host "Validacao: $Name"
    Write-Host "Metodo/rota: $Method $Path"
    Write-Host "Status HTTP: $status"
    if ([string]::IsNullOrWhiteSpace($responseBody)) {
        Write-Host "Corpo da resposta: (vazio)"
    } else {
        Write-Host "Corpo da resposta: $responseBody"
    }

    $leaksInternalDetails = $false
    foreach ($pattern in $internalDetailPatterns) {
        if ($responseBody -match $pattern) {
            $leaksInternalDetails = $true
            break
        }
    }

    if ($status -ne "400") {
        Write-Host "Resultado: FAIL - esperado HTTP 400"
        $script:failures++
    } elseif ($leaksInternalDetails) {
        Write-Host "Resultado: FAIL - resposta parece conter detalhes internos"
        $script:failures++
    } else {
        Write-Host "Resultado: PASS - HTTP 400 sem detalhes internos detectados"
    }
}

try {
    Write-Host "Testando validações em $BaseUrl"

    Invoke-ValidationCheck "CPF fora do formato" "GET" "/motoristas/123" ""
    Invoke-ValidationCheck "Documento fora do formato" "GET" "/agregados/123" ""
    Invoke-ValidationCheck "Placa fora do formato" "GET" "/veiculos/L" ""

    $nullIds = "[null]"
    Invoke-ValidationCheck "ID nulo em resumos" "POST" "/motoristas/resumos" $nullIds
    Invoke-ValidationCheck "ID nulo em placas" "POST" "/veiculos/placas" $nullIds
    Invoke-ValidationCheck "ID nulo em modelos" "POST" "/veiculos/modelos" $nullIds
    Invoke-ValidationCheck "ID nulo em tipos" "POST" "/veiculos/tipos" $nullIds

    $testUuid = '"00000000-0000-0000-0000-000000000001"'
    $tooManyIds = "[" + (($testUuid + ",") * 10000) + $testUuid + "]"
    Invoke-ValidationCheck "Mais de 10000 IDs em resumos" "POST" "/motoristas/resumos" $tooManyIds
    Invoke-ValidationCheck "Mais de 10000 IDs em placas" "POST" "/veiculos/placas" $tooManyIds
    Invoke-ValidationCheck "Mais de 10000 IDs em modelos" "POST" "/veiculos/modelos" $tooManyIds
    Invoke-ValidationCheck "Mais de 10000 IDs em tipos" "POST" "/veiculos/tipos" $tooManyIds

    $tooManyItems = "[" + (("{}," * 10000) + "{}") + "]"
    Invoke-ValidationCheck "Mais de 10000 itens em lote de motoristas" "POST" "/motoristas/lote" $tooManyItems
    Invoke-ValidationCheck "Mais de 10000 itens em lote de agregados" "POST" "/agregados/lote" $tooManyItems
    Invoke-ValidationCheck "Mais de 10000 itens em lote de veículos" "POST" "/veiculos/lote" $tooManyItems
} finally {
    foreach ($path in $temporaryFiles) {
        if (Test-Path -LiteralPath $path) {
            Remove-Item -LiteralPath $path
        }
    }
}

if ($failures -gt 0) {
    Write-Host "$failures teste(s) falharam."
    exit 1
}

Write-Host "Todos os testes passaram."
