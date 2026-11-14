# AstraChefe — painel do cliente

## Funcionalidades

O cardápio usa o layout claro com menu vinho, banner, categorias, filtros, fotos, favoritos e carrinho lateral das referências. Pedidos e avaliações usam tema escuro e vermelho. Inclui adaptações para celular, menu recolhível e carrinho móvel.

- **Início/Cardápio:** busca sem acentos, tipo, preço, tempo, ordenação e favoritos.
- **Pratos Principais:** esteira SushiConveyorMenu fornecida no Texto colado (2), ligada aos produtos reais e ao carrinho.
- **Bebidas:** buscar café abre CoffeePourMenu do texto fornecido. A animação ilustra o estilo; nome, descrição e preço vêm do cadastro.
- **Carrinho:** quantidades, retirada/mesa livre, observação, serviço opcional de 10%, revisão e envio. O servidor calcula valores e promoções e registra referência por tentativa.
- **Meus Pedidos:** atuais, histórico, cancelados, detalhes e cancelamento antes do preparo.
- **Acompanhar Pedido:** etapas com horários registrados e consulta a cada 10 segundos enquanto a página está visível. A estimativa é o maior tempo cadastrado dos itens, sem cálculo da capacidade da cozinha.
- **Promoções:** produtos publicados com desconto real, filtros e inclusão no carrinho.
- **Avaliações:** notas agregadas reais, filtros, paginação, prato mais avaliado e comentário por prato de pedido entregue. Sem avaliações fictícias.
- **Perfil:** nome, telefone e troca de senha.
- **Atendimento:** conversa vinculada ao pedido, acessível pelo cliente e administrador. A equipe precisa abrir a conversa; não há WhatsApp/e-mail.

## Administração e instalação

ADMIN não pode criar pedidos, tanto na interface quanto no servidor. As rotas `/api/v1/cliente/**` exigem CLIENTE; o serviço também verifica o perfil.

Em **Cardápio → Novo produto/Editar**, informe foto JPG/PNG (até 5 MB), nome, descrição, categoria, tipo, preço, tempo, destaque e promoção opcional. Escolha publicado ou rascunho. Somente ativos aparecem ao cliente. Fotos são reencodadas como JPEG em `uploads/produtos`, fora do código; preserve o diretório nos deploys e backups.

A migração V2 adiciona categorias padrão. Categorias antigas com nomes diferentes continuam em Todos. Para aparecer nos filtros específicos, use as categorias padrão. O pacote não cadastra produtos nem preços de demonstração.

1. Preserve o `.env`, faça backup do banco e mantenha `uploads/`.
2. Atualize os fontes. O ZIP não inclui credenciais.
3. Com JDK 21, Maven e MySQL 8, execute `mvn test` e `mvn spring-boot:run` conforme as variáveis do README.
4. Flyway aplica V2 automaticamente. Não modifique a V1 já aplicada nem apague tabelas.
5. Abra `http://localhost:8080`. Cadastre/publique pratos no ADMIN e use uma conta CLIENTE para pedir.

O Java serve o frontend já compilado em `src/main/resources/static/client`; Node não é necessário para executar o aplicativo. Para editar, use Node 22+ e, dentro de `frontend`, execute `npm ci`, `npm run build` e `npm test`. Fontes React/TypeScript, Tailwind e os dois componentes fornecidos estão em `frontend/`.

## Novos endpoints

| Rota | Método | Acesso |
| --- | --- | --- |
| `/api/v1/admin/imagens` | POST multipart campo arquivo | ADMIN |
| `/media/{nome}.jpg` | GET | Público |
| `/api/v1/cliente/mesas` | GET | CLIENTE |
| `/api/v1/cliente/pedidos/{id}/cancelar` | PATCH | Dono |
| `/api/v1/cliente/perfil` | GET, PUT | CLIENTE |
| `/api/v1/publico/avaliacoes` | GET (pagina, nota) | Público |
| `/api/v1/cliente/avaliacoes` | POST | Cliente com item entregue |
| `/api/v1/cliente/pedidos/{id}/mensagens` | GET, POST | Dono |
| `/api/v1/admin/pedidos/{id}/mensagens` | GET, POST | ADMIN |

Criar pedido recebe `mesaId`, `itens` com produtoId/quantidade, `observacao`, `taxaServico` e `referencia` UUID. Não recebe preço/total do cliente. Repetir a referência após sucesso retorna o pedido existente. A chave única impede duplicação concorrente; a mesma referência pode ser repetida após conflito.

## Validação e limites

- Build TypeScript, esbuild e Tailwind concluído.
- 15 testes Node aprovados: regressões administrativas, carrinho, promoção/taxa, payload do pedido, busca café, esteira e navegação. React usa DOM e API simulados: esses testes não comprovam layout visual nem integração MySQL.
- Testes Java adicionados para autorização, preços, repetição e cancelamento por dono/etapa. Maven não conseguiu baixar dependências neste ambiente; execute a suíte no ambiente de desenvolvimento.
- Chromium não iniciou aqui. Inspeção visual desktop/celular e fluxo completo com MySQL permanecem pendentes. Não se afirma equivalência pixel a pixel com as imagens.
- Pagamento continua no restaurante, com registro manual do recebimento pelo administrador. Gateway de pagamento, emissão fiscal, baixa automática de estoque e estornos não fazem parte desta entrega.
