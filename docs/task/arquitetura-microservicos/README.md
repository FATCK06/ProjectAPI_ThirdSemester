# Arquitetura — Responsabilidade de cada serviço

> **Status:** em implementação
> **Atualizado:** 20/09/2026

Referência rápida de quem é dono do quê. Serve para responder, sem discussão,
a pergunta "onde eu coloco esse código?".

## Visão geral

```
                              ┌──────────────┐      ┌──────────────┐
                         ┌───▶│ ms-usuarios  │─────▶│  usuarios    │
                         │    │    :8081     │      └──────────────┘
┌────────┐   ┌────────┐  │    ├──────────────┤      ┌──────────────┐
│ React  │──▶│Gateway │──┼───▶│  ms-frota    │─────▶│ motoristas   │
│  :5173 │   │ :8080  │  │    │    :8082     │      │ veiculos ... │
└────────┘   └────────┘  │    ├──────────────┤      └──────────────┘
                         │    │ ms-operacoes │      ┌──────────────┐
                         └───▶│    :8083     │─────▶│ viagens ...  │
                              └──────────────┘      └──────────────┘
```

## A regra que vale para tudo

> **Cada tabela tem um dono. Nenhum outro serviço dá SELECT, INSERT ou UPDATE
> nela — o acesso é sempre pela API do dono.**

É essa regra que separa microsserviço de monolito distribuído. Quebrá-la é o
que fez o `ms-veiculos` criar tabelas em cima do schema do time em 20/09/2026.

---

## `api-gateway` — porta 8080

Único endereço que o front conhece. Não tem banco nem regra de negócio.

| Responsabilidade | Detalhe |
|---|---|
| Roteamento | `/api/auth/**` → usuarios · `/api/motoristas/**`, `/api/veiculos/**`, `/api/agregados/**` → frota · `/api/importacao/**`, `/api/viagens/**` → operações |
| CORS | Configurado aqui, em um lugar só |
| Autenticação | Valida o JWT na entrada e repassa perfil/usuário para trás |

> Com o gateway validando o token, os serviços de dentro confiam nos headers
> que ele injeta. É o que torna o `PerfilResolver` (que lê `X-User-Role`)
> seguro — desde que os serviços não fiquem expostos direto para fora.

## `ms-usuarios` — porta 8081

| | |
|---|---|
| **Tabelas** | `usuarios` |
| **Responsabilidade** | Login, emissão e validação de JWT, perfis de acesso |
| **Backlog** | item 7 (níveis de acesso por perfil) |
| **Status** | funcionando |

## `ms-frota` — porta 8082

Quem dirige, quem é contratado, e com que veículo.

| | |
|---|---|
| **Tabelas** | `motoristas`, `veiculos`, `agregados`, `categorias_veiculos`, `cidades`, `estados` |
| **Responsabilidade** | Cadastro e consulta de motoristas, veículos e agregados; upsert em lote usado pela importação; resolver cidade/UF a partir de texto |
| **Backlog** | itens 4 e 5 (listar motoristas agregados, disponibilidade) |
| **Status** | em construção |

**Endpoints previstos**

```
GET  /api/motoristas/{cpf}
POST /api/motoristas/lote        → upsert por CPF, devolve cpf → uuid
GET  /api/agregados/{documento}
POST /api/agregados/lote         → upsert por documento
GET  /api/veiculos/{placa}
POST /api/veiculos/lote          → upsert por placa
```

> Os endpoints `/lote` existem porque um manifesto traz centenas de linhas.
> Uma chamada HTTP por motorista seria inviável.

**Por que `cidades` e `estados` moram aqui e não num serviço próprio:**
são dados de referência (lookup), não um domínio de negócio. Um `ms-cidades`
faria toda tela de motorista precisar de uma chamada extra só para traduzir
`id_cidade` em nome — o anti-padrão *serviço por tabela*.

## `ms-operacoes` — porta 8083

Renomeado de `ms-veiculos`, que era um nome enganoso: o serviço nunca cuidou
de veículos.

| | |
|---|---|
| **Tabelas** | `arquivo_importacao`, `viagens`, `viagem_custos`, `viagem_destinos`, `tipos_custo`, `controle_disponibilidade` |
| **Responsabilidade** | Upload e parsing do CSV de manifestos, gravação das viagens, resumo da importação, indicadores do mês |
| **Backlog** | itens 1, 2, 3, 6, 8, 9 |
| **Status** | em migração |

Na importação, este serviço **não grava motorista nem agregado**: ele chama o
`ms-frota` nos endpoints de lote e recebe os ids de volta.

---

## O que acontece com as chaves estrangeiras

FK dentro do mesmo banco continua existindo normalmente. O que não existe é FK
apontando para tabela de outro banco — o Postgres não consegue criar essa
constraint.

**Continuam (6):**

| FK | Onde |
|---|---|
| `veiculos.id_categoria` → `categorias_veiculos` | frota |
| `veiculos.id_motorista_padrao` → `motoristas` | frota |
| `motoristas.id_cidade` → `cidades` | frota |
| `viagem_custos.id_viagem` → `viagens` | operações |
| `viagem_custos.id_tipo_custo` → `tipos_custo` | operações |
| `viagem_destinos.id_viagem` → `viagens` | operações |

**Deixam de existir (4):**

| FK | Substituída por |
|---|---|
| `viagens.id_motorista` → `motoristas` | coluna uuid + validação via API do frota |
| `viagens.id_veiculo` → `veiculos` | idem |
| `viagens.id_agregado` → `agregados` | idem |
| `viagem_destinos.id_cidade` → `cidades` | `nome_cidade` + `uf` gravados na própria linha |

A coluna e o dado continuam idênticos — some apenas a verificação automática
do banco, que passa a ser feita pela aplicação.

> Para `viagem_destinos`, guardar o nome da cidade não é duplicação preguiçosa:
> viagem é registro histórico, e o destino de abril deve continuar mostrando o
> nome que tinha em abril.

---

## Ordem de implementação

| Etapa | O quê | Status |
|---|---|---|
| 1 | Criar `ms-frota` com as entidades no schema real | ✅ concluída |
| 2 | Renomear `ms-veiculos` → `ms-operacoes`, reescrever `Viagem` | ✅ concluída |
| 3 | Importação passa a chamar o `ms-frota` por HTTP | ✅ concluída |
| 4 | Criar o `api-gateway` | ✅ concluída |
| 5 | Separar os bancos por serviço | pendente — ver `docs/task/docker-ambiente-local/` |

> Nada foi compilado ainda: não há JDK na máquina de desenvolvimento. A
> verificação feita até aqui é estática (assinaturas entre camadas, imports,
> alinhamento posicional dos records).

Até a etapa 5, os serviços compartilham o mesmo banco Supabase. A separação de
**código** acontece antes da separação de **dados**, para não quebrar tudo de
uma vez.

## Dúvida em aberto com o PO

Os itens 4, 5 e 6 do backlog falam em *"motoristas agregados"*. Os dados do
arquivo real sugerem que o parceiro se refere ao **agregado** (193 distintos,
831 registros com CNPJ), não ao motorista (150). Isso muda de qual serviço vem
a tela de ranking. **Confirmar antes da Sprint 2.**
