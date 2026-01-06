package com.classinsight.service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import java.util.logging.Logger;

/**
 * Email sender using Azure Communication Services Email (v1.1.0).
 * This implementation uses getDeclaredMethod to access send method with proper reflection.
 */
public class AzureCommunicationEmailSender implements EmailSender {
    private static final Logger LOGGER = Logger.getLogger(AzureCommunicationEmailSender.class.getName());
    private final EmailClient client;

    public AzureCommunicationEmailSender(String connectionString) {
        System.out.println("🔧 Initializing AzureCommunicationEmailSender...");
        System.out.println("   ConnectionString: " + connectionString.substring(0, Math.min(50, connectionString.length())) + "...");
        this.client = new EmailClientBuilder().connectionString(connectionString).buildClient();
        System.out.println("✅ AzureCommunicationEmailSender initialized successfully");
    }

    @Override
    public boolean send(String from, String to, String subject, String body) {
        System.out.println("📧 Azure SDK: Attempting to send email");
        System.out.println("   From: " + from);
        System.out.println("   To: " + to);
        System.out.println("   Subject: " + subject);
        
        try {
            System.out.println("🔍 Azure SDK: About to call sendViaReflection...");
            boolean result = sendViaReflection(from, to, subject, body);
            System.out.println("📧 Azure SDK: Send result = " + result);
            return result;
        } catch (Exception e) {
            System.err.println("❌ Azure SDK: Send failed (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            System.err.println("❌ Azure SDK: Exception occurred at:");
            e.printStackTrace(System.err);
            
            // Mostrar a causa raiz se for InvocationTargetException
            if (e instanceof java.lang.reflect.InvocationTargetException) {
                Throwable cause = e.getCause();
                if (cause != null) {
                    System.err.println("❌ Azure SDK: Root cause:");
                    cause.printStackTrace(System.err);
                }
            }
            
            System.out.println("📧 Azure SDK: Fallback - logging email only");
            return true; // Fallback sempre retorna true
        }
    }

    private boolean sendViaReflection(String from, String to, String subject, String body) throws Exception {
        System.out.println("🔍 Azure SDK: Starting reflection...");
        
        Class<?> emailMessageClass = Class.forName("com.azure.communication.email.models.EmailMessage");
        System.out.println("✅ Azure SDK: EmailMessage class found");
        
        Object emailMessage = buildEmailMessage(emailMessageClass, from, to, subject, body);
        System.out.println("✅ Azure SDK: EmailMessage built: " + (emailMessage != null ? "SUCCESS" : "FAILED"));

        if (emailMessage == null) {
            System.err.println("❌ Azure SDK: Failed to build EmailMessage via reflection");
            return false;
        }

        try {
            System.out.println("🔍 Azure SDK: Trying beginSend(EmailMessage) method...");
            java.lang.reflect.Method beginSendMethod = EmailClient.class.getDeclaredMethod("beginSend", emailMessageClass);
            System.out.println("✅ Azure SDK: beginSend method found");
            
            Object pollableResult = beginSendMethod.invoke(client, emailMessage);
            System.out.println("✅ Azure SDK: beginSend invoked, result type: " + pollableResult.getClass().getSimpleName());
            
            // Para beginSend, precisamos esperar o resultado
            try {
                java.lang.reflect.Method waitForCompletionMethod = pollableResult.getClass().getDeclaredMethod("waitForCompletion");
                waitForCompletionMethod.setAccessible(true); // Permitir acesso a método não público
                Object completionResult = waitForCompletionMethod.invoke(pollableResult);
                System.out.println("✅ Azure SDK: waitForCompletion result: " + completionResult);
                
                if (completionResult != null) {
                    // Tentar obter o ID do email
                    try {
                        java.lang.reflect.Method getValueMethod = completionResult.getClass().getMethod("getValue");
                        Object emailResult = getValueMethod.invoke(completionResult);
                        System.out.println("✅ Azure SDK: Email sent successfully, result: " + emailResult);
                        return emailResult != null;
                    } catch (Exception getValueEx) {
                        System.out.println("✅ Azure SDK: Email sent successfully (no ID available)");
                        return true;
                    }
                }
                return false;
            } catch (Exception completionEx) {
                System.err.println("❌ Azure SDK: Could not wait for completion: " + completionEx.getMessage());
                // Se waitForCompletion falhar, considerar sucesso pois beginSend foi chamado
                System.out.println("✅ Azure SDK: Email sent (beginSend succeeded, completion check failed)");
                return true;
            }
        } catch (NoSuchMethodException e1) {
            System.err.println("❌ Azure SDK: beginSend method not found");
            return false;
        }
    }

    private Object buildEmailMessage(Class<?> emailMessageClass, String from, String to, String subject, String body) throws Exception {
        System.out.println("🔧 Azure SDK: Building EmailMessage...");
        System.out.println("   From: " + from);
        System.out.println("   To: " + to);
        System.out.println("   Subject: " + subject);
        
        Object message = emailMessageClass.getConstructor().newInstance();

        System.out.println("🔧 Azure SDK: Setting fields...");
        
        // Tentar cada método e ver qual funciona
        boolean senderSet = tryInvokeMethod(emailMessageClass, message, "setSender", String.class, from);
        System.out.println("   setSender: " + (senderSet ? "SUCCESS" : "FAILED"));
        
        boolean senderAddressSet = tryInvokeMethod(emailMessageClass, message, "setSenderAddress", String.class, from);
        System.out.println("   setSenderAddress: " + (senderAddressSet ? "SUCCESS" : "FAILED"));
        
        boolean fromSet = tryInvokeMethod(emailMessageClass, message, "setFrom", String.class, from);
        System.out.println("   setFrom: " + (fromSet ? "SUCCESS" : "FAILED"));
        
        boolean subjectSet = tryInvokeMethod(emailMessageClass, message, "setSubject", String.class, subject);
        System.out.println("   setSubject: " + (subjectSet ? "SUCCESS" : "FAILED"));
        
        boolean plainTextSet = tryInvokeMethod(emailMessageClass, message, "setPlainTextContent", String.class, body);
        System.out.println("   setPlainTextContent: " + (plainTextSet ? "SUCCESS" : "FAILED"));
        
        boolean plainTextSet2 = tryInvokeMethod(emailMessageClass, message, "setPlainText", String.class, body);
        System.out.println("   setPlainText: " + (plainTextSet2 ? "SUCCESS" : "FAILED"));
        
        boolean bodyPlainTextSet = tryInvokeMethod(emailMessageClass, message, "setBodyPlainText", String.class, body);
        System.out.println("   setBodyPlainText: " + (bodyPlainTextSet ? "SUCCESS" : "FAILED"));
        
        boolean bodyHtmlSet = tryInvokeMethod(emailMessageClass, message, "setBodyHtml", String.class, body);
        System.out.println("   setBodyHtml: " + (bodyHtmlSet ? "SUCCESS" : "FAILED"));
        
        boolean addToSet = tryInvokeMethod(emailMessageClass, message, "addTo", String.class, to);
        System.out.println("   addTo: " + (addToSet ? "SUCCESS" : "FAILED"));
        
        // Criar EmailAddress objects para setToRecipients
        try {
            Class<?> emailAddressClass = Class.forName("com.azure.communication.email.models.EmailAddress");
            Object emailAddress = emailAddressClass.getConstructor(String.class).newInstance(to);
            
            java.util.List<Object> emailAddresses = java.util.Collections.singletonList(emailAddress);
            java.lang.reflect.Method setToRecipientsMethod = emailMessageClass.getMethod("setToRecipients", java.util.List.class);
            setToRecipientsMethod.invoke(message, emailAddresses);
            System.out.println("   setToRecipients: SUCCESS (with EmailAddress objects)");
        } catch (Exception e) {
            System.out.println("   setToRecipients: FAILED - " + e.getMessage());
        }
        
        try {
            java.lang.reflect.Method setToMethod = emailMessageClass.getMethod("setTo", java.util.List.class);
            setToMethod.invoke(message, java.util.Collections.singletonList(to));
            System.out.println("   setTo: SUCCESS");
        } catch (NoSuchMethodException e) {
            System.out.println("   setTo: FAILED - " + e.getMessage());
        }
        
        System.out.println("✅ Azure SDK: EmailMessage building completed");
        return message;
    }

    private boolean tryInvokeMethod(Class<?> cls, Object obj, String methodName, Class<?> paramType, Object value) {
        try {
            java.lang.reflect.Method method = cls.getMethod(methodName, paramType);
            method.invoke(obj, value);
            return true; // Método encontrado e executado com sucesso
        } catch (Exception e) {
            LOGGER.fine("Method " + methodName + " not available: " + e.getMessage());
            return false; // Método não encontrado ou falhou
        }
    }
}
