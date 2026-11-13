# Backend — Sistema de Gerenciamento de Restaurantes

Backend inicial do projeto acadêmico UC HUB, desenvolvido com Java 21, Spring Boot 3.5.16 e MySQL.

## O que já está preparado nesta primeira etapa

- Estrutura em `model`, `repository`, `service`, `controller`, `dto`, `config`, `security` e `exception`.
- Migração Flyway com tabelas relacionais, chaves estrangeiras e índices.
- Cadastro de cliente e autenticação JWT.
- Senhas armazenadas com BCrypt e endpoint autenticado para alteração da própria senha.
- Perfis ADMIN, ATENDENTE, COZINHA, CAIXA e CLIENTE.
- CRUD administrativo de produtos (exclusão lógica).
- Cardápio público.
- Criação e consulta dos próprios pedidos pelo cliente.
- Painel de cozinha e transições válidas de status.
- Recursos funcionais habilitáveis/desabilitáveis pelo administrador.
- Estrutura inicial para estoque, pagamentos e documentos fiscais no banco.

> Implementação inicial em validação. A integração fiscal, baixa automática por ficha técnica e funcionalidades administrativas adicionais permanecem pendentes.

## Requisitos

- JDK 21.
- Maven 3.6.3 ou superior.
- MySQL local na porta 3306.
- IntelliJ IDEA, Eclipse ou VS Code.

## Configuração

1. Copie `.env.example` para `.env` na raiz do projeto.
2. Configure `SPRING_DATASOURCE_PASSWORD` com a senha do seu MySQL.
3. Defina `APP_JWT_SECRET` com pelo menos 32 bytes aleatórios.
4. Defina `APP_ADMIN_EMAIL` e `APP_ADMIN_PASSWORD` (mínimo 12 caracteres).
5. Inicie a aplicação pelo IntelliJ ou execute `mvn spring-boot:run`.

A URL JDBC está preparada para criar o banco `restaurante_uc_hub` se ele ainda não existir e se o usuário MySQL possuir permissão `CREATE DATABASE`. O Flyway cria as tabelas automaticamente na primeira inicialização.

Também existe `sql/criar_banco.sql` para criação manual do banco. Não execute a migração de tabelas manualmente: o Flyway controla essa etapa.

## Segurança importante

- Nunca envie o arquivo `.env` ao GitHub. Ele já está no `.gitignore`.
- O `.env.example` contém apenas nomes e valores ilustrativos.
- BCrypt é hash de senha, não criptografia reversível: é a forma apropriada para armazenar senhas.
- JPA/Spring Data usa parâmetros nas consultas derivadas, evitando concatenar dados de usuário em SQL.
- O administrador não pode desligar autenticação, autorização ou outras proteções essenciais.
- Para produção, configure HTTPS, política de CORS, rate limiting, backups, gestão de segredos e auditoria.

## Endpoints iniciais

| Método | Rota | Acesso | Finalidade |
|---|---|---|---|
| POST | `/api/v1/auth/registrar` | Público | Cadastrar cliente |
| POST | `/api/v1/auth/login` | Público | Autenticar e receber JWT |
| PUT | `/api/v1/auth/senha` | Autenticado | Alterar a própria senha |
| GET | `/api/v1/publico/cardapio` | Público | Consultar cardápio |
| GET | `/api/v1/admin/produtos` | ADMIN | Listar produtos |
| POST | `/api/v1/admin/produtos` | ADMIN | Cadastrar produto |
| PUT | `/api/v1/admin/produtos/{id}` | ADMIN | Atualizar produto |
| DELETE | `/api/v1/admin/produtos/{id}` | ADMIN | Desativar produto (lógico) |
| GET | `/api/v1/admin/recursos` | ADMIN | Consultar módulos habilitados |
| PUT | `/api/v1/admin/recursos/{codigo}` | ADMIN | Habilitar/desabilitar módulo |
| POST | `/api/v1/cliente/pedidos` | CLIENTE | Criar pedido |
| GET | `/api/v1/cliente/pedidos` | CLIENTE | Consultar próprios pedidos |
| GET | `/api/v1/cozinha/pedidos` | ADMIN/COZINHA | Consultar pedidos da cozinha |
| PATCH | `/api/v1/cozinha/pedidos/{id}/status` | ADMIN/COZINHA | Atualizar status do pedido |

Todas as rotas privadas exigem `Authorization: Bearer <token>`.

## Recursos configuráveis pelo administrador

`CARDAPIO_ONLINE`, `PEDIDOS_ONLINE`, `PAINEL_COZINHA`, `MESAS_E_COMANDAS`, `CONTROLE_ESTOQUE`, `CAIXA_E_PAGAMENTOS`, `RELATORIOS`, `EMISSAO_NFE` e `EMISSAO_NFCE`.

O banco possui tabelas iniciais para ingredientes, movimentações, pagamentos e documentos fiscais. As rotas e regras desses módulos serão desenvolvidas nas próximas etapas.

## NF-e e NFC-e

A estrutura reserva flags e tabela de documentos fiscais, mas não transmite notas à SEFAZ ainda. A emissão real exige, entre outros pontos, certificado digital, credenciamento, CSC quando aplicável, configuração tributária, ambiente de homologação/produção e integração compatível com a UF/SEFAZ ou provedor fiscal. Não armazene certificado ou senha de certificado no repositório.

## Próximas etapas sugeridas

1. Categorias, mesas e gestão de usuários pelo administrador.
2. Estoque completo e baixa automática por ficha técnica dos produtos.
3. Caixa, pagamentos e fechamento.
4. Relatórios e auditoria.
5. Integração fiscal em ambiente de homologação.
6. Testes automatizados, paginação, rate limiting, auditoria e deploy.

Consulte `docs/REQUISITOS_NAO_FUNCIONAIS.txt` para os requisitos não funcionais.

## Gestão adicionada

Rotas ADMIN em `/api/v1/admin`.

| Rota | Métodos | Operação |
| --- | --- | --- |
| /usuarios | GET, POST | Consultar usuários e cadastrar perfis |
| /categorias | GET, POST | Consultar e cadastrar categorias |
| /mesas | GET, POST | Consultar e cadastrar mesas |
| /estoque | GET, POST | Consultar e cadastrar ingredientes |
| /estoque/{id}/movimentos | POST | Movimentar quantidade assinada, com motivo |
| /pedidos | GET | Consultar pedidos |
| /pedidos/{id}/pagamentos | POST | Registrar recebimento integral após entrega |
| /relatorios/vendas | GET | Consultar totais diários recebidos |

O pagamento recebe `forma` (PIX, DINHEIRO ou CARTAO) e `referencia` única por tentativa. Repetir a mesma referência no mesmo pedido é idempotente. O registro pressupõe confirmação manual do recebimento pelo administrador. As rotas estão descritas no Swagger em `/swagger-ui/index.html`.

As movimentações de estoque recebem `quantidade` positiva para entrada e negativa para saída, além de `motivo`. As transações bloqueiam o saldo para impedir saídas concorrentes acima do disponível. A mesa é liberada no cancelamento ou no pagamento, após a entrega.

## Pendências verificadas

- Emissão NF-e/NFC-e depende de implementação do provedor fiscal e homologação. As flags fiscais rejeitam ativação nesta versão.
- Ficha técnica, baixa automática, estornos, pagamentos parciais e gestão completa de usuários/mesas/categorias precisam de implementação.
- Criptografia de todos os dados em repouso, backup automático, auditoria e rate limiting precisam de configuração/implementação adicional.
- O MySQL do computador do usuário será inicializado quando a aplicação for executada nesse ambiente.

## Painel administrativo RestoERP

O painel ADMIN foi redesenhado conforme a referência visual fornecida, com dashboard,
pedidos, mesas, cardápio, cozinha, estoque, clientes, relatórios, financeiro e configurações.
Consulte [docs/PAINEL_RESTOERP.md](docs/PAINEL_RESTOERP.md) para execução, novos endpoints e limites da validação.
