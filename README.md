# Sistema de Gerenciamento de Restaurantes

**Projeto acadêmico da UC HUB** · Em planejamento e desenvolvimento

Aplicação web para centralizar o atendimento de um restaurante, do registro do pedido ao fechamento da comanda. O projeto conecta salão, cozinha, caixa e gerência para reduzir erros de anotação, facilitar o acompanhamento do preparo e dar ao gerente uma visão das vendas e do estoque.

## Como o sistema vai funcionar

1. O gerente cadastra os itens do cardápio, as mesas e os itens de estoque.
2. O garçom registra os itens, as quantidades e as observações do pedido em uma mesa ou comanda.
3. A cozinha recebe o pedido na fila e atualiza seu status durante o preparo.
4. O garçom acompanha a fila, entrega o pedido e registra a entrega.
5. O caixa confere a comanda, registra o pagamento e encerra a conta.
6. O gerente consulta o histórico de vendas e acompanha as movimentações de estoque.

**Status do pedido** · Recebido → Em preparo → Pronto → Entregue

## Funcionalidades previstas

| Área | Funcionalidades |
| --- | --- |
| Acesso | Login e permissões por perfil de usuário. |
| Cardápio | Cadastro e edição de produtos, descrições, preços e disponibilidade. |
| Mesas e comandas | Organização das mesas e associação de pedidos às comandas. |
| Pedidos e cozinha | Registro de itens e observações, fila de preparo e atualização do status. |
| Estoque | Registro de entradas e saídas, consulta de quantidades e alertas de baixo estoque. |
| Caixa e vendas | Registro de pagamentos, encerramento de comandas e histórico de vendas. |
| Relatórios | Consulta de vendas por período e produtos mais vendidos. |

O acesso do cliente ao cardápio, a baixa automática de estoque e a integração com meios de pagamento dependem da definição do escopo.

## Perfis de acesso

| Perfil | Responsabilidades |
| --- | --- |
| Gerente ou administrador | Gerenciar cadastros, controlar o estoque e consultar vendas e relatórios. |
| Garçom ou atendente | Registrar pedidos, acompanhar o preparo e confirmar a entrega. |
| Equipe da cozinha | Consultar a fila e atualizar o andamento do preparo. |
| Operador de caixa | Conferir comandas, registrar pagamentos e finalizar contas. |

## Tecnologias previstas

| Tecnologia | Uso no projeto |
| --- | --- |
| Java e Spring Boot | Backend, APIs e regras de negócio. |
| MySQL | Armazenamento relacional de usuários, produtos, mesas, pedidos, estoque e vendas. |
| HTML, CSS e JavaScript | Estrutura, aparência e interatividade da interface web. |
| Git e GitHub | Controle de versões e colaboração. |

## Segurança e privacidade

O projeto prevê proteção de credenciais, permissões por função e coleta apenas dos dados necessários para a operação. O protótipo pode ser testado com dados fictícios. O uso de dados pessoais reais deverá observar a LGPD.

## Como avaliar o resultado

- Número de pedidos registrados incorretamente ou esquecidos.
- Tempo médio entre o registro e a entrega de um pedido.
- Número de divergências no fechamento do caixa.
- Número de faltas ou perdas de ingredientes identificadas.

## Autor

[Alexandre da Silva](https://github.com/alexandre-da-silva) · Projeto acadêmico da UC HUB
