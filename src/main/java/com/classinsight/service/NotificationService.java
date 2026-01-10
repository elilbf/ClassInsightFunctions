package com.classinsight.service;

import com.classinsight.dto.AvaliacaoResponseDTO;
import com.classinsight.model.Urgencia;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Serviço de notificação: formata mensagens a partir de `AvaliacaoResponseDTO`
 * e publica/processa notificações (integrado com Azure Queue).
 */
public class NotificationService {
    private static final Logger logger = LogManager.getLogger(NotificationService.class);

    @Setter
    private static EmailSender emailSender;

    static {
        // try to initialize ACS sender if connection string is present
        String conn = System.getenv("AZURE_COMMUNICATION_CONNECTION_STRING");
        if (conn != null && !conn.isBlank()) {
            try {
                emailSender = new AzureCommunicationEmailSender(conn);
                logger.info("AzureCommunicationEmailSender inicializado com sucesso");
            } catch (Exception e) {
                logger.error("Falha ao inicializar AzureCommunicationEmailSender: {}", e.getMessage());
            }
        } else {
            logger.warn("AZURE_COMMUNICATION_CONNECTION_STRING não configurada - envio de email desabilitado");
        }
    }

    /**
     * Publica notificação (formata, enfileira e envia por e-mail quando possível).
     */
    public static void publishNotification(AvaliacaoResponseDTO dto) {
        logger.debug("Publicando notificação para avaliação com urgência: {}",
                     dto != null && dto.getUrgencia() != null ? dto.getUrgencia() : "DESCONHECIDA");

        String message = formatarMensagemNotificacao(dto);
        // enqueue for async processing (if storage configured)
        try {
            NotificationQueueClient.enqueueNotification(message);
            logger.debug("Notificação enfileirada com sucesso");
        } catch (Exception e) {
            logger.error("Falha ao enfileirar notificação: {}", e.getMessage());
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
                    logger.debug("Enviando email para {} destinatário(s)", recipients.length);
                    for (String recipient : recipients) {
                        recipient = recipient.trim();
                        if (!recipient.isEmpty()) {
                            boolean sent = emailSender.send(from, recipient, subject, message);
                            if (sent) {
                                logger.info("Email enviado para {} para urgência {}", recipient, dto.getUrgencia());
                            } else {
                                logger.error("Falha ao enviar email para {} para urgência {}", recipient, dto.getUrgencia());
                            }
                        }
                    }
                } else {
                    logger.warn("Sender de email não configurado ou destinatário ausente - envio de email ignorado");
                }
            } else {
                logger.debug("Urgência {} não requer envio de email", dto.getUrgencia());
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
     * Loga a notificação.
     */
    private static boolean logNotification(String message) {
        logger.info("Notificação publicada:");
        logger.info(message);
        return true;
    }
}
