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
    CHECK (nota >= 0 AND nota <= 5)
);

-- 4. Criar tabela de Relatórios
CREATE TABLE relatorios (
    id INT PRIMARY KEY IDENTITY(1,1),
    total_avaliacoes INT,
    media_notas DECIMAL(3,2),
    data_geracao DATETIME DEFAULT GETDATE()
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

SELECT 'Estrutura da tabela relatorios:' AS Status;
EXEC sp_columns relatorios;

-- Pronto! Agora você pode fazer requisições para a Azure Function
-- Os dados serão salvos aqui automaticamente
