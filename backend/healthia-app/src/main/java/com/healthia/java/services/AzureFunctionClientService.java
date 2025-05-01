package com.healthia.java.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthia.java.dtos.azure.AzureImageAnalysisRequest;
import com.healthia.java.dtos.azure.AzureImageAnalysisResponse;
import com.healthia.java.dtos.azure.AzureMedicalTextAnalysisRequest;
import com.healthia.java.dtos.azure.AzureMedicalTextAnalysisResponse;
import com.healthia.java.models.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class AzureFunctionClientService {

    private static final Logger log = LoggerFactory.getLogger(AzureFunctionClientService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${azure.function.image-analysis.url}")
    private String imageAnalysisFunctionUrl; // e.g., http://localhost:7071/api/analyze-image or actual Azure URL

    @Value("${azure.function.medical-text-analysis.url}")
    private String medicalTextAnalysisFunctionUrl; // e.g., http://localhost:7071/api/analyze-medical-text

    @Autowired
    public AzureFunctionClientService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public AzureImageAnalysisResponse analyzeImage(String imageBase64, Integer conversationId) {
        if (imageAnalysisFunctionUrl == null || imageAnalysisFunctionUrl.isBlank()) {
            log.error("Azure Image Analysis Function URL is not configured. Please set azure.function.image-analysis.url property.");
            // Return null or throw a custom exception to indicate configuration error
            return null;
        }

        AzureImageAnalysisRequest requestPayload = AzureImageAnalysisRequest.builder()
                .imageBase64(imageBase64)
                .conversationId(conversationId)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String jsonRequest = objectMapper.writeValueAsString(requestPayload);
            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

            log.info("Calling Azure Image Analysis Function at URL: {}. Conversation ID: {}", imageAnalysisFunctionUrl, conversationId);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(imageAnalysisFunctionUrl, entity, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                log.info("Received successful response from Azure Image Analysis Function.");
                return objectMapper.readValue(responseEntity.getBody(), AzureImageAnalysisResponse.class);
            } else {
                log.error("Azure Image Analysis Function returned status: {} with body: {}", responseEntity.getStatusCode(), responseEntity.getBody());
                return null;
            }
        } catch (HttpClientErrorException e) {
            log.error("HTTP error calling Azure Image Analysis Function: {} - Response: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            log.error("Error calling Azure Image Analysis Function or processing its response: {}", e.getMessage(), e);
            return null;
        }
    }

    public AzureMedicalTextAnalysisResponse analyzeMedicalText(String userInput, UserData userData, Integer conversationId) {
        if (medicalTextAnalysisFunctionUrl == null || medicalTextAnalysisFunctionUrl.isBlank()) {
            log.error("Azure Medical Text Analysis Function URL is not configured. Please set azure.function.medical-text-analysis.url property.");
            return null;
        }

        AzureMedicalTextAnalysisRequest requestPayload = AzureMedicalTextAnalysisRequest.builder()
                .userInputText(userInput)
                .userData(userData) // This UserData should be populated with relevant medical history if needed by the function
                .conversationId(conversationId)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String jsonRequest = objectMapper.writeValueAsString(requestPayload);
            HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

            log.info("Calling Azure Medical Text Analysis Function at URL: {}. Conversation ID: {}", medicalTextAnalysisFunctionUrl, conversationId);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(medicalTextAnalysisFunctionUrl, entity, String.class);

            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                log.info("Received successful response from Azure Medical Text Analysis Function.");
                return objectMapper.readValue(responseEntity.getBody(), AzureMedicalTextAnalysisResponse.class);
            } else {
                log.error("Azure Medical Text Analysis Function returned status: {} with body: {}", responseEntity.getStatusCode(), responseEntity.getBody());
                return null;
            }
        } catch (HttpClientErrorException e) {
            log.error("HTTP error calling Azure Medical Text Analysis Function: {} - Response: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            log.error("Error calling Azure Medical Text Analysis Function or processing its response: {}", e.getMessage(), e);
            return null;
        }
    }
} 