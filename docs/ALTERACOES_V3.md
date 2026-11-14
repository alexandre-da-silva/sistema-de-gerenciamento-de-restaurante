# AstraChefe — evolução de catálogo, pedidos e sessão (V3)

## Como aplicar

1. Faça **backup do banco MySQL e das imagens** antes da atualização.
2. Substitua os arquivos da aplicação pela versão deste ZIP, preservando seu `.env` e os arquivos de imagens armazenados fora do pacote.
3. Execute com **Java 21, Maven e MySQL 8**: `mvn spring-boot:run` na raiz. O Flyway aplicará `V3__catalogo_profissional.sql` automaticamente, depois das versões V1 e V2.
4. Acesse a interface servida pelo próprio Spring Boot, no endereço configurado no `application.yml` (tipicamente `http://localhost:8080`). Use `Ctrl+F5` na primeira visita, se o navegador manteve alguma versão antiga em cache.
5. Em **Admin > Configurações**, habilite **CONTROLE_ESTOQUE** para consumir ingredientes automaticamente. Cadastre os ingredientes, seus custos e saldos em **Admin > Estoque**, e depois monte a ficha técnica em **Admin > Cardápio > Novo produto**. Com o controle desligado, os pedidos continuam sendo aceitos sem baixa de estoque.

> Não execute o SQL V3 manualmente em um banco já gerenciado pelo Flyway. Não publique `.env`, senhas ou segredos JWT.

## Mudanças funcionais

- **Home do cliente:** diferente de Cardápio. Mostra a seleção "Em alta", ordenada por unidades de produtos em pedidos `ENTREGUE` criados nos últimos **30 dias**. O selo **Popular** e a chama vermelha aparecem para produtos com pelo menos **3 unidades** e pelo menos **metade da quantidade do líder**. Itens sem esse nível de vendas exibem chama apagada/cinza, sem selo. Quando ainda não existem vendas concluídas, aparecem produtos com destaque manual ou opções disponíveis do catálogo.
- **Cardápio:** seção independente, com busca e filtro por categoria.
- **Sessão e F5:** JWT e última página ficam no `sessionStorage` da aba. Na recarga, a sessão é validada por `GET /api/v1/auth/me`; se ainda for válida, a mesma tela reaparece com informações atualizadas. O logout remove a sessão. Fechar a aba pode encerrar a sessão (preferência por menor persistência do token).
- **Atualizações periódicas:** cliente e administrador recarregam dados automaticamente a cada **10 segundos**, enquanto a aba está ativa; pedidos também podem ser atualizados manualmente. Isso é **polling** e não WebSocket/SSE instantâneo. Não atualiza o formulário de edição enquanto aberto.
- **Cadastro de produto:** cinco grupos (essenciais; operação; ficha técnica e custos; variações e adicionais; saúde e tributos), com SKU único, setor, porção, ordem, horários por dia, tags, alérgenos, kcal, NCM/CFOP/CST-CSOSN/ICMS e foto.
- **Custo/margem:** custo estimado = soma da quantidade de insumo × custo unitário; margem bruta = preço de venda menos custo (e percentual sobre venda). O preço promocional é considerado na margem. Não inclui impostos, mão de obra, taxas ou perdas. Variações atualmente compartilham a mesma ficha técnica do produto.
- **Variações e adicionais:** o servidor valida IDs pertencentes ao produto, calcula os preços e registra a descrição da seleção no item do pedido. Os nomes e preços ficam preservados no histórico da compra.
- **Disponibilidade:** quando há horários definidos, só aceita pedidos naquele dia e intervalo. A checagem atual usa `America/Sao_Paulo` e o intervalo é início inclusivo/fim exclusivo. Horários que atravessam a meia-noite exigem duas linhas.
- **Baixa de estoque:** se `CONTROLE_ESTOQUE` estiver habilitado, a criação do pedido baixa os insumos da ficha técnica numa transação única, com trava por ingrediente e verificação de saldo; na cancelamento, movimentações vinculadas ao pedido permitem estorno uma vez. A regra considera as quantidades-base da ficha técnica; ingredientes extras de adicionais não têm ficha própria nesta versão. Desativar o módulo faz os pedidos não movimentarem estoque.
- **Concorrência:** o código já utilizava bloqueio pessimista ao procurar mesas/pedidos para alteração; essa proteção foi preservada. O estoque agora também bloqueia ingredientes envolvidos na baixa/estorno.
- **Fiscal:** novos campos servem para cadastro gerencial. **A emissão real NF-e/NFC-e não foi implementada**: exige provedores ou integração com SEFAZ, credenciamento e homologação.

## Rotas novas

| Método | Endpoint | Uso |
|---|---|---|
| GET | `/api/v1/auth/me` | Dados da conta autenticada e verificação de sessão |
| GET | `/api/v1/publico/em-alta` | Ranking dos produtos vendidos nos últimos 30 dias |
| GET | `/api/v1/admin/produtos/ingredientes` | Insumos para montar ficha técnica |
| PUT | `/api/v1/admin/estoque/{id}/custo` | Alterar custo unitário do insumo |

Os contratos de `POST/PUT /api/v1/admin/produtos` e `POST /api/v1/cliente/pedidos` foram ampliados com campos opcionais para preservar compatibilidade com pedidos simples existentes.

## Verificação nesta entrega

- `node --test tests/admin.test.cjs tests/frontend.test.cjs`: **11 testes passaram**.
- `node --check` dos scripts principais: **sem erro de sintaxe**.
- Fluxo visual no Chromium com respostas simuladas: **Home, Cardápio, selo, variações, adicionais, criação de pedido e formulário do administrador** sem exceção de JavaScript observada.
- O Java/Spring/MySQL **não foi compilado nem executado integrado**, porque o ambiente de geração não tinha Maven e banco configurado. Testar em banco de desenvolvimento antes de atualizar uma operação real.
- Não foi possível instalar o conjunto de dependências do `frontend` via npm por falta de rede. O JS estático incluso no ZIP foi recomposto com as dependências React que já vinham no bundle; para gerar um bundle de produção reproduzível, rode `cd frontend && npm ci && npm run build` em uma máquina com acesso ao registro npm.

## Limitações conhecidas

- A atualização a cada 10 s não é entrega instantânea via WebSocket/SSE.
- Setor de produção é cadastrado, mas a impressão automática por setor e divisão de telas independentes por estação ainda não está conectada.
- O cadastro fiscal não transmite notas fiscais.
- Variações e adicionais não possuem ficha técnica própria; baixam a ficha técnica base do produto.
- Manter JWT em `sessionStorage` requer proteção contínua contra XSS. Para produção, recomendamos sessão curta, HTTPS, CSP rigorosa e avaliação de cookie HttpOnly como alternativa.
