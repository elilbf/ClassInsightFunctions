package com.classinsight.service;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NotificationQueueClient {
    private static final Logger logger = LogManager.getLogger(NotificationQueueClient.class);
    private static final String QUEUE_NAME = System.getenv().getOrDefault("NOTIFICATION_QUEUE_NAME", "notificacao-urgencia");
    private static final String CONNECTION_STRING = System.getenv("AZURE_STORAGE_CONNECTION_STRING");

    public static void enqueueNotification(String message) {
        if (CONNECTION_STRING == null || CONNECTION_STRING.isEmpty()) {
            logger.warn("AZURE_STORAGE_CONNECTION_STRING não configurado; mensagem de notificação não enviada. Conteúdo: {}", message);
            return;
        }
        QueueClient queueClient = new QueueClientBuilder()
            .connectionString(CONNECTION_STRING)
            .queueName(QUEUE_NAME)
            .buildClient();

        // cria fila se não existir
        queueClient.createIfNotExists();

        queueClient.sendMessage(message);
    }
}
