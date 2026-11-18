-- Importacao passa a ter dois tipos de arquivo: manifestos (grava viagens) e
-- veiculos (grava o cadastro de veiculos). O tipo decide qual leitor confere o CSV.
-- Tudo que ja existe veio da importacao de manifestos.

ALTER TABLE public.importacoes
    ADD COLUMN IF NOT EXISTS tipo varchar(20) NOT NULL DEFAULT 'MANIFESTO';

ALTER TABLE public.importacoes DROP CONSTRAINT IF EXISTS importacoes_tipo_check;
ALTER TABLE public.importacoes
    ADD CONSTRAINT importacoes_tipo_check CHECK (tipo IN ('MANIFESTO', 'VEICULO'));
