# Painel administrativo AstraChefe

Interface administrativa refeita com base na referência visual fornecida: menu vinho,
destaques laranja, cartões, tabela de pedidos, mapa de mesas, cozinha, gráfico e ranking.
O nome do administrador vem do login; os indicadores e listas vêm do banco.
Nenhum dado fictício é usado pela aplicação. As fixtures estão apenas nos testes.

## Executar

1. Preserve seu `.env` local e o banco existente. O pacote não inclui credenciais.
2. Atualize os fontes com os arquivos deste pacote.
3. Com JDK 21 e Maven instalados, execute `mvn test` e depois `mvn spring-boot:run`.
4. Abra `http://localhost:8080` e entre com seu administrador.
5. Em Configurações, habilite Estoque, Caixa e pagamentos e Relatórios se desejar utilizá-los.

A migração V2 adiciona fotos, promoções, etapas dos pedidos, avaliações e mensagens. O Flyway a aplica ao iniciar; preserve a V1 e não apague tabelas.

## Telas e ações

- Dashboard: indicadores por data, ocupação atual, pedidos em andamento, mapa de até 16 mesas,
  fila de cozinha, recebimentos por hora e ranking de produtos de pedidos pagos.
- Pedidos: busca, filtro, detalhes, mensagens e cancelamento permitido. Somente CLIENTE cria pedidos.
- Mesas: consulta de ocupação, pedidos em aberto e cadastro.
- Cardápio: foto JPG/PNG, descrição, categoria, tipo, tempo, preço, promoção, destaque e publicação/rascunho.
- Cozinha: colunas Aguardando, Em preparo e Pronto, com avanço de status.
- Estoque: saldo, limite mínimo e movimentação manual com motivo.
- Clientes: cadastro de conta, consulta de contato, pedidos e histórico agregado.
- Relatórios: recebimentos diários e exportação CSV.
- Financeiro: confirmação manual do pagamento integral de pedidos entregues.
- Configurações: ativação dos módulos existentes. Emissão fiscal continua indisponível.

A busca filtra a tela atual. O sino abre os pedidos aguardando. O botão de atualização
consulta a API novamente; não há atualização automática em tempo real.

## API nova (somente ADMIN)

- `GET /api/v1/admin/dashboard?data=AAAA-MM-DD`
- `GET /api/v1/admin/pedidos/detalhes`
- `GET /api/v1/admin/clientes`

Listas de pedidos/clientes limitadas a 500 registros. Ocupação e fila são atuais mesmo
quando outra data é selecionada para os indicadores de vendas. O faturamento é baseado
na data dos pagamentos confirmados, não no total de pedidos criados. O gráfico representa
recebimentos por hora (não acumulados). O total do cliente exclui pedidos cancelados,
mas inclui pedidos ainda não pagos. Datas seguem a convenção de DATETIME já utilizada no projeto.

## Limites preservados

Não foram implementados gateway de cobrança, emissão fiscal, baixa automática de estoque,
estorno, pagamento parcial ou reservas de mesa. Produtos sem foto usam uma imagem neutra.
A decoração lateral foi recortada da referência fornecida pelo usuário.
O cliente usa o novo painel React; o perfil COZINHA permanece no fluxo legado.

## Validação desta alteração

- 11 testes Node aprovados: renderização, estados vazios, dados escapados, busca,
  elegibilidade de pagamento, transições exibidas, módulos desabilitados, concorrência de
  renderização e regressões de sessão.
- Todos os arquivos JavaScript passaram em `node --check`.
- Testes Java de autorização e validação adicionados em PainelAccessTest.
- Maven instalado, mas o acesso às dependências falhou. Testes Java e integração MySQL não executados.
- Teste de navegador Playwright preparado em tests/admin-browser.cjs. Execução bloqueada
  pela restrição de sockets do ambiente; inspeção visual do navegador local também bloqueada.
  Portanto a aparência renderizada e os fluxos completos de navegador ainda precisam de validação.

Para executar os testes de navegador em um ambiente com Playwright:

1. Instale Playwright e seu Chromium no ambiente de desenvolvimento.
2. Sirva `src/main/resources/static` em `http://localhost:8085`.
3. Execute `node tests/admin-browser.cjs`.

O teste usa uma API simulada, não altera o banco. TEST_BASE_URL permite mudar o endereço
local e CHROMIUM_PATH permite indicar uma instalação própria do Chromium.

Veja [ASTRACHEFE.md](ASTRACHEFE.md) para a experiência do cliente e testes atualizados.
