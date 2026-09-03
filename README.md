<div align="center">
    <img src="docs/img/rubyfox.png" width="170x" height="170x">
</div>

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


## Proposta de Solução

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
| US01 | Como operador quero uma interface para poder visualizar a quantidade de viagens realizada por cada motorista para acompanhar a produtividade. | ? | Pendente |
| US02 | Como gestor quero um painel para poder visualizar a lista de motoristas agregados da frota. | ? | Pendente |
| US03 | Como operador quero uma interface que através de uma planilha XML, trate os dados da mesma, e me devolva graficamente um resumo visual. | ? | Pendente |
| US04 | Como gestor quero consultar a ficha de desempenho de um motorista, para visualizar seu desempenho. | ? | Pendente |
| US05 | Como operador desejo importar a planilha de controle para carregar os dados dos motoristas do sistema. | ? | Pendente |
| US06 | Como gestor desejo uma interface que apresente um ranking geral dos motoristas para visualizar os motoristas que se destacaram no mês.  | ? | Pendente |
| US07 | Como gestor quero que o sistema possua níveis de acesso para que os dados financeiros restritos ao cargo de gestor.  | ? | Pendente |
| US08 | Como operador quero poder filtrar os motoristas por tipo de veículo para facilitar a análise de desempenho dos diferentes veículos.  | ? | Pendente |

## Tecnologias

Principais tecnologias utilizadas no projeto:

<p align="left"> <img src="https://img.shields.io/badge/JavaScript-black?style=flat&logo=javascript&logoColor=F7DF1E" height="25"/> <img src="https://img.shields.io/badge/TypeScript-black?style=flat&logo=typescript&logoColor=3178C6" height="25"/> <img src="https://img.shields.io/badge/HTML5-black?style=flat&logo=html5&logoColor=E34F26" height="25"/> <img src="https://img.shields.io/badge/CSS3-black?style=flat&logo=css3&logoColor=1572B6" height="25"/> <img src="https://img.shields.io/badge/React-black?style=flat&logo=react&logoColor=61DAFB" height="25"/> <img src="https://img.shields.io/badge/Vite-black?style=flat&logo=vite&logoColor=646CFF" height="25"/> <img src="https://img.shields.io/badge/Node.js-black?style=flat&logo=node.js&logoColor=339933" height="25"/> <img src="https://img.shields.io/badge/MongoDB-black?style=flat&logo=mongodb&logoColor=47A248" height="25"/> <img src="https://img.shields.io/badge/GitHub-black?style=flat&logo=github&logoColor=white" height="25"/> <img src="https://img.shields.io/badge/Figma-black?style=flat&logo=figma&logoColor=F24E1E" height="25"/> <img src="https://img.shields.io/badge/Jira-black?style=flat&logo=jira&logoColor=2684FF" height="25"/> <img src="https://img.shields.io/badge/JFreeChart-black?style=flat&logo=openjdk&logoColor=007396" height="25"/> </p>


## Como Rodar o Projeto

### Pré-requisitos

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

## Padrões de Commit

**Formato:** `<tipo>(#00):mensagem`

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
git commit -m "feat(#06):adiciona tela de ranking mensal dos motoristas"
git commit -m "fix(#02):corrige leitura de linhas invalidas na importacao do XML"
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
| <img src="https://media.licdn.com/dms/image/v2/D4D03AQHccwKf0_ByzQ/profile-displayphoto-crop_800_800/B4DZ6qNcd9H0AI-/0/1780972112692?e=1789603200&v=beta&t=jswXVNPQAB6QAc6LR8lDpvRCZlINIuo6fhi2SDlEdps" width="60" height="60" style="border-radius:50%"/> | Scrum Master | Vinicius Silva Lopes | [GitHub](https://github.com/viniciuslopes2) · [LinkedIn](https://www.linkedin.com/in/vin%C3%ADcius-silva-lopes-976217296/) |
| <img src="https://media.licdn.com/dms/image/v2/D4D03AQFQwd80LGjHEw/profile-displayphoto-shrink_800_800/profile-displayphoto-shrink_800_800/0/1727568322525?e=1789603200&v=beta&t=2btg-d3ZrIerf4ACRXRd9HedNLz60m06PrcH8uC8K_s" width="60" height="60" style="border-radius:50%"/> | Product Owner | Vinicius Konishi Gregório | [GitHub](https://github.com/vinicius-konishi) · [LinkedIn](https://www.linkedin.com/in/vin%C3%ADcius-greg%C3%B3rio-406640232/) |
| <img src="https://media.licdn.com/dms/image/v2/D4D03AQEKrsk9CXyjxA/profile-displayphoto-crop_800_800/B4DZzIlYwdJ4AI-/0/1772891772140?e=1789603200&v=beta&t=1F02hawljbVxSe0ds2zFec2NJgxl-aFBsZVUDmDjQwQ" width="60" height="60" style="border-radius:50%"/> | DEV | Rodolfo Ferreira Venâncio | [GitHub](https://github.com/Clown0o0) · [LinkedIn](https://www.linkedin.com/in/rodolfo-ferreir4/) |
| <img src="https://media.licdn.com/dms/image/v2/D4D03AQEw9VDUxHOlQw/profile-displayphoto-shrink_800_800/profile-displayphoto-shrink_800_800/0/1678970447839?e=1789603200&v=beta&t=qd0SNljhZJNibhbq9CmJ9O40lsJrH9UEw9UrsO9s9AM" width="60" height="60" style="border-radius:50%"/> | DEV | Igor Martins | [GitHub](https://github.com/IgorMartins0729) · [LinkedIn](https://www.linkedin.com/in/igormrtns/) |
| <img src="https://media.licdn.com/dms/image/v2/D4D03AQHZ36h5L5A2XA/profile-displayphoto-crop_800_800/B4DZjKEYgfHYAI-/0/1755736806505?e=1789603200&v=beta&t=4xb-9sKPHoeRIhH3S9uZvNEjYCi62JLOi5kmXJ9Nr5E" width="60" height="60" style="border-radius:50%"/> | DEV | Guilherme Fernando Portela de Oliveira | [GitHub](https://github.com/guilhermefpo/guilhermefpo) · [LinkedIn](https://www.linkedin.com/in/guilhermefernandoportela/) |
| <img src="https://media.licdn.com/dms/image/v2/D4E03AQHiYzkTmAZtlg/profile-displayphoto-shrink_800_800/profile-displayphoto-shrink_800_800/0/1714513797011?e=1789603200&v=beta&t=xN5UL0er77KSJ1A3LHCtgNIrbojDJAt5pvOFfpvJWO0" width="60" height="60" style="border-radius:50%"/> | DEV | Yoseph Levi Rodrigues de Lima | [GitHub](https://github.com/YosephLima) · [LinkedIn](https://www.linkedin.com/in/yoseph-levi-rodrigues-de-lima-7020b324a/) |
| <img src="https://media.licdn.com/dms/image/v2/D4D03AQFRtAeSxgrHNw/profile-displayphoto-crop_800_800/B4DZjYj0RDHwAI-/0/1755979928154?e=1789603200&v=beta&t=mn_ouv4eaMwffqvfKltkPuxTP7abaDaXE6xcuCIwsUE" width="60" height="60" style="border-radius:50%"/> | DEV | Caio Rodrigues de Almeida | [GitHub](https://github.com/Caio-Almeida4) · [LinkedIn](https://www.linkedin.com/in/caio-rodri1/) |
