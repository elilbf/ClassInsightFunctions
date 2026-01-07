package com.classinsight.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoStats {
    private int totalAvaliacoes;
    private double mediaNotas;
    private double notaMinima;
    private double notaMaxima;
}
