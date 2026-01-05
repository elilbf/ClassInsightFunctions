package com.classinsight;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoResponse {
    private long id;
    private String descricao;
    private double nota;
    private String dataCriacao;
}
