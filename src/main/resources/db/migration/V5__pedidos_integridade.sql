-- Preserva dados existentes e dá suporte a idempotência, histórico e consumo por configuração.
ALTER TABLE pedidos ADD COLUMN request_hash CHAR(64) NULL;
ALTER TABLE itens_pedido ADD COLUMN produto_nome VARCHAR(140) NULL,
 ADD COLUMN imagem_url_snapshot VARCHAR(255) NULL,
 ADD COLUMN variacao_id BIGINT NULL;
UPDATE itens_pedido ip JOIN produtos p ON p.id=ip.produto_id
 SET ip.produto_nome=p.nome, ip.imagem_url_snapshot=p.imagem_url
 WHERE ip.produto_nome IS NULL;
CREATE TABLE item_pedido_adicionais (
 item_pedido_id BIGINT NOT NULL, adicional_id BIGINT NOT NULL,
 PRIMARY KEY (item_pedido_id,adicional_id),
 CONSTRAINT fk_ipa_item FOREIGN KEY(item_pedido_id) REFERENCES itens_pedido(id),
 CONSTRAINT fk_ipa_adicional FOREIGN KEY(adicional_id) REFERENCES adicionais_produto(id)
) ENGINE=InnoDB;
CREATE TABLE variacao_ingredientes (
 variacao_id BIGINT NOT NULL, ingrediente_id BIGINT NOT NULL,
 quantidade_necessaria DECIMAL(12,3) NOT NULL,
 PRIMARY KEY(variacao_id,ingrediente_id),
 CONSTRAINT fk_vi_variacao FOREIGN KEY(variacao_id) REFERENCES variacoes_produto(id),
 CONSTRAINT fk_vi_ingrediente FOREIGN KEY(ingrediente_id) REFERENCES ingredientes(id),
 CONSTRAINT chk_vi_q CHECK(quantidade_necessaria>0)
) ENGINE=InnoDB;
CREATE TABLE adicional_ingredientes (
 adicional_id BIGINT NOT NULL, ingrediente_id BIGINT NOT NULL,
 quantidade_necessaria DECIMAL(12,3) NOT NULL,
 PRIMARY KEY(adicional_id,ingrediente_id),
 CONSTRAINT fk_ai_adicional FOREIGN KEY(adicional_id) REFERENCES adicionais_produto(id),
 CONSTRAINT fk_ai_ingrediente FOREIGN KEY(ingrediente_id) REFERENCES ingredientes(id),
 CONSTRAINT chk_ai_q CHECK(quantidade_necessaria>0)
) ENGINE=InnoDB;
