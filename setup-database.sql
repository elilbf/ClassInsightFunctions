-- Script SQL: Criar banco de dados e tabelas para ClassInsight
-- Execute isso no SQL Server (via SSMS ou Azure Data Studio)

-- 1. Criar o banco de dados
CREATE DATABASE classinsight;
GO

-- 2. Usar o banco de dados
USE classinsight;
GO

-- 3. Criar tabela de Avaliações
CREATE TABLE avaliacoes (
    id INT PRIMARY KEY IDENTITY(1,1),
    descricao NVARCHAR(500) NOT NULL,
    nota DECIMAL(3,1) NOT NULL,
    data_criacao DATETIME DEFAULT GETDATE(),
    urgencia NVARCHAR(10) NOT NULL DEFAULT 'BAIXA',
    CHECK (nota >= 0 AND nota <= 5)
);

-- 5. Criar índices para melhor performance
CREATE INDEX idx_data_avaliacao ON avaliacoes(data_criacao);
CREATE INDEX idx_nota ON avaliacoes(nota);

-- 6. Verificar se as tabelas foram criadas
SELECT 'Tabelas criadas:' AS Status;
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'dbo';

-- 7. Verificar estrutura das tabelas
SELECT 'Estrutura da tabela avaliacoes:' AS Status;
EXEC sp_columns avaliacoes;

-- Pronto! Agora você pode fazer requisições para a Azure Function
-- Os dados serão salvos aqui automaticamente
