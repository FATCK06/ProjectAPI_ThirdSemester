# Documentação do Projeto

Índice de toda a documentação do sistema de Controle Simplificado dos Motoristas Agregados.

| Documento | Para quem | Conteúdo |
|---|---|---|
| [Manual do Usuário](./manual-usuario/README.md) · [PDF](./pdf/manual-do-usuario.pdf) | Gestores, operadores e administradores | Como usar cada tela do sistema |
| [Rotas da API](./api/README.md) · [PDF](./pdf/rotas-api.pdf) | Desenvolvedores | Endpoints de cada microsserviço, com exemplos |
| [Arquitetura](./arquitetura/README.md) | Desenvolvedores e avaliadores | Visão geral, microsserviços, fluxos, banco e decisões técnicas |
| [Versionamento do banco](../db/README.md) | Desenvolvedores | Regras do Flyway e estado das migrations |

## Organização das pastas

```
docs/
├── README.md            ← este índice
├── api/                 ← rotas da API, um arquivo por microsserviço
├── arquitetura/         ← documento de arquitetura
│   └── decisoes/        ← ADRs: registro das decisões técnicas
├── manual-usuario/      ← manual do usuário
│   └── img/             ← prints das telas usados no manual
├── pdf/                 ← PDFs gerados a partir dos .md (não editar à mão)
├── scripts/             ← script que gera os PDFs
├── academic/            ← material acadêmico
├── img/                 ← imagens gerais (logo etc.)
└── task/                ← estudos e propostas técnicas em andamento
```

## Gerando os PDFs

Os PDFs em `docs/pdf/` são gerados a partir dos arquivos `.md`. Sempre edite o `.md` e gere o PDF de novo:

```bash
cd docs/scripts
npm install        # só na primeira vez
npm run pdf
```

O script usa o Google Chrome ou o Microsoft Edge instalado na máquina para imprimir o PDF.
