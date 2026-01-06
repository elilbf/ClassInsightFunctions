package com.classinsight.dto.response;

import com.classinsight.model.Urgencia;

import java.time.LocalDateTime;

/**
 * DTO para respostas de avaliação com urgência calculada.
 */
public class AvaliacaoResponseDTO {
    
    private final String descricao;
    private final Urgencia urgencia;
    private final LocalDateTime dataEnvio;
    
    // Construtor
    public AvaliacaoResponseDTO(String descricao, Urgencia urgencia, LocalDateTime dataEnvio) {
        this.descricao = descricao;
        this.urgencia = urgencia;
        this.dataEnvio = dataEnvio;
    }
    
    // Getters
    public String getDescricao() {
        return descricao;
    }
    
    public Urgencia getUrgencia() {
        return urgencia;
    }
    
    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }
    
    @Override
    public String toString() {
        return "AvaliacaoResponseDTO{descricao='" + descricao + "', urgencia=" + urgencia + "', dataEnvio=" + dataEnvio + "}";
    }
}
