-- Categorias de veiculo: a camada de analise sobre os tipos da planilha de origem.
--
-- tipo_veiculos guarda os 14 tipos com a grafia do CSV de veiculos (V12), e a
-- importacao valida contra eles. O cliente compara desempenho e rentabilidade
-- por sete categorias (Fiorino, Van, VUC, 3/4, Toco, Truck e Carreta), entao
-- cada tipo aponta para uma categoria. Tipo sem categoria (id_categoria nulo)
-- aparece como "Nao classificado" nas comparacoes.
--
-- Classificar um tipo novo e um INSERT em tipo_veiculos com a categoria, sem
-- mexer em codigo.
--
-- Pode rodar mais de uma vez: IF NOT EXISTS / ON CONFLICT.

CREATE TABLE IF NOT EXISTS public.categorias_veiculo (
    id_categoria serial      PRIMARY KEY,
    descricao    varchar(20) NOT NULL UNIQUE,
    -- Do menor para o maior porte: e a ordem em que o filtro e os graficos listam.
    ordem        integer     NOT NULL UNIQUE
);

COMMENT ON TABLE public.categorias_veiculo IS 'Categorias de veiculo usadas na analise (definidas pelo cliente).';

INSERT INTO public.categorias_veiculo (descricao, ordem) VALUES
    ('Fiorino', 1),
    ('Van',     2),
    ('VUC',     3),
    ('3/4',     4),
    ('Toco',    5),
    ('Truck',   6),
    ('Carreta', 7)
ON CONFLICT (descricao) DO NOTHING;

ALTER TABLE public.tipo_veiculos
    ADD COLUMN IF NOT EXISTS id_categoria integer;

ALTER TABLE public.tipo_veiculos DROP CONSTRAINT IF EXISTS tipo_veiculos_id_categoria_fkey;
ALTER TABLE public.tipo_veiculos
    ADD CONSTRAINT tipo_veiculos_id_categoria_fkey
    FOREIGN KEY (id_categoria) REFERENCES public.categorias_veiculo (id_categoria);

COMMENT ON COLUMN public.tipo_veiculos.id_categoria IS 'Categoria de analise; nulo = Nao classificado.';

-- De-para dos tipos atuais. Ficam sem categoria, de proposito:
--   Semi-reboque com 2/3/4 eixos: so aparecem como reboque, nunca como o veiculo da viagem.
--   Automoveis e Motocicletas: nao se encaixam em nenhuma das sete.
UPDATE public.tipo_veiculos t
SET id_categoria = c.id_categoria
FROM (VALUES
    ('FIORINO',                 'Fiorino'),
    ('VAN',                     'Van'),
    ('VUC',                     'VUC'),
    ('3/4',                     '3/4'),
    ('Toco ou semi-pesado',     'Toco'),
    ('Truck ou pesado',         'Truck'),
    ('Bi-Truck ou pesado',      'Truck'),
    ('Cavalo mecânico',         'Carreta'),
    ('Cavalo mecânico trucado', 'Carreta')
) AS m (tipo, categoria)
JOIN public.categorias_veiculo c ON c.descricao = m.categoria
WHERE t.descricao = m.tipo;
