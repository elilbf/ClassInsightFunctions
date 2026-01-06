USE classinsight;
GO

-- Adiciona coluna 'urgencia' na tabela avaliacoes para armazenar urgência calculada
ALTER TABLE avaliacoes
ADD urgencia NVARCHAR(10) NOT NULL CONSTRAINT DF_avaliacoes_urgencia DEFAULT 'BAIXA';
GO

-- (Opcional) Atualiza valores existentes com base na nota
UPDATE avaliacoes
SET urgencia = CASE
    WHEN nota <= 2 THEN 'CRITICO'
    WHEN nota <= 5 THEN 'ALTA'
    WHEN nota <= 7 THEN 'MEDIA'
    ELSE 'BAIXA'
END;
GO

-- Verificar
SELECT id, descricao, nota, urgencia FROM avaliacoes;
