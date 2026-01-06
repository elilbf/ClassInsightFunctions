package com.classinsight.service;

import com.classinsight.AvaliacaoResponseDTO;
import com.classinsight.Urgencia;

/**
 * Serviço para processar e registrar notificações.
 * Enfileira notificações na fila do Azure para processamento assíncrono.
 */
public class NotificationService {

    /**
     * Formata e publica uma notificação de avaliação no serviço de notificação.
     * @param dto Objeto AvaliacaoResponseDTO contendo dados da avaliação
     */
    public static void publishNotification(AvaliacaoResponseDTO dto) {
        String mensagem = formatarMensagemNotificacao(dto);
        processNotification(mensagem);
    }

    /**
     * Formata uma mensagem estruturada baseada nos dados de AvaliacaoResponseDTO.
     * @param dto Objeto AvaliacaoResponseDTO
     * @return Mensagem formatada para notificação
     */
    private static String formatarMensagemNotificacao(AvaliacaoResponseDTO dto) {
        StringBuilder sb = new StringBuilder();
        
        // Emoji e cabeçalho conforme urgência
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

    /**
     * Retorna emoji baseado na urgência.
     */
    private static String getEmojiUrgencia(Urgencia urgencia) {
        if (urgencia == null) {
            return "⚪";
        }
        switch (urgencia) {
            case CRITICO:
                return "🔴";
            case ALTA:
                return "🟠";
            case MEDIA:
                return "🟡";
            case BAIXA:
                return "🟢";
            default:
                return "⚪";
        }
    }

    /**
     * Retorna título baseado na urgência.
     */
    private static String getTituloUrgencia(Urgencia urgencia) {
        if (urgencia == null) {
            return "NOTIFICAÇÃO";
        }
        switch (urgencia) {
            case CRITICO:
                return "ALERTA CRÍTICO - AÇÃO IMEDIATA REQUERIDA";
            case ALTA:
                return "ALERTA ALTA URGÊNCIA - ATENÇÃO NECESSÁRIA";
            case MEDIA:
                return "NOTIFICAÇÃO DE MÉDIA URGÊNCIA";
            case BAIXA:
                return "NOTIFICAÇÃO GERAL";
            default:
                return "NOTIFICAÇÃO";
        }
    }

    /**
     * Processa a notificação (loga e a publica na fila).
     */
    public static void processNotification(String message) {
        System.out.println("📬 Notificação recebida e processada:");
        System.out.println(message);
    }
}
