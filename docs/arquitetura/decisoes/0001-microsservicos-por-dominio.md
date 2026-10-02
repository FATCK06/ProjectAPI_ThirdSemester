# ADR 0001: Microsserviços divididos por domínio

**Status:** aceita · **Data:** 20/09/2026

## Contexto

O projeto começou com serviços criados por funcionalidade (`ms-veiculos`, `ms-indicadores`) que acessavam as mesmas tabelas. O `ms-veiculos` nunca cuidou de veículos, e o Hibernate com `ddl-auto=update` criou tabelas duplicadas (`motorista`/`viagem` ao lado de `motoristas`/`viagens`) sem ninguém perceber.

## Decisão

Dividir o backend em três serviços **por domínio**, cada um dono exclusivo das suas tabelas:

- **ms-usuarios**: identidade e acesso;
- **ms-frota**: quem dirige, quem é contratado e com que veículo, além dos dados de referência de cidade e UF;
- **ms-operacoes**: importação, viagens e indicadores (o antigo `ms-veiculos` renomeado; o `ms-indicadores` foi absorvido).

Nenhum serviço lê ou grava tabela de outro. A comunicação é por HTTP, com endpoints em lote.

## Consequências

- Fica claro onde cada código mora.
- Não existem chaves estrangeiras entre domínios: a consistência passa a ser garantida pela aplicação.
- Chamadas entre serviços precisam ser em lote para não degradar a importação.
- `cidades` e `estados` **não** viram um serviço próprio: seriam um "serviço por tabela", o que exigiria uma chamada extra em toda tela de motorista.
