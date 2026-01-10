package com.classinsight.functions;

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
import com.classinsight.model.AvaliacaoRequest;
import com.classinsight.dto.AvaliacaoResponseDTO;
import com.classinsight.util.ValidationUtils;

import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class CriarAvaliacaoFunction {
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

            // Validar corpo da requisição
            ValidationUtils.ValidationResult bodyCheck = ValidationUtils.validateRequestBody(requestBody);
            if (!bodyCheck.isValid()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(bodyCheck.getMessage())
                    .build();
            }

            // Parse JSON para objeto
            AvaliacaoRequest avaliacaoRequest = objectMapper.readValue(requestBody, AvaliacaoRequest.class);
            
            // Validar objeto AvaliacaoRequest
            ValidationUtils.ValidationResult validationResult = ValidationUtils.validateAvaliacaoRequest(avaliacaoRequest);
            if (!validationResult.isValid()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(validationResult.getMessage())
                    .build();
            }

            // Insere no banco de dados via serviço
            AvaliacaoResponseDTO responseDTO = AvaliacaoService.process(avaliacaoRequest);
            
            if (responseDTO != null && responseDTO.getDescricao() != null) {
                context.getLogger().info("Avaliação processada com sucesso");
                String jsonResponse = objectMapper.writeValueAsString(responseDTO);
                return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(jsonResponse)
                    .build();
            } else {
                context.getLogger().severe("Erro ao persistir avaliação no banco de dados");
                return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao persistir avaliação no banco de dados")
                    .build();
            }

        } catch (JsonParseException | JsonMappingException e) {
            context.getLogger().severe("Erro ao fazer parse/mapping do JSON: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("JSON malformado ou com formato inválido. Verifique campos 'descricao' e 'nota'.")
                .build();
        } catch (Exception e) {
            context.getLogger().severe("Erro ao processar requisição: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao processar requisição: " + e.getMessage())
                .build();
        }
    }
}
