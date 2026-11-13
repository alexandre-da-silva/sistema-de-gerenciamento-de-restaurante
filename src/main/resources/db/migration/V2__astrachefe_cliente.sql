ALTER TABLE produtos ADD COLUMN imagem_url VARCHAR(255),
 ADD COLUMN tempo_preparo INT NOT NULL DEFAULT 20,
 ADD COLUMN tipo VARCHAR(60), ADD COLUMN destaque BOOLEAN NOT NULL DEFAULT FALSE,
 ADD COLUMN preco_promocional DECIMAL(12,2),
 ADD CONSTRAINT chk_promocao CHECK (preco_promocional IS NULL OR (preco_promocional>0 AND preco_promocional<preco));
ALTER TABLE pedidos ADD COLUMN taxa_servico DECIMAL(12,2) NOT NULL DEFAULT 0,
 ADD COLUMN referencia VARCHAR(36), ADD COLUMN preparo_em DATETIME, ADD COLUMN pronto_em DATETIME,
 ADD COLUMN entregue_em DATETIME, ADD COLUMN cancelado_em DATETIME,
 ADD UNIQUE KEY uk_pedido_referencia (cliente_id,referencia);
CREATE TABLE avaliacoes (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, pedido_id BIGINT NOT NULL,
 produto_id BIGINT NOT NULL, cliente_id BIGINT NOT NULL, nota INT NOT NULL,
 comentario VARCHAR(1000) NOT NULL, criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_av_pedido FOREIGN KEY(pedido_id) REFERENCES pedidos(id),
 CONSTRAINT fk_av_produto FOREIGN KEY(produto_id) REFERENCES produtos(id),
 CONSTRAINT fk_av_cliente FOREIGN KEY(cliente_id) REFERENCES usuarios(id),
 CONSTRAINT chk_av_nota CHECK(nota BETWEEN 1 AND 5),
 UNIQUE KEY uk_av_item (pedido_id,produto_id,cliente_id)
);
INSERT IGNORE INTO categorias(nome,descricao) VALUES
 ('Pratos Principais','Pratos completos da casa'),('Massas','Massas e molhos'),
 ('Lanches','Hambúrgueres e sanduíches'),('Porções','Para compartilhar'),
 ('Bebidas','Bebidas e cafés'),('Sobremesas','Para fechar com doçura');
CREATE TABLE mensagens_pedido (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, pedido_id BIGINT NOT NULL, usuario_id BIGINT NOT NULL,
 texto VARCHAR(1000) NOT NULL, criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CONSTRAINT fk_msg_pedido FOREIGN KEY(pedido_id) REFERENCES pedidos(id),
 CONSTRAINT fk_msg_usuario FOREIGN KEY(usuario_id) REFERENCES usuarios(id)
);
