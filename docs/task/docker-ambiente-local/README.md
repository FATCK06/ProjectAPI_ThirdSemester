# Docker — Ambiente Local de Banco de Dados

> **Status:** 📋 Proposta — aguardando avaliação do time
> **Autor:** Igor Martins
> **Data:** 20/09/2026
> **Sprint alvo:** a definir (não entra na Sprint 1)

## O que é esta pasta

Documentação da proposta de subir os bancos de dados em containers Docker
locais, em vez de todo o time desenvolver apontando para o mesmo Supabase.

Nada aqui foi implementado. É material para o time discutir e decidir.

## Arquivos

| Arquivo | Conteúdo |
|---|---|
| `README.md` | Este resumo |
| `proposta-docker.md` | Proposta completa: motivação, como funciona, trade-offs e FAQ |

## Resumo em 30 segundos

**Problema:** hoje todos desenvolvem no mesmo banco Supabase. Isso já causou um
incidente real — tabelas `motorista` e `viagem` foram criadas automaticamente
pelo Hibernate (`ddl-auto=update`) em paralelo às `motoristas`/`viagens` que o
time desenhou, e ninguém percebeu por semanas.

**Proposta:** cada dev roda seus próprios bancos em containers Docker. O
Supabase deixa de ser o banco de desenvolvimento e passa a ser o ambiente de
demonstração/apresentação.

**Ganhos:**

- Um dev não quebra o ambiente dos outros
- Zerar e recriar o banco vira um comando (~30s)
- Atende o requisito de "um banco por serviço" com separação física real
- Estrutura do banco sincronizada via git (junto com o Flyway)

**Custo:** cada pessoa do time precisa instalar o Docker Desktop
(requer WSL2 no Windows, ~2 GB).

## Decisão pendente

Escolher entre:

- **Opção A** — Schema por serviço dentro do Supabase atual (zero setup)
- **Opção B** — Um banco por serviço em Docker local (esta proposta)

Ambas atendem ao padrão *Database per Service*. Os detalhes de cada uma estão
em [`proposta-docker.md`](./proposta-docker.md).

> ⚠️ Se alguém do time não conseguir instalar o Docker (máquina sem permissão
> de admin, pouca RAM), a Opção A é a escolha certa. Um time onde metade não
> consegue rodar o projeto é pior que um ambiente menos isolado.

## Pré-requisito

Esta proposta assume que o versionamento de banco com **Flyway** já esteja
adotado. Sem migrations versionadas, cada banco local nasceria diferente e a
proposta perde o sentido.
