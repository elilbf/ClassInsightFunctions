package com.classinsight;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import java.util.List;

/**
 * Azure Functions with Timer Trigger.
 * Executa tarefas agendadas (e.g., gerar relatórios, limpeza, etc).
 */
public class TimerFunction {
    
    @FunctionName("relatorioAvaliacoes")
    public void relatorioAvaliacoes(
            @TimerTrigger(
                name = "processarTimer",
                schedule = "0 */1 * * * *") // A cada 1 minuto
            String timerInfo,
            final ExecutionContext context) {
        context.getLogger().info("Processamento agendado de avaliações iniciado: " + timerInfo);
        
        try {
            // ===== EXEMPLO 1: Obter Estatísticas =====
            AvaliacaoStats stats = AvaliacaoDAO.obterEstatisticas();
            if (stats != null) {
                context.getLogger().info("=== RELATÓRIO DE AVALIAÇÕES ===");
                context.getLogger().info("Total de avaliações: " + stats.getTotalAvaliacoes());
                context.getLogger().info("Nota média: " + String.format("%.2f", stats.getMediaNotas()));
                context.getLogger().info("Nota mínima: " + stats.getNotaMinima());
                context.getLogger().info("Nota máxima: " + stats.getNotaMaxima());
            }
            
            // ===== EXEMPLO 2: Buscar Avaliações com Nota Alta =====
            List<AvaliacaoResponse> avaliacoesBoas = AvaliacaoDAO.obterPorNotaMinima(8.0);
            context.getLogger().info("Avaliações com nota >= 8: " + avaliacoesBoas.size());
            for (AvaliacaoResponse av : avaliacoesBoas) {
                context.getLogger().info("  - " + av.getDescricao() + " (Nota: " + av.getNota() + ")");
            }
            
            // ===== EXEMPLO 3: Processar Todas as Avaliações =====
            List<AvaliacaoResponse> todasAvaliacoes = AvaliacaoDAO.obterTodas();
            context.getLogger().info("Total processado: " + todasAvaliacoes.size());
            
            // Aqui você poderia:
            // - Enviar email com relatório
            // - Armazenar em blob storage
            // - Atualizar dashboard
            // - Gerar alertas
            
            context.getLogger().info("Processamento agendado concluído com sucesso");
            
        } catch (Exception e) {
            context.getLogger().severe("Erro ao processar relatório: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
