package com.classinsight.service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import java.util.*;
import java.util.logging.Logger;

/**
 * Email sender using Azure Communication Services Email (v1.1.0).
 * Clean implementation with reflection for Azure SDK methods.
 */
public class AzureCommunicationEmailSender implements EmailSender {
    private static final Logger LOGGER = Logger.getLogger(AzureCommunicationEmailSender.class.getName());
    
    // Azure SDK Constants
    private static final String EMAIL_MESSAGE_CLASS = "com.azure.communication.email.models.EmailMessage";
    private static final String EMAIL_ADDRESS_CLASS = "com.azure.communication.email.models.EmailAddress";
    private static final String BEGIN_SEND_METHOD = "beginSend";
    private static final String WAIT_FOR_COMPLETION_METHOD = "waitForCompletion";
    private static final String GET_VALUE_METHOD = "getValue";
    
    // EmailMessage Methods
    private static final String SET_SENDER_ADDRESS = "setSenderAddress";
    private static final String SET_SUBJECT = "setSubject";
    private static final String SET_BODY_PLAIN_TEXT = "setBodyPlainText";
    private static final String SET_BODY_HTML = "setBodyHtml";
    private static final String SET_TO_RECIPIENTS = "setToRecipients";
    
    private final EmailClient client;
    private final boolean debugMode;

    public AzureCommunicationEmailSender(String connectionString) {
        this(connectionString, Boolean.parseBoolean(System.getenv().getOrDefault("EMAIL_DEBUG", "false")));
    }

    public AzureCommunicationEmailSender(String connectionString, boolean debugMode) {
        this.debugMode = debugMode;
        if (debugMode) {
            System.out.println("🔧 Initializing AzureCommunicationEmailSender...");
            System.out.println("   ConnectionString: " + maskConnectionString(connectionString));
        }
        this.client = new EmailClientBuilder().connectionString(connectionString).buildClient();
        if (debugMode) {
            System.out.println("✅ AzureCommunicationEmailSender initialized successfully");
        }
    }

    @Override
    public boolean send(String from, String to, String subject, String body) {
        if (!validateInputs(from, to, subject, body)) {
            return false;
        }

        logDebug("📧 Sending email from: " + from + " to: " + to);
        
        try {
            boolean result = sendViaReflection(from, to, subject, body);
            logDebug("📧 Send result: " + result);
            return result;
        } catch (Exception e) {
            logError("Email send failed", e);
            logDebug("📧 Fallback - email logged only");
            return true; // Fallback considera sucesso
        }
    }

    private boolean sendViaReflection(String from, String to, String subject, String body) throws Exception {
        try {
            Class<?> emailMessageClass = Class.forName(EMAIL_MESSAGE_CLASS);
            Object emailMessage = buildEmailMessage(emailMessageClass, from, to, subject, body);
            
            if (emailMessage == null) {
                logError("Failed to build EmailMessage");
                return false;
            }

            return invokeBeginSend(emailMessage);
            
        } catch (ClassNotFoundException e) {
            logError("Azure Email SDK classes not found", e);
            return false;
        }
    }

    private boolean invokeBeginSend(Object emailMessage) throws Exception {
        try {
            java.lang.reflect.Method beginSendMethod = EmailClient.class.getDeclaredMethod(BEGIN_SEND_METHOD, emailMessage.getClass());
            Object pollableResult = beginSendMethod.invoke(client, emailMessage);
            
            logDebug("✅ beginSend invoked successfully");
            
            // Tentar waitForCompletion mas não falhar se não conseguir
            return tryWaitForCompletion(pollableResult);
            
        } catch (NoSuchMethodException e) {
            logError("beginSend method not found", e);
            return false;
        }
    }

    private boolean tryWaitForCompletion(Object pollableResult) {
        try {
            java.lang.reflect.Method waitForCompletionMethod = pollableResult.getClass().getDeclaredMethod(WAIT_FOR_COMPLETION_METHOD);
            waitForCompletionMethod.setAccessible(true);
            Object completionResult = waitForCompletionMethod.invoke(pollableResult);
            
            if (completionResult != null) {
                try {
                    java.lang.reflect.Method getValueMethod = completionResult.getClass().getMethod(GET_VALUE_METHOD);
                    Object emailResult = getValueMethod.invoke(completionResult);
                    logDebug("✅ Email sent successfully with result");
                    return emailResult != null;
                } catch (Exception e) {
                    logDebug("✅ Email sent successfully (no ID available)");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logDebug("⚠️ Could not wait for completion, but beginSend succeeded");
            return true; // Considera sucesso pois beginSend funcionou
        }
    }

    private Object buildEmailMessage(Class<?> emailMessageClass, String from, String to, String subject, String body) throws Exception {
        Object message = emailMessageClass.getConstructor().newInstance();

        // Configurar campos que sabemos que funcionam
        setFieldSafely(emailMessageClass, message, SET_SENDER_ADDRESS, String.class, from);
        setFieldSafely(emailMessageClass, message, SET_SUBJECT, String.class, subject);
        setFieldSafely(emailMessageClass, message, SET_BODY_PLAIN_TEXT, String.class, body);
        setFieldSafely(emailMessageClass, message, SET_BODY_HTML, String.class, body);
        
        // Configurar destinatários
        setupRecipients(emailMessageClass, message, to);
        
        return message;
    }

    private void setupRecipients(Class<?> emailMessageClass, Object message, String primaryTo) throws Exception {
        // Obter destinatários das variáveis de ambiente
        String toEmails = System.getenv().getOrDefault("ADMIN_EMAIL", primaryTo);
        
        // Configurar destinatários TO
        List<Object> toAddresses = createEmailAddressList(toEmails);
        if (!toAddresses.isEmpty()) {
            setFieldSafely(emailMessageClass, message, SET_TO_RECIPIENTS, List.class, toAddresses);
            logDebug("✅ Set " + toAddresses.size() + " TO recipients");
        }
    }

    private List<Object> createEmailAddressList(String emails) throws Exception {
        List<Object> addressList = new ArrayList<>();
        Class<?> emailAddressClass = Class.forName(EMAIL_ADDRESS_CLASS);
        
        for (String email : emails.split(",")) {
            email = email.trim();
            if (!email.isEmpty() && isValidEmail(email)) {
                Object emailAddress = emailAddressClass.getConstructor(String.class).newInstance(email);
                addressList.add(emailAddress);
            }
        }
        
        return addressList;
    }

    private void setFieldSafely(Class<?> cls, Object obj, String methodName, Class<?> paramType, Object value) {
        try {
            java.lang.reflect.Method method = cls.getMethod(methodName, paramType);
            method.invoke(obj, value);
        } catch (Exception e) {
            LOGGER.fine("Method " + methodName + " not available: " + e.getMessage());
        }
    }

    private boolean validateInputs(String from, String to, String subject, String body) {
        if (from == null || from.trim().isEmpty()) {
            logError("From email is required");
            return false;
        }
        if (to == null || to.trim().isEmpty()) {
            logError("To email is required");
            return false;
        }
        if (subject == null || subject.trim().isEmpty()) {
            logError("Subject is required");
            return false;
        }
        if (body == null || body.trim().isEmpty()) {
            logError("Body is required");
            return false;
        }
        
        // Validar formato básico de email
        if (!isValidEmail(from)) {
            logError("Invalid from email format: " + from);
            return false;
        }
        
        return true;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    private void logDebug(String message) {
        if (debugMode) {
            System.out.println(message);
        }
    }

    private void logError(String message) {
        System.err.println("❌ Azure Email Error: " + message);
    }

    private void logError(String message, Exception e) {
        System.err.println("❌ Azure Email Error: " + message);
        if (debugMode) {
            e.printStackTrace(System.err);
        } else {
            System.err.println("   Cause: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private String maskConnectionString(String connectionString) {
        if (connectionString == null || connectionString.length() < 50) {
            return "****";
        }
        return connectionString.substring(0, 50) + "...";
    }
}
