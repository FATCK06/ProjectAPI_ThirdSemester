-- Move o Vale frete de viagem_custos para uma coluna em viagens.
--
-- Vale frete e o total bruto pago ao agregado (Valor Frete + Pedagio + Adicionais,
-- confere em 100% dos manifestos reais). Pelo criterio da V3 ele e valor unico da
-- viagem, como valor_fretes e saldo_a_pagar, entao e coluna. Em viagem_custos ele
-- ficava somado junto com os proprios componentes.
--
-- Viagens gravadas antes dos custos entrarem em viagem_custos ficam com NULL: o
-- valor e desconhecido, nao zero.

ALTER TABLE public.viagens ADD COLUMN vale_frete numeric;

UPDATE public.viagens v
SET vale_frete = c.valor_desembolsado
FROM public.viagem_custos c
JOIN public.tipos_custo t ON t.id_tipo_custo = c.id_tipo_custo
WHERE c.id_viagem = v.id_viagem
  AND t.descricao = 'Vale frete';

DELETE FROM public.viagem_custos c
USING public.tipos_custo t
WHERE t.id_tipo_custo = c.id_tipo_custo
  AND t.descricao = 'Vale frete';

-- O registro 'Vale frete' em tipos_custo fica por enquanto: o codigo anterior a
-- esta versao recusa importar sem ele. Sai numa migration futura, quando todos
-- estiverem com o codigo novo.
