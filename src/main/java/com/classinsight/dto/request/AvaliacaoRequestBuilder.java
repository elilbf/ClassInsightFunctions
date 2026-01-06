package com.classinsight.dto.request;

import com.classinsight.util.ValidationUtilsRefactored;

/**
 * Builder para AvaliacaoRequest.
 * Implementa o padrão Builder para construção segura de objetos.
 */
public class AvaliacaoRequestBuilder {
    private String descricao;
    private Double nota;
    
    public AvaliacaoRequestBuilder() {}
    
    public AvaliacaoRequestBuilder descricao(String descricao) {
        if (!ValidationUtilsRefactored.isValidAlphanumeric(descricao)) {
            throw new IllegalArgumentException("Descrição deve conter apenas caracteres alfanuméricos e espaços");
        }
        this.descricao = descricao;
        return this;
    }
    
    public AvaliacaoRequestBuilder nota(Double nota) {
        if (!ValidationUtilsRefactored.isValidNota(nota)) {
            throw new IllegalArgumentException("Nota deve estar entre 0 e 10");
        }
        this.nota = nota;
        return this;
    }
    
    public AvaliacaoRequest build() {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalStateException("Descrição é obrigatória");
        }
        if (nota == null) {
            throw new IllegalStateException("Nota é obrigatória");
        }
        
        return new AvaliacaoRequest(descricao, nota);
    }
}
