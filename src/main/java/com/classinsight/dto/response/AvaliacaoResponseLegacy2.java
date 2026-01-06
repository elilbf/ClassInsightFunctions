package com.classinsight.dto.response;

/**
 * DTO simples para resposta de avaliação.
 * LEGADO: Manter para compatibilidade, substituir por AvaliacaoResponseDTO.
 */
@Deprecated
public class AvaliacaoResponseLegacy {
    
    private String descricao;
    private String status;
    
    public AvaliacaoResponseLegacy() {}
    
    public AvaliacaoResponseLegacy(String descricao, String status) {
        this.descricao = descricao;
        this.status = status;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
