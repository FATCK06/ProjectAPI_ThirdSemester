-- =====================================================
-- ALTERAÇÕES: renomeação de categorias_veiculos -> tipo_veiculos
-- =====================================================

-- TABELA: tipo_veiculos
-- Antes: categorias_veiculos
-- Cadastro de tipos de veículo (ex: Caminhão, Van, Carreta).
create table public.tipo_veiculos (
  -- Antes: id_categoria
  id_tipo serial not null,

  descricao character varying(50) not null,

  -- Nome antigo mantido: renomear tabela/coluna não renomeia a constraint
  constraint categorias_veiculos_pkey primary key (id_tipo)
) TABLESPACE pg_default;


-- TABELA: veiculos (apenas o que mudou)
create table public.veiculos (
  -- ... demais colunas sem alteração ...

  -- Antes: id_categoria. FK -> tipo_veiculos (tipo do veículo)
  id_tipo integer null,

  -- ... demais colunas sem alteração ...

  -- Nome antigo mantido (veiculos_id_categoria_fkey), mas agora
  -- aponta para tipo_veiculos (id_tipo)
  constraint veiculos_id_categoria_fkey
    foreign KEY (id_tipo) references tipo_veiculos (id_tipo)

  -- ... demais constraints sem alteração ...
) TABLESPACE pg_default;