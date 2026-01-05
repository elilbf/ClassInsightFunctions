package com.classinsight.service;

import com.classinsight.AvaliacaoRequest;
import com.classinsight.AvaliacaoResponseDTO;
import com.classinsight.Urgencia;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AvaliacaoService {

    public static AvaliacaoResponseDTO process(AvaliacaoRequest request) {
        String descricao = request.getDescricao() + " - Nota: " + request.getNota();
        Urgencia urgencia = Urgencia.fromNota(request.getNota());
        String dataEnvio = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        return new AvaliacaoResponseDTO(descricao, urgencia, dataEnvio);
    }
}
