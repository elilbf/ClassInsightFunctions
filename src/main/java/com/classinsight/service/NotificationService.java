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
     * For tests we can inject a mock sender.
     */
    public static void setEmailSender(EmailSender sender) {
        emailSender = sender;
    }

    /**
     * Publica notificação (enfileira e envia por e-mail quando urgência for CRITICO ou ALTA).
     */
    public static void publishNotification(AvaliacaoResponseDTO dto) {
        // enqueue for async processing (if storage configured)
        try {
            NotificationQueueClient.enqueueNotification(dto.toString());
        } catch (Exception e) {
            logger.error("Falha ao enfileirar notificação: {}", e.getMessage());
        }

        // If urgency is CRITICO or ALTA, attempt to send email immediately
        if (dto != null && dto.getUrgencia() != null && 
            (dto.getUrgencia() == Urgencia.CRITICO || dto.getUrgencia() == Urgencia.ALTA)) {
            enviarAlertaProfissional(dto);
        }
    }


    /**
     * Gera corpo de email simples e direto.
     */
    private static String gerarCorpoEmailSimples(AvaliacaoResponseDTO dto) {
        StringBuilder sb = new StringBuilder();
        
        // Saudação
        sb.append("Prezado(a) Gestor(a),\n\n");
        
        // Mensagem principal
        sb.append("Uma avaliação classificada como ");
        sb.append(dto.getUrgencia() != null ? dto.getUrgencia().name() : "Não classificada");
        sb.append(" foi registrada no sistema e requer atenção imediata.\n\n");
        
        // Detalhes essenciais
        sb.append("Descrição: ").append(dto.getDescricao() != null ? dto.getDescricao() : "Sem descrição").append("\n");
        sb.append("Classificação: ").append(dto.getUrgencia() != null ? dto.getUrgencia().name() : "Não definida").append("\n");
        sb.append("Data: ").append(formatarDataProfissional(dto.getDataEnvio())).append("\n");
        
        return sb.toString();
    }

    /**
     * Formata data no padrão brasileiro.
     */
    private static String formatarDataProfissional(String data) {
        if (data == null || data.isEmpty()) {
            return "Data não disponível";
        }
        
        try {
            // Extrair data e hora do timestamp
            String[] partesData = data.split("T");
            if (partesData.length >= 2) {
                String dataParte = partesData[0]; // yyyy-MM-dd
                String horaParte = partesData[1].substring(0, 5); // HH:mm
                
                String[] dataPartes = dataParte.split("-");
                if (dataPartes.length == 3) {
                    return String.format("%s/%s/%s às %s", 
                        dataPartes[2], dataPartes[1], dataPartes[0], horaParte);
                }
            }
        } catch (Exception e) {
            // Em caso de erro, retorna a data original
            return data;
        }
        
        return data;
    }

    /**
     * Envia alerta por email para gestores quando urgência é CRITICO ou ALTA.
     * Utiliza o método gerarCorpoEmailSimples para formatar o corpo do email.
     */
    private static void enviarAlertaProfissional(AvaliacaoResponseDTO dto) {
        if (emailSender == null) {
            System.err.println("Email sender not available - cannot send alert");
            return;
        }

        try {
            // Obter configurações do email
            String fromEmail = System.getenv("NOTIFICATION_FROM_EMAIL");
            String adminEmail = System.getenv("ADMIN_EMAIL");

            if (fromEmail == null || adminEmail == null) {
                System.err.println("Email configuration missing - cannot send alert");
                return;
            }

            // Gerar assunto baseado na urgência
            String subject = "Alerta ClassInsight - " +
                    (dto.getUrgencia() != null ? dto.getUrgencia().name() : "Não classificada");

            // Usar o método gerarCorpoEmailSimples para formatar o corpo do email
            String emailBody = gerarCorpoEmailSimples(dto);

            // Separar os emails por ";"
            String[] destinatarios = adminEmail.split(";");
            for (String destinatario : destinatarios) {
                destinatario = destinatario.trim(); // remover espaços extras
                if (!destinatario.isEmpty()) {
                    boolean enviado = emailSender.send(fromEmail, destinatario, subject, emailBody);

                    if (enviado) {
                        logger.info("Alerta enviado com sucesso para: {}", destinatario);
                    } else {
                        logger.error("Falha ao enviar alerta para: {}", destinatario);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Falha na notificação: {}", e.getMessage());
            e.printStackTrace();
        }
    }


}
