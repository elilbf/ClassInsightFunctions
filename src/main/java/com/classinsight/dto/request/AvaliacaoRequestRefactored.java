package com.classinsight.dto.request;

/**
 * DTO para requisições de criação de avaliação.
 * Versão refatorada com validações internas.
 */
public class AvaliacaoRequest {
    
    private String descricao;
    private Double nota;
    
    // Construtor padrão
    public AvaliacaoRequest() {}
    
    // Construtor com validação
    public AvaliacaoRequest(String descricao, Double nota) {
        setDescricao(descricao);
        setNota(nota);
    }
    
    // Getters
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }
        this.descricao = descricao;
    }
    
    public Double getNota() {
        return nota;
    }
    
    public void setNota(Double nota) {
        if (nota == null || nota < 0 || nota > 10) {
            throw new IllegalArgumentException("Nota deve estar entre 0 e 10");
        }
        this.nota = nota;
    }
    
    @Override
    public String toString() {
        return "AvaliacaoRequest{descricao='" + descricao + "', nota=" + nota + "}";
    }
}
