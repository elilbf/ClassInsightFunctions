package com.classinsight.model;

/**
 * Enum que representa os níveis de urgência de uma avaliação.
 */
public enum Urgencia {
    
    BAIXA("Baixa", 1),
    MEDIA("Média", 2),
    ALTA("Alta", 3),
    CRITICO("Crítico", 4);
    
    private final String descricao;
    private final int nivel;
    
    Urgencia(String descricao, int nivel) {
        this.descricao = descricao;
        this.nivel = nivel;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    public int getNivel() {
        return nivel;
    }
    
    /**
     * Determina a urgência com base na nota.
     * @param nota nota da avaliação (0-10)
     * @return urgência correspondente
     */
    public static Urgencia fromNota(Double nota) {
        if (nota == null) {
            return BAIXA;
        }
        
        if (nota <= 3) {
            return CRITICO;
        } else if (nota <= 5) {
            return ALTA;
        } else if (nota <= 7) {
            return MEDIA;
        } else {
            return BAIXA;
        }
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}
