-- Não altera registros existentes. Se dados antigos violarem alguma regra,
-- o Flyway interrompe esta migração sem apagar ou corrigir dados silenciosamente.
-- Confira e sane os registros com movimentos auditáveis antes de executá-la.
ALTER TABLE ingredientes
 ADD CONSTRAINT chk_ingrediente_saldo CHECK (quantidade_atual >= 0),
 ADD CONSTRAINT chk_ingrediente_minimo CHECK (quantidade_minima >= 0),
 ADD CONSTRAINT chk_ingrediente_custo CHECK (custo_unitario >= 0);
ALTER TABLE produto_ingredientes
 ADD CONSTRAINT chk_ficha_quantidade CHECK (quantidade_necessaria > 0);
ALTER TABLE movimentacoes_estoque
 ADD CONSTRAINT chk_movimento_quantidade CHECK (quantidade > 0);
ALTER TABLE mesas
 ADD CONSTRAINT chk_mesa_capacidade CHECK (capacidade_pessoas IS NULL OR capacidade_pessoas > 0);
