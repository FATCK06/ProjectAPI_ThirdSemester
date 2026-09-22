-- Corrige o tipo de agregados.tipo_pessoa, declarado como char(2) na V2.
--
-- Dois motivos:
--   1. O Hibernate mapeia String como varchar, entao o ddl-auto=validate recusava
--      subir: "found [bpchar], but expecting [varchar(2)]".
--   2. char(n) no Postgres completa o valor com espacos a direita. 'PF' virava
--      'PF' com padding, e comparacoes passariam a depender de trim.
--
-- Vai numa migration nova, e nao editando a V2, porque a V2 ja foi aplicada.
-- Editar migration aplicada quebra o checksum do Flyway e faz o banco de quem
-- ja rodou divergir do de quem rodar depois.

ALTER TABLE public.agregados
    ALTER COLUMN tipo_pessoa TYPE varchar(2) USING trim(tipo_pessoa);
