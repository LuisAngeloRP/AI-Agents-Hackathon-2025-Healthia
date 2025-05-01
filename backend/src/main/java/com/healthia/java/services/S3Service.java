package com.healthia.java.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    private static final int MAX_FILENAME_LENGTH = 100;
    private static final Pattern SANITIZE_PATTERN = Pattern.compile("[^a-zA-Z0-9._-]");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneOffset.UTC); // Use UTC for consistency

    private final S3Client s3Client;

    @Value("${app.aws.s3.bucket-name}")
    private String bucketName;

    @Value("${app.aws.s3.folder-name}")
    private String defaultFolder;

    @Value("${app.aws.region}")
    private String region;

    @Autowired
    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public Map<String, Object> uploadFileToS3(
            InputStream fileContentStream,
            long contentLength, // Provide content length for non-resettable streams
            String fileExtension,
            Integer conversationId, // Using Integer to allow null
            String originalFilename,
            String targetFolder // Optional target folder override
    ) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (fileContentStream == null) {
                throw new IllegalArgumentException("El contenido del archivo no puede ser nulo");
            }

            String effectiveFolder = StringUtils.isNotBlank(targetFolder) ? targetFolder : this.defaultFolder;
            log.info("Uploading to S3 bucket '{}', folder '{}'", bucketName, effectiveFolder);

            String validatedExtension = validateFileExtension(originalFilename, fileExtension);
            String safeOriginalFilename = (originalFilename != null) ? originalFilename : "file." + validatedExtension;
            String s3Key = generateS3Key(effectiveFolder, conversationId, safeOriginalFilename);
            String contentType = "image/" + validatedExtension; // Basic content type

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    // .contentLength(contentLength) // contentLength needed for non-resettable streams
                    .build();

            // Use RequestBody.fromInputStream for efficient streaming
            s3Client.putObject(putRequest, RequestBody.fromInputStream(fileContentStream, contentLength));

            // Construct URL
            // Note: URL structure can vary slightly depending on region and settings.
            // Using the common virtual-hosted style URL.
            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);

            // Verify upload (optional but good practice)
            try {
                s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(s3Key).build());
                log.info("File uploaded successfully to S3: {}", url);
                result.put("success", true);
                result.put("url", url);
                result.put("file_name", s3Key);
            } catch (NoSuchKeyException e) {
                log.error("Verification failed for S3 upload: Key '{}' not found after put.", s3Key);
                 throw new IOException("No se pudo verificar la subida del archivo a S3");
            }

        } catch (IllegalArgumentException | IOException | SdkException e) {
            log.error("Error uploading file to S3: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", "Error uploading to S3: " + e.getMessage());
        } /* Ensure stream is closed if necessary, though try-with-resources is better if the stream is created here */

        return result;
    }

    public Map<String, Object> deleteFileFromS3(String fileUrl) {
         Map<String, Object> result = new HashMap<>();
         try {
             String s3Key = extractKeyFromUrl(fileUrl);

             // Check if exists
             try {
                 s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(s3Key).build());
             } catch (NoSuchKeyException e) {
                 log.warn("File not found for deletion: {}", fileUrl);
                 result.put("success", false);
                 result.put("error", "El archivo no existe en S3");
                 return result;
             }

             // Delete object
             DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                     .bucket(bucketName)
                     .key(s3Key)
                     .build();
             s3Client.deleteObject(deleteRequest);

             // Verify deletion (expect NoSuchKeyException)
             try {
                 s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(s3Key).build());
                 // If headObject succeeds, deletion failed
                 log.error("Verification failed for S3 deletion: Key '{}' still exists after delete.", s3Key);
                 result.put("success", false);
                 result.put("error", "La verificación de la eliminación del archivo falló.");
             } catch (NoSuchKeyException e) {
                 // This is expected, means deletion was successful
                 log.info("File deleted successfully from S3: {}", fileUrl);
                 result.put("success", true);
                 result.put("message", String.format("Archivo %s eliminado correctamente", s3Key));
             }

         } catch (IllegalArgumentException | SdkException e) {
             log.error("Error deleting file from S3 (URL: {}): {}", fileUrl, e.getMessage(), e);
             result.put("success", false);
             result.put("error", "Error deleting from S3: " + e.getMessage());
         }
         return result;
     }

    public Map<String, Object> deleteFolderFromS3(String folderPath) {
         Map<String, Object> result = new HashMap<>();
         log.info("Attempting to delete folder/prefix from S3: {}/", folderPath);
         try {
             // Ensure the path ends with / to avoid deleting unintended objects if folderPath is also an object key
             String prefix = folderPath.endsWith("/") ? folderPath : folderPath + "/";

             ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                     .bucket(bucketName)
                     .prefix(prefix)
                     .build();

             List<ObjectIdentifier> objectsToDelete = new ArrayList<>();
             // Use listObjectsV2Paginator for potentially large folders
             s3Client.listObjectsV2Paginator(listRequest).stream()
                 .flatMap(response -> response.contents().stream())
                 .forEach(s3Object -> {
                     log.debug("Adding object to delete list: {}", s3Object.key());
                     objectsToDelete.add(ObjectIdentifier.builder().key(s3Object.key()).build());
                 });

            if (objectsToDelete.isEmpty()) {
                 log.info("No objects found in folder '{}' to delete.", prefix);
                 result.put("success", true);
                 result.put("message", String.format("No se encontraron objetos en la carpeta %s", prefix));
                 return result;
             }

             DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                     .bucket(bucketName)
                     .delete(Delete.builder().objects(objectsToDelete).quiet(false).build())
                     .build();

             DeleteObjectsResponse deleteResponse = s3Client.deleteObjects(deleteRequest);

             int successCount = deleteResponse.deleted().size();
             int errorCount = deleteResponse.hasErrors() ? deleteResponse.errors().size() : 0;
             log.info("S3 folder delete result for '{}': {} successful, {} errors.", prefix, successCount, errorCount);

            if (errorCount > 0) {
                 deleteResponse.errors().forEach(error ->
                     log.error("S3 delete error: Key={}, Code={}, Message={}", error.key(), error.code(), error.message()));
                 result.put("success", false);
                 result.put("error", String.format("Se eliminaron %d objetos, pero ocurrieron %d errores.", successCount, errorCount));
             } else {
                 result.put("success", true);
                 result.put("message", String.format("Carpeta %s eliminada correctamente (%d objetos).", prefix, successCount));
             }

         } catch (SdkException e) {
             log.error("Error deleting folder from S3 (Prefix: {}): {}", folderPath, e.getMessage(), e);
             result.put("success", false);
             result.put("error", "Error deleting folder from S3: " + e.getMessage());
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

        // Limit length, preserving extension
        int maxNameLength = MAX_FILENAME_LENGTH - extension.length();
        if (sanitizedName.length() > maxNameLength) {
            sanitizedName = sanitizedName.substring(0, maxNameLength);
        }

        return sanitizedName + extension;
    }

    private String validateFileExtension(String originalFilename, String defaultExtension) {
        String ext = defaultExtension != null ? defaultExtension.toLowerCase() : "jpg"; // Default
        if (originalFilename != null) {
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < originalFilename.length() - 1) {
                String originalExt = originalFilename.substring(lastDot + 1).toLowerCase();
                if (ALLOWED_EXTENSIONS.contains(originalExt)) {
                    return originalExt;
                }
            }
        }
        // If original filename didn't provide a valid extension, use the default if it's valid
        if (ALLOWED_EXTENSIONS.contains(ext)) {
             return ext;
        }
        return "jpg"; // Fallback to jpg if default is also invalid
    }

    private String generateS3Key(String folder, Integer id, String filename) {
        String safeFilename = sanitizeFilename(filename);
        String timestamp = TIMESTAMP_FORMATTER.format(LocalDateTime.now());
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);

        if (id != null) {
            return String.format("%s/%d/%s_%s_%s", folder, id, timestamp, uniqueId, safeFilename);
        } else {
            return String.format("%s/%s_%s_%s", folder, timestamp, uniqueId, safeFilename);
        }
    }

     private String extractKeyFromUrl(String fileUrl) throws IllegalArgumentException {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            // Path starts with a '/', remove it
            if (path != null && path.startsWith("/")) {
                return path.substring(1);
            }
            throw new IllegalArgumentException("Invalid S3 URL path: " + path);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid S3 URL format: " + fileUrl, e);
        }
    }
} 