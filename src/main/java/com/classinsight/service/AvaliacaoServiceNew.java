package com.classinsight.service;

import com.classinsight.dto.request.AvaliacaoRequest;
import com.classinsight.dto.response.AvaliacaoResponseDTO;
import com.classinsight.exception.DatabaseException;
import com.classinsight.exception.ValidationException;
import com.classinsight.model.Avaliacao;
import com.classinsight.model.Urgencia;
import com.classinsight.repository.AvaliacaoRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service para processamento de avaliações com injeção de dependências.
 * Versão refatorada sem dependências de frameworks específicos.
 */
public class AvaliacaoServiceNew {
    
    private final AvaliacaoRepository avaliacaoRepository;
    private final NotificationService notificationService;
    
    public AvaliacaoServiceNew(AvaliacaoRepository avaliacaoRepository,
                              NotificationService notificationService) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.notificationService = notificationService;
    }
    
    /**
     * Processa uma requisição de avaliação.
     * @param request dados da avaliação
     * @return DTO com resultado processado
     * @throws ValidationException se dados inválidos
     * @throws DatabaseException se erro no banco
     */
    public AvaliacaoResponseDTO process(AvaliacaoRequest request) {
        validateRequest(request);
        
        try {
            // Criar entidade
            Avaliacao avaliacao = createAvaliacao(request);
            
            // Salvar no banco
            Avaliacao saved = avaliacaoRepository.save(avaliacao);
            
            // Criar resposta
            AvaliacaoResponseDTO response = createResponseDTO(saved);
            
            // Enviar notificação se necessário
            if (shouldNotify(saved.getUrgencia())) {
                notificationService.sendNotification(saved);
            }
            
            return response;
            
        } catch (Exception e) {
            throw new DatabaseException("Erro ao processar avaliação", e);
        }
    }
    
    /**
     * Valida os dados da requisição.
     */
    private void validateRequest(AvaliacaoRequest request) {
        if (request == null) {
            throw new ValidationException("Request não pode ser nulo");
        }
        
        if (request.getDescricao() == null || request.getDescricao().trim().isEmpty()) {
            throw new ValidationException("Descrição é obrigatória");
        }
        
        if (request.getNota() == null || request.getNota() < 0 || request.getNota() > 10) {
            throw new ValidationException("Nota deve estar entre 0 e 10");
        }
    }
    
    /**
     * Cria entidade Avaliacao a partir do request.
     */
    private Avaliacao createAvaliacao(AvaliacaoRequest request) {
        String descricaoCompleta = request.getDescricao() + " - Nota: " + request.getNota();
        Urgencia urgencia = Urgencia.fromNota(request.getNota());
        
        return new Avaliacao(descricaoCompleta, request.getNota());
    }
    
    /**
     * Cria DTO de resposta a partir da entidade.
     */
    private AvaliacaoResponseDTO createResponseDTO(Avaliacao avaliacao) {
        String dataEnvio = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        return new AvaliacaoResponseDTO(avaliacao.getDescricao(), avaliacao.getUrgencia(), LocalDateTime.parse(dataEnvio, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }
    
    /**
     * Verifica se deve enviar notificação.
     */
    private boolean shouldNotify(Urgencia urgencia) {
        return urgencia == Urgencia.CRITICO || urgencia == Urgencia.ALTA;
    }
}
