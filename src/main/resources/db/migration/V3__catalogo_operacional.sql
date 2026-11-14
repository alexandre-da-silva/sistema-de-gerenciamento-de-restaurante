ALTER TABLE produtos ADD COLUMN sku VARCHAR(60) NULL, ADD UNIQUE KEY uk_produto_sku(sku), ADD COLUMN detalhes_json LONGTEXT;
ALTER TABLE ingredientes ADD COLUMN custo_unitario DECIMAL(12,4) NOT NULL DEFAULT 0;
ALTER TABLE itens_pedido ADD COLUMN configuracao VARCHAR(4000), ADD COLUMN setor_producao VARCHAR(60) NOT NULL DEFAULT 'Cozinha';
CREATE TABLE pedido_consumos (
 pedido_id BIGINT NOT NULL, ingrediente_id BIGINT NOT NULL, quantidade DECIMAL(12,3) NOT NULL,
 devolvido BOOLEAN NOT NULL DEFAULT FALSE, PRIMARY KEY(pedido_id,ingrediente_id),
 FOREIGN KEY(pedido_id) REFERENCES pedidos(id), FOREIGN KEY(ingrediente_id) REFERENCES ingredientes(id)
);
INSERT IGNORE INTO categorias(nome,descricao) VALUES ('Entradas','Para começar');
