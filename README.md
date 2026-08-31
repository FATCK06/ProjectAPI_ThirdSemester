# Controle simplificado dos motoristas agregrados (NeweLog)

 Sistema simplificado para gestão de motoristas agregados, permitindo acompanhar a quantidade de viagens realizadas, a disponibilidade dos motoristas e a rentabilidade das operações. A solução tem como objetivo centralizar essas informações e facilitar o acompanhamento e a tomada de decisões operacionais.

## Sumário

[Sobre o Projeto](#sobre-o-projeto) · [Objetivo](#objetivo) · [Proposta de Solução](#proposta-de-solucao) · [Sprints](#sprints) · [Backlog do Produto e User Stories](#backlog-do-produto-e-user-stories) · [Tecnologias](#tecnologias) · [Como Rodar o Projeto](#como-rodar-o-projeto) · [Padrões de Commit](#padroes-de-commit) · [Estrutura do Projeto](#estrutura-do-projeto) · [Membros da Equipe](#membros-da-equipe)

## Sobre o Projeto

Este projeto foi desenvolvido com o objetivo de solucionar a dificuldade no acompanhamento e controle das operações realizadas por motoristas agregados. A proposta é centralizar informações como quantidade de viagens realizadas, disponibilidade dos motoristas e rentabilidade das operações, facilitando a análise dos resultados e o acompanhamento da operação.

O sistema é direcionado principalmente a gestores e responsáveis pela operação logística que precisam de uma visão simplificada e organizada da performance dos motoristas e da rentabilidade das viagens.

O projeto foi concebido como uma solução prática para representar e automatizar um cenário comum de gestão operacional, podendo ser utilizado tanto como projeto de estudo quanto como base para uma futura aplicação de uso interno.

## Objetivo

Centralizar o controle dos motoristas agregados, permitindo acompanhar viagens realizadas, disponibilidade e rentabilidade das operações. O projeto busca facilitar a gestão operacional e apoiar decisões com base nesses indicadores.


## Proposta de Solucao

A solução adota uma abordagem centralizada de acompanhamento operacional, reunindo informações sobre motoristas, viagens e resultados financeiros em uma única visão. A partir desses dados, busca-se facilitar o monitoramento da disponibilidade, da produtividade e da rentabilidade das operações.


## Sprints

O desenvolvimento do projeto foi organizado seguindo a metodologia Scrum, sendo dividido em três sprints com períodos e entregas previamente definidos. A tabela abaixo apresenta o cronograma previsto e o status de cada sprint, permitindo acompanhar a evolução do projeto ao longo do desenvolvimento.

| Sprint | Início | Término | Status |
|--------|--------|---------|--------|
| 1 | 07/09/2026 | 27/09/2026 | Pendente |
| 2 | 05/10/2026 | 25/10/2026 | Pendente |
| 3 | 02/11/2026 | 22/11/2026 | Pendente |

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

