package com.classinsight;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.classinsight.AvaliacaoResponseDTO;
import com.classinsight.service.NotificationService;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;

/**
 * Função que recebe mensagens da fila de notificações e processa notificações de urgência.
 * A notificação é registrada/processada e pode ser integrada a um serviço de notificações externo.
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
            
            String notificationContent = String.format(
                "Notificação de urgência recebida\nDescrição: %s\nUrgência: %s\nData: %s",
                dto.getDescricao(), dto.getUrgencia(), dto.getDataEnvio()
            );

            NotificationService.processNotification(notificationContent);
            context.getLogger().info("Notificação processada com sucesso");
        } catch (Exception e) {
            context.getLogger().severe("Erro ao processar mensagem de notificação: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
