# Proposta: Ambiente Local de Banco com Docker

> **Status:** proposta, não implementada
> **Relacionado:** adoção do Flyway (versionamento de banco) e separação em
> microsserviços (`ms-usuarios`, `ms-frota`, `ms-operacoes`)

---

## 1. Como está hoje

Todos os desenvolvedores apontam a aplicação para o **mesmo banco Supabase**,
na nuvem:

```
  Igor ────────┐
  Rodolfo ─────┼──── internet ────▶  Supabase (1 Postgres compartilhado)
  Caio ────────┤
  Guilherme ───┘
```

Consequência: qualquer alteração que uma pessoa faz no banco é vista
imediatamente por todas as outras — inclusive as alterações erradas.

## 2. O problema (caso real do projeto)

Em 20/09/2026, ao revisar o banco, encontramos **tabelas duplicadas**:

| Conceito | Tabela desenhada pelo time | Tabela que apareceu sozinha |
|---|---|---|
| Motorista | `motoristas` (uuid, endereço normalizado) | `motorista` (bigint, endereço em texto) |
| Viagem | `viagens` (enxuta, custos normalizados) | `viagem` (~50 colunas achatadas) |

**Causa:** o `ms-veiculos` estava com `spring.jpa.hibernate.ddl-auto=update`.
Essa configuração faz o Hibernate criar e alterar tabelas sozinho, a partir das
classes Java. Como as entidades apontavam para `motorista`/`viagem`, ele criou
um schema paralelo ao nosso — no banco que **todo o time usa**.

Ninguém percebeu porque:

1. Não havia versionamento de banco (nenhum registro de "quem mudou o quê")
2. O banco é compartilhado, então a mudança de um virou a realidade de todos
3. Tabela nova não quebra nada imediatamente — só quebra quando alguém confia nela

Esse é exatamente o tipo de acidente que ambiente isolado + migrations
versionadas tornam impossível.

## 3. O que é Docker (para quem nunca usou)

Um **container** é uma caixa pronta com um programa já instalado e configurado.

Sem Docker, ter Postgres local significa: baixar instalador, rodar wizard,
definir senha de superusuário, configurar serviço do Windows, descobrir onde
ficou o `pg_hba.conf` — e, se der errado, desinstalar e torcer.

Com Docker, você escreve algumas linhas num arquivo e roda **um comando**.
O Postgres sobe pronto e isolado: não instala nada no Windows, não cria
serviço, não mexe no registro. Para remover, basta apagar o container.

## 4. Como ficaria

```
┌─────────────────────────────────────────────────┐
│ máquina do desenvolvedor                        │
│                                                 │
│  ms-usuarios  :8081 ──▶ ┌──────────────────┐    │
│                         │ db-usuarios :5435│    │
│                         └──────────────────┘    │
│  ms-frota     :8082 ──▶ ┌──────────────────┐    │
│                         │ db-frota    :5433│    │
│                         └──────────────────┘    │
│  ms-operacoes :8083 ──▶ ┌──────────────────┐    │
│                         │ db-operacoes:5434│    │
│                         └──────────────────┘    │
└─────────────────────────────────────────────────┘
```

### `docker-compose.yml` (rascunho, na raiz do projeto)

```yaml
services:
  db-usuarios:
    image: postgres:16
    container_name: db-usuarios
    environment:
      POSTGRES_DB: usuarios
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: dev
    ports: ["5435:5432"]
    volumes: ["dados-usuarios:/var/lib/postgresql/data"]

  db-frota:
    image: postgres:16
    container_name: db-frota
    environment:
      POSTGRES_DB: frota
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: dev
    ports: ["5433:5432"]
    volumes: ["dados-frota:/var/lib/postgresql/data"]

  db-operacoes:
    image: postgres:16
    container_name: db-operacoes
    environment:
      POSTGRES_DB: operacoes
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: dev
    ports: ["5434:5432"]
    volumes: ["dados-operacoes:/var/lib/postgresql/data"]

volumes:
  dados-usuarios:
  dados-frota:
  dados-operacoes:
```

> As portas externas são 5433/5434/5435 (e não 5432) para não conflitar com
> uma instalação de Postgres que alguém já tenha na máquina.
>
> A senha `dev` em texto puro é aceitável aqui: são bancos locais, descartáveis,
> que nunca saem da máquina. Credenciais reais continuam em variável de ambiente.

### Comandos do dia a dia

```bash
docker compose up -d      # sobe os três bancos em segundo plano
docker compose down       # desliga (os dados continuam salvos)
docker compose down -v    # desliga e APAGA os dados
docker compose ps         # mostra o que está rodando
```

### Onde os dados ficam

Num **volume**, uma área gerenciada pelo Docker. É isso que faz os dados
sobreviverem ao `down` e ao desligar o computador. Ligou de novo,
`docker compose up -d`, está tudo lá.

### O comando que justifica a proposta

```bash
docker compose down -v && docker compose up -d
```

**Banco zerado em ~30 segundos.** Como o Flyway roda sozinho quando a aplicação
sobe, o schema inteiro se reconstrói idêntico ao de todo mundo.

Compare com o que foi necessário para limpar as tabelas duplicadas no Supabase
compartilhado: um script SQL com `lo_unlink`, backup de *large object*, ordem
de dependência entre FKs — e com o risco de afetar o time inteiro se errasse.

## 5. Configuração por ambiente (profiles do Spring)

O Supabase **não é abandonado**. Ele deixa de ser onde desenvolvemos e passa a
ser o ambiente de demonstração.

```properties
# application-dev.properties  →  Docker local (desenvolvimento)
spring.datasource.url=jdbc:postgresql://localhost:5433/frota
spring.datasource.username=postgres
spring.datasource.password=dev
```

```properties
# application-demo.properties  →  Supabase (apresentação)
spring.datasource.url=${SUPABASE_DB_URL}
spring.datasource.username=${SUPABASE_DB_USER}
spring.datasource.password=${SUPABASE_DB_PASSWORD}
```

```bash
./mvnw spring-boot:run                                    # usa dev (padrão)
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo    # usa Supabase
```

As **mesmas migrations** rodam nos dois ambientes, então os schemas ficam
garantidamente iguais.

---

## 6. FAQ

### Como eu subo o banco local para o Supabase?

São duas coisas diferentes:

**Estrutura (tabelas, colunas)** — o Flyway sobe sozinho. Você aponta a
aplicação para o Supabase com o profile `demo`, ela sobe, o Flyway compara o
`flyway_schema_history` de lá com os arquivos do repositório e aplica o que
faltar, na ordem.

**Dados (as linhas)** — não são "enviados". Para o nosso caso a resposta mais
natural é: *importar a planilha de novo, pelo próprio sistema*. O produto é um
importador de manifestos; preparar a apresentação é literalmente usar o
sistema. Serve de ensaio da demo.

Se precisar copiar dados de verdade, existe `pg_dump` / `pg_restore`.

### Como meu colega vê minhas alterações?

Depende do tipo de alteração:

**Mudou a estrutura** (tabela nova, coluna nova) — compartilha automático via
git. Você cria `V5__adiciona_coluna_x.sql` e dá push; o colega dá `git pull`,
roda a aplicação, e o Flyway aplica no banco local dele.

Isso é melhor que hoje: atualmente, se alguém cria uma coluna pelo painel do
Supabase, o código dos outros continua sem saber e quebra sem explicar por quê.

**Mudou os dados** (linhas inseridas em teste) — normalmente **não se
compartilha**, e isso é intencional. No mercado, devs compartilharem banco é
tratado como problema, não como recurso: o dado sujo de um quebra o teste de
todos, e ninguém sabe de quem foi.

Se o time precisar da mesma massa de dados, o jeito certo é **dados de exemplo
versionados**:

```
db/
  migration/          ← estrutura, vai para todos os ambientes
    V1__referencia.sql
    V2__frota.sql
  seed-dev/           ← dados de exemplo, SÓ no profile dev
    V900__motoristas_exemplo.sql
```

```properties
# application-dev.properties
spring.flyway.locations=classpath:db/migration,classpath:db/seed-dev
```

A faixa `V900+` fica reservada para seed, para nunca colidir com a numeração
da estrutura. Todo mundo roda a aplicação e começa com exatamente a mesma base.

### "O Rodolfo importou uma planilha e eu quero ver o resultado"

Ele manda **o CSV**, não o banco. Você importa no seu ambiente e obtém o mesmo
resultado, porque o código é o mesmo. E se **não** der o mesmo resultado,
acabamos de encontrar um bug que o banco compartilhado estaria escondendo.

### Preciso instalar o Postgres na minha máquina?

Não. O container já traz o Postgres dentro.

### E se eu já tiver Postgres instalado?

Sem conflito — as portas externas dos containers são 5433/5434/5435.

### Quanto de memória consome?

Três Postgres pequenos ficam em torno de 300–500 MB no total, enquanto estão
ligados. Em máquina de 8 GB dá para sentir; dá para subir só o banco do serviço
em que se está trabalhando (`docker compose up -d db-frota`).

---

## 7. Trade-offs

| | Supabase compartilhado (hoje) | Docker local (proposta) |
|---|---|---|
| Ver dados do colega | automático | via seed ou troca de profile |
| Colega quebrar seu banco | **acontece** (já aconteceu) | impossível |
| Zerar e recomeçar | script SQL com risco | um comando, ~30s |
| Estrutura sincronizada | não existe | automática, via git |
| Funciona sem internet | não | sim |
| Setup inicial | zero | instalar Docker Desktop |
| Consumo de RAM | zero | ~300–500 MB |

## 8. Opção A × Opção B

Ambas implementam o padrão *Database per Service*. Chris Richardson lista
*schema-per-service* como uma das variantes válidas do padrão, então a Opção A
é defensável academicamente.

| | **A — Schema por serviço** | **B — Banco por serviço (Docker)** |
|---|---|---|
| Onde roda | Supabase atual | containers locais |
| Separação | por permissão (usuário só enxerga seu schema) | física |
| Setup | nenhum | Docker Desktop em cada máquina |
| Isolamento entre devs | não | sim |
| Risco se alguém do time travar | nenhum | fica sem rodar o projeto |

**Recomendação:** B, no formato híbrido (Docker para desenvolver, Supabase para
apresentar). Mas **A é a escolha certa se qualquer pessoa do time não conseguir
instalar o Docker.**

## 9. Se aprovado — checklist

- [ ] Todo o time instala o Docker Desktop e valida com `docker --version`
- [ ] Adotar o Flyway antes (pré-requisito)
- [ ] Criar `docker-compose.yml` na raiz do projeto
- [ ] Criar `application-dev.properties` e `application-demo.properties` por serviço
- [ ] Migrar as migrations existentes e validar em banco zerado
- [ ] Documentar no README principal como subir o ambiente
- [ ] Validar o profile `demo` contra o Supabase antes da apresentação

## 10. Referências

- [Padrão Database per Service](https://microservices.io/patterns/data/database-per-service.html) — Chris Richardson
- [Documentação do Docker Compose](https://docs.docker.com/compose/)
- [Imagem oficial do Postgres](https://hub.docker.com/_/postgres)
- [Flyway — Getting Started](https://documentation.red-gate.com/fd/quickstart-how-flyway-works-184127223.html)
