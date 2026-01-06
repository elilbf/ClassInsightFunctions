package com.classinsight.util;

import com.classinsight.dto.request.AvaliacaoRequest;
import com.classinsight.exception.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilitário para validação de dados de requisição.
 * Versão refatorada com logging estruturado e validações reutilizáveis.
 */
public class ValidationUtilsRefactored {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtilsRefactored.class);
    
    /**
     * Resultado de validação.
     */
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
        logger.debug("Validando corpo da requisição");
        
        if (requestBody == null || requestBody.trim().isEmpty()) {
            String errorMsg = "Corpo da requisição é obrigatório";
            logger.warn("Validação falhou: {}", errorMsg);
            return new ValidationResult(false, errorMsg);
        }
        
        logger.debug("Corpo da requisição validado com sucesso");
        return new ValidationResult(true, null);
    }
    
    /**
     * Valida o objeto AvaliacaoRequest.
     * @param request objeto a ser validado
     * @return resultado da validação
     * @throws ValidationException se dados inválidos
     */
    public static void validateAvaliacaoRequest(AvaliacaoRequest request) throws ValidationException {
        logger.debug("Validando objeto AvaliacaoRequest");
        
        if (request == null) {
            String errorMsg = "Request não pode ser nulo";
            logger.error("Validação falhou: {}", errorMsg);
            throw new ValidationException(errorMsg);
        }
        
        if (!isValidString(request.getDescricao())) {
            String errorMsg = "Descrição é obrigatória";
            logger.error("Validação falhou: {}", errorMsg);
            throw new ValidationException(errorMsg);
        }
        
        if (!isValidNota(request.getNota())) {
            String errorMsg = "Nota deve estar entre 0 e 10";
            logger.error("Validação falhou: {}", errorMsg);
            throw new ValidationException(errorMsg);
        }
        
        logger.debug("AvaliacaoRequest validado com sucesso");
    }
    
    /**
     * Valida se uma string é válida (não nula e não vazia).
     * @param value string a ser validada
     * @return true se válida
     */
    private static boolean isValidString(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Valida se uma nota é válida (entre 0 e 10).
     * @param nota nota a ser validada
     * @return true se válida
     */
    public static boolean isValidNota(Double nota) {
        return nota != null && nota >= 0 && nota <= 10;
    }
    
    /**
     * Valida formato básico de email.
     * @param email email a ser validado
     * @return true se formato básico válido
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[A-Za-z0-9+._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    /**
     * Valida se uma string contém apenas caracteres alfanuméricos e espaços.
     * @param value string a ser validada
     * @return true se válida
     */
    public static boolean isValidAlphanumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        return value.matches("^[a-zA-Z0-9\\s]*$");
    }
}
