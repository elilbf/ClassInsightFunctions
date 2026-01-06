package com.classinsight.controller;

import com.classinsight.dto.request.AvaliacaoRequest;
import com.classinsight.dto.response.AvaliacaoResponseDTO;
import com.classinsight.service.AvaliacaoService;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.util.Optional;

/**
 * Controller moderno para Azure Function de avaliação.
 * Implementa boas práticas e estrutura limpa.
 */
public class AvaliacaoController {
    
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
            if (requestBody == null || requestBody.trim().isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Corpo da requisição é obrigatório")
                    .build();
            }

            // Parse JSON para objeto
            AvaliacaoRequest avaliacaoRequest = objectMapper.readValue(requestBody, AvaliacaoRequest.class);
            
            // Validar objeto AvaliacaoRequest
            if (avalicaoRequest.getDescricao() == null || avaliacaoRequest.getDescricao().trim().isEmpty()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Descrição é obrigatória")
                    .build();
            }
            
            if (avalicaoRequest.getNota() == null || avalicaoRequest.getNota() < 0 || avalicaoRequest.getNota() > 10) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Nota deve estar entre 0 e 10")
                    .build();
            }

            // Processar via serviço
            AvaliacaoResponseDTO responseDTO = AvaliacaoService.process(avalicaoRequest);
            
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
