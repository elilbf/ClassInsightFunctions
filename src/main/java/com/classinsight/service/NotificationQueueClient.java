package com.classinsight.service;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;

public class NotificationQueueClient {
    private static final String QUEUE_NAME = System.getenv().getOrDefault("NOTIFICATION_QUEUE_NAME", "notificacoes-urgencia");
    private static final String CONNECTION_STRING = System.getenv("AZURE_STORAGE_CONNECTION_STRING");

    public static void enqueueNotification(String message) {
        if (CONNECTION_STRING == null || CONNECTION_STRING.isEmpty()) {
            System.err.println("AZURE_STORAGE_CONNECTION_STRING não configurado; mensagem de notificação não enviada. Conteúdo: " + message);
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
