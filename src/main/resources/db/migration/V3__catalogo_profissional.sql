-- Evolução aditiva: não apaga produtos, pedidos, fotos ou movimentações existentes.
ALTER TABLE produtos
 ADD COLUMN sku VARCHAR(60) NULL,
 ADD COLUMN setor_producao VARCHAR(40) NOT NULL DEFAULT 'COZINHA',
 ADD COLUMN porcao VARCHAR(100) NULL,
 ADD COLUMN ordem_exibicao INT NOT NULL DEFAULT 0,
 ADD COLUMN observacoes_permitidas VARCHAR(500) NULL,
 ADD COLUMN alergenos VARCHAR(500) NULL,
 ADD COLUMN tags VARCHAR(500) NULL,
 ADD COLUMN calorias INT NULL,
 ADD COLUMN ncm VARCHAR(8) NULL,
 ADD COLUMN cfop VARCHAR(4) NULL,
 ADD COLUMN cst_csosn VARCHAR(4) NULL,
 ADD COLUMN aliquota_icms DECIMAL(5,2) NULL,
 ADD UNIQUE KEY uk_produto_sku (sku);
ALTER TABLE ingredientes ADD COLUMN custo_unitario DECIMAL(12,4) NOT NULL DEFAULT 0;
CREATE TABLE variacoes_produto (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, produto_id BIGINT NOT NULL, nome VARCHAR(80) NOT NULL,
 preco DECIMAL(12,2) NOT NULL,
 CONSTRAINT fk_var_produto FOREIGN KEY(produto_id) REFERENCES produtos(id),
 CONSTRAINT chk_var_preco CHECK(preco > 0)
) ENGINE=InnoDB;
CREATE TABLE adicionais_produto (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, produto_id BIGINT NOT NULL, nome VARCHAR(80) NOT NULL,
 preco DECIMAL(12,2) NOT NULL,
 CONSTRAINT fk_ad_produto FOREIGN KEY(produto_id) REFERENCES produtos(id),
 CONSTRAINT chk_ad_preco CHECK(preco >= 0)
) ENGINE=InnoDB;
CREATE TABLE produto_disponibilidade (
 id BIGINT PRIMARY KEY AUTO_INCREMENT, produto_id BIGINT NOT NULL,
 dia_semana INT NOT NULL, hora_inicio TIME NOT NULL, hora_fim TIME NOT NULL,
 CONSTRAINT fk_disp_produto FOREIGN KEY(produto_id) REFERENCES produtos(id),
 CONSTRAINT chk_disp_dia CHECK(dia_semana BETWEEN 1 AND 7),
 CONSTRAINT chk_disp_hora CHECK(hora_inicio < hora_fim)
) ENGINE=InnoDB;
ALTER TABLE itens_pedido ADD COLUMN variacao_nome VARCHAR(80) NULL,
 ADD COLUMN adicionais_descricao VARCHAR(500) NULL;
ALTER TABLE pedidos ADD COLUMN estoque_baixado BOOLEAN NOT NULL DEFAULT FALSE,
 ADD COLUMN estoque_estornado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE movimentacoes_estoque ADD COLUMN pedido_id BIGINT NULL,
 ADD CONSTRAINT fk_mov_pedido FOREIGN KEY(pedido_id) REFERENCES pedidos(id),
 ADD INDEX idx_mov_pedido(pedido_id,tipo);

INSERT IGNORE INTO categorias(nome,descricao) VALUES ('Entradas','Entradas, aperitivos e petiscos'), ('Drinks','Bebidas preparadas no bar');
