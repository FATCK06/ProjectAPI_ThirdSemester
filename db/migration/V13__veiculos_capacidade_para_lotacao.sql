-- A importacao de manifestos gravava a "Capacidade Veículo" em veiculos.capacidade.
-- Esse valor e peso: comparando 192 placas presentes no manifesto e no cadastro de
-- veiculos, ele bate com a Capacidade Lotação em 189 e com a Capacidade (m³) em 22.
-- A partir desta versao o manifesto grava em capacidade_lotacao, e capacidade fica
-- para o volume em m³ que vem da importacao de veiculos.
--
-- Ate aqui so o manifesto escrevia em capacidade, entao todo valor nela e lotacao.

UPDATE public.veiculos
SET capacidade_lotacao = capacidade,
    capacidade = NULL
WHERE capacidade IS NOT NULL
  AND capacidade_lotacao IS NULL;
