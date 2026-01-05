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
import com.classinsight.service.AvaliacaoService;

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

            ValidationUtils.ValidationResult bodyCheck = ValidationUtils.validateRequestBody(requestBody);
            if (!bodyCheck.isValid()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(bodyCheck.getMessage()).build();
            }

            if (responseBuilder == null) {
                AvaliacaoRequest avaliacaoRequest = objectMapper.readValue(requestBody, AvaliacaoRequest.class);
                
                result = ValidationUtils.validateAvaliacaoRequest(avaliacaoRequest);
                if (!result.isValid()) {
                    responseBuilder = request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(result.getMessage());
                } else {
                    // Insere no banco de dados
                    long avaliacaoId = AvaliacaoDAO.inserirAvaliacao(avaliacaoRequest);
                    
                    if (avaliacaoId > 0) {
                        context.getLogger().info("Avaliação inserida com ID: " + avaliacaoId);
                        
                        // Cria response com ID gerado
                        AvaliacaoResponse responseData = new AvaliacaoResponse(
                            avaliacaoId,
                            avaliacaoRequest.getDescricao(),
                            avaliacaoRequest.getNota(),
                            java.time.LocalDateTime.now().toString()
                        );
                        
                        String jsonResponse = objectMapper.writeValueAsString(responseData);
                        responseBuilder = request.createResponseBuilder(HttpStatus.CREATED)
                            .header("Content-Type", "application/json")
                            .body(jsonResponse);
                    } else {
                        context.getLogger().severe("Erro ao inserir avaliação no banco de dados");
                        responseBuilder = request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Erro ao persistir avaliação no banco de dados");
                    }
                }
            }

            // Processamento em serviço separado
            AvaliacaoResponseDTO responseDTO = AvaliacaoService.process(avaliacaoRequest);
            String jsonResponse = objectMapper.writeValueAsString(responseDTO);
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(jsonResponse)
                    .build();

        } catch (JsonParseException | JsonMappingException e) {
            context.getLogger().severe("JSON parsing/mapping error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("JSON malformado ou com formato inválido. Verifique campos 'descricao' e 'nota'.")
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error processing request: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request: " + e.getMessage())
                    .build();
        }
    }
}
