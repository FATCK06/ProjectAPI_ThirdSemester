-- Tira de viagem_custos o que nao e custo da operacao.
--
-- Adiantamento e a parte do vale frete paga antes da viagem: o vale frete ja a
-- inclui, entao somar as duas conta o mesmo dinheiro duas vezes. INSS, SEST/SENAT
-- e IR sao retidos do agregado (Saldo a pagar = Vale frete - Adiantamento - INSS
-- - SEST/SENAT - IR, confere em 100% dos manifestos reais), nao gasto da Newe.
-- A partir desta versao a importacao deixa de grava-los (CustoDoManifesto).
--
-- Os valores nao se perdem: continuam no CSV e o efeito deles esta em
-- viagens.saldo_a_pagar.

DELETE FROM public.viagem_custos c
USING public.tipos_custo t
WHERE t.id_tipo_custo = c.id_tipo_custo
  AND t.descricao IN ('Adiantamento', 'INSS', 'SEST/SENAT', 'IR');

-- Os registros em tipos_custo ficam por enquanto, como na V8: o codigo anterior
-- a esta versao recusa importar sem eles. Saem numa migration futura, quando
-- todos estiverem com o codigo novo.
