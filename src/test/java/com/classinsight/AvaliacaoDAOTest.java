package com.classinsight;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para o AvaliacaoDAO (Data Access Object).
 * Execução: mvn test -Dtest=AvaliacaoDAOTest
 */
@DisplayName("AvaliacaoDAO Tests")
public class AvaliacaoDAOTest {

    private AvaliacaoDAO dao;

    @BeforeEach
    void setup() {
        // Instancia o DAO
        dao = new AvaliacaoDAO();
    }

    /**
     * Teste de inserção de avaliação.
     * Execução: mvn test -Dtest=AvaliacaoDAOTest#testInsertAvaliacao
     */
    @Test
    @DisplayName("Deve inserir uma avaliação no banco de dados")
    void testInsertAvaliacao() {
        // Arrange
        AvaliacaoRequest request = new AvaliacaoRequest();
        request.setDescricao("Teste de avaliação");
        request.setNota(4.5);

        // Act
        try {
            long id = AvaliacaoDAO.inserirAvaliacao(request);
            
            // Assert
            assertTrue(id > 0, "ID da avaliação deve ser positivo");
            System.out.println("✅ Avaliação inserida com sucesso. ID: " + id);
        } catch (Exception e) {
            fail("Erro ao inserir avaliação: " + e.getMessage());
        }
    }

    /**
     * Teste de consulta de avaliações.
     * Execução: mvn test -Dtest=AvaliacaoDAOTest#testGetAvaliacoes
     */
    @Test
    @DisplayName("Deve recuperar avaliações do banco de dados")
    void testGetAvaliacoes() {
        try {
            // Act
            long total = dao.obterTotalAvaliacoes();
            
            // Assert
            assertTrue(total >= 0, "Total de avaliações deve ser >= 0");
            System.out.println("✅ Total de avaliações: " + total);
        } catch (Exception e) {
            fail("Erro ao recuperar avaliações: " + e.getMessage());
        }
    }

    /**
     * Teste de relatório de avaliações.
     * Execução: mvn test -Dtest=AvaliacaoDAOTest#testGetRelatorio
     */
    @Test
    @DisplayName("Deve gerar um relatório de avaliações")
    void testGetRelatorio() {
        try {
            // Act
            double media = dao.obterMediaAvaliacoes();
            
            // Assert
            assertTrue(media >= 0 && media <= 5, "Média deve estar entre 0 e 5");
            System.out.println("✅ Média de avaliações: " + media);
        } catch (Exception e) {
            fail("Erro ao gerar relatório: " + e.getMessage());
        }
    }
}
