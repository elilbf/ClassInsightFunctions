package com.classinsight.service;

import com.classinsight.dao.AvaliacaoDAO;
import com.classinsight.model.AvaliacaoRequest;
import com.classinsight.dto.AvaliacaoResponseDTO;
import com.classinsight.model.Urgencia;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AvaliacaoService {

    public static AvaliacaoResponseDTO process(AvaliacaoRequest request) {
        try {
            // 1. Insere no banco de dados
            long avaliacaoId = AvaliacaoDAO.inserirAvaliacao(request);
            
            if (avaliacaoId <= 0) {
                System.err.println("❌ Falha ao inserir avaliação no banco de dados");
                return null;
            }
            
            System.out.println("✅ Avaliação inserida no BD com ID: " + avaliacaoId);
            
            // 2. Cria resposta com os dados processados
            String descricao = request.getDescricao() + " - Nota: " + request.getNota();
            Urgencia urgencia = Urgencia.fromNota(request.getNota());
            String dataEnvio = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            
            AvaliacaoResponseDTO dto = new AvaliacaoResponseDTO(descricao, urgencia, dataEnvio);

            // 3. Se urgência for CRITICO ou ALTA, publica notificação
            if (urgencia == Urgencia.CRITICO || urgencia == Urgencia.ALTA) {
                try {
                    NotificationService.publishNotification(dto);
                    System.out.println("🔔 Notificação publicada para urgência: " + urgencia);
                } catch (Exception e) {
                    System.err.println("Erro ao publicar notificação: " + e.getMessage());
                }
            }

            return dto;
        } catch (Exception e) {
            System.err.println("❌ Erro ao processar avaliação: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
