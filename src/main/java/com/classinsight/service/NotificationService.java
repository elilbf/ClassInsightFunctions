package com.classinsight.service;

import com.classinsight.dto.AvaliacaoResponseDTO;
import com.classinsight.model.Urgencia;

/**
 * Serviço de notificação: formata mensagens a partir de `AvaliacaoResponseDTO`
 * e publica/processa notificações (integrado com Azure Queue).
 */
public class NotificationService {

    private static EmailSender emailSender;

    static {
        // try to initialize ACS sender if connection string is present
        String conn = System.getenv("AZURE_COMMUNICATION_CONNECTION_STRING");
        if (conn != null && !conn.isBlank()) {
            try {
                emailSender = new AzureCommunicationEmailSender(conn);
            } catch (Exception e) {
                System.err.println("Failed to init AzureCommunicationEmailSender: " + e.getMessage());
            }
        }
    }

    /**
     * For tests we can inject a mock sender.
     */
    public static void setEmailSender(EmailSender sender) {
        emailSender = sender;
    }

    /**
     * Publica notificação (formata, enfileira e envia por e-mail quando possível).
     */
    public static void publishNotification(AvaliacaoResponseDTO dto) {
        String message = formatarMensagemNotificacao(dto);
        // enqueue for async processing (if storage configured)
        try {
            NotificationQueueClient.enqueueNotification(message);
        } catch (Exception e) {
            System.err.println("Failed to enqueue notification: " + e.getMessage());
        }

        // If urgency is CRITICO or ALTA, attempt to send email immediately
        if (dto != null && dto.getUrgencia() != null) {
            if (dto.getUrgencia() == Urgencia.CRITICO || dto.getUrgencia() == Urgencia.ALTA) {
                String from = System.getenv().getOrDefault("NOTIFICATION_FROM_EMAIL", System.getenv("ADMIN_EMAIL"));
                String toEnv = System.getenv().getOrDefault("ADMIN_EMAIL", null);
                String subject = getTituloUrgencia(dto.getUrgencia());

                if (emailSender != null && from != null && toEnv != null) {
                    // separa os emails por ";"
                    String[] recipients = toEnv.split(";");
                    for (String recipient : recipients) {
                        recipient = recipient.trim();
                        if (!recipient.isEmpty()) {
                            boolean sent = emailSender.send(from, recipient, subject, message);
                            if (sent) {
                                System.out.println("Email sent to " + recipient + " for urgency " + dto.getUrgencia());
                            } else {
                                System.err.println("Failed to send email to " + recipient + " for urgency " + dto.getUrgencia());
                            }
                        }
                    }
                } else {
                    System.err.println("Email sender not configured or recipient missing; skipping email send.");
                }
            }
        }
    }


    private static String formatarMensagemNotificacao(AvaliacaoResponseDTO dto) {
        StringBuilder sb = new StringBuilder();
        String emoji = getEmojiUrgencia(dto.getUrgencia());
        String titulo = getTituloUrgencia(dto.getUrgencia());
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║ ").append(emoji).append(" ").append(titulo).append("\n");
        sb.append("╚════════════════════════════════════════════════════════════════╝\n\n");
        sb.append("📋 DESCRIÇÃO:\n");
        sb.append("  ").append(dto.getDescricao()).append("\n\n");
        sb.append("🚨 URGÊNCIA:\n");
        sb.append("  ").append(dto.getUrgencia().name()).append("\n\n");
        sb.append("📅 DATA:\n");
        sb.append("  ").append(dto.getDataEnvio()).append("\n\n");
        sb.append("═══════════════════════════════════════════════════════════════\n");
        return sb.toString();
    }

    private static String getEmojiUrgencia(Urgencia urgencia) {
        if (urgencia == null) return "⚪";
        switch (urgencia) {
            case CRITICO: return "🔴";
            case ALTA: return "🟠";
            case MEDIA: return "🟡";
            case BAIXA: return "🟢";
            default: return "⚪";
        }
    }

    private static String getTituloUrgencia(Urgencia urgencia) {
        if (urgencia == null) return "NOTIFICAÇÃO";
        switch (urgencia) {
            case CRITICO: return "ALERTA CRÍTICO - AÇÃO IMEDIATA REQUERIDA";
            case ALTA: return "ALERTA ALTA URGÊNCIA - ATENÇÃO NECESSÁRIA";
            case MEDIA: return "NOTIFICAÇÃO DE MÉDIA URGÊNCIA";
            case BAIXA: return "NOTIFICAÇÃO GERAL";
            default: return "NOTIFICAÇÃO";
        }
    }

    /**
     * Loga a notificação (stdout e stderr).
     */
    private static boolean logNotification(String message) {
        System.out.println("🔔 NOTIFICAÇÃO PUBLICADA:");
        System.out.println(message);
        return true;
    }
}
