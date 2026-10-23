#!/usr/bin/env bash

# Precisa de Bash, curl, e Python 3. Rode assim para testar se os serviços estão retornando erros de validação estruturados corretamente:
#   bash backend/ms-frota/testar-validacao.sh
# Override service URLs with FROTA_BASE_URL, OPERACOES_BASE_URL, and USUARIOS_BASE_URL.
set -uo pipefail

FROTA_BASE_URL="${FROTA_BASE_URL:-http://localhost:8082/api}"
OPERACOES_BASE_URL="${OPERACOES_BASE_URL:-http://localhost:8083/api}"
USUARIOS_BASE_URL="${USUARIOS_BASE_URL:-http://localhost:8081/api}"
failures=0

run_check() {
    local name="$1"
    local base_url="$2"
    local method="$3"
    local path="$4"
    local body="$5"
    local expected_field="$6"
    local response_file
    local request_file
    local status
    local curl_status

    response_file="$(mktemp)"
    if [ -n "$body" ]; then
        request_file="$(mktemp)"
        printf '%s' "$body" > "$request_file"
        status="$(curl --silent --show-error --output "$response_file" --write-out '%{http_code}' \
            --request "$method" --header 'Content-Type: application/json' --data-binary "@$request_file" \
            "${base_url%/}${path}")"
        curl_status=$?
        rm -f "$request_file"
    else
        status="$(curl --silent --show-error --output "$response_file" --write-out '%{http_code}' \
            --request "$method" "${base_url%/}${path}")"
        curl_status=$?
    fi

    printf '\nValidação: %s\n' "$name"
    printf 'Método/rota: %s %s%s\n' "$method" "${base_url%/}" "$path"
    printf 'Status HTTP: %s\n' "${status:-sem resposta}"
    printf 'Corpo da resposta: '
    cat "$response_file"
    printf '\n'

    if [ "$curl_status" -ne 0 ]; then
        printf 'Resultado: FAIL - curl terminou com código %s\n' "$curl_status"
        failures=$((failures + 1))
    elif [ "$status" != "400" ]; then
        printf 'Resultado: FAIL - esperado HTTP 400\n'
        failures=$((failures + 1))
    elif ! python3 - "$response_file" "$expected_field" <<'PY'
import json
import re
import sys

with open(sys.argv[1], encoding="utf-8") as response_file:
    raw = response_file.read()

try:
    response = json.loads(raw)
except json.JSONDecodeError:
    print("Corpo não é JSON válido")
    sys.exit(1)

if not isinstance(response, dict) or not isinstance(response.get("erro"), str) or not response["erro"].strip():
    print('Corpo deve conter uma chave "erro" não vazia')
    sys.exit(1)

expected_field = sys.argv[2]
if expected_field and (
    not isinstance(response.get("campos"), dict)
    or expected_field not in response["campos"]
):
    print(f'Corpo deve indicar o campo "{expected_field}" em "campos"')
    sys.exit(1)

if re.search(r"at [\w.$]+\(|Exception|StackTrace|org\.springframework|org\.hibernate|java\.sql|jdbc:", raw, re.I):
    print("Resposta parece conter detalhes internos")
    sys.exit(1)
PY
    then
        printf 'Resultado: FAIL - resposta de erro fora do formato esperado\n'
        failures=$((failures + 1))
    else
        printf 'Resultado: PASS - HTTP 400 com erro estruturado\n'
    fi

    rm -f "$response_file"
}

printf 'Testando validações nos serviços\n'

run_check "CPF fora do formato" "$FROTA_BASE_URL" GET "/motoristas/123" "" "cpf"
run_check "Documento fora do formato" "$FROTA_BASE_URL" GET "/agregados/123" "" "documento"
run_check "Placa fora do formato" "$FROTA_BASE_URL" GET "/veiculos/L" "" "placa"
run_check "CPF inválido no lote de motoristas" "$FROTA_BASE_URL" POST "/motoristas/lote" '[{"cpf":"abc"}]' "cpf"

run_check "ID nulo em resumos" "$FROTA_BASE_URL" POST "/motoristas/resumos" '[null]' ""
run_check "ID nulo em placas" "$FROTA_BASE_URL" POST "/veiculos/placas" '[null]' ""
run_check "ID nulo em modelos" "$FROTA_BASE_URL" POST "/veiculos/modelos" '[null]' ""
run_check "ID nulo em tipos" "$FROTA_BASE_URL" POST "/veiculos/tipos" '[null]' ""

too_many_ids="$(python3 -c 'import json; print(json.dumps(["00000000-0000-0000-0000-000000000001"] * 10001))')"
run_check "Mais de 10000 IDs em resumos" "$FROTA_BASE_URL" POST "/motoristas/resumos" "$too_many_ids" ""
run_check "Mais de 10000 IDs em placas" "$FROTA_BASE_URL" POST "/veiculos/placas" "$too_many_ids" ""
run_check "Mais de 10000 IDs em modelos" "$FROTA_BASE_URL" POST "/veiculos/modelos" "$too_many_ids" ""
run_check "Mais de 10000 IDs em tipos" "$FROTA_BASE_URL" POST "/veiculos/tipos" "$too_many_ids" ""

# Blank identifiers are deliberately ignored by the services if the batch-size check regresses.
too_many_motoristas="$(python3 -c 'import json; print(json.dumps([{"cpf":""}] * 10001))')"
run_check "Mais de 10000 itens em lote de motoristas" "$FROTA_BASE_URL" POST "/motoristas/lote" "$too_many_motoristas" ""

too_many_agregados="$(python3 -c 'import json; print(json.dumps([{"documento":"","nome":"Teste"}] * 10001))')"
run_check "Mais de 10000 itens em lote de agregados" "$FROTA_BASE_URL" POST "/agregados/lote" "$too_many_agregados" ""

too_many_veiculos="$(python3 -c 'import json; print(json.dumps([{"placa":""}] * 10001))')"
run_check "Mais de 10000 itens em lote de veículos" "$FROTA_BASE_URL" POST "/veiculos/lote" "$too_many_veiculos" ""

run_check "Mês de referência inválido no ranking" "$OPERACOES_BASE_URL" GET "/ranking/mensal?mesReferencia=2026-13" "" "mesReferencia"
run_check "Ordenação inválida no ranking" "$OPERACOES_BASE_URL" GET "/ranking/mensal?mesReferencia=2026-01&ordenarPor=campo-invalido" "" "ordenarPor"
run_check "Direção inválida no ranking" "$OPERACOES_BASE_URL" GET "/ranking/mensal?mesReferencia=2026-01&direcao=ascendente" "" "direcao"
run_check "Limite acima do máximo do ranking" "$OPERACOES_BASE_URL" GET "/dashboard/ranking-motoristas?mesReferencia=2026-01&limite=51" "" "limite"
run_check "Tamanho de página acima do máximo" "$OPERACOES_BASE_URL" GET "/dashboard/motoristas?mesReferencia=2026-01&size=101" "" "size"

run_check "Cadastro sem campos obrigatórios" "$USUARIOS_BASE_URL" POST "/usuarios" '{}' "nome"
run_check "Login sem credenciais" "$USUARIOS_BASE_URL" POST "/auth/login" '{}' "email"

if [ "$failures" -gt 0 ]; then
    printf '\n%s teste(s) falharam.\n' "$failures"
    exit 1
fi

printf '\nTodos os testes passaram.\n'
