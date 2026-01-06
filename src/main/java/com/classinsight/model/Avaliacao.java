package com.classinsight.model;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma avaliação no sistema.
 */
public class Avaliacao {
    
    private Long id;
    private String descricao;
    private Double nota;
    private Urgencia urgencia;
    private LocalDateTime dataCriacao;
    
    // Construtores
    public Avaliacao() {}
    
    public Avaliacao(String descricao, Double nota) {
        this.descricao = descricao;
        this.nota = nota;
        this.urgencia = Urgencia.fromNota(nota);
        this.dataCriacao = LocalDateTime.now();
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public Urgencia getUrgencia() {
        return urgencia;
    }
    
    public void setUrgencia(Urgencia urgencia) {
        this.urgencia = urgencia;
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    @Override
    public String toString() {
        return "Avaliacao{id=" + id + ", descricao='" + descricao + "', nota=" + nota + ", urgencia=" + urgencia + "}";
    }
}
