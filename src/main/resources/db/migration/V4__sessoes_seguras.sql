-- Invalidação de tokens após alteração de senha, perfil, ativação ou logout.
ALTER TABLE usuarios ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0;
