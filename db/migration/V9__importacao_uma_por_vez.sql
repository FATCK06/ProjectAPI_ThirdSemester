-- Uma importacao gravando por vez.
--
-- Em 15/10/2026 o mesmo arquivo foi executado de novo enquanto a primeira
-- execucao (9 min, uma transacao so) ainda gravava. A segunda ficou esperando o
-- lock do indice de id_manifesto ate estourar o statement_timeout.
--
-- A aplicacao so reserva EM_PROCESSAMENTO se nenhuma outra estiver nesse estado.
-- Este indice garante a mesma regra no banco: duas reservas no mesmo instante, ou
-- vindas de instancias diferentes do ms-operacoes (cada dev roda a sua contra o
-- mesmo Supabase), nao passam juntas.

ALTER TABLE public.importacoes ADD COLUMN processamento_iniciado_em timestamp;

-- Indice unico sobre uma constante: no maximo uma linha satisfaz o WHERE.
CREATE UNIQUE INDEX importacoes_uma_em_processamento
    ON public.importacoes ((true))
    WHERE status = 'EM_PROCESSAMENTO';
