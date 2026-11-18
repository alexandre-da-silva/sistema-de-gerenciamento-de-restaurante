# AstraChefe — relatório técnico de correções (14/11/2026)

**Base:** `AstraChefe_atualizado(3).zip`, preservando a estrutura Java 21 / Spring Boot / MySQL / React + TypeScript e os ativos existentes.

**Natureza da entrega:** implementação parcial e incremental com evidências locais. **Não equivale a homologação de produção**: a integração real Java/MySQL, a construção completa via npm e as migrações Flyway **não puderam ser executadas neste ambiente**. A ausência de erros nos testes isolados não certifica o sistema inteiro.

## 1. Correções implementadas

| Área | Alteração direta nos arquivos | Estado de verificação |
|---|---|---|
| JWT/autorização | Claim `tokenVersion` persistida, checagem de conta ativa e perfil atual em cada requisição JWT; troca de senha, mudança de perfil/ativação e logout revogam tokens anteriores | Código aplicado, **backend não executado** |
| Proteção de acesso | Rate limiting em memória por IP para login (12/min) e cadastro (5/min); recusa de tokens revogados | Código aplicado, **não testado em servidor real** |
| Administração | PATCH de ativação e papel de usuários; recusa remoção do último administrador ativo e autodesativação administrativa | Código aplicado, **não testado em servidor real** |
| Carrinho | Identificação separada por produto, variação, adicionais ordenados e observação; quantidades independentes e persistência local | **Passou** smoke no Chromium com APIs simuladas (390 px) |
| Checkout | Valida preço esperado fornecido pelo cliente contra preço atual e responde 409 antes de cobrar quando divergir; cliente recarrega cardápio e mantém carrinho | Código e payload verificados com mocks; regra Java **não testada em servidor** |
| Idempotência | Hash SHA-256 de conteúdo, 409 para reutilizar referência com payload distinto; bloqueio pessimista por cliente antes da verificação; constraint única preexistente | Código aplicado, concorrência real **não medida** |
| Mesa | Bloqueio pessimista de `MesaRepository.findById` já existia e foi preservado; fluxo do pedido utiliza esse repositório | Validação integrada **pendente** |
| Histórico de pedidos | Snapshot persistido de nome e imagem do item, variação e descrição de adicionais e observação independente; suporte a registros antigos | Migrações e código aplicados, **DB não testado** |
| Variações/adicionais | Atualização mantendo IDs e fazendo exclusão lógica; receitas adicionais por opção; campos ausentes preservam configurações, listas vazias removem da exibição | Código aplicado, **DB não testado** |
| Estoque | Consumo transacional agregado de ficha base + variação + adicionais; baixa/estorno rastreados por pedido; constraints adicionais de integridade | Código aplicado, **DB não testado** |
| Home/ranking | Mantida home distinta do cardápio; ranking de pedidos efetivamente entregues nos últimos 30 dias e fallback de itens disponíveis no cliente | Código auditado; teste end-to-end **pendente** |
| Navegação e F5 | Estado de página por hash/SessionStorage, carrinho em localStorage; redução da frequência de consultas do cliente e preservação de contexto | Remontagem de React **passou**; F5 real **não verificado** |
| Administração em tempo real | Contador leve de pedidos recebidos sem redesenhar todo o painel a cada 10 s; atualiza pedidos e cozinha quando há mudanças | Código aplicado, comportamento ao vivo **não verificado** |
| Layout mobile | Corrigido conflito de seletor CSS global com `aside` e gaveta móvel, rolagem indevida e sobreposição de botão flutuante | **Passou** smoke no Chromium em 390 × 844 px, com mocks |
| Teste Java | `PedidoServiceTest` atualizado para construtor e DTO atuais; novos cenários de idempotência e preço alterado | Arquivo ajustado, **Maven não disponível** |
| Migrações | Novas V4 a V8, incrementais, sem `DROP TABLE` ou apagamento de dados históricos | Criadas, **não rodadas contra MySQL** |

## 2. Testes realmente executados

| Teste | Resultado | Comando ou observação |
|---|---|---|
| JavaScript sem DOM | **11 passaram / 0 falharam** | `node --test tests/admin.test.cjs tests/frontend.test.cjs` |
| JavaScript dependente de jsdom | **2 suítes bloqueadas** | `node --test tests/client.test.cjs tests/admin-browser.cjs` → `MODULE_NOT_FOUND` para `frontend/node_modules/jsdom` |
| Sintaxe JavaScript | **Passou** | `node --check` em `static/client/client.js`, `static/js/*.js` e gerador offline |
| Smoke React no Chromium | **Passou com mocks** | `python scripts/browser_smoke.py` — duas configurações do mesmo produto, carrinho móvel, persistência na remontagem e payload de pedido |
| Construção offline do cliente | **Passou** | `NODE_PATH="$(npm root -g)" node scripts/gerar_bundle_offline.cjs` (reaproveita o bundle React já distribuído; **não substitui** `npm run build`) |
| Maven / JUnit | **Não executado** | Maven não instalado |
| MySQL / Flyway | **Não executado** | MySQL não instalado/conectado |
| `npm ci` / `npm run build` | **Não concluído** | Instalação de dependências indisponível no ambiente; pacote `jsdom` faltando |
| F5 real, login e endpoints | **Não verificados** | O navegador de teste não conseguia navegar diretamente ao servidor local; o smoke usa HTML/JS e API simulada |
| Testes responsivos 320–1920 px | **Incompletos** | Apenas 390 px testado com o smoke |

**Sobre a imagem do teste:** `/mnt/data/astrachefe_carrinho_mobile.png` foi capturada em Chromium com dados artificiais e não mostra a aplicação conectada a um MySQL real. Imagens de pratos do servidor não estavam disponíveis no teste isolado.

## 3. Pendências importantes (não declarar como concluídas)

1. **P0:** Executar `mvn clean test`, iniciar Java/Spring e validar autenticação, autorização, revogação, permissões, migrações e concorrência contra MySQL 8 real. Adicionar testes de integração com Testcontainers e validar conflito de pagamentos simultâneos.
2. **P0:** Implantar cabeçalhos de segurança/CSP, revisão profunda de XSS e opções de sessão mais protegidas (cookie HttpOnly + CSRF, se o modelo de autenticação for migrado). No estado atual, o cliente ainda utiliza Bearer token na sessão JavaScript.
3. **P1:** Confirmar concorrência de checkout e estoque em instâncias múltiplas, uso de locks e plano de índice/performance. Rate limiting atual é local ao processo; produção distribuída deve usar Redis/gateway.
4. **P1:** Melhorar o tratamento de produtos com estoque controlado e sem ficha técnica; criar editor de receitas *por variação/adicional* no painel, pois apenas a API aceita os novos campos. Validar produtos indisponíveis por falta de estoque.
5. **P1:** Validar a migração V8 em cópia/backup do banco. Se houver registros legados negativos, a migration falhará de maneira deliberada para preservar a integridade. Corrigir esses registros por movimentações auditáveis antes da migração, não desativar o CHECK.
6. **P1:** Concluir `npm ci`, `npm run build` e rodar as duas suítes com `jsdom`. Testar F5, hash/voltar/avançar, dispositivo real e tamanhos 320, 360, 428, 768, 1024, 1440, 1920 px.
7. **P2:** Atualização verdadeira por WebSocket/SSE, reconexão, indicadores de sincronização e dados transacionais; avisos de mensagens lidas/não lidas; status de retirada e entrega distintos; validação de formulários avançada; indicadores de acessibilidade WCAG 2.2 AA.
8. **P2:** Completar edição/desativação no painel administrativo, telas de gerenciamento de usuários, auditoria, relatórios, paginação de endpoints e otimização N+1 de catálogo (a camada JPA ainda carrega dados por item).
9. **P3:** Solução de arquivos persistentes, limpeza segura de uploads, backups, observabilidade, índices verificados por `EXPLAIN`, deploy HTTPS/TLS e usuário MySQL de privilégios mínimos.
10. **Integrações:** Emissão NF-e/NFC-e permanece **não implementada**; não existe certificação fiscal, transmissão à SEFAZ nem conciliação bancária verdadeira. Os pagamentos existentes são lançamentos manuais, não confirmação bancária.

## 4. Migrações Flyway adicionadas

- `V4__sessoes_seguras.sql`: versão de sessão por conta.
- `V5__pedidos_integridade.sql`: hash da requisição, retratos do item, relações de adicionais e fichas opcionais.
- `V6__opcoes_sem_exclusao.sql`: opções inativas em vez de excluir IDs e horários atravessando a meia-noite.
- `V7__observacao_por_item.sql`: observação do item.
- `V8__integridade_estoque.sql`: CHECKs não negativos e capacidade de mesa positiva. **A migration pode bloquear caso dados legados sejam inválidos, e isso é intencional.**

Nunca execute `DROP TABLE` ou `flyway clean` em banco existente para contornar erro de migration. Faça backup antes de alterar dados reais.

## 5. Execução sugerida em máquina com dependências

```bash
# 1) Configurar MySQL 8 e criar um banco vazio quando ainda não existir
# 2) Copiar .env.example para .env e preencher variáveis sem compartilhar credenciais
# 3) O segredo JWT deve ter 32 bytes aleatórios ou mais
# Backend (a partir da raiz do ZIP)
mvn clean test
mvn spring-boot:run
# Em outro terminal
cd frontend
npm ci
npm test
npm run build
```

Atenção: a `.env` de desenvolvimento contém `useSSL=false`, adequada apenas à conexão local de testes. Em produção, habilite TLS, não use o usuário MySQL `root`, não publique `APP_JWT_SECRET` e não ative o sistema fiscal sem integração homologada. Reinicie o backend após reconstruir o frontend.

## 6. Arquivos modificados

A lista exata, comparada byte a byte contra o ZIP anexado pelo usuário, consta em `docs/ARQUIVOS_ALTERADOS_2026-11-14.md`. **Nenhum commit nem push foi realizado.**
