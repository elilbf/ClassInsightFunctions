package com.classinsight.service;

import com.classinsight.dao.AvaliacaoDAO;
import com.classinsight.model.AvaliacaoRequest;
import com.classinsight.dto.AvaliacaoResponseDTO;
import com.classinsight.model.Urgencia;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AvaliacaoService {
    private static final Logger logger = LogManager.getLogger(AvaliacaoService.class);

    public static AvaliacaoResponseDTO process(AvaliacaoRequest request) {
        try {
            // 1. Insere no banco de dados
            long avaliacaoId = AvaliacaoDAO.inserirAvaliacao(request);
            
            if (avaliacaoId <= 0) {
                logger.error("Falha ao inserir avaliação no banco de dados");
                return null;
            }

            // 2. Cria resposta com os dados processados
            String descricao = request.getDescricao() + " - Nota: " + request.getNota();
            Urgencia urgencia = Urgencia.fromNota(request.getNota());
            String dataEnvio = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            
            AvaliacaoResponseDTO dto = new AvaliacaoResponseDTO(descricao, urgencia, dataEnvio);

            // 3. Se urgência for CRITICO ou ALTA, publica notificação
            if (urgencia == Urgencia.CRITICO || urgencia == Urgencia.ALTA) {
                try {
                    NotificationService.publishNotification(dto);
                    logger.info("Notificação publicada para urgência {}: ID {}", urgencia, avaliacaoId);
                } catch (Exception e) {
                    logger.error("Erro ao publicar notificação: {}", e.getMessage());
                }
            } else {
                logger.debug("Avaliação processada - ID: {}, Urgência: {}", avaliacaoId, urgencia);
            }

            return dto;
        } catch (Exception e) {
            logger.error("Erro ao processar avaliação: {}", e.getMessage(), e);
            return null;
        }
    }
}
