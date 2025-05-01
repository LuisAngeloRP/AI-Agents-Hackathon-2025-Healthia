package com.healthia.java.controllers;

import com.healthia.java.models.*;
import com.healthia.java.services.ImageAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1") // Using a base path for the API
public class ImageAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(ImageAnalysisController.class);

    private final ImageAnalysisService imageAnalysisService;

    @Autowired
    public ImageAnalysisController(ImageAnalysisService imageAnalysisService) {
        this.imageAnalysisService = imageAnalysisService;
    }

    // Handles JSON requests
    @PutMapping(value = "/analyze-image", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AnalisisPlato> analyzeImageJson(@RequestBody ImageAnalysisRequest analysisRequest) {
        log.info("Received /analyze-image [JSON] request. Conv ID: {}", analysisRequest.getConversationId());
        if (analysisRequest.getImageBase64() == null || analysisRequest.getImageBase64().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo 'imageBase64' es obligatorio en JSON");
        }
        // Pass null for fields not present in JSON request
        return processImageAnalysisRequestInternal(analysisRequest.getImageBase64(), analysisRequest.getConversationId(), null, null);
    }

    // Handles Form Data requests
    @PutMapping(value = "/analyze-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalisisPlato> analyzeImageForm(
            @RequestParam(value = "message", required = false) String message, // Message is ignored in service, but accept it
            @RequestParam(value = "id", required = false) Integer id,
            @RequestPart("media_file") MultipartFile mediaFile // Required in form data
    ) {
         log.info("Received /analyze-image [Form] request. ID: {}, File: {}", id, mediaFile.getOriginalFilename());
        if (mediaFile == null || mediaFile.isEmpty()) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El campo 'media_file' es obligatorio en Form");
        }
        // Pass null for fields not present in Form request
        return processImageAnalysisRequestInternal(null, id, mediaFile, mediaFile.getOriginalFilename());
    }

    // Internal processing logic
    private ResponseEntity<AnalisisPlato> processImageAnalysisRequestInternal(
            String imageBase64,
            Integer analysisId,
            MultipartFile mediaFile,
            String originalFilename
    ) {
        try {
            AnalisisPlato result;
            if (mediaFile != null) {
                 // Call service with byte array
                 byte[] fileContent = mediaFile.getBytes();
                 result = imageAnalysisService.analyzeImage(null, analysisId, fileContent, originalFilename);
            } else {
                 // Call service with base64 string
                 result = imageAnalysisService.analyzeImage(imageBase64, analysisId, null, null);
            }

            if (result == null) {
                 // Should not happen if service throws exceptions, but as a safeguard
                 throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El análisis de imagen devolvió un resultado nulo.");
            }

            return ResponseEntity.ok(result);

        } catch (IOException e) { // From mediaFile.getBytes()
             log.error("IOException processing image analysis request ID {}: {}", analysisId, e.getMessage(), e);
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error de I/O al leer archivo de imagen", e);
        } catch (IllegalArgumentException e) {
             log.warn("Invalid argument processing image analysis request ID {}: {}", analysisId, e.getMessage());
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Argumento inválido: " + e.getMessage(), e);
        } catch (Exception e) {
             log.error("Error processing image analysis request ID {}: {}", analysisId, e.getMessage(), e);
             // Check if it's a custom exception from the service layer if needed
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al analizar la imagen", e);
        }
    }

    @DeleteMapping("/delete-analysis")
    public ResponseEntity<Map<String, String>> deleteAnalysis(@RequestBody DeleteImageAnalysisRequest deleteRequest) {
        log.info("Received /delete-analysis request for ID: {}", deleteRequest.getId());
        try {
            boolean exists = imageAnalysisService.analysisExists(deleteRequest.getId());
            if (!exists) {
                 throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el análisis con ID " + deleteRequest.getId());
            }
            boolean success = imageAnalysisService.deleteAnalysis(deleteRequest.getId());
            if (success) {
                return ResponseEntity.ok(Collections.singletonMap("mensaje", "Análisis con ID " + deleteRequest.getId() + " eliminado correctamente"));
            } else {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo eliminar el análisis con ID " + deleteRequest.getId());
            }
        } catch (ResponseStatusException rse) {
            throw rse; // Re-throw specific HTTP exceptions
        } catch (Exception e) {
            log.error("Error deleting analysis ID {}: {}", deleteRequest.getId(), e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al eliminar el análisis", e);
        }
    }

    @GetMapping("/show-analysis/{analysis_id}")
    public ResponseEntity<ImageAnalysisHistoryResponse> showAnalysis(@PathVariable("analysis_id") int analysisId) {
         log.info("Received /show-analysis request for ID: {}", analysisId);
         try {
             ImageAnalysisHistoryResponse history = imageAnalysisService.getAnalysisHistory(analysisId);
             if (history == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el análisis con ID " + analysisId);
             }
             return ResponseEntity.ok(history);
         } catch (ResponseStatusException rse) {
             throw rse;
         } catch (Exception e) {
             log.error("Error retrieving analysis history for ID {}: {}", analysisId, e.getMessage(), e);
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al obtener el historial del análisis", e);
        }
    }

    @GetMapping("/list-analyses")
    public ResponseEntity<Map<String, List<ImageAnalysisHistoryResponse>>> listAnalyses() {
        log.info("Received /list-analyses request");
         try {
             List<ImageAnalysisHistoryResponse> analyses = imageAnalysisService.getAllAnalyses();
             return ResponseEntity.ok(Collections.singletonMap("analyses", analyses));
         } catch (Exception e) {
             log.error("Error retrieving all analyses: {}", e.getMessage(), e);
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al obtener la lista de análisis", e);
        }
    }

    @GetMapping("/debug-analyses")
    public ResponseEntity<Map<String, Object>> debugAnalyses() {
        log.info("Received /debug-analyses request");
        try {
            Map<String, Object> debugInfo = imageAnalysisService.debugAnalysesState();
            return ResponseEntity.ok(debugInfo);
         } catch (Exception e) {
             log.error("Error retrieving debug analyses state: {}", e.getMessage(), e);
             throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno al obtener el estado de depuración", e);
        }
    }
} 