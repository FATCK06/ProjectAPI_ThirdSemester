-- Acomoda em viagens os campos do manifesto que a modelagem original nao previa.
--
-- Criterio: o campo se repete dentro de uma viagem?
--   nao (valor unico, depende so da viagem) -> coluna aqui
--   sim (N desembolsos, N paradas)          -> linha em viagem_custos / viagem_destinos
--
-- Por isso os ~11 custos viram linhas em viagem_custos (conjunto aberto: surge
-- categoria nova de despesa), enquanto os contadores ficam como coluna (conjunto
-- fechado, e os itens 4 e 6 do backlog consultam direto por eles).

-- ── Operacao ─────────────────────────────────────────────────────────────────
ALTER TABLE public.viagens ADD COLUMN km_saida                 integer;
ALTER TABLE public.viagens ADD COLUMN km_chegada               integer;
ALTER TABLE public.viagens ADD COLUMN servicos                 integer;
ALTER TABLE public.viagens ADD COLUMN servicos_finalizados     integer;
ALTER TABLE public.viagens ADD COLUMN nfs                      integer;
ALTER TABLE public.viagens ADD COLUMN coletas                  integer;
ALTER TABLE public.viagens ADD COLUMN entregas                 integer;
ALTER TABLE public.viagens ADD COLUMN despachos                integer;
ALTER TABLE public.viagens ADD COLUMN retiradas                integer;
ALTER TABLE public.viagens ADD COLUMN coletas_reversa          integer;
ALTER TABLE public.viagens ADD COLUMN percentual_efetividade   numeric;
ALTER TABLE public.viagens ADD COLUMN percentual_aprov_veiculo numeric;

-- ── Trajeto e identificacao ──────────────────────────────────────────────────
-- destino como texto: o CSV traz um unico destino, enquanto viagem_destinos foi
-- desenhada para varias paradas com ordem. Quando aparecer manifesto multi-parada,
-- o texto vira linha la e esta coluna sai.
ALTER TABLE public.viagens ADD COLUMN destino                  varchar;
ALTER TABLE public.viagens ADD COLUMN reboque1                 varchar;
ALTER TABLE public.viagens ADD COLUMN reboque2                 varchar;
ALTER TABLE public.viagens ADD COLUMN reboque3                 varchar;
ALTER TABLE public.viagens ADD COLUMN classificacao            varchar;
ALTER TABLE public.viagens ADD COLUMN observacoes_operacionais text;
ALTER TABLE public.viagens ADD COLUMN usuario_manifesto        varchar;

-- ── Valores e totais ─────────────────────────────────────────────────────────
-- Os totais sao derivaveis somando viagem_custos, mas guardamos o numero que o
-- ERP informou: se a nossa soma divergir, a diferenca e sinal util.
ALTER TABLE public.viagens ADD COLUMN valor_nf                 numeric;
ALTER TABLE public.viagens ADD COLUMN valor_fretes             numeric;
ALTER TABLE public.viagens ADD COLUMN total_despesas           numeric;
ALTER TABLE public.viagens ADD COLUMN saldo_despesas           numeric;
ALTER TABLE public.viagens ADD COLUMN saldo_a_pagar            numeric;

-- ── Mes de referencia ────────────────────────────────────────────────────────
-- Derivado da data de cada linha, nao do upload: um arquivo cobre varios meses
-- (o de abr-jun tem 792/596/483 registros). Indexado porque os itens 6, 8 e 9
-- do backlog filtram por mes.
ALTER TABLE public.viagens ADD COLUMN mes_referencia varchar(7);
CREATE INDEX idx_viagens_mes_referencia ON public.viagens (mes_referencia);

-- ── Vinculo com o arquivo de origem ──────────────────────────────────────────
-- Permite responder "o que entrou nesta importacao" e refazer a conferencia.
ALTER TABLE public.viagens ADD COLUMN arquivo_importacao_id bigint;
ALTER TABLE public.viagens
    ADD CONSTRAINT viagens_arquivo_importacao_fkey
    FOREIGN KEY (arquivo_importacao_id) REFERENCES public.arquivo_importacao(id);
CREATE INDEX idx_viagens_arquivo_importacao ON public.viagens (arquivo_importacao_id);

-- ── Tipos de custo ───────────────────────────────────────────────────────────
-- Descricoes iguais as colunas do CSV, para o mapeamento ser obvio na leitura.
ALTER TABLE public.tipos_custo ADD CONSTRAINT tipos_custo_descricao_key UNIQUE (descricao);

INSERT INTO public.tipos_custo (descricao) VALUES
    ('Vale frete'),
    ('Combustível'),
    ('Pedágio'),
    ('Diária'),
    ('Adicionais'),
    ('Descontos'),
    ('Adiantamento'),
    ('Despesas'),
    ('INSS'),
    ('SEST/SENAT'),
    ('IR')
ON CONFLICT (descricao) DO NOTHING;
