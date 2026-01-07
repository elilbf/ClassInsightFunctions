package com.classinsight;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.classinsight.AvaliacaoResponseDTO;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;

/**
 * Função que recebe mensagens da fila de notificações e processa notificações de urgência.
 * A notificação já foi publicada pela AvaliacaoService, esta função apenas a processa/loga.
 */
public class NotificationFunction {
    private static final ObjectMapper mapper = new ObjectMapper();

    @FunctionName("notificarUrgencia")
    public void run(
            @QueueTrigger(name = "message", queueName = "notificacoes-urgencia", connection = "AzureWebJobsStorage") String message,
            final ExecutionContext context) {
        context.getLogger().info("Processando notificação da fila: " + message);
        try {
            AvaliacaoResponseDTO dto = mapper.readValue(message, AvaliacaoResponseDTO.class);
            // A notificação já foi publicada e email tentado em AvaliacaoService
            // Aqui apenas processamos/registramos de forma assíncrona
            context.getLogger().info("✅ Notificação processada com sucesso para urgência: " + dto.getUrgencia());
            context.getLogger().info("   Descrição: " + dto.getDescricao());
        } catch (Exception e) {
            context.getLogger().severe("Erro ao processar mensagem de notificação: " + e.getMessage());
            throw new RuntimeException("Failed to process notification message", e);
        }
    }
}
