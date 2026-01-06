package com.classinsight.dto.request;

/**
 * DTO para requisições de criação de avaliação.
 */
public class AvaliacaoRequest {
    
    private String descricao;
    private Double nota;
    
    // Construtor padrão
    public AvaliacaoRequest() {}
    
    // Construtor com parâmetros
    public AvaliacaoRequest(String descricao, Double nota) {
        this.descricao = descricao;
        this.nota = nota;
    }
    
    // Getters e Setters
    public String getDescricao() {
        return descricao;
    }
    
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public Double getNota() {
        return nota;
    }
    
    public void setNota(Double nota) {
        this.nota = nota;
    }
    
    @Override
    public String toString() {
        return "AvaliacaoRequest{descricao='" + descricao + "', nota=" + nota + "}";
    }
}
