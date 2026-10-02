# Rotas da API

Documentação dos endpoints expostos pelo sistema. Toda chamada passa pelo **API Gateway**; o frontend e qualquer cliente externo nunca falam direto com os microsserviços.

- [ms-usuarios](./ms-usuarios.md): autenticação e cadastro de usuários
- [ms-frota](./ms-frota.md): motoristas, veículos e agregados
- [ms-operacoes](./ms-operacoes.md): importação de manifestos, viagens e dashboard
- [Gateway](#rotas-do-próprio-gateway): status dos serviços

## Endereço base

| Ambiente | URL base |
|---|---|
| Local | `http://localhost:8080/api` |

O frontend lê a URL da variável `VITE_API_URL`. Sem ela, usa o valor acima.

## Roteamento no gateway

| Prefixo | Microsserviço | Porta interna |
|---|---|---|
| `/api/auth/**`, `/api/usuarios/**` | ms-usuarios | 8081 |
| `/api/motoristas/**`, `/api/veiculos/**`, `/api/agregados/**` | ms-frota | 8082 |
| `/api/importacao/**`, `/api/viagens/**`, `/api/dashboard/**` | ms-operacoes | 8083 |
| `/api/status` | o próprio gateway | 8080 |

## Autenticação

A API usa **JWT** (HMAC-256, emissor `ms-usuarios`, validade de **2 horas**).

1. Faça login em `POST /api/auth/login` e guarde o `token` da resposta.
2. Envie o token em todas as outras chamadas:

```
Authorization: Bearer <token>
```

O gateway valida o token antes de rotear. Com o token válido, ele injeta dois headers na requisição que segue para o microsserviço:

| Header | Conteúdo |
|---|---|
| `X-User-Name` | e-mail do usuário (subject do token) |
| `X-User-Role` | perfil de acesso (`Operador`, `Gestor` ou `Administrador`) |

> Qualquer `X-User-Name` ou `X-User-Role` enviado pelo cliente é **descartado** pelo gateway. Não adianta forjar esses headers.

### Rotas públicas (sem token)

| Rota | Motivo |
|---|---|
| `POST /api/auth/login` | login |
| `POST /api/usuarios` | cadastro, aberto provisoriamente para criar o primeiro Administrador |

## Perfis de acesso

| Perfil | Descrição |
|---|---|
| `Operador` | uso operacional (importação e consultas) |
| `Gestor` | acompanhamento gerencial (dashboard e indicadores) |
| `Administrador` | tudo acima, mais cadastro de usuários e status dos serviços |

## Formato de erro

Os erros tratados pela aplicação retornam JSON com a chave `erro`:

```json
{ "erro": "Token invalido ou expirado" }
```

| Status | Quando acontece |
|---|---|
| `400 Bad Request` | parâmetro inválido (ex.: `mesReferencia` fora do formato `AAAA-MM`) ou campo obrigatório ausente |
| `401 Unauthorized` | token ausente, inválido ou expirado; ou e-mail/senha incorretos no login |
| `403 Forbidden` | usuário inativo, ou perfil sem permissão para a rota |
| `404 Not Found` | recurso não encontrado |
| `409 Conflict` | conflito de estado (e-mail já cadastrado, importação já executada) |
| `422 Unprocessable Entity` | falha ao gravar a importação |

Alguns `400` e `404` voltam **sem corpo**. Nesses casos, o status já basta.

## Rotas do próprio gateway

### `GET /api/status`

Consulta o `/actuator/health` de cada microsserviço (timeout de 3 s). O health já leva em conta a conexão com o banco, então um serviço que responde na porta mas não alcança o banco aparece como `DOWN`.

**Perfil exigido:** `Administrador`. Outros perfis recebem `403`.

**Resposta `200`:**

```json
{
  "geral": "DOWN",
  "servicos": [
    { "servico": "ms-usuarios",  "status": "UP",   "tempoMs": 42 },
    { "servico": "ms-frota",     "status": "UP",   "tempoMs": 38 },
    { "servico": "ms-operacoes", "status": "DOWN", "tempoMs": 3001, "detalhe": "I/O error on GET request ..." }
  ]
}
```

## Limites

| Item | Limite |
|---|---|
| Tamanho do arquivo CSV | 10 MB (requisição até 11 MB) |
| Validade do token | 2 horas |
| Página da validação de importação | 1 a 200 linhas |
| Ranking de motoristas | 1 a 50 posições |
