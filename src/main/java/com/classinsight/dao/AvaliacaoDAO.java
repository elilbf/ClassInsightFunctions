package com.classinsight.dao;

import com.classinsight.config.DatabaseManager;
import com.classinsight.model.AvaliacaoRequest;
import com.classinsight.model.AvaliacaoResponse;
import com.classinsight.model.AvaliacaoStats;
import com.classinsight.model.Urgencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para Avaliações.
 * Realiza operações CRUD no banco de dados.
 */
public class AvaliacaoDAO {
    
    /**
     * Insere uma nova avaliação no banco de dados.
     * @param avaliacao AvaliacaoRequest contendo descrição e nota
     * @return ID da avaliação inserida, ou -1 se falhar
     */
    public static long inserirAvaliacao(AvaliacaoRequest avaliacao) {
        String sql = "INSERT INTO avaliacoes (descricao, nota, data_criacao, urgencia) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, avaliacao.getDescricao());
            pstmt.setDouble(2, avaliacao.getNota());
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            // Calcular urgência a partir da nota
            Urgencia urg = Urgencia.fromNota(avaliacao.getNota());
            pstmt.setString(4, urg.name());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    System.out.println("✅ Avaliação inserida com sucesso. ID: " + id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao inserir avaliação:");
            System.err.println("  SQL: " + sql);
            System.err.println("  Descrição: " + avaliacao.getDescricao());
            System.err.println("  Nota: " + avaliacao.getNota());
            System.err.println("  Erro: " + e.getMessage());
            System.err.println("  Cause: " + e.getCause());
            e.printStackTrace();
        }
        return -1;
    }
    
    /**
     * Obtém uma avaliação pelo ID.
     * @param id ID da avaliação
     * @return AvaliacaoResponse ou null se não encontrado
     */
    public static AvaliacaoResponse obterAvaliacao(long id) {
        String sql = "SELECT id, descricao, nota, data_criacao, urgencia FROM avaliacoes WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String urgStr = rs.getString("urgencia");
                    Urgencia urgObj = (urgStr == null) ? Urgencia.BAIXA : Urgencia.valueOf(urgStr);
                    return new AvaliacaoResponse(
                        rs.getLong("id"),
                        rs.getString("descricao"),
                        rs.getDouble("nota"),
                        urgObj,
                        rs.getTimestamp("data_criacao").toLocalDateTime().toString()
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter avaliação: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Obtém todas as avaliações.
     * @return Lista de AvaliacaoResponse
     */
    public static List<AvaliacaoResponse> obterTodas() {
        String sql = "SELECT id, descricao, nota, data_criacao, urgencia FROM avaliacoes ORDER BY data_criacao DESC";
        List<AvaliacaoResponse> lista = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String urgStr = rs.getString("urgencia");
                Urgencia urgObj = (urgStr == null) ? Urgencia.BAIXA : Urgencia.valueOf(urgStr);
                lista.add(new AvaliacaoResponse(
                    rs.getLong("id"),
                    rs.getString("descricao"),
                    rs.getDouble("nota"),
                    urgObj,
                    rs.getTimestamp("data_criacao").toLocalDateTime().toString()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter todas as avaliações: " + e.getMessage());
        }
        return lista;
    }
    
    /**
     * Obtém avaliações com filtro por nota mínima.
     * @param notaMinima Nota mínima
     * @return Lista de AvaliacaoResponse
     */
    public static List<AvaliacaoResponse> obterPorNotaMinima(double notaMinima) {
        String sql = "SELECT id, descricao, nota, data_criacao, urgencia FROM avaliacoes WHERE nota >= ? ORDER BY nota DESC";
        List<AvaliacaoResponse> lista = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, notaMinima);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String urgStr = rs.getString("urgencia");
                    Urgencia urgObj = (urgStr == null) ? Urgencia.BAIXA : Urgencia.valueOf(urgStr);
                    lista.add(new AvaliacaoResponse(
                        rs.getLong("id"),
                        rs.getString("descricao"),
                        rs.getDouble("nota"),
                        urgObj,
                        rs.getTimestamp("data_criacao").toLocalDateTime().toString()
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter avaliações por nota mínima: " + e.getMessage());
        }
        return lista;
    }
    
    /**
     * Atualiza uma avaliação.
     * @param id ID da avaliação
     * @param avaliacao Novos dados
     * @return true se atualizado com sucesso
     */
    public static boolean atualizarAvaliacao(long id, AvaliacaoRequest avaliacao) {
        String sql = "UPDATE avaliacoes SET descricao = ?, nota = ?, urgencia = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, avaliacao.getDescricao());
            pstmt.setDouble(2, avaliacao.getNota());
            pstmt.setString(3, Urgencia.fromNota(avaliacao.getNota()).name());
            pstmt.setLong(4, id);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar avaliação: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Deleta uma avaliação.
     * @param id ID da avaliação
     * @return true se deletado com sucesso
     */
    public static boolean deletarAvaliacao(long id) {
        String sql = "DELETE FROM avaliacoes WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao deletar avaliação: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Obtém o total de avaliações.
     * @return Número total de avaliações
     */
    public long obterTotalAvaliacoes() {
        String sql = "SELECT COUNT(*) as cnt FROM avaliacoes";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong("cnt");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter total de avaliações: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Obtém a média de notas das avaliações.
     * @return Média das notas (0 a 5)
     */
    public double obterMediaAvaliacoes() {
        String sql = "SELECT AVG(nota) as media FROM avaliacoes";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                double media = rs.getDouble("media");
                return rs.wasNull() ? 0.0 : media;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter média de avaliações: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * Obtém estatísticas das avaliações.
     * @return Objeto com min, max, avg, count
     */
    public static AvaliacaoStats obterEstatisticas() {
        String sql = "SELECT COUNT(*) as cnt, AVG(nota) as media, MIN(nota) as minima, MAX(nota) as maxima FROM avaliacoes";
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return new AvaliacaoStats(
                    rs.getInt("cnt"),
                    rs.getDouble("media"),
                    rs.getDouble("minima"),
                    rs.getDouble("maxima")
                );
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter estatísticas: " + e.getMessage());
        }
        return null;
    }
}
