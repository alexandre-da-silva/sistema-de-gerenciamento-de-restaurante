# Validação das correções

- `node --test tests/frontend.test.cjs`: 3 testes aprovados (sessão expirada, botão de pagamento e login sem token antigo).
- `node --check`: os seis scripts do frontend passaram.
- Consulta de pagamentos verificada com dados pagos, pendentes e confirmações duplicadas em SQLite. Integração MySQL ainda não executada.
- Compilação e testes Spring Boot pendentes: o ambiente de revisão não possui compilador Java. Execute `mvn test` com JDK 21 antes de implantar.
- Credenciais locais e arquivos `.env` não integram o commit; configure o ambiente usando `.env.example`.
