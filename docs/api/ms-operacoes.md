# ms-operacoes

Importação do CSV de manifestos, consulta de viagens e indicadores do dashboard. Porta interna **8083**, dono das tabelas `importacoes`, `viagens`, `viagem_custos`, `viagem_destinos`, `tipos_custo` e `controle_disponibilidade`.

Todas as rotas exigem `Authorization: Bearer <token>`.

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/importacao/arquivos` | Etapa 1: recebe o CSV |
| `POST` | `/api/importacao/arquivos/{id}/validar` | Etapa 2: valida sem gravar |
| `POST` | `/api/importacao/arquivos/{id}/executar` | Etapa 3: grava as viagens |
| `GET` | `/api/importacao/arquivos` | Lista as importações |
| `GET` | `/api/importacao/arquivos/resumo` | Quantidade de importações por status |
| `GET` | `/api/importacao/arquivos/{id}` | Detalhe de uma importação |
| `GET` | `/api/importacao/arquivos/{id}/conteudo` | Baixa o CSV original |
| `GET` | `/api/importacao/arquivos/{id}/viagens` | Viagens gravadas por uma importação |
| `GET` | `/api/importacao/arquivos/{id}/motoristas` | Ids dos motoristas de uma importação |
| `GET` | `/api/viagens` | Viagens de um mês |
| `GET` | `/api/dashboard/indicadores` | Indicadores do mês e por motorista |
| `GET` | `/api/dashboard/indicadores-por-modelo` | Indicadores por modelo de veículo |
| `GET` | `/api/dashboard/ranking-motoristas` | Top N motoristas por número de viagens |
| `GET` | `/api/dashboard/motoristas/situacao` | Situação (disponível/indisponível) de cada motorista no mês |

---

## Importação de manifestos

A importação é feita **em etapas**. Enviar o arquivo não grava nada: as viagens só entram no banco no `/executar`. Antes disso, o usuário pode validar e revisar quantas vezes quiser.

```
POST /arquivos  ──▶  POST /{id}/validar  ──▶  POST /{id}/executar
 (AGUARDANDO)        (sem efeito no banco)      (CONCLUIDO)
```

### Status de uma importação

| Status | Significado |
|---|---|
| `AGUARDANDO` | arquivo recebido e guardado; nada foi lido nem gravado |
| `VALIDADO` | arquivo lido e conferido; ainda nada gravado |
| `EM_PROCESSAMENTO` | gravação em andamento |
| `CONCLUIDO` | viagens gravadas |
| `ERRO` | falha na leitura ou na gravação |

### `POST /api/importacao/arquivos`

Recebe o arquivo e devolve o id da importação. O arquivo não é lido aqui.

**Content-Type:** `multipart/form-data`

| Campo | Tipo | Regra |
|---|---|---|
| `arquivo` | file | obrigatório, extensão `.csv`, até 10 MB |

```bash
curl -X POST http://localhost:8080/api/importacao/arquivos \
  -H "Authorization: Bearer $TOKEN" \
  -F "arquivo=@manifestos_3tri.csv"
```

**Resposta `201`:**

```json
{ "id": 42, "arquivoNome": "manifestos_3tri.csv", "status": "AGUARDANDO" }
```

**Erros:**

| Status | Corpo |
|---|---|
| `400` | `{ "erro": "Arquivo vazio" }` |
| `400` | `{ "erro": "Formato nao aceito. Envie um arquivo .csv" }` |

> O mês de referência não é informado no envio. A planilha da Newelog é trimestral, então o mês de cada viagem vem da coluna **Data** da própria linha.

### `POST /api/importacao/arquivos/{id}/validar`

Lê o arquivo e devolve as colunas encontradas, as linhas com problema (paginadas, erros antes de pendências) e uma amostra das linhas válidas. **Não grava nada** e pode ser chamado quantas vezes for preciso.

| Parâmetro | Tipo | Padrão | Regra |
|---|---|---|---|
| `id` | path | | id da importação |
| `pagina` | query | `0` | ≥ 0 |
| `tamanho` | query | `50` | 1 a 200 |
| `severidade` | query | todas | `ERRO` ou `PENDENCIA`. Filtra só a lista; o resumo sempre conta o arquivo inteiro |

**Resposta `200`:**

```json
{
  "importacaoId": 42,
  "arquivoNome": "manifestos_3tri.csv",
  "totalLinhas": 1024,
  "linhasValidas": 1019,
  "linhasInvalidas": 2,
  "linhasComPendencia": 3,
  "colunasDetectadas": ["Manifesto", "Filial", "Data", "Motorista", "CPF", "..."],
  "mapeamento": [
    { "campoSistema": "MANIFESTO", "obrigatoria": true,  "encontrada": true,  "colunaArquivo": "Manifesto" },
    { "campoSistema": "PIS",       "obrigatoria": false, "encontrada": false, "colunaArquivo": null }
  ],
  "pagina": { "numero": 0, "tamanho": 50, "totalElementos": 5 },
  "problemas": [
    {
      "numeroLinha": 17,
      "valores": { "CPF": "1234567890", "Data": "12/07/2026" },
      "problemas": [
        {
          "campo": "CPF",
          "coluna": "CPF",
          "severidade": "ERRO",
          "estado": "NAO_CONVERTE",
          "valorEncontrado": "1234567890",
          "valorEsperado": "11 digitos",
          "mensagem": "A coluna 'CPF' na linha 17 nao esta no formato esperado: Esperado 11 digitos, veio com 10"
        }
      ]
    }
  ],
  "amostra": [
    {
      "numeroLinha": 2,
      "manifesto": 900123,
      "data": "2026-07-01",
      "mesReferencia": "2026-07",
      "motorista": "JOAO DA SILVA",
      "cpf": "12345678901",
      "agregado": "TRANSPORTES EXEMPLO LTDA",
      "veiculo": "ABC1D23",
      "destino": "Campinas",
      "valorFrete": 1850.00
    }
  ]
}
```

**Como a severidade é decidida:**

| Nível da coluna | Célula vazia | Valor em formato errado |
|---|---|---|
| Obrigatória (`Manifesto`, `Data`, `CPF`, `Veículo`) | `ERRO` | `ERRO` |
| Importante (ex.: `Motorista`, `Agregado`, `Destino`, `Valor Frete`) | `PENDENCIA` | `PENDENCIA` |
| Opcional | aceita | `PENDENCIA` |

Estados possíveis de uma célula: `OK`, `VAZIO`, `NAO_CONVERTE` e `AUSENTE_COMO_ZERO` (colunas em que o CSV usa `0,00` para dizer "não informado").

O arquivo só pode ser gravado quando `linhasInvalidas = 0`, existe ao menos uma linha válida e nenhuma coluna obrigatória está faltando (todo item de `mapeamento` com `obrigatoria: true` tem `encontrada: true`). Linhas só com pendência não bloqueiam.

**Erros:** `404` se a importação não existir; `400` para paginação inválida ou `severidade=OK`.

### `POST /api/importacao/arquivos/{id}/executar`

Grava as viagens. É o **único** endpoint do fluxo que escreve no banco. Motoristas, veículos e agregados são criados ou atualizados no ms-frota pelos endpoints de lote. Manifestos que já existem no banco são ignorados, então reimportar o mesmo arquivo não duplica dados.

**Resposta `200`:**

```json
{ "id": 42, "status": "CONCLUIDO", "linhasGravadas": 1019 }
```

**Erros:**

| Status | Corpo |
|---|---|
| `404` | sem corpo, se a importação não existir |
| `409` | `{ "erro": "Esta importacao ja foi executada" }` |
| `422` | `{ "erro": "<motivo da falha>" }` |

### `GET /api/importacao/arquivos`

Lista todas as importações.

**Resposta `200`:**

```json
[
  {
    "id": 42,
    "arquivoNome": "manifestos_3tri.csv",
    "dataImportacao": "2026-09-28T10:15:00",
    "status": "CONCLUIDO",
    "usuarioResponsavel": "operador@newelog.com",
    "totalLinhas": 1024,
    "linhasValidas": 1019,
    "linhasInvalidas": 0,
    "linhasGravadas": 1019
  }
]
```

O conteúdo binário do arquivo não aparece. Para obtê-lo, use `/{id}/conteudo`.

### `GET /api/importacao/arquivos/resumo`

**Resposta `200`:**

```json
{
  "totalImportacoes": 7,
  "porStatus": { "AGUARDANDO": 1, "VALIDADO": 0, "EM_PROCESSAMENTO": 0, "CONCLUIDO": 5, "ERRO": 1 }
}
```

### `GET /api/importacao/arquivos/{id}`

Mesmo formato de um item da listagem. `404` se não existir.

### `GET /api/importacao/arquivos/{id}/conteudo`

Baixa o CSV original como anexo (`Content-Disposition: attachment`). `404` se não existir.

### `GET /api/importacao/arquivos/{id}/viagens`

Viagens gravadas por essa importação, no mesmo formato de [`GET /api/viagens`](#get-apiviagens). `404` se a importação não existir.

### `GET /api/importacao/arquivos/{id}/motoristas`

Ids (UUID) dos motoristas presentes na importação. Nome e CPF ficam no ms-frota: consulte [`POST /api/motoristas/resumos`](./ms-frota.md#post-apimotoristasresumos) com esses ids.

```json
["a1b2c3d4-0000-4000-8000-000000000001", "a1b2c3d4-0000-4000-8000-000000000002"]
```

---

## Viagens

### `GET /api/viagens`

| Parâmetro | Tipo | Regra |
|---|---|---|
| `mesReferencia` | query | obrigatório, formato `AAAA-MM` |

```
GET /api/viagens?mesReferencia=2026-09
```

**Resposta `200`** (campos principais):

```json
[
  {
    "idViagem": 1501,
    "idManifesto": 900123,
    "idMotorista": "a1b2c3d4-0000-4000-8000-000000000001",
    "idVeiculo": "b1b2c3d4-0000-4000-8000-000000000010",
    "idAgregado": "c1b2c3d4-0000-4000-8000-000000000020",
    "importacaoId": 42,
    "dataViagem": "2026-09-03",
    "mesReferencia": "2026-09",
    "filial": "SJC",
    "destino": "Campinas",
    "kmSaida": 120300,
    "kmChegada": 120480,
    "kgReal": 8200.00,
    "kgTaxado": 8500.00,
    "servicos": 12,
    "servicosFinalizados": 12,
    "nfs": 14,
    "valorFrete": 1850.00,
    "valorNf": 95000.00,
    "totalDespesas": 420.00,
    "saldoAPagar": 1430.00,
    "status": "Finalizado",
    "usuarioManifesto": "fulano"
  }
]
```

A entidade também traz `reboque1..3`, `m3`, `coletas`, `entregas`, `despachos`, `retiradas`, `coletasReversa`, `percentualEfetividade`, `percentualAprovVeiculo`, `valorFretes`, `saldoDespesas`, `classificacao`, `observacoesOperacionais` e `criadoEm`.

**Erros:** `400` se `mesReferencia` estiver fora do formato.

---

## Dashboard

Valores monetários em R$; percentuais de 0 a 100. Divisões por zero retornam `0`, assim um motorista sem viagem aparece zerado em vez de derrubar a tela.

### `GET /api/dashboard/indicadores`

Totais do mês e uma linha por motorista, ordenada por rentabilidade.

| Parâmetro | Tipo | Regra |
|---|---|---|
| `mesReferencia` | query | obrigatório, `AAAA-MM` com mês de 01 a 12 |

**Resposta `200`:**

```json
{
  "mesReferencia": "2026-09",
  "diasNoMes": 30,
  "diasDisponiveis": 26,
  "totais": {
    "motoristas": 48,
    "numeroViagens": 612,
    "utilizacaoMedia": 71.40,
    "valorFrete": 1203450.00,
    "custoTotal": 845000.00,
    "rentabilidade": 358450.00,
    "rentabilidadeMediaViagem": 585.70,
    "margem": 29.78
  },
  "motoristas": [
    {
      "motoristaId": "a1b2c3d4-0000-4000-8000-000000000001",
      "nome": "JOAO DA SILVA",
      "numeroViagens": 22,
      "diasOperacao": 20,
      "diasDisponiveis": 26,
      "utilizacao": 76.92,
      "disponibilidade": 86.67,
      "valorFrete": 41200.00,
      "custoTotal": 27800.00,
      "rentabilidade": 13400.00,
      "rentabilidadeMediaViagem": 609.09,
      "margem": 32.52
    }
  ]
}
```

**Fórmulas:**

| Indicador | Cálculo |
|---|---|
| Utilização | dias em operação ÷ dias disponíveis × 100 |
| Disponibilidade | dias disponíveis ÷ dias do mês × 100 |
| Rentabilidade | valor do frete − custo total |
| Rentabilidade média por viagem | rentabilidade ÷ número de viagens |
| Margem | rentabilidade ÷ valor do frete × 100 |
| Utilização média (totais) | soma dos dias em operação ÷ (dias disponíveis × motoristas) × 100 |

**Erros:** `400` se `mesReferencia` for inválido.

### `GET /api/dashboard/indicadores-por-modelo`

Os mesmos indicadores agrupados por modelo de veículo.

| Parâmetro | Tipo | Regra |
|---|---|---|
| `mesReferencia` | query | obrigatório, `AAAA-MM` |

**Resposta `200`:**

```json
[
  {
    "modelo": "FH 540",
    "numeroVeiculos": 12,
    "numeroViagens": 180,
    "diasOperacao": 240,
    "utilizacao": 76.90,
    "valorFrete": 402000.00,
    "custoTotal": 280000.00,
    "rentabilidade": 122000.00,
    "margem": 30.35
  }
]
```

### `GET /api/dashboard/ranking-motoristas`

Os N motoristas com mais viagens no mês, com o tipo do veículo usado na viagem mais longa (km de chegada − km de saída; em empate, a mais recente).

| Parâmetro | Tipo | Padrão | Regra |
|---|---|---|---|
| `mesReferencia` | query | | obrigatório, `AAAA-MM` |
| `limite` | query | `5` | 1 a 50 |

```
GET /api/dashboard/ranking-motoristas?mesReferencia=2026-09&limite=5
```

**Resposta `200`:**

```json
[
  {
    "posicao": 1,
    "motoristaId": "a1b2c3d4-0000-4000-8000-000000000001",
    "nome": "JOAO DA SILVA",
    "cpf": "12345678901",
    "totalViagens": 22,
    "tipoVeiculo": "FH 540",
    "distanciaMaximaKm": 480
  }
]
```

Um mês sem viagens devolve `[]`. **Erros:** `400` para parâmetros inválidos.

### `GET /api/dashboard/motoristas/situacao`

Situação de cada motorista no mês, a partir da tabela `controle_disponibilidade`. A importação (fim do `/executar`) grava os **dias disponíveis e em operação** de cada motorista; a consulta só lê essa tabela, sem tocar em `viagens`.

A tabela não tem colunas para utilização, faixa e situação (o schema do Supabase é fixo), então esses três campos são **calculados na consulta** a partir dos dias gravados. Por isso, mudar os cortes vale na hora, sem reimportar.

**Escopo:** só motoristas com viagem no mês. Quem não rodou nenhum dia não aparece.

| Parâmetro | Tipo | Regra |
|---|---|---|
| `mesReferencia` | query | obrigatório, `AAAA-MM` com mês de 01 a 12 |
| `situacao` | query | opcional: `DISPONIVEL`, `INDISPONIVEL` ou `SEM_DIAS_DISPONIVEIS` |

```
GET /api/dashboard/motoristas/situacao?mesReferencia=2026-09&situacao=DISPONIVEL
```

**Resposta `200`:**

```json
[
  {
    "motoristaId": "a1b2c3d4-0000-4000-8000-000000000001",
    "nome": "JOAO DA SILVA",
    "diasDisponiveis": 30,
    "diasOperacao": 9,
    "utilizacao": 30.00,
    "faixaUtilizacao": "BAIXA",
    "situacao": "DISPONIVEL",
    "aviso": null
  }
]
```

Ordem: `DISPONIVEL` primeiro, depois menor utilização, depois nome. Sem resultado devolve `[]` (a mensagem "Nenhum motorista disponível com os filtros aplicados." é do front).

**Regras do tratamento:**

| Item | Regra |
|---|---|
| Utilização | dias em operação ÷ dias disponíveis × 100, 2 casas (HALF_UP). Pode passar de 100 |
| Dias em operação | dias distintos com viagem no mês, somando todas as importações |
| Dias disponíveis | mesmo valor de `/indicadores` (hoje, todos os dias do mês — PENDENTE do cliente) |
| `BAIXA` | utilização < corte baixo → `DISPONIVEL` |
| `INTERMEDIARIA` | corte baixo ≤ utilização < corte alto → `DISPONIVEL` |
| `ALTA` | utilização ≥ corte alto → `INDISPONIVEL` |
| Dias disponíveis ≤ 0 | sem faixa, `utilizacao: null`, situação `SEM_DIAS_DISPONIVEIS` e `aviso: "Motorista sem dias disponíveis registrados no período."` |

Os cortes ficam em `application.properties` (`situacao.utilizacao.corte-baixa=40` e `situacao.utilizacao.corte-alta=70`) e são **provisórios — PENDENTE (Newe)**.

A cada importação, os dias dos meses presentes no arquivo são recalculados a partir de todas as viagens do mês no banco. Se o recálculo falhar, a importação continua `CONCLUIDO` (as viagens já foram gravadas) e o erro fica no log; a próxima importação do mês recalcula.

**Erros:** `400` se `mesReferencia` ou `situacao` forem inválidos.
