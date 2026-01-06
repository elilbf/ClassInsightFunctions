package com.classinsight.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * REST-based email sender using Azure Communication Services Email API.
 * Extracts endpoint and access key from connection string, builds JSON payload,
 * and calls /emails:send endpoint with proper authentication headers.
 * Reuses existing project patterns: Jackson ObjectMapper, System.getenv, error handling.
 */
public class RestAzureCommunicationEmailSender implements EmailSender {
    
    private final String endpoint;
    private final String accessKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    
    public RestAzureCommunicationEmailSender(String connectionString) {
        Map<String, String> parsed = parseConnectionString(connectionString);
        this.endpoint = parsed.get("endpoint");
        this.accessKey = parsed.get("accesskey");
        this.objectMapper = new ObjectMapper();
        
        if (endpoint == null || accessKey == null) {
            throw new IllegalArgumentException("Invalid connection string: missing endpoint or accesskey");
        }
        
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
            
        System.out.println("✅ RestAzureCommunicationEmailSender initialized");
        System.out.println("   Endpoint: " + endpoint);
        System.out.println("   AccessKey: " + (accessKey != null ? accessKey.substring(0, Math.min(10, accessKey.length())) + "..." : "NULL"));
    }
    
    @Override
    public boolean send(String from, String to, String subject, String body) {
        Map<String, Object> emailPayload = buildEmailPayload(from, to, subject, body);
        String emailEndpoint = endpoint.endsWith("/") ? 
            endpoint.substring(0, endpoint.length() - 1) + "/emails:send?api-version=2023-03-31" :
            endpoint + "/emails:send?api-version=2023-03-31";
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                String jsonPayload = objectMapper.writeValueAsString(emailPayload);
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(emailEndpoint))
                    .header("Content-Type", "application/json")
                    .header("Ocp-Apim-Subscription-Key", accessKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
                
                System.out.println("📧 Sending email attempt " + attempt + "/" + MAX_RETRIES + " to: " + to);
                System.out.println("   Endpoint: " + emailEndpoint);
                System.out.println("   Payload: " + jsonPayload);
                System.out.println("   Header Ocp-Apim-Subscription-Key: " + accessKey.substring(0, Math.min(10, accessKey.length())) + "...");
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 202) {
                    System.out.println("✅ Email sent successfully via REST API. Status: " + response.statusCode());
                    return true;
                } else {
                    System.err.println("❌ Email send failed. Status: " + response.statusCode() + 
                                     ", Body: " + response.body());
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error on attempt " + attempt + ": " + e.getMessage());
            }
            
            if (attempt < MAX_RETRIES) {
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        
        System.err.println("❌ Failed to send email after " + MAX_RETRIES + " attempts");
        return false;
    }
    
    private Map<String, String> parseConnectionString(String connectionString) {
        Map<String, String> result = new HashMap<>();
        
        if (connectionString == null || connectionString.trim().isEmpty()) {
            return result;
        }
        
        String[] parts = connectionString.split(";");
        for (String part : parts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().toLowerCase();
                String value = keyValue[1].trim();
                
                if (key.equals("endpoint")) {
                    result.put("endpoint", value);
                } else if (key.equals("accesskey")) {
                    result.put("accesskey", value);
                }
            }
        }
        
        return result;
    }
    
    private Map<String, Object> buildEmailPayload(String from, String to, String subject, String body) {
        Map<String, Object> payload = new HashMap<>();
        
        payload.put("senderAddress", from);
        
        Map<String, Object> recipients = new HashMap<>();
        Map<String, String> toRecipient = new HashMap<>();
        toRecipient.put("address", to);
        recipients.put("to", new Object[]{toRecipient});
        payload.put("recipients", recipients);
        
        Map<String, Object> content = new HashMap<>();
        content.put("subject", subject);
        content.put("plainText", body);
        payload.put("content", content);
        
        return payload;
    }
}
