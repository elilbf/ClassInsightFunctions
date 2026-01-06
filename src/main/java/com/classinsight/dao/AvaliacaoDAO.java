package com.classinsight.dao;

import com.classinsight.dto.request.AvaliacaoRequest;
import com.classinsight.model.Avaliacao;
import com.classinsight.model.Urgencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para Avaliações.
 * Realiza operações CRUD no banco de dados.
 * LEGADO: Manter para compatibilidade, substituir por AvaliacaoRepository.
 */
@Deprecated
public class AvaliacaoDAO {
    
    /**
     * Insere uma nova avaliação no banco de dados.
     * @param avaliacao AvaliacaoRequest contendo descrição e nota
     * @return ID da avaliação inserida, ou -1 se erro
     */
    public static long inserirAvaliacao(AvaliacaoRequest avaliacao) {
        String sql = "INSERT INTO avaliacoes (descricao, nota, urgencia, data_criacao) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, avaliacao.getDescricao());
            stmt.setDouble(2, avaliacao.getNota());
            stmt.setString(3, Urgencia.fromNota(avalicao.getNota()).name());
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                return -1;
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao inserir avaliação: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    
    /**
     * Busca avaliações por urgência.
     * @param urgencia nível de urgência
     * @return lista de avaliações com a urgência especificada
     */
    public static List<Avaliacao> buscarPorUrgencia(Urgencia urgencia) {
        List<Avaliacao> avaliacoes = new ArrayList<>();
        String sql = "SELECT id, descricao, nota, urgencia, data_criacao FROM avaliacoes WHERE urgencia = ? ORDER BY data_criacao DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, urgencia.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Avaliacao avaliacao = new Avaliacao();
                    avaliacao.setId(rs.getLong("id"));
                    avaliacao.setDescricao(rs.getString("descricao"));
                    avaliacao.setNota(rs.getDouble("nota"));
                    avaliacao.setUrgencia(Urgencia.valueOf(rs.getString("urgencia")));
                    avaliacao.setDataCriacao(rs.getTimestamp("data_criacao").toLocalDateTime());
                    
                    avaliacoes.add(avalicao);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar avaliações por urgência: " + e.getMessage());
            e.printStackTrace();
        }
        
        return avaliacoes;
    }
    
    /**
     * Busca todas as avaliações.
     * @return lista com todas as avaliações
     */
    public static List<Avaliacao> buscarTodas() {
        List<Avaliacao> avaliacoes = new ArrayList<>();
        String sql = "SELECT id, descricao, nota, urgencia, data_criacao FROM avaliacoes ORDER BY data_criacao DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Avaliacao avaliacao = new Avaliacao();
                    avaliacao.setId(rs.getLong("id"));
                    avaliacao.setDescricao(rs.getString("descricao"));
                    avaliacao.setNota(rs.getDouble("nota"));
                    avaliacao.setUrgencia(Urgencia.valueOf(rs.getString("urgencia")));
                    avaliacao.setDataCriacao(rs.getTimestamp("data_criacao").toLocalDateTime());
                    
                    avaliacoes.add(avalicao);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar todas as avaliações: " + e.getMessage());
            e.printStackTrace();
        }
        
        return avaliacoes;
    }
}
