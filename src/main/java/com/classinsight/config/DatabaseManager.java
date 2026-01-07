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
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
    
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
        if (DB_URL == null || DB_USER == null || DB_PASSWORD == null) {
            String msg = "Variáveis de ambiente não configuradas:\n";
            msg += "  DB_URL: " + (DB_URL == null ? "NÃO DEFINIDA" : "OK") + "\n";
            msg += "  DB_USER: " + (DB_USER == null ? "NÃO DEFINIDA" : "OK") + "\n";
            msg += "  DB_PASSWORD: " + (DB_PASSWORD == null ? "NÃO DEFINIDA" : "OK") + "\n";
            msg += "Configure em local.settings.json ou variáveis de ambiente do sistema.";
            throw new SQLException(msg);
        }
        
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✅ Conexão com BD estabelecida com sucesso");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Erro ao conectar no BD:");
            System.err.println("  URL: " + DB_URL);
            System.err.println("  User: " + DB_USER);
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
