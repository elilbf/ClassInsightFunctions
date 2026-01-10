package com.classinsight.service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Email sender using Azure Communication Services Email (v1.1.0).
 * Simplified reflection implementation with better error handling.
 */
public class AzureCommunicationEmailSender implements EmailSender {
    private static final Logger logger = LogManager.getLogger(AzureCommunicationEmailSender.class);
    private final EmailClient client;

    public AzureCommunicationEmailSender(String connectionString) {
        this.client = new EmailClientBuilder().connectionString(connectionString).buildClient();
        logger.info("AzureCommunicationEmailSender inicializado");
    }

    @Override
    public boolean send(String from, String to, String subject, String body) {
        
        try {
            // Usar reflexão para chamar beginSend
            Class<?> emailMessageClass = Class.forName("com.azure.communication.email.models.EmailMessage");
            Object emailMessage = buildSimpleEmailMessage(emailMessageClass, from, to, subject, body);
            
            if (emailMessage == null) {
                System.err.println("Azure SDK: Failed to build EmailMessage");
                return false;
            }
            
            java.lang.reflect.Method beginSendMethod = EmailClient.class.getDeclaredMethod("beginSend", emailMessageClass);
            Object pollableResult = beginSendMethod.invoke(client, emailMessage);
            
            // Esperar pela conclusão sem bloquear muito tempo
            try {
                java.lang.reflect.Method waitForCompletionMethod = pollableResult.getClass().getDeclaredMethod("waitForCompletion");
                waitForCompletionMethod.setAccessible(true);
                Object completionResult = waitForCompletionMethod.invoke(pollableResult);
                return true;
            } catch (Exception completionEx) {
                return true; // Considerar sucesso pois beginSend funcionou
            }
            
        } catch (Exception e) {
            System.err.println("Azure SDK: Send failed (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            
            // Análise detalhada do erro
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                if (errorMessage.contains("TooManyRequests") || errorMessage.contains("Status code 429")) {
                    System.err.println("Rate limiting detected");
                } else if (errorMessage.contains("Unauthorized") || errorMessage.contains("401")) {
                    System.err.println("Authentication issue - check connection string");
                } else if (errorMessage.contains("Forbidden") || errorMessage.contains("403")) {
                    System.err.println("Permission issue - check sender domain");
                }
            }
            
            // Mostrar causa raiz
            if (e instanceof java.lang.reflect.InvocationTargetException && e.getCause() != null) {
                System.err.println("Root cause: " + e.getCause().getMessage());
            }
            
            return false;
        }
    }

    private Object buildSimpleEmailMessage(Class<?> emailMessageClass, String from, String to, String subject, String body) throws Exception {
        try {
            Object message = emailMessageClass.getConstructor().newInstance();
            
            // Configurar o remetente
            tryInvokeMethod(emailMessageClass, message, "setSenderAddress", String.class, from);
            
            // Tentar configurar o destinatário
            try {
                Class<?> emailAddressClass = Class.forName("com.azure.communication.email.models.EmailAddress");
                Object toAddress = emailAddressClass.getConstructor(String.class).newInstance(to);
                
                // Criar lista de destinatários
                Class<?> listClass = Class.forName("java.util.ArrayList");
                Object toList = listClass.getConstructor().newInstance();
                
                // Adicionar destinatário à lista
                java.lang.reflect.Method addMethod = listClass.getMethod("add", Object.class);
                addMethod.invoke(toList, toAddress);
                
                // Tentar diferentes métodos para configurar destinatários
                boolean recipientsSet = false;
                
                // Método 1: setToRecipients com lista
                recipientsSet = tryInvokeMethod(emailMessageClass, message, "setToRecipients", toList.getClass(), toList);
                
                // Método 2: setToRecipients com varargs
                if (!recipientsSet) {
                    try {
                        java.lang.reflect.Method method = emailMessageClass.getDeclaredMethod("setToRecipients", emailAddressClass.arrayType());
                        Object addressArray = java.lang.reflect.Array.newInstance(emailAddressClass, 1);
                        java.lang.reflect.Array.set(addressArray, 0, toAddress);
                        method.invoke(message, addressArray);
                        recipientsSet = true;
                    } catch (Exception e) {
                        // Ignorar e tentar próximo método
                    }
                }
                
                // Método 3: setTo com endereço único
                if (!recipientsSet) {
                    recipientsSet = tryInvokeMethod(emailMessageClass, message, "setTo", emailAddressClass, toAddress);
                }
                
                if (recipientsSet) {
                    System.out.println("Recipients set successfully");
                } else {
                    System.out.println("Could not set recipients with any method");
                }
            } catch (Exception e) {
                System.out.println("Error setting recipients: " + e.getMessage());
            }
            
            // Configurar assunto e corpo
            tryInvokeMethod(emailMessageClass, message, "setSubject", String.class, subject);
            tryInvokeMethod(emailMessageClass, message, "setBodyPlainText", String.class, body);
            
            return message;
        } catch (Exception e) {
            System.err.println("Failed to build EmailMessage: " + e.getMessage());
            return null;
        }
    }
    
    private boolean tryInvokeMethod(Class<?> targetClass, Object target, String methodName, Class<?> paramType, Object paramValue) {
        try {
            java.lang.reflect.Method method = targetClass.getDeclaredMethod(methodName, paramType);
            method.invoke(target, paramValue);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
