package com.classinsight.repository;

import com.classinsight.exception.DatabaseException;
import com.classinsight.model.Avaliacao;
import com.classinsight.model.Urgencia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementação do repository de avaliações usando JDBC puro.
 */
public class AvaliacaoRepositoryImpl implements AvaliacaoRepository {
    
    private final Connection connection;
    
    public AvaliacaoRepositoryImpl(Connection connection) {
        this.connection = connection;
    }
    
    @Override
    public Avaliacao save(Avaliacao avaliacao) {
        String sql = "INSERT INTO avaliacoes (descricao, nota, urgencia, data_criacao) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, avaliacao.getDescricao());
            stmt.setDouble(2, avaliacao.getNota());
            stmt.setString(3, avaliacao.getUrgencia().name());
            stmt.setTimestamp(4, Timestamp.valueOf(avaliacao.getDataCriacao()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Falha ao inserir avaliação");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    avaliacao.setId(generatedKeys.getLong(1));
                }
            }
            
            return avaliacao;
            
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao salvar avaliação", e);
        }
    }
    
    @Override
    public Optional<Avaliacao> findById(Long id) {
        String sql = "SELECT id, descricao, nota, urgencia, data_criacao FROM avaliacoes WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Avaliacao avaliacao = mapResultSetToAvaliacao(rs);
                    return Optional.of(avalicao);
                }
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar avaliação por ID", e);
        }
    }
    
    @Override
    public List<Avaliacao> findByUrgencia(Urgencia urgencia) {
        String sql = "SELECT id, descricao, nota, urgencia, data_criacao FROM avaliacoes WHERE urgencia = ? ORDER BY data_criacao DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, urgencia.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Avaliacao> avaliacoes = new ArrayList<>();
                
                while (rs.next()) {
                    avaliacoes.add(mapResultSetToAvaliacao(rs));
                }
                
                return avaliacoes;
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar avaliações por urgência", e);
        }
    }
    
    @Override
    public List<Avaliacao> findAll() {
        String sql = "SELECT id, descricao, nota, urgencia, data_criacao FROM avaliacoes ORDER BY data_criacao DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                List<Avaliacao> avaliacoes = new ArrayList<>();
                
                while (rs.next()) {
                    avaliacoes.add(mapResultSetToAvaliacao(rs));
                }
                
                return avaliacoes;
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar todas as avaliações", e);
        }
    }
    
    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM avaliacoes WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new DatabaseException("Falha ao excluir avaliação");
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao excluir avaliação", e);
        }
    }
    
    /**
     * Mapeia ResultSet para entidade Avaliacao.
     */
    private Avaliacao mapResultSetToAvaliacao(ResultSet rs) throws SQLException {
        Avaliacao avaliacao = new Avaliacao();
        
        avaliacao.setId(rs.getLong("id"));
        avaliacao.setDescricao(rs.getString("descricao"));
        avaliacao.setNota(rs.getDouble("nota"));
        avaliacao.setUrgencia(Urgencia.valueOf(rs.getString("urgencia")));
        avaliacao.setDataCriacao(rs.getTimestamp("data_criacao").toLocalDateTime());
        
        return avaliacao;
    }
}
