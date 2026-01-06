package com.classinsight.exception;

/**
 * Exceção lançada quando ocorre erro no envio de email.
 */
public class EmailException extends ClassInsightException {
    
    public EmailException(String message) {
        super(message);
    }
    
    public EmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
