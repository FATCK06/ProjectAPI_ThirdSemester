# Versionamento do banco (Flyway)

Toda mudança de estrutura do banco vive aqui, versionada junto com o código.

## Regras

1. **Nome do arquivo:** `V<número>__<descrição>.sql` — são **dois** underscores.
2. **Migration aplicada nunca é editada.** O Flyway guarda um checksum de cada
   arquivo; se um que já rodou for alterado, a aplicação se recusa a subir.
   Errou? Cria a próxima versão corrigindo.
3. **Ninguém mexe no banco pela mão** — nem pelo painel do Supabase, nem com
   `ddl-auto=update`. Precisa de uma coluna? Cria uma migration.

A regra 3 não é burocracia: foi ignorá-la que gerou as tabelas duplicadas
`motorista`/`viagem` ao lado de `motoristas`/`viagens`, em 20/09/2026.

## Numeração

| Faixa | Uso |
|---|---|
| `V1` | Baseline — schema que já existia quando adotamos o Flyway |
| `V2+` | Mudanças de estrutura |
| `V900+` | Dados de exemplo (apenas no profile `dev`) |

## Estado atual

| Arquivo | Situação |
|---|---|
| `V1__baseline.sql` | ⚠️ **ainda não escrito** |
| `V2__criar_agregados.sql` | criado — tabela `agregados` + `viagens.id_agregado` |
| `V3__viagens_campos_do_manifesto.sql` | criado — campos do CSV, `mes_referencia`, seed de `tipos_custo` |
| `V4__agregados_tipo_pessoa_varchar.sql` | criado — `agregados.tipo_pessoa` para varchar |

**O Flyway ainda está desligado em todos os serviços** (`spring.flyway.enabled=false`).
Ligar exige dois cuidados, explicados abaixo.

### Cuidado 1 — o baseline depende do que já foi aplicado na mão

O Flyway não tem como saber que alguém rodou um `.sql` pelo painel do Supabase.
Se ligar sem ajustar, ele tenta aplicar de novo e falha com "objeto já existe".

Escolha o `baseline-version` conforme o que **já foi rodado manualmente**:

| Já rodou manualmente | `baseline-version` | O Flyway aplica |
|---|---|---|
| nada | `1` | V2 e V3 |
| só o V2 | `2` | só o V3 |
| V2 e V3 | `3` | nada (só registra o estado) |

Confira o que foi aplicado antes de decidir:

```sql
-- a tabela agregados existe?  (V2)
SELECT to_regclass('public.agregados');
-- viagens já tem mes_referencia?  (V3)
SELECT column_name FROM information_schema.columns
WHERE table_name = 'viagens' AND column_name = 'mes_referencia';
```

### Cuidado 2 — `V1` ainda não existe

Com `baseline-on-migrate=true` o banco atual é marcado como já versionado, então
**o ambiente do Supabase funciona normalmente**. Mas um banco vazio não se
reconstrói do zero até o `V1` ser escrito — isso vira bloqueante se/quando
adotarmos Docker.

## Configuração

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

```properties
spring.flyway.enabled=true
spring.flyway.locations=filesystem:../../db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=2
spring.jpa.hibernate.ddl-auto=validate
```

A pasta fica na raiz (e não dentro de um serviço) porque hoje os serviços
compartilham o mesmo banco Supabase — se cada um rodasse o próprio Flyway,
eles brigariam pela tabela de histórico. **Um serviço é designado dono das
migrations e só ele roda o Flyway;** os demais ficam com
`spring.flyway.enabled=false`.

Quando os bancos forem separados por serviço, cada um passa a ter sua própria
pasta `src/main/resources/db/migration`.

## Comandos

No dia a dia, nenhum: o Flyway roda sozinho quando a aplicação sobe.

Para inspecionar:

```bash
./mvnw flyway:info       # o que já rodou e o que falta
./mvnw flyway:validate   # confere se alguém editou migration antiga
```

```sql
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;
```
