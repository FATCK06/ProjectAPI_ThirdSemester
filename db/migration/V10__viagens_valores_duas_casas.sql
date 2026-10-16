-- Duas casas decimais nas colunas de valor que a V3 e a V8 criaram como numeric
-- sem escala.
--
-- No Supabase elas ja estao como numeric(38,2): alguem alterou direto no banco, e
-- o repositorio ficou para tras. Esta migration so registra esse estado. La nao
-- muda nada (mesmo tipo, sem reescrita da tabela); num ambiente recriado a partir
-- das migrations, garante as duas casas que a conferencia com a planilha exige.
--
-- 38,2 e o que esta no banco e o padrao do Hibernate para BigDecimal.
--
-- Fora daqui: kg_real, kg_taxado, m3, valor_frete, viagem_custos.valor_desembolsado
-- e as de veiculos. Vem de antes do Flyway (V1, baseline ainda nao escrito), entao
-- nenhuma migration as cria.

ALTER TABLE public.viagens
    ALTER COLUMN percentual_efetividade   TYPE numeric(38,2),
    ALTER COLUMN percentual_aprov_veiculo TYPE numeric(38,2),
    ALTER COLUMN valor_nf                 TYPE numeric(38,2),
    ALTER COLUMN valor_fretes             TYPE numeric(38,2),
    ALTER COLUMN total_despesas           TYPE numeric(38,2),
    ALTER COLUMN saldo_despesas           TYPE numeric(38,2),
    ALTER COLUMN saldo_a_pagar            TYPE numeric(38,2),
    ALTER COLUMN vale_frete               TYPE numeric(38,2);
