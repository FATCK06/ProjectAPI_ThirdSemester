-- Job de limpeza das importacoes que nao chegaram a gravar viagens.
--
-- Roda no proprio banco (pg_cron do Supabase), de hora em hora, sem depender
-- de backend no ar. Apaga:
--   ERRO, AGUARDANDO, VALIDADO  -> apos 24h (arquivo recusado ou abandonado)
--   EM_PROCESSAMENTO            -> apos 6h  (servidor caiu no meio)
-- CONCLUIDO nunca e apagado: viagens apontam para ele por importacao_id.
--
-- data_importacao e gravada pelo Java com LocalDateTime.now() (horario de
-- Brasilia), e o now() do Supabase e UTC - dai o "at time zone".
--
-- O not exists protege o caso raro de o servidor cair depois de gravar as
-- viagens e antes de marcar CONCLUIDO.
--
-- cron.schedule com um nome que ja existe substitui o job, entao este script
-- pode ser rodado de novo sem duplicar.

create extension if not exists pg_cron;

select cron.schedule(
  'limpar-importacoes',
  '0 * * * *',
  $$
  delete from public.importacoes i
  where (
          (i.status in ('ERRO', 'AGUARDANDO', 'VALIDADO')
           and i.data_importacao < (now() at time zone 'America/Sao_Paulo') - interval '24 hours')
       or (i.status = 'EM_PROCESSAMENTO'
           and i.data_importacao < (now() at time zone 'America/Sao_Paulo') - interval '6 hours')
        )
    and not exists (select 1 from public.viagens v where v.importacao_id = i.id);
  $$
);

-- Conferencia:
--   select jobid, jobname, schedule, active from cron.job;
--   select status, return_message, start_time from cron.job_run_details
--     order by start_time desc limit 10;
--
-- Remover o job:
--   select cron.unschedule('limpar-importacoes');
