package com.classinsight;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

/**
 * Azure Functions with Timer Trigger.
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
        
        // Lógica para processar avaliações em lote
        // Ex: limpar dados antigos, gerar relatórios, enviar notificações, etc.
        
        context.getLogger().info("Processamento agendado concluído");
    }
}
