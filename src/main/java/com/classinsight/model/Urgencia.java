package com.classinsight.model;

public enum Urgencia {
    CRITICO,
    ALTA,
    MEDIA,
    BAIXA;

    public static Urgencia fromNota(Double nota) {
        if (nota == null) {
            return BAIXA;
        }
        if (nota <= 2) {
            return CRITICO;
        } else if (nota <= 5) {
            return ALTA;
        } else if (nota <= 7) {
            return MEDIA;
        } else {
            return BAIXA;
        }
    }
}
