# AstraChefe V3 — Home, sessão e cadastro operacional

## Instalar sem perder dados

Preserve seu `.env`, banco e diretório `uploads/`. Faça backup antes de atualizar. Execute `mvn test` e `mvn spring-boot:run` com Java 21 e MySQL 8. O Flyway aplica `V3__catalogo_operacional.sql` automaticamente; mantenha as migrações V1/V2 originais. O cliente compilado acompanha o pacote.

Para editar o frontend: Node 22+, `cd frontend`, `npm ci`, `npm run build`. Na raiz, `node --test tests/*.test.cjs` executa os testes de interface com DOM simulado.

## Home e popularidade

A Home deixou de ser uma cópia do Cardápio: exibe uma apresentação, Em alta, sugestões da casa e bebidas. O catálogo completo, categorias, busca, filtros, animação de café e esteira ficam em Cardápio.

O ranking usa a soma das unidades em pedidos ENTREGUES com pagamento CONFIRMADO nos últimos 30 dias. Cancelamentos e pedidos não pagos não contam. A busca de pagamentos usa EXISTS para não duplicar unidades. No começo, sem vendas, a Home informa isso e mostra sugestões reais disponíveis, sem inventar popularidade.

O calor é `log(1 + unidades) / log(1 + maior_volume)`. O selo **Popular** exige pelo menos 5 unidades e 35% do volume do item líder. Populares recebem fogo vermelho; os demais ficam com fogo vermelho acinzentado, sem rótulo. O destaque manual do chef não altera o algoritmo. O ranking filtra os produtos disponíveis agora.

## Atualização e F5

A sessão e a página são guardadas no sessionStorage da aba. Ao atualizar, a aplicação verifica o token no servidor e restaura a tela ADMIN/CLIENTE/COZINHA. Não guarda senha. Sair limpa a sessão e a página; token expirado pede autenticação novamente. Uma falha de conexão permite tentar novamente sem apagar uma sessão potencialmente válida. A sessão não é permanente: o prazo padrão do JWT permanece 60 minutos, configurável pelo ambiente.

Cliente: pedidos, catálogo e recomendações são consultados a cada 10 segundos enquanto a aba está visível. ADMIN/COZINHA: dados consultados a cada 10 segundos; no ADMIN a atualização aguarda o fechamento de formulários, menu móvel e edição de campos para não atrapalhar o trabalho. Não é WebSocket: há até 10 segundos de intervalo entre consultas. F5 sempre busca dados novos.

## Cadastro de produtos

O formulário possui seis seções:

1. Nome, descrição, preço, preço promocional, categoria, foto, status, SKU e destaque do chef.
2. Tempo, setor, porção, ordem de exibição, dias e intervalo de disponibilidade.
3. Ficha técnica com insumos/quantidades, custo calculado, margem bruta e controle de estoque.
4. Variações, adicionais com preço próprio e insumos, observações permitidas por item.
5. Alérgenos, tags, calorias e texto nutricional.
6. NCM, CFOP, CST/CSOSN e alíquotas de ICMS/PIS/COFINS opcionais.

O SKU é único. As fotos continuam limitadas a JPG/PNG de 5 MB. Inativar preserva o produto e o histórico. Nenhum dia selecionado significa todos os dias; ambos os horários vazios significam dia inteiro. O horário segue America/Bahia. Intervalos que atravessam a meia-noite pertencem ao dia de início; o horário final é exclusivo. O servidor também impede comprar fora da disponibilidade.

Alérgenos e tags são cadastrados pela equipe, sem inferência automática. A descrição e detalhes do prato mostram essas informações ao cliente. Custos, ficha técnica e dados fiscais não são expostos nas respostas públicas do cardápio.

## Custos e estoque

O insumo possui custo por unidade de medida. Dentro do editor é possível cadastrar insumos e atualizar o custo compartilhado. Insumos novos começam com saldo zero: a entrada é feita na tela Estoque. As quantidades da receita usam a mesma unidade do cadastro (ex.: 200 g = 0,200 kg).

Custo do prato = soma das quantidades × custos unitários da ficha. Margem exibida = preço efetivo − custo da ficha, em reais; não é lucro líquido e não inclui despesas gerais, taxas ou tributos. Variações mostram seu custo e margem considerando o multiplicador da receita.

Quando o controle está ligado, é obrigatório ter ficha e habilitar CONTROLE_ESTOQUE. Ao confirmar o pedido, o sistema baixa os insumos na mesma transação do pedido, em ordem de ID com bloqueio no banco. Se faltar qualquer ingrediente, a operação inteira é revertida. As quantidades consumidas são registradas por pedido; editar a receita depois não altera o consumo histórico.

Cancelamento ainda em RECEBIDO devolve o consumo uma única vez. Cancelamento após iniciar o preparo conserva a baixa, pois os ingredientes podem ter sido usados. Não há devolução automática de comida já preparada ao estoque. Pagamento não baixa novamente os insumos.

## Variações e produção

O cliente escolhe uma variação quando cadastrada; seu preço substitui o preço base. Portanto produtos com variações não aceitam promoção no preço base: ajuste o preço de cada variação. O multiplicador ajusta a ficha (grande = 1,5, por exemplo). Adicionais somam preço e seus insumos, uma unidade de cada adicional por item. Configurações diferentes viram linhas separadas no carrinho. Quantidades e valores são recalculados no servidor.

Observações permitidas se aplicam a cada item. A observação geral do carrinho é para atendimento. O pedido guarda preço, personalização e setor. A cozinha administrativa pode filtrar por setor; avanço de status continua sendo do pedido completo e fica disponível na visão Todos os setores. A interface COZINHA também mostra o setor do item. Não há integração com impressora física nem estados independentes por setor nesta versão.

## Fiscal

Os campos fiscais são apenas cadastrais. Não foi ativada emissão de nota nem cálculo de tributos. A integração fiscal já pendente no projeto continua pendente; os dados devem ser preenchidos conforme a configuração contábil do estabelecimento.

## Verificação

- TypeScript/esbuild/Tailwind: build aprovado.
- 21 testes Node: interface, carrinho, café, esteira, Home independente, popularidade visual, variações/adicionais, disponibilidade, restauração de sessão, logout, erro de rede e formulário/custos.
- Maven/Java 21: compilação e suíte aprovadas no GitHub Actions, com MySQL isolado. A suíte cobre regras de popularidade, disponibilidade noturna, personalizações e permissões/cancelamento.
- Integração MySQL aprovada: `tests/api-integration.py` valida migrações, preços, receitas, idempotência, cancelamento, ranking e concorrência de estoque contra um banco isolado. Não execute esse roteiro contra seu banco de produção.
- `.github/workflows/validate.yml` prepara Java 21, Node 22 e MySQL isolado para executar essas verificações no PR. A primeira execução passou em 14/11/2026 (run 43627630536). A execução Maven local não conseguiu resolver as dependências por restrição de rede; a validação foi feita pelo CI.
- Testes com DOM simulado não substituem inspeção visual real. A validação visual de desktop/celular ainda precisa ser feita em navegador funcional.
