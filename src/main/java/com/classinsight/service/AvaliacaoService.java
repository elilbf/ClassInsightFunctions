package com.classinsight.service;

import com.classinsight.AvaliacaoDAO;
import com.classinsight.AvaliacaoRequest;
import com.classinsight.AvaliacaoResponseDTO;
import com.classinsight.AvaliacaoResponse;
import com.classinsight.Urgencia;

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
            
            return new AvaliacaoResponseDTO(descricao, urgencia, dataEnvio);
        } catch (Exception e) {
            System.err.println("❌ Erro ao processar avaliação: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
