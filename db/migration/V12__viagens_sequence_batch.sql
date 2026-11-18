ALTER TABLE public.viagens
    ALTER COLUMN id_viagem DROP IDENTITY;

CREATE SEQUENCE public.viagens_id_viagem_seq
    INCREMENT BY 50
    MINVALUE 1;

SELECT setval(
    'public.viagens_id_viagem_seq',
    COALESCE((SELECT MAX(id_viagem)::bigint + 1 FROM public.viagens), 1),
    false
);

ALTER TABLE public.viagens
    ALTER COLUMN id_viagem SET DEFAULT nextval('public.viagens_id_viagem_seq');

ALTER SEQUENCE public.viagens_id_viagem_seq
    OWNED BY public.viagens.id_viagem;
