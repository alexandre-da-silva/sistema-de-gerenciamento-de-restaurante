# Sistema de Gerenciamento de Restaurantes

Sistema desenvolvido como parte de um projeto acadêmico da UC HUB, com a proposta de organizar e centralizar os principais processos de um restaurante em uma única plataforma.

> **Status:** em planejamento e desenvolvimento.

## Sobre o projeto

A proposta é criar uma aplicação simples e intuitiva para auxiliar no gerenciamento de pedidos, mesas, estoque e vendas. A ideia é reduzir falhas de comunicação entre atendimento e cozinha, facilitar o acompanhamento dos pedidos e oferecer ao responsável pelo estabelecimento uma visão mais organizada da operação.

O projeto tem foco acadêmico e busca implementar as funcionalidades essenciais de um sistema de gerenciamento de restaurantes.

## Problema identificado

Em restaurantes que utilizam anotações em papel, planilhas e comunicação verbal, algumas atividades podem ficar descentralizadas. Isso pode ocasionar:

- Pedidos anotados incorretamente ou esquecidos;
- Demora na comunicação entre garçons e cozinha;
- Dificuldade para acompanhar o andamento dos pedidos;
- Divergências no fechamento do caixa;
- Falta de atualização e controle do estoque;
- Dificuldade para consultar o histórico de vendas.

## Objetivo

Desenvolver um sistema que centralize as informações e facilite o gerenciamento das atividades diárias do restaurante, tornando os processos mais organizados, ágeis e confiáveis.

## Solução proposta

A aplicação deverá permitir o cadastro de produtos e preços, o registro de pedidos vinculados a mesas ou comandas, o acompanhamento do preparo pela cozinha, o controle básico de estoque e o registro de pagamentos e vendas.

## Funcionalidades planejadas

- **Autenticação e acesso:** login e controle de acesso conforme o perfil do usuário.
- **Cardápio:** cadastro, edição e consulta de produtos, preços e disponibilidade.
- **Mesas e comandas:** organização das mesas e associação de pedidos.
- **Pedidos:** lançamento de itens, quantidades e observações.
- **Painel da cozinha:** visualização dos pedidos e atualização de status — recebido, em preparo, pronto e entregue.
- **Estoque:** registro de entradas e saídas e consulta das quantidades disponíveis.
- **Caixa:** registro de pagamentos e encerramento de comandas.
- **Relatórios:** consulta básica do histórico de vendas e dos pedidos.

*As funcionalidades acima representam o escopo previsto; ainda não significa que estejam implementadas.*

## Perfis de usuário

| Perfil | Responsabilidade prevista |
| --- | --- |
| Gerente / Administrador | Gerenciar cadastros, acompanhar a operação e consultar relatórios. |
| Garçom / Atendente | Registrar pedidos e acompanhar as comandas das mesas. |
| Cozinheiro / Equipe da cozinha | Visualizar pedidos e atualizar o andamento do preparo. |
| Operador de caixa | Registrar pagamentos e finalizar comandas. |
| Cliente | Poderá consultar o cardápio, caso essa funcionalidade seja incluída no escopo. |

## Fluxo de funcionamento previsto

1. O usuário realiza o login.
2. O gerente cadastra os produtos, preços, mesas e informações de estoque.
3. O garçom registra o pedido e o associa a uma mesa ou comanda.
4. A cozinha recebe o pedido e atualiza seu status durante o preparo.
5. O garçom acompanha o andamento e entrega o pedido.
6. O caixa registra o pagamento e encerra a comanda.
7. O sistema atualiza os registros de vendas e as informações de estoque previstas.

## Tecnologias previstas

| Tecnologia | Utilização |
| --- | --- |
| Java | Desenvolvimento da aplicação e regras de negócio. |
| Spring Boot | Construção da estrutura do backend e das APIs. |
| MySQL | Armazenamento e gerenciamento dos dados. |
| HTML | Estrutura das páginas da aplicação. |
| CSS | Estilização e organização visual. |
| JavaScript | Interatividade da interface. |
| Git e GitHub | Versionamento e colaboração no projeto. |

## Indicadores para avaliar os resultados

- Redução de erros e esquecimentos nos pedidos;
- Tempo médio entre o registro e a entrega do pedido;
- Melhor acompanhamento do preparo;
- Redução de divergências no caixa;
- Maior controle das entradas e saídas do estoque;
- Organização e produtividade das atividades.

## Organização inicial dos módulos

A aplicação poderá ser organizada nos seguintes módulos:

- Autenticação e usuários;
- Produtos e cardápio;
- Mesas e comandas;
- Pedidos;
- Cozinha;
- Estoque;
- Caixa e vendas;
- Relatórios.

A estrutura de pastas e os detalhes de implementação serão definidos durante o desenvolvimento.

## Segurança e proteção de dados

O desenvolvimento deverá considerar boas práticas de segurança, como armazenamento adequado de credenciais, controle de acesso por perfil e proteção das informações. Também deverão ser observados os princípios da Lei Geral de Proteção de Dados (LGPD), evitando coletar informações pessoais desnecessárias.

## Escopo acadêmico

Este repositório reúne o desenvolvimento do projeto acadêmico **Sistema de Gerenciamento de Restaurantes**. O sistema será construído gradualmente, e as funcionalidades serão documentadas conforme forem implementadas e testadas.

## Autor

Desenvolvido por [alexandre-da-silva](https://github.com/alexandre-da-silva).

---

Projeto acadêmico — UC HUB.
