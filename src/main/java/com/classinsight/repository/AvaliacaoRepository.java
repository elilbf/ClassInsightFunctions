package com.classinsight.repository;

import com.classinsight.model.Avaliacao;
import com.classinsight.model.Urgencia;

import java.util.List;
import java.util.Optional;

/**
 * Interface para acesso a dados de avaliações.
 * Segue o padrão Repository para desacoplamento.
 */
public interface AvaliacaoRepository {
    
    /**
     * Salva uma avaliação no banco de dados.
     * @param avaliacao avaliação a ser salva
     * @return avaliação salva com ID gerado
     */
    Avaliacao save(Avaliacao avaliacao);
    
    /**
     * Busca avaliação por ID.
     * @param id ID da avaliação
     * @return Optional com a avaliação encontrada
     */
    Optional<Avaliacao> findById(Long id);
    
    /**
     * Busca avaliações por urgência.
     * @param urgencia nível de urgência
     * @return lista de avaliações com a urgência especificada
     */
    List<Avaliacao> findByUrgencia(Urgencia urgencia);
    
    /**
     * Busca todas as avaliações.
     * @return lista com todas as avaliações
     */
    List<Avaliacao> findAll();
    
    /**
     * Exclui uma avaliação por ID.
     * @param id ID da avaliação a ser excluída
     */
    void deleteById(Long id);
}
