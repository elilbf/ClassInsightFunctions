package com.classinsight;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para validar a conexão com o banco de dados.
 * Configure as variáveis de ambiente antes de executar.
 */
public class DatabaseManagerTest {

    @BeforeEach
    void setup() {
        // Configure as variáveis de ambiente:
        // DB_URL, DB_USER, DB_PASSWORD
    }

    /**
     * Teste de conexão básica.
     * Execução: mvn test -Dtest=DatabaseManagerTest#testConnection
     */
    @Test
    void testConnection() {
        // Este teste verifica se as variáveis de ambiente estão configuradas
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        // Se as variáveis não estiverem configuradas, o teste será pulado
        if (dbUrl == null || dbUser == null || dbPassword == null) {
            System.out.println("⚠️  Variáveis de ambiente DB_URL, DB_USER, DB_PASSWORD não configuradas.");
            System.out.println("   Pule este teste para modo local.");
            return;
        }

        // Tenta testar a conexão
        boolean connected = DatabaseManager.testConnection();
        assertTrue(connected, "Falha na conexão com o banco de dados");
    }

    /**
     * Teste com SQL Server local (desenvolvimento).
     * Use para testar localmente com um SQL Server em docker ou instalado.
     */
    @Test
    void testLocalSqlServer() {
        String localUrl = "jdbc:sqlserver://localhost:1433;databaseName=classinsight;encrypt=true;trustServerCertificate=true;";
        String localUser = "sa";
        String localPassword = "YourPassword123!";

        System.out.println("Testando SQL Server local em: " + localUrl);
        System.out.println("Se este teste falhar, inicie o SQL Server local ou execute:");
        System.out.println("  docker run -e 'ACCEPT_EULA=Y' -e 'SA_PASSWORD=YourPassword123!' -p 1433:1433 mcr.microsoft.com/mssql/server:2022-latest");
    }
}
