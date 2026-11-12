CREATE TABLE usuarios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    senha_hash VARCHAR(100) NOT NULL,
    telefone VARCHAR(25),
    perfil VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_usuarios_perfil (perfil),
    INDEX idx_usuarios_ativo (ativo)
) ENGINE=InnoDB;

CREATE TABLE categorias (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(80) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    ativa BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE produtos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(140) NOT NULL,
    descricao VARCHAR(1000),
    preco DECIMAL(12,2) NOT NULL,
    categoria_id BIGINT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_produtos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id),
    INDEX idx_produtos_ativo_nome (ativo, nome),
    CONSTRAINT chk_produto_preco CHECK (preco >= 0)
) ENGINE=InnoDB;

CREATE TABLE mesas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    numero INT NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'LIVRE',
    capacidade_pessoas INT,
    INDEX idx_mesas_status (status)
) ENGINE=InnoDB;

CREATE TABLE pedidos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    mesa_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEBIDO',
    total DECIMAL(12,2) NOT NULL DEFAULT 0,
    observacao VARCHAR(1000),
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios(id),
    CONSTRAINT fk_pedidos_mesa FOREIGN KEY (mesa_id) REFERENCES mesas(id),
    INDEX idx_pedidos_status_criado (status, criado_em),
    INDEX idx_pedidos_cliente (cliente_id, criado_em)
) ENGINE=InnoDB;

CREATE TABLE itens_pedido (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_itens_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    CONSTRAINT fk_itens_pedido_produto FOREIGN KEY (produto_id) REFERENCES produtos(id),
    CONSTRAINT chk_item_quantidade CHECK (quantidade > 0),
    INDEX idx_itens_pedido_pedido (pedido_id)
) ENGINE=InnoDB;

CREATE TABLE ingredientes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(120) NOT NULL UNIQUE,
    unidade_medida VARCHAR(20) NOT NULL,
    quantidade_atual DECIMAL(12,3) NOT NULL DEFAULT 0,
    quantidade_minima DECIMAL(12,3) NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE produto_ingredientes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    produto_id BIGINT NOT NULL,
    ingrediente_id BIGINT NOT NULL,
    quantidade_necessaria DECIMAL(12,3) NOT NULL,
    CONSTRAINT fk_pi_produto FOREIGN KEY (produto_id) REFERENCES produtos(id),
    CONSTRAINT fk_pi_ingrediente FOREIGN KEY (ingrediente_id) REFERENCES ingredientes(id),
    UNIQUE KEY uk_produto_ingrediente (produto_id, ingrediente_id)
) ENGINE=InnoDB;

CREATE TABLE movimentacoes_estoque (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ingrediente_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    quantidade DECIMAL(12,3) NOT NULL,
    motivo VARCHAR(255),
    usuario_id BIGINT NOT NULL,
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mov_ingrediente FOREIGN KEY (ingrediente_id) REFERENCES ingredientes(id),
    CONSTRAINT fk_mov_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    INDEX idx_mov_estoque_data (ingrediente_id, criado_em)
) ENGINE=InnoDB;

CREATE TABLE pagamentos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    valor DECIMAL(12,2) NOT NULL,
    forma_pagamento VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    referencia_transacao VARCHAR(120),
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pagamento_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    INDEX idx_pagamento_data (criado_em)
) ENGINE=InnoDB;

CREATE TABLE recursos_sistema (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    codigo VARCHAR(60) NOT NULL UNIQUE,
    descricao VARCHAR(180) NOT NULL,
    habilitado BOOLEAN NOT NULL DEFAULT FALSE,
    atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE configuracoes_sistema (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chave VARCHAR(80) NOT NULL UNIQUE,
    valor VARCHAR(500) NOT NULL,
    descricao VARCHAR(255),
    atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE documentos_fiscais (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pedido_id BIGINT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    chave_acesso VARCHAR(60),
    protocolo VARCHAR(60),
    xml_path VARCHAR(500),
    mensagem_retorno VARCHAR(1000),
    criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documento_pedido FOREIGN KEY (pedido_id) REFERENCES pedidos(id),
    INDEX idx_documento_status (status, criado_em)
) ENGINE=InnoDB;

INSERT INTO recursos_sistema (codigo, descricao, habilitado) VALUES
('CARDAPIO_ONLINE', 'Exibir cardápio digital para clientes', TRUE),
('PEDIDOS_ONLINE', 'Permitir que clientes registrem pedidos', TRUE),
('PAINEL_COZINHA', 'Habilitar painel e atualização de pedidos da cozinha', TRUE),
('MESAS_E_COMANDAS', 'Habilitar gestão de mesas e comandas', TRUE),
('CONTROLE_ESTOQUE', 'Habilitar controle de ingredientes e estoque', FALSE),
('CAIXA_E_PAGAMENTOS', 'Habilitar caixa e registro de pagamentos', FALSE),
('RELATORIOS', 'Habilitar relatórios gerenciais', FALSE),
('EMISSAO_NFE', 'Habilitar integração de emissão de NF-e após configuração fiscal', FALSE),
('EMISSAO_NFCE', 'Habilitar integração de emissão de NFC-e após configuração fiscal', FALSE);
