# Correções da revisão — AstraChefe

Data: 15/11/2026. Base: AstraChefe(2).zip.

## Alterações

1. **Compilação TypeScript:** categorias agora são filtradas com um predicado de tipo; `npm run build` conclui a verificação e regenera JS/CSS do cliente.
2. **Logout da cozinha:** remove a sessão salva, interrompe a atualização periódica e solicita revogação do token ao servidor.
3. **F5 e falhas de rede:** falhas temporárias preservam o acesso e oferecem “Tentar novamente”. Respostas 401 continuam encerrando sessões inválidas. Mudanças de perfil exigem nova autenticação.
4. **Mesas sem caixa:** a entrega libera a mesa quando o caixa está desativado. Mesas antigas presas podem ser liberadas em Mesas → detalhes → Liberar mesa. Pedidos em andamento impedem a liberação; com caixa habilitado, pagamentos pendentes também impedem. Pagamento de pedido antigo não libera uma mesa com outro pedido pendente.
5. **Credenciais no pacote:** `.env`, arquivos da IDE, diretórios de compilação e dependências locais não entram na distribuição. O modelo `.env.example` contém somente valores de exemplo.
6. **Recuperação de pedido:** o corpo completo da solicitação e sua referência são salvos antes do envio. Falha de rede/5xx mantém a recuperação disponível após recarregar. “Verificar envio pendente” reapresenta exatamente o mesmo corpo, mesmo que o catálogo tenha mudado. O carrinho fica bloqueado até resolver o envio. A referência só é removida após confirmação ou rejeição definitiva. Armazenamento local bloqueado impede o envio e mostra uma mensagem.
7. **Atualização da cozinha:** consulta automática a cada 15 segundos enquanto a aba está visível e não há formulário em edição. Falhas de conexão preservam o conteúdo. A atualização para ao sair.
8. **Histórico de pedidos:** a API de detalhes aceita `antesDe` como cursor, com páginas de 500 registros. O painel percorre todas as páginas, mantendo pedidos antigos acessíveis, inclusive pendentes e não pagos. Respostas atrasadas não substituem outra tela. O carregamento completo cresce com o volume do histórico.
9. **Receitas de opções:** o formulário administrativo permite cadastrar, editar e remover insumos de cada variação/adicional. A API administrativa devolve essas receitas; a API pública não expõe os custos/receitas internos. As quantidades das opções são adicionais à receita base, conforme a regra de consumo já existente.
10. **Estoque mínimo:** incluído no cadastro de ingrediente e editável pelo botão “Mínimo”. A API rejeita valores negativos e excesso de casas decimais.
11. **Validação de listas:** itens nulos são rejeitados em pedidos, adicionais, variações, receitas e horários antes da execução dos serviços.
12. **Diagnóstico:** erros inesperados são registrados nos logs do servidor, com causa/stack trace; a resposta pública permanece genérica.
13. **Testes existentes:** caminhos agora independem do diretório de execução; o teste do carrinho escolhe o produto explicitamente e verifica `precoEsperado`; dois testes Java foram corrigidos para não configurar um mock dentro de outro stubbing.

## Validação executada

- `npm run build --prefix frontend`: aprovado, incluindo TypeScript e geração dos arquivos servidos pelo Spring Boot.
- `npm test --prefix frontend`: 24 testes aprovados, zero falhas.
- Backend Java 21: compilação aprovada; 27 testes aprovados, zero falhas.
- Os testes de repositório usam H2 exclusivamente no escopo de teste, em modo MySQL. Exercitam paginação com 502 pedidos, estoque mínimo, receitas e liberação de mesa.
- Não houve execução integrada com um servidor MySQL real nem validação das migrações nesse banco nesta rodada.
- A execução do Chromium para validação visual foi bloqueada pelo ambiente (criação de socket não permitida). A interação de formulários foi validada com DOM simulado; não se declara validação visual completa.

O runtime de validação disponibiliza Java 21 sem `ct.sym` e restringe autoanexação de agentes. Por isso os testes Maven foram executados com `maven.compiler.release` vazio, `source/target=21` e Mockito como `-javaagent`. O projeto continua configurado para Java 21; em um JDK 21 completo, o comando usual é `mvn test`. Nenhum caminho específico deste ambiente foi adicionado ao POM.

## Como usar

1. Extraia o pacote e abra a pasta `AstraChefe/astrachefe`.
2. Configure um `.env` local usando `.env.example` como modelo. Preencha acesso ao MySQL, e-mail/senha administrativa e um segredo JWT aleatório com pelo menos 32 bytes.
3. Use Java **JDK 21** e Maven. Execute `mvn test` e depois `mvn spring-boot:run` na pasta que contém `pom.xml`.
4. O frontend compilado já acompanha o projeto. Para recompilar: `npm ci --prefix frontend` e `npm run build --prefix frontend`.
5. As tabelas necessárias já faziam parte das migrações V1–V8. Esta correção não altera migrações antigas nem apaga dados. Mantenha o banco existente e uma cópia de segurança antes de atualizar seu ambiente.

As credenciais do pacote anterior não foram alteradas no seu ambiente externo. Caso estejam em uso e tenham sido compartilhadas, faça a rotação de senha do banco, senha do usuário administrador e segredo JWT. Mudar `APP_ADMIN_PASSWORD` sozinho não redefine a senha de um administrador já existente: essa configuração serve à criação inicial. A troca do segredo JWT invalida os tokens anteriores.
