-- Opções removidas ficam inativas, preservando IDs utilizados pelo histórico de pedidos.
ALTER TABLE variacoes_produto ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE adicionais_produto ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
-- Permite expediente que atravessa meia-noite (ex.: 19h até 02h).
ALTER TABLE produto_disponibilidade DROP CHECK chk_disp_hora;
