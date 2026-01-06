package com.classinsight.service;

/**
 * Abstraction for sending email so we can mock in tests and support multiple providers.
 */
public interface EmailSender {
    /**
     * Send an email.
     * @param from sender address
     * @param to recipient address
     * @param subject subject
     * @param body plain-text body
     * @return true if send succeeded (best-effort)
     */
    boolean send(String from, String to, String subject, String body);
}
