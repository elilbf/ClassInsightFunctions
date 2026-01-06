package com.classinsight.exception;

/**
 * Exceção lançada quando ocorre erro de acesso ao banco de dados.
 */
public class DatabaseException extends ClassInsightException {
    
    public DatabaseException(String message) {
        super(message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
