package com.classinsight;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @FunctionName("avaliacao")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Processando requisição...");

        try {
            String requestBody = request.getBody().orElse(null);
            HttpResponseMessage.Builder responseBuilder = null;
            
            // Validações usando ValidationUtils
            ValidationUtils.ValidationResult result = ValidationUtils.validateRequestBody(requestBody);
            if (!result.isValid()) {
                responseBuilder = request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(result.getMessage());
            }
            
            if (responseBuilder == null) {
                result = ValidationUtils.validateRequiredFields(requestBody);
                if (!result.isValid()) {
                    responseBuilder = request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(result.getMessage());
                }
            }
            
            if (responseBuilder == null) {
                result = ValidationUtils.validateNotaField(requestBody);
                if (!result.isValid()) {
                    responseBuilder = request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(result.getMessage());
                }
            }

            if (responseBuilder == null) {
                AvaliacaoRequest avaliacaoRequest = objectMapper.readValue(requestBody, AvaliacaoRequest.class);
                
                result = ValidationUtils.validateAvaliacaoRequest(avaliacaoRequest);
                if (!result.isValid()) {
                    responseBuilder = request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(result.getMessage());
                } else {
                    // Cria response DTO com os dados validados
                    AvaliacaoResponseDTO responseDTO = new AvaliacaoResponseDTO(
                        avaliacaoRequest.getDescricao(),
                        avaliacaoRequest.getNota()
                    );
                    
                    String jsonResponse = objectMapper.writeValueAsString(responseDTO);
                    responseBuilder = request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(jsonResponse);
                }
            }
            
            // Único return do método
            return responseBuilder.build();
                
        } catch (JsonParseException e) {
            context.getLogger().severe("JSON parsing error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("JSON malformado. Verifique se todas as chaves e aspas estão corretas.").build();
        } catch (JsonMappingException e) {
            context.getLogger().severe("JSON mapping error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("Formato JSON inválido. Verifique se os campos 'descricao' (string) e 'nota' (número) estão corretos.").build();
        } catch (Exception e) {
            context.getLogger().severe("Error processing request: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing request: " + e.getMessage()).build();
        }
    }
}
