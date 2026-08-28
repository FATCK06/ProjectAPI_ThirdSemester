# Nome do Projeto

> Uma frase curta descrevendo o que o projeto faz e para quem e.

## Sumario

- [Sobre o Projeto](#sobre-o-projeto)
- [Objetivo](#objetivo)
- [Proposta de Solucao](#proposta-de-solucao)
- [Sprints](#sprints)
- [Backlog do Produto e User Stories](#backlog-do-produto-e-user-stories)
- [Tecnologias](#tecnologias)
- [Como Rodar o Projeto](#como-rodar-o-projeto)
  - [Pre-requisitos](#pre-requisitos)
  - [Frontend](#frontend)
  - [Backend](#backend)
- [Padroes de Commit](#padroes-de-commit)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Membros da Equipe](#membros-da-equipe)

## Sobre o Projeto

Descreva aqui o contexto do projeto: qual problema ele resolve, qual o publico-alvo, e um pouco do historico (ex: trabalho academico, projeto interno, produto).

Exemplo:
> O Aerocode e uma aplicacao frontend desenvolvida como parte da disciplina X, que simula um fluxo de producao industrial (Aeronave -> Peca -> Etapa -> Teste -> Relatorio) atraves de uma interface wizard.

## Objetivo

Descreva aqui, de forma direta e objetiva (uma ou duas frases), qual problema o projeto resolve ou qual meta ele cumpre.

Exemplo:
> Automatizar e padronizar o registro do processo de producao de aeronaves, desde a fabricacao de pecas ate a geracao do relatorio final.

## Proposta de Solucao

Descreva aqui a abordagem escolhida para atingir o objetivo: o raciocinio geral por tras da solucao, sem entrar em detalhes de implementacao (isso fica para a secao de Tecnologias/Arquitetura).

Exemplo:
> A solucao proposta consiste em uma interface do tipo wizard, que guia o usuario sequencialmente pelas etapas do processo produtivo (Aeronave -> Peca -> Etapa -> Teste -> Relatorio), utilizando gerenciamento de estado centralizado via Context API para manter a consistencia dos dados entre as telas.

## Sprints

Descreva aqui a divisao do desenvolvimento em sprints, com a previsao de entrega e o status de cada uma.

| Sprint | Previsao | Status |
|--------|----------|--------|
| 1 | dd/mm/aaaa | Concluida |
| 2 | dd/mm/aaaa | Em andamento |
| 3 | dd/mm/aaaa | Pendente |

## Backlog do Produto e User Stories

Descreva aqui as user stories que compoem o backlog do produto, com prioridade e status de cada uma.

| ID | User Story | Prioridade | Status |
|----|------------|------------|--------|
| US01 | Como usuario, quero cadastrar uma aeronave para iniciar o processo produtivo. | Alta | Concluida |
| US02 | Como usuario, quero registrar uma peca vinculada a aeronave. | Alta | Em andamento |
| US03 | Como usuario, quero gerar um relatorio final do processo. | Media | Pendente |

## Tecnologias

Principais tecnologias utilizadas no projeto:

<img src="https://img.shields.io/badge/React-black?style=flat&logo=react&logoColor=61DAFB" height="25"/>
<img src="https://img.shields.io/badge/Vite-black?style=flat&logo=vite&logoColor=646CFF" height="25"/>
<img src="https://img.shields.io/badge/JavaScript-black?style=flat&logo=javascript&logoColor=F7DF1E" height="25"/>
<img src="https://img.shields.io/badge/HTML5-black?style=flat&logo=html5&logoColor=E34F26" height="25"/>
<img src="https://img.shields.io/badge/CSS3-black?style=flat&logo=css3&logoColor=1572B6" height="25"/>

<!-- Adicione ou remova badges conforme as tecnologias do seu projeto. -->
<!-- Lista completa de icones disponiveis em: https://simpleicons.org -->


## Como Rodar o Projeto

### Pre-requisitos

Antes de iniciar, certifique-se de ter instalado:

- Node.js (versao >= 18)
- npm ou yarn
- Git

### Frontend

```bash
# Clonar o repositorio
git clone https://github.com/seu-usuario/nome-do-projeto.git

# Acessar o diretorio do frontend
cd nome-do-projeto/frontend

# Instalar as dependencias
npm install

# Ambiente de desenvolvimento
npm run dev

# Build de producao
npm run build
```

Caso o projeto utilize variaveis de ambiente, copie o arquivo de exemplo:

```bash
cp .env.example .env
```

### Backend

```bash
# Acessar o diretorio do backend
cd nome-do-projeto/backend

# Instalar as dependencias
npm install

# Ambiente de desenvolvimento
npm run dev
```

Caso o projeto utilize variaveis de ambiente, copie o arquivo de exemplo:

```bash
cp .env.example .env
```

## Padroes de Commit

Este projeto segue o padrao de [Conventional Commits](https://www.conventionalcommits.org/) para manter o historico de alteracoes claro e organizado.

| Tipo | Descricao |
|------|-----------|
| `feat` | Nova funcionalidade |
| `fix` | Correcao de bug |
| `docs` | Alteracao na documentacao |
| `style` | Formatacao, sem alteracao de logica |
| `refactor` | Refatoracao de codigo |
| `test` | Adicao ou ajuste de testes |
| `chore` | Tarefas gerais, configuracao, dependencias |

Exemplo:

```bash
git commit -m "feat: adiciona tela de cadastro de aeronave"
git commit -m "fix: corrige validacao do formulario de peca"
```

## Estrutura do Projeto

```
nome-do-projeto/
├── src/
│   ├── components/     # Componentes reutilizaveis
│   ├── hooks/          # Custom hooks (ex: useWizard)
│   ├── context/        # Context API
│   ├── pages/          # Paginas e rotas
│   └── App.jsx
├── public/
├── package.json
└── vite.config.js
```

## Membros da Equipe

| Foto | Funcao | Nome | Links |
|------|--------|------|-------|
| <img src="https://via.placeholder.com/60" width="60" height="60" style="border-radius:50%"/> | Master | Vinicius Silva Lopes | [GitHub](#) · [LinkedIn](#) |
| <img src="https://via.placeholder.com/60" width="60" height="60" style="border-radius:50%"/> | Product Owner | Vinicius Konishi Gregório | [GitHub](#) · [LinkedIn](#) |
| <img src="https://via.placeholder.com/60" width="60" height="60" style="border-radius:50%"/> | DEV | Rodolfo Ferreira Venâncio | [GitHub](#) · [LinkedIn](#) |
| <img src="https://via.placeholder.com/60" width="60" height="60" style="border-radius:50%"/> | DEV | Igor Martins | [GitHub](#) · [LinkedIn](#) |
| <img src="https://via.placeholder.com/60" width="60" height="60" style="border-radius:50%"/> | DEV | Guilherme Fernando Portela de Oliveira | [GitHub](#) · [LinkedIn](#) |
| <img src="https://via.placeholder.com/60" width="60" height="60" style="border-radius:50%"/> | DEV | Yoseph Levi Rodrigues de Lima | [GitHub](#) · [LinkedIn](#) |

