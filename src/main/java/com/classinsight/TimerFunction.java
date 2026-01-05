package com.classinsight;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Azure Functions with Timer Trigger.
 * Executa a cada 1 minuto para gerar relatório de avaliações.
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
            // Obter todas as avaliações
            List<AvaliacaoResponse> avaliacoes = AvaliacaoDAO.obterTodas();
            
            if (avaliacoes == null || avaliacoes.isEmpty()) {
                context.getLogger().info("Nenhuma avaliação encontrada");
                return;
            }
            
            // Gerar relatório
            String relatorio = gerarRelatorio(avaliacoes);
            
            // Exibir relatório nos logs
            context.getLogger().info("\n" + relatorio);
            
            // Salvar relatório no banco de dados (opcional)
            salvarRelatorioBD(avaliacoes);
            
            context.getLogger().info("Relatório processado com sucesso");
            
        } catch (Exception e) {
            context.getLogger().severe("Erro ao processar relatório: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Gera relatório em formato texto conforme especificado.
     */
    private static String gerarRelatorio(List<AvaliacaoResponse> avaliacoes) {
        StringBuilder sb = new StringBuilder();
        
        // Cabeçalho
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║              RELATÓRIO DE AVALIAÇÕES                           ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════╝\n\n");
        
        // Data de envio
        String dataEnvio = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        sb.append("📅 Data de Envio: ").append(dataEnvio).append("\n\n");
        
        // Seção 1: Descrição das avaliações
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("📋 DESCRIÇÃO DAS AVALIAÇÕES\n");
        sb.append("═══════════════════════════════════════════════════════════════\n");
        for (AvaliacaoResponse av : avaliacoes) {
            sb.append(String.format("• %s (Nota: %.1f)\n", av.getDescricao(), av.getNota()));
        }
        sb.append("\n");
        
        // Seção 2: Quantidade de avaliações por dia
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("📊 QUANTIDADE DE AVALIAÇÕES POR DIA\n");
        sb.append("═══════════════════════════════════════════════════════════════\n");
        Map<String, Long> avaliacoesPorDia = agruparPorDia(avaliacoes);
        if (avaliacoesPorDia.isEmpty()) {
            sb.append("Nenhuma avaliação encontrada\n");
        } else {
            avaliacoesPorDia.forEach((dia, quantidade) -> {
                sb.append(String.format("  %s: %d avaliação(ções)\n", dia, quantidade));
            });
        }
        sb.append("\n");
        
        // Seção 3: Quantidade de avaliações por urgência
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append("⚠️ QUANTIDADE DE AVALIAÇÕES POR URGÊNCIA\n");
        sb.append("═══════════════════════════════════════════════════════════════\n");
        Map<Urgencia, Long> avaliacoesPorUrgencia = agruparPorUrgencia(avaliacoes);
        for (Urgencia urgencia : Urgencia.values()) {
            long quantidade = avaliacoesPorUrgencia.getOrDefault(urgencia, 0L);
            String emoji = getEmojiUrgencia(urgencia);
            sb.append(String.format("  %s %s: %d avaliação(ções)\n", emoji, urgencia.name(), quantidade));
        }
        sb.append("\n");
        
        // Rodapé
        sb.append("═══════════════════════════════════════════════════════════════\n");
        sb.append(String.format("Total de Avaliações: %d\n", avaliacoes.size()));
        sb.append(String.format("Nota Média: %.2f\n", calcularMediaNotas(avaliacoes)));
        sb.append("═══════════════════════════════════════════════════════════════\n");
        
        return sb.toString();
    }
    
    /**
     * Agrupa avaliações por dia.
     */
    private static Map<String, Long> agruparPorDia(List<AvaliacaoResponse> avaliacoes) {
        return avaliacoes.stream()
            .collect(Collectors.groupingBy(
                av -> extrairData(av.getDataCriacao()),
                Collectors.counting()
            ));
    }
    
    /**
     * Agrupa avaliações por urgência (baseada na nota).
     */
    private static Map<Urgencia, Long> agruparPorUrgencia(List<AvaliacaoResponse> avaliacoes) {
        Map<Urgencia, Long> mapa = new HashMap<>();
        for (Urgencia u : Urgencia.values()) {
            mapa.put(u, 0L);
        }
        
        for (AvaliacaoResponse av : avaliacoes) {
            Urgencia urgencia = Urgencia.fromNota(av.getNota());
            mapa.put(urgencia, mapa.get(urgencia) + 1);
        }
        
        return mapa;
    }
    
    /**
     * Extrai a data (dd/MM/yyyy) do timestamp.
     */
    private static String extrairData(String dataCriacao) {
        if (dataCriacao == null || dataCriacao.isEmpty()) {
            return "Desconhecida";
        }
        try {
            // Formato esperado: yyyy-MM-ddTHH:mm:ss ou similar
            String[] partes = dataCriacao.split("T")[0].split("-");
            if (partes.length >= 3) {
                return String.format("%s/%s/%s", partes[2], partes[1], partes[0]);
            }
        } catch (Exception e) {
            // Se falhar, retorna como está
        }
        return dataCriacao;
    }
    
    /**
     * Calcula a média de notas.
     */
    private static double calcularMediaNotas(List<AvaliacaoResponse> avaliacoes) {
        if (avaliacoes.isEmpty()) {
            return 0.0;
        }
        return avaliacoes.stream()
            .mapToDouble(AvaliacaoResponse::getNota)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Retorna emoji baseado na urgência.
     */
    private static String getEmojiUrgencia(Urgencia urgencia) {
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
     * Salva o relatório no banco de dados.
     */
    private static void salvarRelatorioBD(List<AvaliacaoResponse> avaliacoes) {
        try {
            int totalAvaliacoes = avaliacoes.size();
            double mediaNotas = calcularMediaNotas(avaliacoes);
            
            // Aqui você poderia salvar na tabela de relatórios
            // INSERT INTO relatorios (total_avaliacoes, media_notas, data_geracao)
            System.out.println("✅ Relatório salvo: Total=" + totalAvaliacoes + ", Média=" + mediaNotas);
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao salvar relatório no BD: " + e.getMessage());
        }
    }
}
