package com.classinsight.exception;

/**
 * Exceção lançada quando ocorre erro de validação de dados.
 */
public class ValidationException extends ClassInsightException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
