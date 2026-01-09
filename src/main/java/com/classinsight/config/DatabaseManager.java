package com.classinsight.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gerenciador de conexão com o banco de dados.
 * Suporta Azure SQL, MySQL, PostgreSQL.
 */
public class DatabaseManager {
    
    // Variáveis de conexão (virão de environment variables)
    private static final String DB_URL = System.getenv("DB_URL");

    private static final String DRIVER_CLASS = System.getenv("DB_DRIVER") != null 
        ? System.getenv("DB_DRIVER") 
        : "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    
    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver não encontrado: " + DRIVER_CLASS, e);
        }
    }
    
    /**
     * Obtém uma conexão com o banco de dados.
     * @return Connection
     * @throws SQLException Se houver erro na conexão
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            System.out.println("✅ Conexão com BD estabelecida com sucesso");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Erro ao conectar no BD:");
            System.err.println("  URL: " + DB_URL);
            System.err.println("  Erro: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Testa a conexão com o banco de dados.
     * @return true se a conexão foi bem-sucedida
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(5);
        } catch (SQLException e) {
            System.err.println("Erro ao testar conexão: " + e.getMessage());
            return false;
        }
    }
}
