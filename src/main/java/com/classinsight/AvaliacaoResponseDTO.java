package com.classinsight;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoResponseDTO {
    private String descricao;
    private Urgencia urgencia;
    private String dataEnvio;
    
    public AvaliacaoResponseDTO(String descricao, Double nota) {
        this.descricao = descricao + " - Nota: " + nota;
        this.urgencia = Urgencia.fromNota(nota);
        this.dataEnvio = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}
