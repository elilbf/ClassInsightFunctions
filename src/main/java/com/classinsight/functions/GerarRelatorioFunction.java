package com.classinsight.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.classinsight.model.AvaliacaoResponse;
import com.classinsight.dao.AvaliacaoDAO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.classinsight.service.EmailSender;
import com.classinsight.service.AzureCommunicationEmailSender;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Azure Functions with Timer Trigger.
 * Executa todos os dias às 17:00 para gerar e enviar relatório de avaliações por email.
 */
public class GerarRelatorioFunction {
    private static final Logger logger = LogManager.getLogger(GerarRelatorioFunction.class);

    @FunctionName("relatorioAvaliacoes")
    public void relatorioAvaliacoes(
            @TimerTrigger(
                    name = "processarTimer",
                    schedule = "0 0 22 * * *") // Todos os dias às 22h
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

            // Enviar relatório por email
            enviarRelatorioPorEmail(relatorio, context);

            context.getLogger().info("Relatório processado e enviado com sucesso");

        } catch (Exception e) {
            context.getLogger().severe("Erro ao processar relatório: " + e.getMessage());
        }
    }

    /**
     * Gera relatório analítico profissional com classificação por faixas de urgência.
     */
    private static String gerarRelatorio(List<AvaliacaoResponse> avaliacoes) {
        StringBuilder sb = new StringBuilder();

        // Cabeçalho
        sb.append("RELATÓRIO DE AVALIAÇÕES DE DESEMPENHO\n");
        sb.append("========================================\n\n");

        // Data de geração no formato DD/MM/YYYY
        String dataGeracao = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        sb.append("Data de Geração: ").append(dataGeracao).append("\n\n");

        // Resumo
        sb.append("RESUMO\n");
        sb.append("----------------\n");
        sb.append(String.format("Total de Avaliações Analisadas: %d\n", avaliacoes.size()));
        sb.append(String.format("Média Geral de Desempenho: %.2f\n", calcularMediaNotas(avaliacoes)));
        sb.append("\n");

        // Análise por classificação de notas (faixas de urgência)
        sb.append("ANÁLISE POR CLASSIFICAÇÃO DE DESEMPENHO\n");
        sb.append("---------------------------------------\n");

        if (avaliacoes.isEmpty()) {
            sb.append("Nenhuma avaliação registrada no período de análise.\n");
        } else {
            // Classificar em faixas
            long baixa = contarAvaliacoesPorFaixa(avaliacoes, 7.0, 10.0);
            long media = contarAvaliacoesPorFaixa(avaliacoes, 5.0, 6.9);
            long alta = contarAvaliacoesPorFaixa(avaliacoes, 2.0, 4.9);
            long critica = contarAvaliacoesPorFaixa(avaliacoes, 0.0, 1.9);

            double percentualBaixa = (baixa * 100.0) / avaliacoes.size();
            double percentualMedia = (media * 100.0) / avaliacoes.size();
            double percentualAlta = (alta * 100.0) / avaliacoes.size();
            double percentualCritica = (critica * 100.0) / avaliacoes.size();

            sb.append(String.format("Urgência Baixa (7,0 - 10,0): %d avaliações (%.2f%%)\n", baixa, percentualBaixa));
            sb.append(String.format("Urgência Média (5,0 - 6,9): %d avaliações (%.2f%%)\n", media, percentualMedia));
            sb.append(String.format("Urgência Alta (2,0 - 4,9): %d avaliações (%.2f%%)\n", alta, percentualAlta));
            sb.append(String.format("Urgência Crítica (0,0 - 1,9): %d avaliações (%.2f%%)\n", critica, percentualCritica));
        }
        sb.append("\n");

        // Estatísticas detalhadas
        if (!avaliacoes.isEmpty()) {
            sb.append("INDICADORES\n");
            sb.append("-------------------------\n");
            double mediaGeral = calcularMediaNotas(avaliacoes);
            double maxNota = avaliacoes.stream().mapToDouble(AvaliacaoResponse::getNota).max().orElse(0.0);
            double minNota = avaliacoes.stream().mapToDouble(AvaliacaoResponse::getNota).min().orElse(0.0);

            sb.append(String.format("Média de Avaliações: %.2f\n", mediaGeral));
            sb.append(String.format("Avaliação com nota Máxima: %.2f\n", maxNota));
            sb.append(String.format("Avaliação com nota Mínima: %.2f\n", minNota));
            sb.append("\n");

            // Análise por período
            sb.append("DISTRIBUIÇÃO POR DIA\n");
            sb.append("---------------------\n");
            Map<String, Long> avaliacoesPorDia = agruparPorDia(avaliacoes);
            if (avaliacoesPorDia.isEmpty()) {
                sb.append("Nenhuma avaliação registrada por data.\n");
            } else {
                avaliacoesPorDia.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByKey().reversed())
                        .forEach(entry -> {
                            sb.append(String.format("%s: %d avaliação(ões)\n", entry.getKey(), entry.getValue()));
                        });
            }
        }

        // Rodapé
        sb.append("\n----------------------------------------\n");
        sb.append("Documento gerado automaticamente pelo ClassInsight\n");
        sb.append("----------------------------------------\n");

        return sb.toString();
    }

    /**
     * Conta avaliações dentro de uma faixa de notas específica.
     */
    private static long contarAvaliacoesPorFaixa(List<AvaliacaoResponse> avaliacoes, double notaMinima, double notaMaxima) {
        return avaliacoes.stream()
                .filter(av -> av.getNota() >= notaMinima && av.getNota() <= notaMaxima)
                .count();
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
     * Envia o relatório por email usando Azure Communication Services com retry.
     */
    private void enviarRelatorioPorEmail(String relatorio, ExecutionContext context) {
        try {
            // Obter configurações do email
            String connectionString = System.getenv("AZURE_COMMUNICATION_CONNECTION_STRING");
            String fromEmail = System.getenv("NOTIFICATION_FROM_EMAIL");
            String adminEmail = System.getenv("ADMIN_EMAIL");

            if (connectionString == null || fromEmail == null || adminEmail == null) {
                context.getLogger().warning("Configurações de email não encontradas. Relatório será apenas logado.");
                context.getLogger().info("\n" + relatorio);
                return;
            }

            // Criar o serviço de email
            EmailSender emailSender = new AzureCommunicationEmailSender(connectionString);

            // Preparar assunto e corpo do email
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String subject = String.format("Relatório de Avaliações - %s", timestamp);
            String emailBody = "Relatório gerado em " + timestamp + "\n\n" + relatorio;

            // Enviar email diretamente
            boolean enviado = emailSender.send(fromEmail, adminEmail, subject, emailBody);

            if (enviado) {
                context.getLogger().info("Relatório enviado por email para: " + adminEmail);
            } else {
                context.getLogger().warning("Falha ao enviar relatório por email após tentativas.");
            }

            // Aqui você poderia salvar na tabela de relatórios
            // INSERT INTO relatorios (total_avaliacoes, media_notas, data_geracao)
            logger.debug("Relatório gerado com sucesso");
        } catch (Exception e) {
            logger.warn("Erro ao salvar relatório no BD: {}", e.getMessage());
        }
    }
}
