package com.healthia.java.services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.azure.storage.blob.models.ListBlobsOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.azure.core.util.BinaryData;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class BlobStorageService {

    private static final Logger log = LoggerFactory.getLogger(BlobStorageService.class);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    private static final int MAX_FILENAME_LENGTH = 100;
    private static final Pattern SANITIZE_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneOffset.UTC);

    private final BlobServiceClient blobServiceClient;
    private final BlobContainerClient blobContainerClient;


    @Value("${app.azure.blob.container-name}")
    private String containerName;

    @Value("${app.azure.blob.folder-name}")
    private String defaultFolder;

    @Autowired
    public BlobStorageService(BlobServiceClient blobServiceClient, @Value("${app.azure.blob.container-name}") String containerName) {
        this.blobServiceClient = blobServiceClient;
        this.blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        // Create container if it doesn't exist
        if (!this.blobContainerClient.exists()) {
            this.blobContainerClient.create();
            log.info("Blob container '{}' created.", containerName);
        }
    }

    public Map<String, Object> uploadFileToBlob(
            InputStream fileContentStream,
            long contentLength,
            String fileExtension,
            Integer conversationId,
            String originalFilename,
            String targetFolder
    ) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (fileContentStream == null) {
                throw new IllegalArgumentException("El contenido del archivo no puede ser nulo");
            }

            String effectiveFolder = StringUtils.hasText(targetFolder) ? targetFolder : this.defaultFolder;
            log.info("Uploading to Azure Blob container '{}', folder '{}'", containerName, effectiveFolder);

            String validatedExtension = validateFileExtension(originalFilename, fileExtension);
            String safeOriginalFilename = (originalFilename != null) ? originalFilename : "file." + validatedExtension;
            String blobName = generateBlobName(effectiveFolder, conversationId, safeOriginalFilename);
            String contentType = "image/" + validatedExtension;

            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            // Use BinaryData.fromStream for efficient streaming
            blobClient.upload(BinaryData.fromStream(fileContentStream, contentLength), true);

            // Set content type
            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);
            blobClient.setHttpHeaders(headers);


            String url = blobClient.getBlobUrl();

            // Verify upload
            if (blobClient.exists()) {
                log.info("File uploaded successfully to Azure Blob: {}", url);
                result.put("success", true);
                result.put("url", url);
                result.put("file_name", blobName);
            } else {
                log.error("Verification failed for Azure Blob upload: Blob '{}' not found after put.", blobName);
                 throw new IOException("No se pudo verificar la subida del archivo a Azure Blob");
            }

        } catch (IllegalArgumentException | IOException | RuntimeException e) {
            log.error("Error uploading file to Azure Blob: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "Error uploading to Azure Blob: " + e.getMessage());
        }

        return result;
    }

    public Map<String, Object> deleteFileFromBlob(String fileUrl) {
         Map<String, Object> result = new HashMap<>();
         try {
             String blobName = extractNameFromUrl(fileUrl);
             BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

             if (!blobClient.exists()) {
                 log.warn("File not found for deletion: {}", fileUrl);
                 result.put("success", false);
                 result.put("error", "El archivo no existe en Azure Blob");
                 return result;
             }

             blobClient.deleteIfExists(DeleteSnapshotsOptionType.INCLUDE, null);


            if (!blobClient.exists()) {
                log.info("File deleted successfully from Azure Blob: {}", fileUrl);
                result.put("success", true);
                result.put("message", String.format("Archivo %s eliminado correctamente", blobName));

            } else {
                 log.error("Verification failed for Azure Blob deletion: Blob '{}' still exists after delete.", blobName);
                 result.put("success", false);
                 result.put("error", "La verificación de la eliminación del archivo falló.");
            }

         } catch (IllegalArgumentException | RuntimeException e) {
             log.error("Error deleting file from Azure Blob (URL: {}): {}", fileUrl, e.getMessage(), e);
             result.put("success", false);
             result.put("error", "Error deleting from Azure Blob: " + e.getMessage());
         }
         return result;
     }

    public Map<String, Object> deleteFolderFromBlob(String folderPath) {
        Map<String, Object> result = new HashMap<>();
        String prefix = folderPath.endsWith("/") ? folderPath : folderPath + "/";
        log.info("Attempting to delete folder/prefix from Azure Blob: {}/{}", containerName, prefix);

        try {
            ListBlobsOptions options = new ListBlobsOptions().setPrefix(prefix);
            List<String> blobsToDelete = new ArrayList<>();

            blobContainerClient.listBlobs(options, null).forEach(blobItem -> {
                log.debug("Adding blob to delete list: {}", blobItem.getName());
                blobsToDelete.add(blobItem.getName());
            });

            if (blobsToDelete.isEmpty()) {
                log.info("No blobs found in folder '{}' to delete.", prefix);
                result.put("success", true);
                result.put("message", String.format("No se encontraron blobs en la carpeta %s", prefix));
                return result;
            }

            int successCount = 0;
            int errorCount = 0;
            List<String> errorMessages = new ArrayList<>();

            for (String blobName : blobsToDelete) {
                try {
                    BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
                    blobClient.deleteIfExists();
                    log.info("Successfully deleted blob: {}/{}", containerName, blobName);
                    successCount++;
                } catch (Exception e) {
                    log.error("Error deleting blob {}/{}: {}", containerName, blobName, e.getMessage(), e);
                    errorCount++;
                    errorMessages.add(String.format("Error deleting %s: %s", blobName, e.getMessage()));
                }
            }

            log.info("Azure Blob folder delete result for '{}': {} successful, {} errors.", prefix, successCount, errorCount);

            if (errorCount > 0) {
                result.put("success", false);
                String errorDetails = String.join("; ", errorMessages);
                result.put("error", String.format("Se eliminaron %d blobs, pero ocurrieron %d errores. Detalle: %s", successCount, errorCount, errorDetails));
            } else {
                result.put("success", true);
                result.put("message", String.format("Carpeta %s eliminada correctamente (%d blobs).", prefix, successCount));
            }

        } catch (RuntimeException e) {
            log.error("Error deleting folder from Azure Blob (Prefix: {}): {}", prefix, e.getMessage(), e);
            result.put("success", false);
            result.put("error", "Error deleting folder from Azure Blob: " + e.getMessage());
        }
        return result;
    }


    // --- Helper Methods ---

    private String sanitizeFilename(String filename) {
        if (filename == null) return "unknown";
        String nameOnly = filename;
        String extension = "";

        int extPos = filename.lastIndexOf('.');
        if (extPos >= 0) {
            nameOnly = filename.substring(0, extPos);
            extension = filename.substring(extPos); // Keep the dot
        }

        String sanitizedName = SANITIZE_PATTERN.matcher(nameOnly).replaceAll("").toLowerCase();

        int maxNameLength = MAX_FILENAME_LENGTH - extension.length();
        if (sanitizedName.length() > maxNameLength) {
            sanitizedName = sanitizedName.substring(0, maxNameLength);
        }

        return sanitizedName + extension;
    }

    private String validateFileExtension(String originalFilename, String defaultExtension) {
        String ext = defaultExtension != null ? defaultExtension.toLowerCase() : "jpg";
        if (originalFilename != null) {
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < originalFilename.length() - 1) {
                String originalExt = originalFilename.substring(lastDot + 1).toLowerCase();
                if (ALLOWED_EXTENSIONS.contains(originalExt)) {
                    return originalExt;
                }
            }
        }
        if (ALLOWED_EXTENSIONS.contains(ext)) {
             return ext;
        }
        return "jpg";
    }

    private String generateBlobName(String folder, Integer id, String filename) {
        String safeFilename = sanitizeFilename(filename);
        String timestamp = TIMESTAMP_FORMATTER.format(LocalDateTime.now());
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        if (id != null) {
            return String.format("%s/%d/%s_%s_%s", folder, id, timestamp, uniqueId, safeFilename);
        } else {
            return String.format("%s/%s_%s_%s", folder, timestamp, uniqueId, safeFilename);
        }
    }

     private String extractNameFromUrl(String fileUrl) throws IllegalArgumentException {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            // Path is typically /containerName/blobName. We need to extract blobName.
            if (path != null && path.startsWith("/")) {
                // Find the second slash, which separates container name from blob path
                int containerEndIndex = path.indexOf('/', 1);
                if (containerEndIndex != -1 && containerEndIndex < path.length() -1) {
                    return path.substring(containerEndIndex + 1);
                }
            }
            throw new IllegalArgumentException("Invalid Azure Blob URL path: " + path + ". Expected format /containerName/blobName");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Azure Blob URL format: " + fileUrl, e);
        }
    }
} 