package com.classinsight.exception;

/**
 * Exceção base para o sistema ClassInsight.
 * Todas as exceções específicas devem estender esta classe.
 */
public class ClassInsightException extends RuntimeException {
    
    public ClassInsightException(String message) {
        super(message);
    }
    
    public ClassInsightException(String message, Throwable cause) {
        super(message, cause);
    }
}
