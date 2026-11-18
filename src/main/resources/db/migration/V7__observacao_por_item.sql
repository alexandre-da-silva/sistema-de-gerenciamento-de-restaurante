-- Observações independentes para itens do mesmo produto com configurações diferentes.
ALTER TABLE itens_pedido ADD COLUMN observacao_item VARCHAR(500) NULL;
