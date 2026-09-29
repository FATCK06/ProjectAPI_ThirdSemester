# ADR 0003: Importação de manifestos em etapas (staging)

**Status:** aceita · **Data:** 09/2026

## Contexto

Antes, o upload do CSV gravava as viagens na hora. Com isso, as etapas de mapeamento, validação e prévia da tela não tinham função, e um arquivo com problema ia direto para o banco. A planilha da Newelog é trimestral e tem centenas de linhas, então corrigir depois de gravado era caro.

## Decisão

Separar a importação em três chamadas:

| Etapa | Endpoint | Efeito no banco |
|---|---|---|
| Receber | `POST /api/importacao/arquivos` | guarda só o arquivo (`importacoes`, status `AGUARDANDO`) |
| Validar | `POST /arquivos/{id}/validar` | nenhum; o parse roda em memória a cada chamada |
| Executar | `POST /arquivos/{id}/executar` | grava as viagens e marca `CONCLUIDO` |

Regras associadas:

- o mês de cada viagem vem da coluna **Data** da linha, não do arquivo;
- o arquivo só é gravado sem nenhuma linha com `ERRO`; linhas com `PENDENCIA` não bloqueiam;
- manifestos já existentes são ignorados, então reimportar não duplica dados;
- `executar` numa importação `CONCLUIDO` retorna `409`, para que recarregar a tela não reprocesse o arquivo;
- um job `pg_cron` apaga as importações abandonadas (migration V6).

## Consequências

- O usuário vê todos os problemas antes de qualquer gravação.
- O arquivo original fica guardado (`bytea`) e pode ser baixado depois.
- A validação relê o arquivo a cada chamada. Para arquivos de até 10 MB isso é aceitável.
