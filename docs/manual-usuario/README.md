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
6. [Motoristas](#6-motoristas)
7. [Ranking mensal](#7-ranking-mensal)
8. [Importação de manifestos](#8-importação-de-manifestos)
9. [Histórico de importações](#9-histórico-de-importações)
10. [Cadastrar usuário](#10-cadastrar-usuário-administrador)
11. [Status dos serviços](#11-status-dos-serviços-administrador)
12. [Dúvidas frequentes](#12-dúvidas-frequentes)
13. [Glossário](#13-glossário)

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
| **Motoristas** | Consulta motoristas e indicadores operacionais do mês, como viagens, dias em operação, disponibilidade e utilização |
| **Ranking mensal** | Compara e ordena o desempenho dos motoristas no período selecionado |
| **Importação de Dados → Manifestos** | Importação do CSV de manifestos |
| **Importação de Dados → Histórico de Importações** | Listagem de arquivos importados |
| **Cadastrar usuário** | Somente Administrador |
| **Status dos serviços** | Somente Administrador |
| **Sair** | Encerra a sessão |

O botão no topo do menu recolhe a barra para mostrar só os ícones, deixando mais espaço para o conteúdo. Clique de novo para expandir.

## 5. Dashboard

<!-- Print sugerido: img/03-dashboard.png -->

O Dashboard mostra o desempenho da operação no mês. Ele tem um seletor de mês e quatro blocos. Usuários Operadores veem os indicadores operacionais e o ranking; Gestores e Administradores também veem os indicadores financeiros e os gráficos de rentabilidade.

### 5.0 Escolhendo o mês

O seletor no topo da tela vale para **todos os blocos** (indicadores, gráficos e ranking):

| Opção | O que mostra |
|---|---|
| **Mês atual** | O mês do calendário (é a opção que abre por padrão) |
| **Mês anterior** | O mês anterior ao atual |
| **Selecionar mês** | Abre o calendário para escolher qualquer mês até o atual. O mês escolhido aparece no próprio botão |

> O mês atual costuma aparecer vazio até a planilha dele ser importada. Para ver meses já fechados, use **Mês anterior** ou **Selecionar mês**.

### 5.1 Card de indicadores

O título mostra o mês de referência (ex.: **Indicadores de junho de 2026**). Os indicadores são divididos em duas categorias:

| Categoria | Indicador | O que significa |
|---|---|---|
| Operacional | **Nº de viagens** | Total de viagens no mês |
| Operacional | **Dias em operação*** | Soma, por motorista, das datas diferentes com manifesto. Viagem de mais de um dia conta como 1, pois a coluna Diária do CSV vem vazia |
| Operacional | **Dias disponíveis** | Dias do mês em que os motoristas podiam operar. Exibe "–": o CSV não informa férias, afastamento ou manutenção |
| Operacional | **% de utilização** | Quanto dos dias disponíveis foi de fato usado em viagens. Exibe "–" enquanto não houver dias disponíveis |
| Financeiro | **Valor total dos fretes** | Quanto os clientes pagaram à Newe (coluna *Valor Fretes*) |
| Financeiro | **Custo total** | Quanto a Newe pagou ao motorista (coluna *Vale frete*) |
| Financeiro | **Rentabilidade total** | Valor dos fretes menos custo total |
| Financeiro | **Rentabilidade média/viagem** | Rentabilidade dividida pelas viagens com frete e custo informados no CSV |
| Financeiro | **Margem** | Rentabilidade como percentual do valor dos fretes |

> Os cinco indicadores financeiros só somam as viagens com frete **e** custo informados no CSV. Quando alguma viagem do mês veio sem esses valores, o valor ganha um **\*** e o tooltip mostra quantas ficaram de fora.

**Comparação com meses anteriores:** use **Comparar com**, no canto do card, para escolher o período. O subtítulo do card mostra contra o que está comparando (ex.: "Comparando com a média de abr. a mai. de 2026").

| Opção | Variação abaixo do valor | Gráfico à direita |
|---|---|---|
| **Mês anterior** (padrão) | Contra o mês anterior, ex.: "vs. mai. de 2026 (596)" | Não tem: dois pontos não formam tendência |
| **Últimos 2 meses** / **Últimos 3 meses** | Contra a **média** do período, ex.: "vs. média de abr. a mai. de 2026 (694)" | Linha com o período e o mês exibido. Passe o mouse para ver o valor de cada mês |

A seta fica verde quando o indicador melhorou e vermelha quando piorou. Dias disponíveis e % de utilização não têm comparação enquanto exibirem "-".

**Só aparecem as opções que têm dados:** um período só fica disponível se **todos** os meses dele tiverem viagens importadas. Exemplo com abril a junho importados:

| Mês exibido | Opções disponíveis |
|---|---|
| Junho | Mês anterior e Últimos 2 meses (maio e abril). Últimos 3 meses aparece como "(sem dados)", porque março não foi importado |
| Maio | Só Mês anterior (abril) |
| Abril ou janeiro | Nenhuma: não há meses anteriores importados |
| Outubro (ainda não importado) | Nenhuma: sem viagens no próprio mês, não há o que comparar |

A escolha do período e dos indicadores continua valendo quando você troca o mês no seletor do topo. Se o novo mês tiver menos histórico, o card usa o maior período possível e volta ao escolhido quando houver dados.

Quando o número de indicadores não fecha a última linha, os que sobram ficam **centralizados**, com o mesmo tamanho dos de cima.

**Escolhendo o que aparece no card:**

1. Clique em **Filtros**, no canto do card. O número ao lado mostra quantos indicadores estão visíveis.
2. Marque ou desmarque os indicadores desejados.
3. Use **Selecionar todos** ou **Limpar** para mudar tudo de uma vez.

### 5.2 Rentabilidade por motorista

Gráfico de barras com os **10 motoristas mais rentáveis** do mês. Passe o mouse sobre uma barra para ver o nome completo e o valor em reais. Disponível para Gestores e Administradores.

### 5.3 Rentabilidade por modelo de veículo

Gráfico de barras com a rentabilidade agrupada pelo modelo do veículo. Serve para comparar quais tipos de veículo rendem mais. Disponível para Gestores e Administradores.

### 5.4 Ranking de motoristas

<!-- Print sugerido: img/04-ranking.png -->

Tabela com os **5 motoristas com mais viagens** no mês escolhido no seletor do topo da tela.

| Coluna | Descrição |
|---|---|
| Motorista | Nome do motorista |
| Tipo de veículo | Tipo/modelo do veículo usado na viagem mais longa do mês |
| Nº de viagens | Viagens realizadas no mês |
| Disponibilidade | Dias disponíveis sobre os dias do mês. Exibe "–" até termos os dias disponíveis |
| Utilização | Dias em operação sobre os dias disponíveis. Exibe "–" até termos os dias disponíveis |
| Valor dos fretes | Soma dos fretes do motorista |
| Custos | Soma dos custos |
| Rentabilidade | Fretes menos custos |
| Rentab. média por viagem | Rentabilidade dividida pelas viagens |

Se aparecer **"Nenhuma viagem importada para este mês"**, ainda não há manifestos importados para aquele período. Se aparecer **"Não foi possível carregar o ranking"**, clique em **Tentar novamente**. Os valores financeiros não aparecem para Operadores.

## 6. Motoristas

<!-- Print sugerido: img/12-motoristas.png -->

A página **Motoristas** apresenta os motoristas e seus indicadores operacionais no mês escolhido. Use os filtros para localizar registros:

- **Buscar motorista ou placa**: filtra pelo nome do motorista ou pela placa;
- **Tipo de veículo**: mostra apenas os motoristas associados ao tipo selecionado;
- **Mês**: consulta os dados do período escolhido.

Clique nos cabeçalhos da tabela para ordenar por motorista, tipo ou modelo de veículo, placa, viagens, dias em operação, disponibilidade, utilização ou efetividade. A lista é paginada; use **Anterior** e **Próxima** para navegar.

Clique no nome de um motorista para abrir sua ficha cadastral, com dados pessoais e endereço. Use **Voltar para motoristas** para retornar à lista.

Se não houver registros para o mês ou para os filtros escolhidos, a página informa que nenhum motorista foi encontrado. 

## 7. Ranking mensal

<!-- Print sugerido: img/13-ranking-mensal.png -->

A página **Ranking mensal** lista os motoristas e permite comparar e ordenar seus resultados no período. Escolha o **Mês de referência** no topo da página. Clique no cabeçalho de uma coluna para ordenar; clique novamente para alternar entre ordem crescente e decrescente.

A tabela inclui motorista, tipo de veículo, viagens, disponibilidade e utilização. Gestores e Administradores também veem valor dos fretes, custos, rentabilidade e rentabilidade média por viagem. Operadores não veem os valores financeiros.

Se não houver dados para o período, a página informa que não há dados importados.

## 8. Importação de manifestos

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

## 9. Histórico de importações

<!-- Print sugerido: img/14-historico-importacoes.png -->

A página **Importação de Dados → Histórico de Importações** apresenta uma tabela com o nome do arquivo, a data da importação, o usuário responsável e o status. Enquanto os dados são carregados, aparece a indicação **Carregando...**; se não for possível obter o histórico, a página mostra uma mensagem de erro.

## 10. Cadastrar usuário (Administrador)

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

## 11. Status dos serviços (Administrador)

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

## 12. Dúvidas frequentes

**O Dashboard está vazio.**
Ainda não há viagens importadas para o mês exibido. O Dashboard abre no mês atual, que costuma estar vazio até a planilha dele ser importada. Use **Mês anterior** ou **Selecionar mês** no topo da tela.

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

## 13. Glossário

| Termo | Significado |
|---|---|
| **Agregado** | Transportador terceiro (pessoa física ou jurídica) contratado pela Newelog |
| **Manifesto** | Documento de uma viagem; cada linha do CSV é um manifesto |
| **Mês de referência** | Mês ao qual a viagem pertence, tirado da data da linha |
| **Disponibilidade** | Dias disponíveis ÷ dias do mês |
| **Utilização** | Dias em operação ÷ dias disponíveis |
| **Rentabilidade** | Valor do frete − custos |
| **Margem** | Rentabilidade ÷ valor do frete |
| **Valor Fretes** (coluna do CSV) | Quanto o cliente pagou à Newe pela viagem. É a **receita** |
| **Valor Frete** (coluna do CSV) | Frete-base que a Newe paga ao motorista, sem pedágio nem extras |
| **Vale frete** (coluna do CSV) | Total pago ao motorista: Valor Frete + pedágio + diárias + adicionais. É o **custo** |
| **Pendência / aviso** | Problema que não impede a importação |
| **Erro** | Problema que impede a importação do arquivo |
