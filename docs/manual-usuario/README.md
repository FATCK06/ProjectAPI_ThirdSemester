# Manual do Usuário

**Sistema de Controle Simplificado dos Motoristas Agregados**\
Parceiro: Newelog · Equipe RubyFox · FATEC São José dos Campos

---

## Sumário

1. [Apresentação](#1-apresentação)
2. [Perfis de acesso](#2-perfis-de-acesso)
3. [Acessando o sistema](#3-acessando-o-sistema)
4. [Navegação](#4-navegação)
5. [Dashboard](#5-dashboard)
6. [Importação de manifestos](#6-importação-de-manifestos)
7. [Cadastrar usuário](#7-cadastrar-usuário-administrador)
8. [Status dos serviços](#8-status-dos-serviços-administrador)
9. [Dúvidas frequentes](#9-dúvidas-frequentes)
10. [Glossário](#10-glossário)

---

## 1. Apresentação

O sistema centraliza o acompanhamento dos motoristas agregados: quantas viagens cada um fez, quantos dias esteve disponível e quanto cada operação rendeu. Os dados vêm da planilha de manifestos (CSV) exportada pela operação e são transformados em indicadores no Dashboard.

**Fluxo de uso típico:**

1. O operador importa o CSV de manifestos do período.
2. O sistema confere o arquivo e aponta os problemas antes de gravar qualquer coisa.
3. Com o arquivo corrigido, as viagens são gravadas.
4. O gestor acompanha os indicadores do mês no Dashboard.

## 2. Perfis de acesso

| Perfil | O que pode fazer |
|---|---|
| **Operador** | Acessar o Dashboard e importar manifestos |
| **Gestor** | Acessar o Dashboard e importar manifestos |
| **Administrador** | Tudo acima, além de cadastrar usuários e consultar o status dos serviços |

O perfil aparece no rodapé do menu lateral, abaixo do seu nome.

## 3. Acessando o sistema

<!-- Print sugerido: img/01-login.png -->

1. Abra o endereço do sistema no navegador.
2. Na tela **Faça seu Login**, informe seu **E-mail** e sua **Senha**.
3. Clique em **Login**.

Com o login feito, você é levado ao Dashboard.

**Se aparecer "Não foi possível entrar":**

- confira se o e-mail e a senha estão corretos;
- se o usuário estiver inativo, procure o Administrador;
- se os dados estiverem certos, o servidor pode estar fora do ar. Tente de novo em alguns minutos ou avise o Administrador.

> A sessão dura **2 horas**. Depois disso, faça login novamente.

### Saindo do sistema

Clique em **Sair**, no rodapé do menu lateral.

## 4. Navegação

<!-- Print sugerido: img/02-menu.png -->

O menu lateral fica à esquerda da tela:

| Item | Descrição |
|---|---|
| **Dashboard** | Indicadores e ranking de motoristas |
| **Importação de Dados → Manifestos** | Importação do CSV de manifestos |
| **Cadastrar usuário** | Somente Administrador |
| **Status dos serviços** | Somente Administrador |
| **Sair** | Encerra a sessão |

O botão no topo do menu recolhe a barra para mostrar só os ícones, deixando mais espaço para o conteúdo. Clique de novo para expandir.

## 5. Dashboard

<!-- Print sugerido: img/03-dashboard.png -->

O Dashboard mostra o desempenho da operação no mês. Ele tem quatro blocos.

### 5.1 Card de indicadores

O título mostra o mês de referência (ex.: **Indicadores de 2026-06**). Os indicadores são divididos em duas categorias:

| Categoria | Indicador | O que significa |
|---|---|---|
| Operacional | **Nº de viagens** | Total de viagens no mês |
| Operacional | **Dias disponíveis** | Dias do mês em que os motoristas podiam operar |
| Operacional | **% de utilização** | Quanto dos dias disponíveis foi de fato usado em viagens |
| Financeiro | **Valor total dos fretes** | Soma do valor de frete das viagens |
| Financeiro | **Custo total** | Soma dos custos das viagens |
| Financeiro | **Rentabilidade total** | Valor dos fretes menos custo total |
| Financeiro | **Rentabilidade média/viagem** | Rentabilidade dividida pelo número de viagens |
| Financeiro | **Margem** | Rentabilidade como percentual do valor dos fretes |

**Escolhendo o que aparece no card:**

1. Clique em **Filtros**, no canto do card. O número ao lado mostra quantos indicadores estão visíveis.
2. Marque ou desmarque os indicadores desejados.
3. Use **Selecionar todos** ou **Limpar** para mudar tudo de uma vez.

### 5.2 Rentabilidade por motorista

Gráfico de barras com os **10 motoristas mais rentáveis** do mês. Passe o mouse sobre uma barra para ver o nome completo e o valor em reais.

### 5.3 Rentabilidade por modelo de veículo

Gráfico de barras com a rentabilidade agrupada pelo modelo do veículo. Serve para comparar quais tipos de veículo rendem mais.

### 5.4 Ranking de motoristas

<!-- Print sugerido: img/04-ranking.png -->

Tabela com os **5 motoristas com mais viagens** no mês escolhido.

1. Use o campo **Mês**, no canto do ranking, para escolher o período.
2. A tabela é atualizada sozinha.

| Coluna | Descrição |
|---|---|
| Motorista | Nome do motorista |
| Tipo de veículo | Placa do veículo usado na viagem mais longa do mês |
| Nº de viagens | Viagens realizadas no mês |
| Disponibilidade | Dias disponíveis sobre os dias do mês |
| Utilização | Dias em operação sobre os dias disponíveis |
| Valor dos fretes | Soma dos fretes do motorista |
| Custos | Soma dos custos |
| Rentabilidade | Fretes menos custos |
| Rentab. média por viagem | Rentabilidade dividida pelas viagens |

Se aparecer **"Nenhuma viagem importada para este mês"**, ainda não há manifestos importados para aquele período. Se aparecer **"Não foi possível carregar o ranking"**, clique em **Tentar novamente**.

## 6. Importação de manifestos

Acesse **Importação de Dados → Manifestos**. A importação é feita em **6 etapas**, mostradas no topo da tela. Cada etapa aparece como *Pendente*, *Em progresso* ou *Concluído*.

> **Importante:** nada é gravado no banco até você confirmar na etapa 4. Até lá, você pode voltar, conferir e trocar o arquivo à vontade.

Use **Anterior** e **Continuar**, no rodapé, para navegar entre as etapas. Se o botão **Continuar** estiver desabilitado, passe o mouse sobre ele para ver o motivo.

### Etapa 1: Upload de arquivo

<!-- Print sugerido: img/05-upload.png -->

1. Arraste o arquivo para a área indicada, ou clique nela para escolher o arquivo no computador. O envio começa sozinho.
2. Confira o nome do arquivo em **Arquivo selecionado**. Para trocar, clique no ícone de lixeira.
3. Aparece a mensagem *"Arquivo #N recebido e aguardando. Nada foi gravado ainda"*. Clique em **Continuar**.

Se o envio falhar (por queda de rede, por exemplo), clique em **Tentar novamente**.

**Requisitos do arquivo:**

- formato **.csv**;
- tamanho máximo de **10 MB**;
- a primeira linha deve ser o cabeçalho com os nomes das colunas.

### Etapa 2: Mapeamento de colunas

<!-- Print sugerido: img/06-mapeamento.png -->

O sistema compara as colunas do arquivo com as que ele espera e mostra, para cada uma, o nome encontrado no seu arquivo ou **não encontrada**.

- O **\*** marca as colunas **obrigatórias**: **Manifesto**, **Data**, **CPF** e **Veículo**.
- Sem uma coluna obrigatória, não é possível continuar. Corrija o cabeçalho do arquivo e envie de novo.
- Colunas opcionais ausentes não impedem a importação. Os campos correspondentes ficam vazios.

### Etapa 3: Validação de dados

<!-- Print sugerido: img/07-validacao.png -->

O sistema confere linha por linha e mostra um resumo:

| Card | Significado |
|---|---|
| **linhas no arquivo** | Total de linhas lidas, sem contar o cabeçalho |
| **sem problema** | Linhas prontas para gravar |
| **com aviso** | Linhas com pendência. **Não bloqueiam** a importação, mas algum dado importante veio vazio ou em formato errado |
| **com erro** | Linhas que **bloqueiam** a importação |

Clique em **com aviso** ou **com erro** para listar só esse tipo de linha. **Mostrar todas** remove o filtro. Com muitas linhas, use **Anterior** e **Próxima** para trocar de página.

Cada linha com problema mostra a coluna, o valor que veio no arquivo, o formato esperado e uma mensagem explicando o que corrigir. Por exemplo:

> *A coluna 'CPF' na linha 17 não está no formato esperado: Esperado 11 dígitos, veio com 10*

**Formatos esperados:**

| Tipo de dado | Formato |
|---|---|
| Data | `DD/MM/AAAA` |
| Número decimal | `1.234,56` |
| Número inteiro | `12` (também aceita `12,00`) |
| CPF | 11 dígitos |
| Documentos (PIS, CEP, CNPJ) | Só dígitos. Pontos, traços e barras são ignorados |

**Quando há erro:** corrija o arquivo na planilha, volte à etapa 1, remova o arquivo antigo e envie o corrigido. Enquanto houver linha com erro, não dá para avançar.

### Etapa 4: Revisão / Preview

<!-- Print sugerido: img/08-revisao.png -->

Mostra as primeiras linhas que serão importadas, com **Manifesto**, **Data**, **Mês**, **Motorista**, **CPF**, **Agregado**, **Veículo**, **Destino** e **Valor do frete**. Confira se os dados fazem sentido.

> A coluna **Mês** vem da data de cada linha, não do arquivo. Por isso, um arquivo trimestral gera viagens em três meses diferentes.

Quando tudo estiver certo, clique em **Enviar os Dados**. O sistema pede confirmação:

> *Enviar os dados para o banco? N viagens serão gravadas a partir de arquivo.csv.*

Clique em **Sim, enviar** para gravar ou em **Cancelar** para voltar.

### Etapa 5: Confirmação e execução

A tela mostra o andamento da gravação. Aguarde sem fechar a página. Ao terminar, aparece **Dados gravados** e o sistema avança sozinho.

Se aparecer **Não foi possível gravar**, leia a mensagem exibida e tente novamente. Se o erro continuar, avise o Administrador.

### Etapa 6: Resultado

<!-- Print sugerido: img/09-resultado.png -->

Mostra o resumo da importação:

| Informação | Significado |
|---|---|
| **viagens gravadas** | Viagens novas salvas no banco |
| **linhas lidas** | Total de linhas do arquivo |
| **já existiam** | Manifestos que já tinham sido importados antes e foram ignorados |

> Reimportar o mesmo arquivo **não duplica** dados: manifestos já gravados são ignorados.

Clique em **Importar outro arquivo** para começar de novo.

## 7. Cadastrar usuário (Administrador)

<!-- Print sugerido: img/10-cadastro-usuario.png -->

1. No menu, clique em **Cadastrar usuário**.
2. Preencha:
   - **Nome**: nome completo;
   - **E-mail**: usado no login, não pode repetir;
   - **Senha**: mínimo de 4 caracteres. Use o ícone de olho para mostrar ou ocultar;
   - **Perfil de acesso**: Operador, Gestor ou Administrador.
3. Clique em **Cadastrar**.

Se der certo, aparece *"Nome cadastrado como Perfil. Já pode fazer login."*. Se o e-mail já estiver cadastrado, o sistema avisa e não cria o usuário.

> A senha é criptografada antes de ser gravada. Nem o Administrador consegue vê-la depois.

## 8. Status dos serviços (Administrador)

<!-- Print sugerido: img/11-status.png -->

Mostra se cada parte do sistema está funcionando. É útil quando alguma tela não carrega.

- No topo aparece **Todos os serviços estão no ar** ou **Há serviço fora do ar**.
- Cada serviço aparece com o status **No ar** ou **Fora** e o tempo de resposta em milissegundos.
- A tela atualiza sozinha a cada 30 segundos. Use **Atualizar** para consultar na hora.

| Serviço | Responsável por |
|---|---|
| ms-usuarios | Login e cadastro de usuários |
| ms-frota | Motoristas, veículos e agregados |
| ms-operacoes | Importação, viagens e Dashboard |

> Um serviço que está rodando mas não consegue acessar o banco de dados aparece como **Fora**.

## 9. Dúvidas frequentes

**O Dashboard está vazio.**
Ainda não há viagens importadas para o mês exibido. Importe o manifesto do período, ou troque o mês no Ranking.

**Importei o arquivo, mas os números não mudaram.**
Confira se a importação chegou à etapa 6 (Resultado). Se todas as linhas aparecerem como *já existiam*, o arquivo já tinha sido importado antes.

**O botão Continuar não funciona.**
Passe o mouse sobre ele para ver o motivo. Em geral falta enviar o arquivo, falta uma coluna obrigatória ou há linhas com erro.

**Minha linha tem "aviso". Preciso corrigir?**
Não é obrigatório: avisos não bloqueiam a importação. Mas o dado que faltou fica vazio e pode afetar os indicadores. Por exemplo, sem **Valor Frete** a rentabilidade da viagem fica errada.

**Fui desconectado do nada.**
A sessão expira em 2 horas. Faça login novamente.

**Não vejo "Cadastrar usuário" nem "Status dos serviços".**
Essas opções aparecem só para o perfil Administrador.

## 10. Glossário

| Termo | Significado |
|---|---|
| **Agregado** | Transportador terceiro (pessoa física ou jurídica) contratado pela Newelog |
| **Manifesto** | Documento de uma viagem; cada linha do CSV é um manifesto |
| **Mês de referência** | Mês ao qual a viagem pertence, tirado da data da linha |
| **Disponibilidade** | Dias disponíveis ÷ dias do mês |
| **Utilização** | Dias em operação ÷ dias disponíveis |
| **Rentabilidade** | Valor do frete − custos |
| **Margem** | Rentabilidade ÷ valor do frete |
| **Pendência / aviso** | Problema que não impede a importação |
| **Erro** | Problema que impede a importação do arquivo |
