-- Agregado: o contratado dono do veiculo, distinto do motorista que dirige.
--
-- Evidencia no arquivo real da Newelog (manifestos_abr-jun.csv, 1871 registros):
--   - 193 agregados distintos contra 150 motoristas
--   - 831 registros com CNPJ (14 digitos) e 956 com CPF (11)
--   - 50 motoristas rodaram para mais de um agregado (ate 3)
--
-- Por isso o vinculo vive na viagem, e nao em motoristas: ele muda a cada manifesto.

CREATE TABLE public.agregados (
    id_agregado   uuid NOT NULL DEFAULT gen_random_uuid(),
    documento     varchar(14) NOT NULL,
    tipo_pessoa   char(2) NOT NULL,
    nome          varchar(150) NOT NULL,
    pis           varchar(11),
    regime_fiscal varchar(50),
    criado_em     timestamp without time zone DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT agregados_pkey PRIMARY KEY (id_agregado),
    CONSTRAINT agregados_documento_key UNIQUE (documento),

    -- Guardamos so digitos: o CSV traz o documento como ="00000000000"
    CONSTRAINT agregados_documento_digitos CHECK (documento ~ '^[0-9]+$'),
    CONSTRAINT agregados_tipo_pessoa CHECK (tipo_pessoa IN ('PF', 'PJ')),
    CONSTRAINT agregados_documento_tamanho CHECK (
        (tipo_pessoa = 'PF' AND length(documento) = 11) OR
        (tipo_pessoa = 'PJ' AND length(documento) = 14)
    )
);

COMMENT ON TABLE  public.agregados IS 'Contratado dono do veiculo (PF ou PJ). Pode coincidir com o motorista quando ele e autonomo.';
COMMENT ON COLUMN public.agregados.documento IS 'Somente digitos: 11 = CPF, 14 = CNPJ.';
COMMENT ON COLUMN public.agregados.regime_fiscal IS 'Ex.: Simples nacional, MEI. Nulo na maioria dos registros.';

-- Nulo e esperado: 84 dos 1871 registros do arquivo real nao informam agregado.
ALTER TABLE public.viagens ADD COLUMN id_agregado uuid;

-- FK valida enquanto viagens e agregados moram no mesmo banco.
-- Quando ms-operacoes e ms-frota forem separados, esta constraint sai e a
-- verificacao passa a ser feita por chamada ao ms-frota.
ALTER TABLE public.viagens
    ADD CONSTRAINT viagens_id_agregado_fkey
    FOREIGN KEY (id_agregado) REFERENCES public.agregados(id_agregado);

CREATE INDEX idx_viagens_id_agregado ON public.viagens (id_agregado);
