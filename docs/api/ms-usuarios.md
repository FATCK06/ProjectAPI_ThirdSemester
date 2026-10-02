# ms-usuarios

Autenticação, emissão de JWT e cadastro de usuários. Porta interna **8081**, dono da tabela `usuarios`.

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/api/auth/login` | pública | Autentica e devolve o token JWT |
| `POST` | `/api/usuarios` | pública (provisório) | Cadastra um usuário |

---

## `POST /api/auth/login`

Autentica o usuário por e-mail e senha.

**Body:**

```json
{
  "email": "gestor@newelog.com",
  "senha": "minhasenha"
}
```

**Resposta `200`:**

```json
{
  "sucesso": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "nome": "Maria Souza",
  "perfil": "Gestor"
}
```

O token carrega as claims `sub` (e-mail), `nome`, `perfil`, `iss` (`ms-usuarios`) e `exp` (2 horas).

**Erros:**

| Status | Corpo |
|---|---|
| `401` | `{ "sucesso": false, "mensagem": "E-mail ou senha incorretos." }` |
| `403` | `{ "sucesso": false, "mensagem": "Usuário inativo. Contate o adminstrador." }` |

---

## `POST /api/usuarios`

Cadastra um novo usuário. A senha chega em texto e é gravada com **BCrypt**, então criar usuário direto no banco não funciona: o login compara hashes.

> **Atenção:** a rota está aberta provisoriamente para permitir criar o primeiro Administrador. Ela deve ser restrita ao perfil `Administrador` assim que ele existir.

**Body:**

```json
{
  "nome": "Maria Souza",
  "email": "maria@newelog.com",
  "senha": "1234",
  "perfilAcesso": "Gestor"
}
```

| Campo | Regra |
|---|---|
| `nome` | obrigatório |
| `email` | obrigatório, único, gravado em minúsculas |
| `senha` | obrigatório, mínimo de 4 caracteres |
| `perfilAcesso` | `Operador`, `Gestor` ou `Administrador` (maiúsculas e minúsculas tanto faz) |

**Resposta `201`:**

```json
{
  "idUsuario": "3f1c2a8e-5b7d-4e9a-9c1f-2d6b8a0e4f11",
  "nome": "Maria Souza",
  "email": "maria@newelog.com",
  "perfilAcesso": "Gestor",
  "statusAtivo": true
}
```

A senha (nem o hash dela) nunca aparece na resposta.

**Erros:**

| Status | Exemplo de corpo |
|---|---|
| `400` | `{ "erro": "Campo obrigatorio: email" }` |
| `400` | `{ "erro": "A senha precisa de ao menos 4 caracteres" }` |
| `400` | `{ "erro": "Perfil invalido: Chefe. Use um destes: Operador, Gestor, Administrador" }` |
| `409` | `{ "erro": "Ja existe usuario com o e-mail maria@newelog.com" }` |
