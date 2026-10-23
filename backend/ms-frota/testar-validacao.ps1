param(
    [string]$FrotaBaseUrl = "http://localhost:8082/api",
    [string]$OperacoesBaseUrl = "http://localhost:8083/api",
    [string]$UsuariosBaseUrl = "http://localhost:8081/api"
)

$ErrorActionPreference = "Stop"
$FrotaBaseUrl = $FrotaBaseUrl.TrimEnd("/")
$OperacoesBaseUrl = $OperacoesBaseUrl.TrimEnd("/")
$UsuariosBaseUrl = $UsuariosBaseUrl.TrimEnd("/")
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
        [string]$Body,
        [string]$ServiceBaseUrl = $FrotaBaseUrl,
        [string]$ExpectedField
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

    $curlArguments += "$ServiceBaseUrl$Path"
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

    $erroValido = $false
    $camposValidos = [string]::IsNullOrEmpty($ExpectedField)
    try {
        $corpoJson = $responseBody | ConvertFrom-Json
        $erroValido = $corpoJson.erro -is [string] -and -not [string]::IsNullOrWhiteSpace($corpoJson.erro)
        if (-not [string]::IsNullOrEmpty($ExpectedField) -and $corpoJson.campos) {
            $camposValidos = $null -ne $corpoJson.campos.PSObject.Properties[$ExpectedField]
        }
    } catch {
        $erroValido = $false
    }

    if ($status -ne "400") {
        Write-Host "Resultado: FAIL - esperado HTTP 400"
        $script:failures++
    } elseif (-not $erroValido) {
        Write-Host "Resultado: FAIL - corpo JSON deve conter uma chave erro não vazia"
        $script:failures++
    } elseif (-not $camposValidos) {
        Write-Host "Resultado: FAIL - corpo deve indicar o campo '$ExpectedField'"
        $script:failures++
    } elseif ($leaksInternalDetails) {
        Write-Host "Resultado: FAIL - resposta parece conter detalhes internos"
        $script:failures++
    } else {
        Write-Host "Resultado: PASS - HTTP 400 com erro estruturado e sem detalhes internos"
    }
}

try {
    Write-Host "Testando validações nos serviços"

    Invoke-ValidationCheck "CPF fora do formato" "GET" "/motoristas/123" "" $FrotaBaseUrl "cpf"
    Invoke-ValidationCheck "Documento fora do formato" "GET" "/agregados/123" "" $FrotaBaseUrl "documento"
    Invoke-ValidationCheck "Placa fora do formato" "GET" "/veiculos/L" "" $FrotaBaseUrl "placa"
    Invoke-ValidationCheck "CPF inválido no lote de motoristas" "POST" "/motoristas/lote" '[{"cpf":"abc"}]' $FrotaBaseUrl "cpf"

    $nullIds = "[null]"
    Invoke-ValidationCheck "ID nulo em resumos" "POST" "/motoristas/resumos" $nullIds $FrotaBaseUrl
    Invoke-ValidationCheck "ID nulo em placas" "POST" "/veiculos/placas" $nullIds $FrotaBaseUrl
    Invoke-ValidationCheck "ID nulo em modelos" "POST" "/veiculos/modelos" $nullIds $FrotaBaseUrl
    Invoke-ValidationCheck "ID nulo em tipos" "POST" "/veiculos/tipos" $nullIds $FrotaBaseUrl

    $testUuid = '"00000000-0000-0000-0000-000000000001"'
    $tooManyIds = "[" + (($testUuid + ",") * 10000) + $testUuid + "]"
    Invoke-ValidationCheck "Mais de 10000 IDs em resumos" "POST" "/motoristas/resumos" $tooManyIds $FrotaBaseUrl
    Invoke-ValidationCheck "Mais de 10000 IDs em placas" "POST" "/veiculos/placas" $tooManyIds $FrotaBaseUrl
    Invoke-ValidationCheck "Mais de 10000 IDs em modelos" "POST" "/veiculos/modelos" $tooManyIds $FrotaBaseUrl
    Invoke-ValidationCheck "Mais de 10000 IDs em tipos" "POST" "/veiculos/tipos" $tooManyIds $FrotaBaseUrl

    # Identificadores vazios são ignorados pelos serviços se o limite do lote regredir.
    $motoristaSemCpf = '{"cpf":""}'
    $tooManyMotoristas = "[" + (($motoristaSemCpf + ",") * 10000) + $motoristaSemCpf + "]"
    Invoke-ValidationCheck "Mais de 10000 itens em lote de motoristas" "POST" "/motoristas/lote" $tooManyMotoristas $FrotaBaseUrl

    $agregadoSemDocumento = '{"documento":"","nome":"Teste"}'
    $tooManyAgregados = "[" + (($agregadoSemDocumento + ",") * 10000) + $agregadoSemDocumento + "]"
    Invoke-ValidationCheck "Mais de 10000 itens em lote de agregados" "POST" "/agregados/lote" $tooManyAgregados $FrotaBaseUrl

    $veiculoSemPlaca = '{"placa":""}'
    $tooManyVeiculos = "[" + (($veiculoSemPlaca + ",") * 10000) + $veiculoSemPlaca + "]"
    Invoke-ValidationCheck "Mais de 10000 itens em lote de veículos" "POST" "/veiculos/lote" $tooManyVeiculos $FrotaBaseUrl

    Invoke-ValidationCheck "Mês de referência inválido no ranking" "GET" "/ranking/mensal?mesReferencia=2026-13" "" $OperacoesBaseUrl "mesReferencia"
    Invoke-ValidationCheck "Ordenação inválida no ranking" "GET" "/ranking/mensal?mesReferencia=2026-01&ordenarPor=campo-invalido" "" $OperacoesBaseUrl "ordenarPor"
    Invoke-ValidationCheck "Direção inválida no ranking" "GET" "/ranking/mensal?mesReferencia=2026-01&direcao=ascendente" "" $OperacoesBaseUrl "direcao"
    Invoke-ValidationCheck "Limite acima do máximo do ranking" "GET" "/dashboard/ranking-motoristas?mesReferencia=2026-01&limite=51" "" $OperacoesBaseUrl "limite"
    Invoke-ValidationCheck "Tamanho de página acima do máximo" "GET" "/dashboard/motoristas?mesReferencia=2026-01&size=101" "" $OperacoesBaseUrl "size"

    Invoke-ValidationCheck "Cadastro sem campos obrigatórios" "POST" "/usuarios" '{}' $UsuariosBaseUrl "nome"
    Invoke-ValidationCheck "Login sem credenciais" "POST" "/auth/login" '{}' $UsuariosBaseUrl "email"
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
