# ms-frota

Cadastro e consulta de motoristas, veículos e agregados. Porta interna **8082**, dono das tabelas `motoristas`, `veiculos`, `agregados`, `tipo_veiculos`, `categorias_veiculo`, `cidades` e `estados`.

Todas as rotas exigem `Authorization: Bearer <token>` quando chamadas pelo gateway.

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/motoristas/{cpf}` | Busca motorista por CPF |
| `GET` | `/api/motoristas/id/{id}` | Busca ficha do motorista por id, incluindo nome da cidade |
| `POST` | `/api/motoristas/resumos` | Nome e CPF de vários motoristas por id |
| `POST` | `/api/motoristas/lote` | Upsert em lote por CPF |
| `GET` | `/api/veiculos/{placa}` | Busca veículo por placa |
| `POST` | `/api/veiculos/placas` | Placa de vários veículos por id |
| `POST` | `/api/veiculos/modelos` | Modelo de vários veículos por id |
| `GET` | `/api/veiculos/tipos` | Os tipos crus da planilha de veículos |
| `POST` | `/api/veiculos/tipos` | Categoria de vários veículos por id |
| `GET` | `/api/veiculos/categorias` | As sete categorias de análise |
| `POST` | `/api/veiculos/classificacao` | Placa, tipo cru e categoria de vários veículos por id |
| `POST` | `/api/veiculos/lote` | Upsert em lote por placa |
| `GET` | `/api/agregados/{documento}` | Busca agregado por CPF/CNPJ |
| `POST` | `/api/agregados/lote` | Upsert em lote por documento |

> As rotas `/lote`, `/resumos`, `/placas`, `/modelos`, `POST /tipos` e `/classificacao` existem para uso **entre serviços**: o ms-operacoes as chama durante a importação e no dashboard. Um manifesto traz centenas de linhas, e uma chamada HTTP por motorista seria inviável.

---

## Motoristas

### `GET /api/motoristas/{cpf}`

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `cpf` | path | CPF com 11 dígitos, só números |

**Resposta `200`:**

```json
{
  "idMotorista": "a1b2c3d4-0000-4000-8000-000000000001",
  "nome": "JOAO DA SILVA",
  "cpf": "12345678901",
  "pis": "12345678901",
  "dataNascimento": "1985-03-12",
  "cep": "12220000",
  "logradouro": "Rua das Flores",
  "numero": "100",
  "bairro": "Centro",
  "idCidade": 3549904,
  "criadoEm": "2026-09-20T14:31:00"
}
```

**Erros:** `404` se o CPF não existir.

### `GET /api/motoristas/id/{id}`

Busca os dados cadastrais do motorista pelo UUID. A resposta inclui `idCidade` e `nomeCidade`; o nome vem vazio quando o motorista não tem cidade cadastrada.

**Erros:** `404` se o id não existir.

### `POST /api/motoristas/resumos`

Recebe uma lista de ids e devolve nome e CPF de cada um.

**Body:**

```json
["a1b2c3d4-0000-4000-8000-000000000001", "a1b2c3d4-0000-4000-8000-000000000002"]
```

**Resposta `200`:**

```json
[
  { "id": "a1b2c3d4-0000-4000-8000-000000000001", "nome": "JOAO DA SILVA", "cpf": "12345678901" }
]
```

### `POST /api/motoristas/lote`

Cria ou atualiza motoristas pelo CPF. Devolve o mapa `cpf → id` de **todos** os CPFs recebidos, tanto os criados agora quanto os que já existiam. Itens sem CPF são ignorados, sem derrubar o lote.

**Body:**

```json
[
  {
    "cpf": "12345678901",
    "nome": "JOAO DA SILVA",
    "pis": "12345678901",
    "dataNascimento": "1985-03-12",
    "endereco": "Rua das Flores, 100",
    "cep": "12220000",
    "bairro": "Centro",
    "cidade": "São José dos Campos",
    "estado": "SP"
  }
]
```

`endereco`, `cidade` e `estado` chegam como texto livre do CSV. O ms-frota separa logradouro e número e resolve a cidade na tabela `cidades`.

**Resposta `200`:**

```json
{ "12345678901": "a1b2c3d4-0000-4000-8000-000000000001" }
```

---

## Veículos

### `GET /api/veiculos/{placa}`

**Resposta `200`:**

```json
{
  "idVeiculo": "b1b2c3d4-0000-4000-8000-000000000010",
  "placa": "ABC1D23",
  "marca": "Volvo",
  "modelo": "FH 540",
  "numeroFrota": "F-012",
  "capacidade": 27000.00,
  "nomeAgregado": "TRANSPORTES EXEMPLO LTDA",
  "statusVeiculo": "ATIVO",
  "criadoEm": "2026-09-20T14:31:00"
}
```

**Erros:** `404` se a placa não existir.

### `POST /api/veiculos/placas`

**Body:** lista de ids de veículos.
**Resposta `200`:** mapa `id → placa`.

```json
{ "b1b2c3d4-0000-4000-8000-000000000010": "ABC1D23" }
```

### `POST /api/veiculos/modelos`

**Body:** lista de ids de veículos.
**Resposta `200`:** mapa `id → modelo`.

```json
{ "b1b2c3d4-0000-4000-8000-000000000010": "FH 540" }
```

### `POST /api/veiculos/lote`

Cria ou atualiza veículos pela placa. O manifesto traz só placa, capacidade e nome do agregado, então o cadastro nasce mínimo e é completado depois.

**Body:**

```json
[
  { "placa": "ABC1D23", "capacidade": 27000.00, "nomeAgregado": "TRANSPORTES EXEMPLO LTDA" }
]
```

**Resposta `200`:** mapa `placa → id`.

```json
{ "ABC1D23": "b1b2c3d4-0000-4000-8000-000000000010" }
```

### Tipo e categoria de veículo

Dois níveis:

- **Tipo**: o texto da planilha de veículos do sistema de origem (`FIORINO`, `Truck ou pesado`, `Cavalo mecânico`...), em `tipo_veiculos`. A importação de veículos só aceita tipo cadastrado ali.
- **Categoria**: uma das sete categorias que o cliente usa na análise (Fiorino, Van, VUC, 3/4, Toco, Truck, Carreta), em `categorias_veiculo`. Cada tipo aponta para uma categoria pela coluna `tipo_veiculos.id_categoria`, que é a tabela de-para. Classificar um tipo novo é um `INSERT` em `tipo_veiculos` com a categoria, sem mexer em código.

Onde o sistema mostra "tipo de veículo" (listagem, ranking, indicadores, exportação), o valor é a **categoria**, ou um destes rótulos:

| Rótulo | Quando |
|---|---|
| `Não identificado` | o veículo não tem tipo: só veio por manifesto, nunca pela planilha de veículos |
| `Não classificado` | o tipo existe mas não aponta para nenhuma categoria (hoje: Automóveis, Motocicletas e semi-reboques) |

### `GET /api/veiculos/tipos`

Os tipos crus cadastrados, em ordem alfabética.

```json
[ { "idTipo": 3, "descricao": "3/4" }, { "idTipo": 1, "descricao": "FIORINO" } ]
```

### `POST /api/veiculos/tipos`

**Body:** lista de ids de veículos.
**Resposta `200`:** mapa `id → categoria` (ou um dos rótulos acima). Ids que não existem ficam de fora.

```json
{
  "b1b2c3d4-0000-4000-8000-000000000010": "Truck",
  "b1b2c3d4-0000-4000-8000-000000000011": "Não identificado"
}
```

### `GET /api/veiculos/categorias`

As sete categorias, do menor para o maior porte. Usado pelo filtro "Tipo de veículo" do front.

```json
[
  { "idCategoria": 1, "descricao": "Fiorino" },
  { "idCategoria": 2, "descricao": "Van" }
]
```

### `POST /api/veiculos/classificacao`

**Body:** lista de ids de veículos.
**Resposta `200`:** placa, tipo cru e categoria de cada veículo encontrado, **sem rótulo**: `tipo` vem nulo quando o veículo nunca veio na planilha, e `categoria` vem nula quando não há tipo ou o tipo não tem categoria. Usado pelo resultado do tratamento no ms-operacoes.

```json
[
  { "idVeiculo": "b1b2c3d4-0000-4000-8000-000000000010", "placa": "ABC1D23", "tipo": "Bi-Truck ou pesado", "categoria": "Truck" },
  { "idVeiculo": "b1b2c3d4-0000-4000-8000-000000000012", "placa": "XYZ9K87", "tipo": "Automóveis", "categoria": null }
]
```

---

## Agregados

### `GET /api/agregados/{documento}`

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `documento` | path | CPF (11 dígitos) ou CNPJ (14 dígitos), só números |

**Resposta `200`:**

```json
{
  "idAgregado": "c1b2c3d4-0000-4000-8000-000000000020",
  "documento": "12345678000199",
  "tipoPessoa": "PJ",
  "nome": "TRANSPORTES EXEMPLO LTDA",
  "pis": null,
  "regimeFiscal": "Simples Nacional",
  "criadoEm": "2026-09-20T14:31:00"
}
```

`tipoPessoa` é calculado pelo tamanho do documento: `PF` para CPF (11 dígitos) e `PJ` para CNPJ (14 dígitos).

**Erros:** `404` se o documento não existir.

### `POST /api/agregados/lote`

Cria ou atualiza agregados pelo documento.

**Body:**

```json
[
  { "documento": "12345678000199", "nome": "TRANSPORTES EXEMPLO LTDA", "pis": null, "regimeFiscal": "Simples Nacional" }
]
```

**Resposta `200`:** mapa `documento → id`.

```json
{ "12345678000199": "c1b2c3d4-0000-4000-8000-000000000020" }
```
