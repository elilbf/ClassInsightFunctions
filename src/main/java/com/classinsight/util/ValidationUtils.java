package com.classinsight.util;

import com.classinsight.dto.request.AvaliacaoRequest;

/**
 * Utilitário para validação de dados de requisição.
 * LEGADO: Manter para compatibilidade, substituir por ValidationUtils refatorado.
 */
@Deprecated
public class ValidationUtils {
    
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * Valida o corpo da requisição HTTP.
     * @param requestBody corpo JSON da requisição
     * @return resultado da validação
     */
    public static ValidationResult validateRequestBody(String requestBody) {
        if (requestBody == null || requestBody.trim().isEmpty()) {
            return new ValidationResult(false, "Corpo da requisição é obrigatório");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Valida o objeto AvaliacaoRequest.
     * @param request objeto a ser validado
     * @return resultado da validação
     */
    public static ValidationResult validateAvaliacaoRequest(AvaliacaoRequest request) {
        if (request == null) {
            return new ValidationResult(false, "Request não pode ser nulo");
        }
        
        if (request.getDescricao() == null || request.getDescricao().trim().isEmpty()) {
            return new ValidationResult(false, "Descrição é obrigatória");
        }
        
        if (request.getNota() == null || request.getNota() < 0 || request.getNota() > 10) {
            return new ValidationResult(false, "Nota deve estar entre 0 e 10");
        }
        
        return new ValidationResult(true, null);
    }
}
