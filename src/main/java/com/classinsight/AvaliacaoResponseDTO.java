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
    private String Descricao;
    private String Urgencia;
    private String DataEnvio;
    
    public AvaliacaoResponseDTO(String descricao, double nota) {
        this.Descricao = descricao + " - Nota: " + nota;
        this.Urgencia = "URGENCIA";
        this.DataEnvio = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
}
