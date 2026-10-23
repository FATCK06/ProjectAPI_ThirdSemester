-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.usuarios (
  id_usuario uuid NOT NULL DEFAULT gen_random_uuid(),
  nome character varying NOT NULL,
  email character varying NOT NULL UNIQUE,
  perfil_acesso character varying NOT NULL,
  status_ativo boolean DEFAULT true,
  criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
  senha character varying,
  CONSTRAINT usuarios_pkey PRIMARY KEY (id_usuario)
);
CREATE TABLE public.estados (
  id_estado integer NOT NULL DEFAULT nextval('estados_id_estado_seq'::regclass),
  sigla character varying NOT NULL UNIQUE,
  nome_estado character varying NOT NULL,
  CONSTRAINT estados_pkey PRIMARY KEY (id_estado)
);
CREATE TABLE public.cidades (
  id_cidade integer NOT NULL DEFAULT nextval('cidades_id_cidade_seq'::regclass),
  id_estado integer NOT NULL,
  nome_cidade character varying NOT NULL,
  CONSTRAINT cidades_pkey PRIMARY KEY (id_cidade),
  CONSTRAINT cidades_id_estado_fkey FOREIGN KEY (id_estado) REFERENCES public.estados(id_estado)
);
CREATE TABLE public.tipo_veiculos (
  id_tipo integer NOT NULL DEFAULT nextval('categorias_veiculos_id_categoria_seq'::regclass),
  descricao character varying NOT NULL,
  CONSTRAINT tipo_veiculos_pkey PRIMARY KEY (id_tipo)
);
CREATE TABLE public.tipos_custo (
  id_tipo_custo integer NOT NULL DEFAULT nextval('tipos_custo_id_tipo_custo_seq'::regclass),
  descricao character varying NOT NULL UNIQUE,
  CONSTRAINT tipos_custo_pkey PRIMARY KEY (id_tipo_custo)
);
CREATE TABLE public.motoristas (
  id_motorista uuid NOT NULL DEFAULT gen_random_uuid(),
  nome character varying NOT NULL,
  cpf character varying NOT NULL UNIQUE,
  pis character varying,
  data_nascimento date,
  cep character varying,
  logradouro character varying,
  numero character varying,
  bairro character varying,
  id_cidade integer,
  criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT motoristas_pkey PRIMARY KEY (id_motorista),
  CONSTRAINT motoristas_id_cidade_fkey FOREIGN KEY (id_cidade) REFERENCES public.cidades(id_cidade)
);
CREATE TABLE public.veiculos (
  id_veiculo uuid NOT NULL DEFAULT gen_random_uuid(),
  placa character varying NOT NULL UNIQUE,
  id_tipo integer,
  id_motorista_padrao uuid,
  criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
  marca character varying,
  modelo character varying,
  cor character varying,
  combustivel character varying,
  numero_frota character varying,
  ano_fabricacao integer,
  ano_modelo integer,
  renavam character varying UNIQUE,
  chassi character varying UNIQUE,
  id_cidade_licenciamento integer,
  tipo_carroceria character varying,
  tipo_proprietario character varying,
  nome_proprietario character varying,
  nome_agregado character varying,
  tabela_agregado character varying,
  data_compra date,
  valor_compra numeric,
  data_venda date,
  valor_venda numeric,
  pbt numeric,
  tara numeric,
  capacidade numeric,
  capacidade_lotacao numeric,
  numero_autorizacao character varying,
  validade_guarda date,
  status_veiculo character varying DEFAULT 'Ativo'::character varying,
  lote_fechamento_ciot character varying,
  CONSTRAINT veiculos_pkey PRIMARY KEY (id_veiculo),
  CONSTRAINT veiculos_id_motorista_agregado_fkey FOREIGN KEY (id_motorista_padrao) REFERENCES public.motoristas(id_motorista),
  CONSTRAINT veiculos_id_cidade_licenciamento_fkey FOREIGN KEY (id_cidade_licenciamento) REFERENCES public.cidades(id_cidade),
  CONSTRAINT veiculos_id_categoria_fkey FOREIGN KEY (id_tipo) REFERENCES public.tipo_veiculos(id_tipo)
);
CREATE TABLE public.viagem_destinos (
  id_viagem integer NOT NULL,
  id_cidade integer NOT NULL,
  ordem_parada integer NOT NULL,
  CONSTRAINT viagem_destinos_pkey PRIMARY KEY (id_viagem, id_cidade, ordem_parada),
  CONSTRAINT viagem_destinos_id_cidade_fkey FOREIGN KEY (id_cidade) REFERENCES public.cidades(id_cidade),
  CONSTRAINT viagem_destinos_id_viagem_fkey FOREIGN KEY (id_viagem) REFERENCES public.viagens(id_viagem)
);
CREATE TABLE public.viagem_custos (
  id_viagem integer NOT NULL,
  id_tipo_custo integer NOT NULL,
  valor_desembolsado numeric NOT NULL,
  CONSTRAINT viagem_custos_pkey PRIMARY KEY (id_viagem, id_tipo_custo),
  CONSTRAINT viagem_custos_id_tipo_custo_fkey FOREIGN KEY (id_tipo_custo) REFERENCES public.tipos_custo(id_tipo_custo),
  CONSTRAINT viagem_custos_id_viagem_fkey FOREIGN KEY (id_viagem) REFERENCES public.viagens(id_viagem)
);
CREATE TABLE public.controle_disponibilidade (
  id_controle uuid NOT NULL DEFAULT gen_random_uuid(),
  id_motorista uuid NOT NULL,
  mes_ano character varying NOT NULL,
  dias_disponiveis integer NOT NULL DEFAULT 0,
  dias_em_operacao integer NOT NULL DEFAULT 0,
  CONSTRAINT controle_disponibilidade_pkey PRIMARY KEY (id_controle),
  CONSTRAINT controle_disponibilidade_id_motorista_fkey FOREIGN KEY (id_motorista) REFERENCES public.motoristas(id_motorista)
);
CREATE TABLE public.viagens (
  id_viagem integer GENERATED ALWAYS AS IDENTITY NOT NULL,
  id_manifesto integer NOT NULL UNIQUE,
  id_motorista uuid NOT NULL,
  id_veiculo uuid NOT NULL,
  filial character varying,
  data_viagem date NOT NULL,
  kg_real numeric,
  kg_taxado numeric,
  m3 numeric,
  valor_frete numeric,
  status character varying,
  criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
  id_agregado uuid,
  km_saida integer,
  km_chegada integer,
  servicos integer,
  servicos_finalizados integer,
  nfs integer,
  coletas integer,
  entregas integer,
  despachos integer,
  retiradas integer,
  coletas_reversa integer,
  percentual_efetividade numeric,
  percentual_aprov_veiculo numeric,
  destino character varying,
  reboque1 character varying,
  reboque2 character varying,
  reboque3 character varying,
  classificacao character varying,
  observacoes_operacionais text,
  usuario_manifesto character varying,
  valor_nf numeric,
  valor_fretes numeric,
  total_despesas numeric,
  saldo_despesas numeric,
  saldo_a_pagar numeric,
  mes_referencia character varying,
  importacao_id bigint,
  vale_frete numeric,
  CONSTRAINT viagens_pkey PRIMARY KEY (id_viagem),
  CONSTRAINT viagens_id_motorista_fkey FOREIGN KEY (id_motorista) REFERENCES public.motoristas(id_motorista),
  CONSTRAINT viagens_id_veiculo_fkey FOREIGN KEY (id_veiculo) REFERENCES public.veiculos(id_veiculo),
  CONSTRAINT viagens_id_agregado_fkey FOREIGN KEY (id_agregado) REFERENCES public.agregados(id_agregado),
  CONSTRAINT viagens_arquivo_importacao_fkey FOREIGN KEY (importacao_id) REFERENCES public.importacoes(id)
);
CREATE TABLE public.importacoes (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  arquivo_nome character varying,
  data_importacao timestamp without time zone,
  status character varying CHECK (status::text = ANY (ARRAY['AGUARDANDO'::character varying, 'VALIDADO'::character varying, 'EM_PROCESSAMENTO'::character varying, 'CONCLUIDO'::character varying, 'ERRO'::character varying]::text[])),
  linhas_gravadas integer,
  usuario_responsavel character varying,
  arquivo_conteudo bytea,
  total_linhas integer,
  linhas_validas integer,
  linhas_invalidas integer,
  processamento_iniciado_em timestamp without time zone,
  CONSTRAINT importacoes_pkey PRIMARY KEY (id)
);
CREATE TABLE public.agregados (
  id_agregado uuid NOT NULL DEFAULT gen_random_uuid(),
  documento character varying NOT NULL UNIQUE CHECK (documento::text ~ '^[0-9]+$'::text),
  tipo_pessoa character varying NOT NULL CHECK (tipo_pessoa::bpchar = ANY (ARRAY['PF'::bpchar, 'PJ'::bpchar])),
  nome character varying NOT NULL,
  pis character varying,
  regime_fiscal character varying,
  criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT agregados_pkey PRIMARY KEY (id_agregado)
);