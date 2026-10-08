-- Situacao de cada motorista por mes, gravada no tratamento da importacao.
--
-- Calculada no fim do /executar a partir de TODAS as viagens do mes no banco
-- (nao so do arquivo): outro arquivo pode trazer viagens do mesmo mes, e a
-- reimportacao ignora manifestos repetidos. A consulta so le esta tabela.
--
-- So existe linha para motorista com viagem no mes.
--
-- id_motorista e UUID simples, mesma convencao de viagens: motorista pertence
-- ao ms-frota, entao nao ha FK declarada aqui.

CREATE TABLE IF NOT EXISTS public.controle_disponibilidade (
    id                serial       PRIMARY KEY,
    id_motorista      uuid         NOT NULL,
    mes_referencia    varchar(7)   NOT NULL,
    dias_disponiveis  integer      NOT NULL,
    dias_operacao     integer      NOT NULL,
    utilizacao        numeric(7,2),
    faixa_utilizacao  varchar(20),
    situacao          varchar(30)  NOT NULL,
    atualizado_em     timestamp    NOT NULL,

    CONSTRAINT controle_disponibilidade_motorista_mes_uk UNIQUE (id_motorista, mes_referencia),
    CONSTRAINT controle_disponibilidade_faixa_check
        CHECK (faixa_utilizacao IS NULL OR faixa_utilizacao IN ('ALTA', 'INTERMEDIARIA', 'BAIXA')),
    CONSTRAINT controle_disponibilidade_situacao_check
        CHECK (situacao IN ('DISPONIVEL', 'INDISPONIVEL', 'SEM_DIAS_DISPONIVEIS'))
);

COMMENT ON TABLE  public.controle_disponibilidade IS 'Situacao do motorista no mes, gravada na importacao. So motoristas com viagem no mes.';
COMMENT ON COLUMN public.controle_disponibilidade.utilizacao IS 'dias_operacao / dias_disponiveis x 100; nula quando dias_disponiveis <= 0. Pode passar de 100.';
COMMENT ON COLUMN public.controle_disponibilidade.faixa_utilizacao IS 'ALTA, INTERMEDIARIA ou BAIXA; nula quando dias_disponiveis <= 0.';
