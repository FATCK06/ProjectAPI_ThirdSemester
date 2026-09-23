-- Prepara a importacao para o fluxo em etapas (staging).
--
-- Ate aqui o upload gravava as viagens na hora, entao os passos de mapeamento,
-- validacao e preview da tela nao tinham o que fazer. Agora o arquivo fica
-- parado com status AGUARDANDO, e so o passo de confirmacao grava.
--
-- Muda tambem o nome da tabela: ela guarda status, data, responsavel e contagens,
-- ou seja, o registro de uma EXECUCAO de importacao, nao de um arquivo. E fica no
-- plural como as demais (usuarios, motoristas, viagens, agregados).

-- ── Limpeza: 6 linhas de teste do fluxo antigo ───────────────────────────────
-- Os oid de arquivo_conteudo apontam para large objects ja destruidos
-- ("large object 18013 does not exist"), entao nao ha o que converter. Eram
-- fixtures sinteticos (138 a 1948 bytes) e nenhuma viagem foi gerada a partir
-- deles. A copia em arquivo_importacao_backup continua intacta.
DELETE FROM public.arquivo_importacao;

-- ── Renomeacao ───────────────────────────────────────────────────────────────
ALTER TABLE public.arquivo_importacao RENAME TO importacoes;

ALTER TABLE public.viagens RENAME COLUMN arquivo_importacao_id TO importacao_id;

-- ── Conteudo: oid -> bytea ───────────────────────────────────────────────────
-- @Lob byte[] no Hibernate vira "oid" (large object) no Postgres, e o conteudo
-- vai parar em pg_largeobject em vez da propria tabela. Isso quebra o download
-- fora de transacao e deixa lixo orfao quando a linha e apagada. Com bytea o
-- binario mora na tabela e segue o ciclo de vida da linha.
ALTER TABLE public.importacoes DROP COLUMN arquivo_conteudo;
ALTER TABLE public.importacoes ADD COLUMN arquivo_conteudo bytea;

-- ── Fora: mes de referencia do arquivo ───────────────────────────────────────
-- A planilha da Newelog e trimestral (abr-jun tem 792/596/483 registros), entao
-- um unico mes para o arquivo inteiro nao existe. O mes de cada viagem continua
-- em viagens.mes_referencia, derivado da coluna Data de cada linha.
ALTER TABLE public.importacoes DROP COLUMN mes_referencia;

-- ── Contagens, para o passo de resultado nao precisar recontar ───────────────
ALTER TABLE public.importacoes RENAME COLUMN quantidade_linhas_lidas TO linhas_gravadas;
ALTER TABLE public.importacoes ADD COLUMN total_linhas     integer;
ALTER TABLE public.importacoes ADD COLUMN linhas_validas   integer;
ALTER TABLE public.importacoes ADD COLUMN linhas_invalidas integer;

-- ── Estados novos ────────────────────────────────────────────────────────────
--   AGUARDANDO        arquivo recebido, nada parseado
--   VALIDADO          conferido, erros conhecidos, nada gravado
--   EM_PROCESSAMENTO  gravando
--   CONCLUIDO         viagens no banco
--   ERRO              falhou
ALTER TABLE public.importacoes DROP CONSTRAINT IF EXISTS arquivo_importacao_status_check;

ALTER TABLE public.importacoes
    ADD CONSTRAINT importacoes_status_check
    CHECK (status IN ('AGUARDANDO', 'VALIDADO', 'EM_PROCESSAMENTO', 'CONCLUIDO', 'ERRO'));

COMMENT ON TABLE  public.importacoes IS 'Execucao de importacao de manifestos: arquivo, status e contagens.';
COMMENT ON COLUMN public.importacoes.linhas_gravadas IS 'Viagens efetivamente inseridas; menor que total_linhas quando o arquivo repete manifesto ja importado.';
