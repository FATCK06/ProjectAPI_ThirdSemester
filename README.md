<div align="center">
    <img src="docs/img/rubyfox.png" width="170x" height="170x">

# Controle Simplificado dos Motoristas Agregados

**Parceiro acadêmico:** Newelog · **Equipe:** RubyFox · **Instituição:** FATEC São José dos Campos

</div>

Sistema simplificado para gestão de motoristas agregados, permitindo acompanhar a quantidade de viagens realizadas, a disponibilidade dos motoristas e a rentabilidade das operações. A solução tem como objetivo centralizar essas informações e facilitar o acompanhamento e a tomada de decisões operacionais.

## Sumário

<div align="center">

**Projeto** · [Sobre o Projeto](#sobre-o-projeto) · [Objetivo](#objetivo) · [Proposta de Solução](#proposta-de-solução)

**Gestão** · [Sprints](#sprints) · [Backlog do Produto](#backlog-do-produto)

**Desenvolvimento** · [Tecnologias](#tecnologias) · [Pré-requisitos](#pré-requisitos) · [Como Rodar o Projeto](#como-rodar-o-projeto) · [Estrutura do Projeto](#estrutura-do-projeto)

**Documentação** · [Padrões de Commit](#padrões-de-commit) · [Documentações](#documentações) · [Membros da Equipe](#membros-da-equipe)

</div>

## Sobre o Projeto

Este projeto foi desenvolvido com o objetivo de solucionar a dificuldade no acompanhamento e controle das operações realizadas por motoristas agregados. A proposta é centralizar informações como quantidade de viagens realizadas, disponibilidade dos motoristas e rentabilidade das operações, facilitando a análise dos resultados e o acompanhamento da operação.

O sistema é direcionado principalmente a gestores e responsáveis pela operação logística que precisam de uma visão simplificada e organizada da performance dos motoristas e da rentabilidade das viagens.

O projeto foi concebido como uma solução prática para representar e automatizar um cenário comum de gestão operacional, podendo ser utilizado tanto como projeto de estudo quanto como base para uma futura aplicação de uso interno.

## Objetivo

Centralizar o controle dos motoristas agregados, permitindo acompanhar viagens realizadas, disponibilidade e rentabilidade das operações. O projeto busca facilitar a gestão operacional e apoiar decisões com base nesses indicadores.

## Proposta de Solução

A solução adota uma abordagem centralizada de acompanhamento operacional, reunindo informações sobre motoristas, viagens e resultados financeiros em uma única visão. A partir desses dados, busca-se facilitar o monitoramento da disponibilidade, da produtividade e da rentabilidade das operações.

## Sprints

O desenvolvimento do projeto foi organizado seguindo a metodologia Scrum, sendo dividido em três sprints com períodos e entregas previamente definidos.

| Sprint |         Período         |    Status    | Vídeo de Incremento |
| :----: | :---------------------: | :----------: | :-----------------: |
|   1    | 07/09/2026 a 27/09/2026 | Concluído | A publicar |
|   2    | 05/10/2026 a 25/10/2026 | Em andamento | A publicar |
|   3    | 02/11/2026 a 22/11/2026 | Pendente | A publicar |


## Backlog do Produto

| Rank | Prioridade | User Story                                                                                                                  | Estimativa | Sprint |    Status    |
| :--: | :--------: | :-------------------------------------------------------------------------------------------------------------------------- | :--------: | :----: | :----------: |
|  1   |    Alta    | Como operador, quero importar a planilha de manifestos no sistema para carregar os dados das operações;                     |     4      |   1    | Concluído |
|  2   |    Alta    | Como operador, quero que o sistema trate os dados das planilhas de manifestos para garantir a consistência das informações; |     8      |   1    | Concluído |
|  3   |    Alta    | Como operador, quero um resumo visual dos dados importados para conferir o resultado da importação;                         |     6      |   1    | Concluído |
|  4   |    Alta    | Como gestor, quero listar os motoristas agregados com a quantidade de viagens para acompanhar a produtividade da frota;     |     6      |   2    |   Em andamento   |
|  5   |    Alta    | Como gestor, quero consultar a situação e a disponibilidade dos motoristas para planejar as operações;                      |     5      |   2    |   Em andamento   |
|  6   |    Alta    | Como gestor, quero um ranking geral dos motoristas do mês para identificar os que se destacaram;                            |     6      |   2    |   Em andamento   |
|  7   |    Alta    | Como gestor, quero níveis de acesso por perfil para restringir as informações sensíveis aos usuários autorizados;           |     4      |   2    |   Em andamento   |
|  8   |   Média    | Como gestor, quero exportar o resultado do mês em planilha para compartilhar os dados fora do sistema;                      |     6      |   3    |   Pendente   |
|  9   |   Baixa    | Como gestor, quero comparar meses anteriores de um motorista para avaliar sua evolução ao longo do tempo.                   |     6      |   3    |   Pendente   |

## Tecnologias

Principais tecnologias utilizadas no projeto:

**Front-end:** <span><img src="https://img.shields.io/badge/TypeScript-black?style=flat&logo=typescript&logoColor=3178C6" height="25"/> <img src="https://img.shields.io/badge/React-black?style=flat&logo=react&logoColor=61DAFB" height="25"/> <img src="https://img.shields.io/badge/CSS3-black?style=flat&logo=css3&logoColor=1572B6" height="25"/></span>

**Back-end:** <span><img src="https://img.shields.io/badge/Java-black?style=flat&logo=openjdk&logoColor=ED8B00" height="25"/></span>

**Banco de Dados:** <span><img src="https://img.shields.io/badge/PostgreSQL-black?style=flat&logo=postgresql&logoColor=4169E1" height="25"/></span>

**Serviços:** <span><img src="https://img.shields.io/badge/Supabase-black?style=flat&logo=supabase&logoColor=3ECF8E" height="25"/></span>

**Ferramentas:** <span><img src="https://img.shields.io/badge/Git-black?style=flat&logo=git&logoColor=F05032" height="25"/> <img src="https://img.shields.io/badge/GitHub-black?style=flat&logo=github&logoColor=white" height="25"/> <img src="https://img.shields.io/badge/Figma-black?style=flat&logo=figma&logoColor=F24E1E" height="25"/> <img src="https://img.shields.io/badge/Jira-black?style=flat&logo=jira&logoColor=2684FF" height="25"/></span>

## Pré-requisitos

Antes de iniciar, certifique-se de ter instalado:
- JDK 17
- Node.js 18 ou superior
- npm ou yarn
- PostgreSQL (banco hospedado no Supabase)
- Git

## Como Rodar o Projeto

### Frontend

```bash
git clone https://github.com/seu-usuario/nome-do-projeto.git

cd nome-do-projeto/frontend

npm install

npm run dev

npm run build
```

Caso o projeto utilize variaveis de ambiente, copie o arquivo de exemplo:

```bash
cp .env.example .env
```

### Backend

```bash
cd nome-do-projeto/backend

npm install

npm run dev
```

Caso o projeto utilize variaveis de ambiente, copie o arquivo de exemplo:

```bash
cp .env.example .env
```

## Padrões de Commit

**Formato:** `<tipo>(#00):mensagem`

| Tipo       | Descricao                                  |
| ---------- | ------------------------------------------ |
| `feat`     | Nova funcionalidade                        |
| `fix`      | Correcao de bug                            |
| `docs`     | Alteracao na documentacao                  |
| `style`    | Formatacao, sem alteracao de logica        |
| `refactor` | Refatoracao de codigo                      |
| `test`     | Adicao ou ajuste de testes                 |
| `chore`    | Tarefas gerais, configuracao, dependencias |

Exemplo:

```bash
git commit -m "feat(#06):adiciona tela de ranking mensal dos motoristas"
git commit -m "fix(#02):corrige leitura de linhas invalidas na importacao da planilha de manifestos"
```

## Estratégia de Branches

**Formato:** `<tipo>/descricao-breve-da-task`

| Branch       | Descricao                                         |
| ------------ | ------------------------------------------------- |
| `main`       | Codigo estavel, pronto para entrega               |
| `develop`    | Integracao das funcionalidades em desenvolvimento |
| `feat/*`     | Nova funcionalidade                               |
| `fix/*`      | Correcao de bug                                   |
| `docs/*`     | Alteracao na documentacao                         |
| `style/*`    | Formatacao, sem alteracao de logica               |
| `refactor/*` | Refatoracao de codigo                             |
| `test/*`     | Adicao ou ajuste de testes                        |
| `chore/*`    | Tarefas gerais, configuracao, dependencias        |

Cada task e desenvolvida em uma branch criada a partir da `develop`. Depois, abre-se um PR para a `develop` e, com a `develop` estavel, um PR da `develop` para a `main`. Nenhum commit direto na `main` ou na `develop`.

Exemplo:

```
git checkout develop
git pull origin develop
git checkout -b feat/pagina-login
```

## Estrutura do Projeto

```
ProjectAPI_ThirdSemester                    | Sistema de gestão logística
├─ backend/                                 | Serviços do sistema
│  ├─ api-gateway/                          | Gateway da API
│  ├─ ms-frota/                             | Gestão da frota
│  ├─ ms-operacoes/                         | Operações e importações
│  └─ ms-usuarios/                          | Gestão de usuários
├─ db/                                      | Banco de dados
│  └─ migration/                            | Migrações do banco
├─ docs/                                    | Documentação
│  ├─ academic/                             | Materiais acadêmicos
│  ├─ api/                                  | Documentação da API
│  ├─ arquitetura/                          | Arquitetura do sistema
│  │  └─ decisoes/                          | Decisões arquiteturais
│  ├─ img/                                  | Imagens da documentação
│  ├─ manual-usuario/                       | Manual do usuário
│  │  └─ img/                               | Imagens do manual
│  ├─ pdf/                                  | Documentos PDF
│  ├─ scripts/                              | Geração de documentos
│  └─ task/                                 | Tarefas do projeto
│     ├─ arquitetura-microservicos/         | Arquitetura de microsserviços
│     └─ docker-ambiente-local/             | Ambiente local com Docker
└─ frontend-app/                            | Aplicação web
   ├─ public/                               | Arquivos públicos
   └─ src/                                  | Código-fonte
      ├─ assets/                            | Recursos visuais
      │  └─ images/                         | Imagens
      ├─ components/                        | Componentes da interface
      │  ├─ Loading/                        | Carregamento
      │  ├─ RankingMotoristas/              | Ranking de motoristas
      │  ├─ Sidebar/                        | Navegação lateral
      │  ├─ SkeletonLoader/                 | Esqueletos de carregamento
      │  │  ├─ CardSkeleton/                | Esqueleto de cartão
      │  │  └─ TableSkeleton/               | Esqueleto de tabela
      │  └─ Toast/                          | Notificações
      ├─ contexts/                          | Contextos compartilhados
      ├─ layouts/                           | Layouts das páginas
      ├─ pages/                             | Páginas da aplicação
      │  ├─ Dashboard/                      | Painel principal
      │  │  └─ components/                  | Componentes do painel
      │  ├─ Importacoes/                    | Importação de dados
      │  │  ├─ Manifestos/                  | Importação de manifestos
      │  │  │  └─ components/               | Componentes de manifestos
      │  │  └─ Veiculos/                    | Importação de veículos
      │  ├─ Login/                          | Autenticação
      │  ├─ Status/                         | Status dos serviços
      │  └─ Usuarios/                       | Gestão de usuários
      │     └─ Cadastro/                    | Cadastro de usuários
      ├─ routes/                            | Rotas da aplicação
      └─ services/                          | Comunicação com as APIs
```

## Documentações

- [**Documentação do Projeto**](./docs/README.md)
- [**Manual do Usuário**](./docs/manual-usuario/README.md) · [PDF](./docs/pdf/manual-do-usuario.pdf)
- [**Rotas da API**](./docs/api/README.md) · [PDF](./docs/pdf/rotas-api.pdf)

## Membros da Equipe

|                                                                                                                                       Foto                                                                                                                                        |      Função       |                  Nome                  |                                                                                                                                                                                                Links                                                                                                                                                                                                |
| :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: | :---------------: | :------------------------------------: | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: |
| <img src="https://media.licdn.com/dms/image/v2/D4D03AQFQwd80LGjHEw/profile-displayphoto-shrink_800_800/profile-displayphoto-shrink_800_800/0/1727568322525?e=1792022400&v=beta&t=pKoLDrqXsgJxefBp-RnfL7OyJwYBLZQPsBAVVd7rbA8" width="60" height="60" style="border-radius:12px"/> | **Product Owner** |       Vinicius Konishi Gregório        | <a href="https://github.com/vinicius-konishi"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub" /></a> <a href="https://www.linkedin.com/in/vin%C3%ADcius-greg%C3%B3rio-406640232/" rel="nofollow"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn" /></a> |
|           <img src="https://media.licdn.com/dms/image/v2/D4D03AQHccwKf0_ByzQ/profile-displayphoto-scale_200_200/B4DZ6qNcd9H0Ac-/0/1780972112691?e=1792022400&v=beta&t=6IanZd98GvHubQmO8wItDwYg9SyLhw_XnNl5NIdBsX0" width="60" height="60" style="border-radius:12px"/>            | **Scrum Master**  |          Vinicius Silva Lopes          |   <a href="https://github.com/viniciuslopes2"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub" /></a> <a href="https://www.linkedin.com/in/vin%C3%ADcius-silva-lopes-976217296/" rel="nofollow"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn" /></a>   |
|           <img src="https://media.licdn.com/dms/image/v2/D4D03AQFRtAeSxgrHNw/profile-displayphoto-scale_200_200/B4DZjYj0RDHwAY-/0/1755979928269?e=1792022400&v=beta&t=XzmsQz-FtEFux0n2a_akNIXhV5EL9fcHj-jMl2KTr-U" width="60" height="60" style="border-radius:12px"/>            |   **Dev Team**    |       Caio Rodrigues de Almeida        |               <a href="https://github.com/Caio-Almeida4"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub" /></a> <a href="https://www.linkedin.com/in/caio-rodri1/" rel="nofollow"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn" /></a>                |
|           <img src="https://media.licdn.com/dms/image/v2/D4D03AQHZ36h5L5A2XA/profile-displayphoto-scale_200_200/B4DZjKEYgfHYAY-/0/1755736806626?e=1792022400&v=beta&t=K6V2GUziqsgtPGinMf-5t1Welcc4pFdXx_yZZClf9BE" width="60" height="60" style="border-radius:12px"/>            |   **Dev Team**    | Guilherme Fernando Portela de Oliveira |   <a href="https://github.com/guilhermefpo/guilhermefpo"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub" /></a> <a href="https://www.linkedin.com/in/guilhermefernandoportela/" rel="nofollow"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn" /></a>   |
| <img src="https://media.licdn.com/dms/image/v2/D4D03AQEw9VDUxHOlQw/profile-displayphoto-shrink_800_800/profile-displayphoto-shrink_800_800/0/1678970447839?e=1792022400&v=beta&t=QtT0Wfw3H8FFdUg8w4562eNRvFQzfP4-guqcDc-9QNs" width="60" height="60" style="border-radius:12px"/> |   **Dev Team**    |              Igor Martins              |               <a href="https://github.com/IgorMartins0729"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub" /></a> <a href="https://www.linkedin.com/in/igormrtns/" rel="nofollow"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn" /></a>                |
|            <img src="https://media.licdn.com/dms/image/v2/D4D03AQEKrsk9CXyjxA/profile-displayphoto-crop_800_800/B4DZzIlYwdJ4AI-/0/1772891772140?e=1792022400&v=beta&t=NkRoHvhMEF-QlS-WIzYRXSCJ_Tlbk1RkOfCjNZj3vrE" width="60" height="60" style="border-radius:12px"/>            |   **Dev Team**    |       Rodolfo Ferreira Venâncio        |               <a href="https://github.com/Clown0o0"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub" /></a> <a href="https://www.linkedin.com/in/rodolfo-ferreir4/" rel="nofollow"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn" /></a>                |
| <img src="https://media.licdn.com/dms/image/v2/D4E03AQHiYzkTmAZtlg/profile-displayphoto-shrink_800_800/profile-displayphoto-shrink_800_800/0/1714513797011?e=1792022400&v=beta&t=X74wb5Hj30kwV8tMretJMJfgcTpF64RVwOhHTz3no8s" width="60" height="60" style="border-radius:12px"/> |   **Dev Team**    |     Yoseph Levi Rodrigues de Lima      |   <a href="https://github.com/YosephLima"><img src="https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white" alt="GitHub" /></a> <a href="https://www.linkedin.com/in/yoseph-levi-rodrigues-de-lima-7020b324a/" rel="nofollow"><img src="https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white" alt="LinkedIn" /></a>   |
