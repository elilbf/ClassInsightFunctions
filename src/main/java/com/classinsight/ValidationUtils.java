package com.classinsight;

public class ValidationUtils {
    
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    public static ValidationResult validateRequestBody(String requestBody) {
        if (requestBody == null || requestBody.trim().isEmpty()) {
            return ValidationResult.error("Um corpo de requisição deve ser enviado");
        }
        return ValidationResult.success();
    }
    
    public static ValidationResult validateAvaliacaoRequest(AvaliacaoRequest request) {
        if (request == null) {
            return ValidationResult.error("Requisição inválida");
        }

        // Validar descrição
        if (request.getDescricao() == null || request.getDescricao().trim().isEmpty()) {
            return ValidationResult.error("É obrigatório informar uma descrição");
        }

        // Validar nota não nula
        if (request.getNota() == null) {
            return ValidationResult.error("É obrigatório informar a nota");
        }

        double nota = request.getNota();
        // Validar valores especiais do double
        if (Double.isNaN(nota)) {
            return ValidationResult.error("Nota não pode ser NaN (Not a Number)");
        }

        if (Double.isInfinite(nota)) {
            return ValidationResult.error("Nota não pode ser infinita");
        }

        // Validar nota range
        if (nota < 0 || nota > 10) {
            return ValidationResult.error("Nota precisa ser um numero entre 0 e 10");
        }

        return ValidationResult.success();
    }
}
