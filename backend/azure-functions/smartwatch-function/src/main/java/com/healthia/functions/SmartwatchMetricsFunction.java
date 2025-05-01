package com.healthia.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory; // Consider an alternative for Azure Functions
// import com.google.api.services.fitness.Fitness; // If using the Fitness SDK
// import com.google.api.services.fitness.model.*;
import com.healthia.functions.entities.UserHealthMetricEntity;
import com.healthia.functions.entities.UserTimeSeriesDataPointEntity;
import com.healthia.functions.util.JPAUtil;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.io.File; // For FileDataStoreFactory, needs careful handling in Azure Functions
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class SmartwatchMetricsFunction {

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String APPLICATION_NAME = "HealthIA Smartwatch Integration";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    // TODO: Securely manage client secrets. For Functions, consider Azure Key Vault.
    private static final String CREDENTIALS_FILE_PATH = "/google_fit_credentials.json"; // Path within resources
    private static final String TOKENS_DIRECTORY_PATH = "tokens"; // For FileDataStoreFactory - problematic in Functions

    // --- Request Models ---
    public static class FetchMetricsRequest {
        public String userId;
        public String googleAccessToken; // If user provides it directly (e.g., after client-side OAuth)
                                     // Or, the function might manage refresh tokens if it handles OAuth server-side.
    }

    /**
     * HTTP-triggered function to fetch smartwatch data for a specific user.
     * This would typically be called after a user authorizes the application.
     */
    @FunctionName("FetchSmartwatchDataHttp")
    public HttpResponseMessage fetchHttp(
            @HttpTrigger(name = "req", 
                         methods = {HttpMethod.POST}, 
                         authLevel = AuthorizationLevel.FUNCTION, 
                         dataType = "json") 
            HttpRequestMessage<Optional<FetchMetricsRequest>> request,
            final ExecutionContext context) {
        context.getLogger().info("SmartwatchMetricsFunction: FetchSmartwatchDataHttp triggered.");

        FetchMetricsRequest reqBody = request.getBody().orElse(null);
        if (reqBody == null || reqBody.userId == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("Please pass userId in the request body.").build();
        }

        try {
            // Placeholder: In a real scenario, you'd use reqBody.googleAccessToken or 
            // stored refresh tokens to get data from Google Fit API for reqBody.userId.
            context.getLogger().info("Simulating fetching data for user: " + reqBody.userId);
            List<UserHealthMetricEntity> fetchedMetrics = simulateFetchingGoogleFitData(reqBody.userId, context);
            
            if (fetchedMetrics.isEmpty()) {
                 context.getLogger().info("No new metrics fetched or simulated for user: " + reqBody.userId);
                 return request.createResponseBuilder(HttpStatus.OK).body("No new metrics data to process for user " + reqBody.userId).build();
            }

            saveHealthMetrics(fetchedMetrics, context);
            return request.createResponseBuilder(HttpStatus.OK)
                .body("Successfully fetched and stored smartwatch data for user " + reqBody.userId).build();

        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Error in FetchSmartwatchDataHttp: " + e.getMessage(), e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing smartwatch data: " + e.getMessage()).build();
        }
    }

    /**
     * Timer-triggered function to periodically fetch smartwatch data for all relevant users.
     * The schedule is defined in function.json or via annotation (e.g., "0 */30 * * * *" for every 30 mins).
     */
    @FunctionName("FetchSmartwatchDataTimer")
    public void fetchTimer(
            @TimerTrigger(name = "timerInfo", schedule = "0 */30 * * * *") String timerInfo, // Example: every 30 mins
            final ExecutionContext context) {
        context.getLogger().info("SmartwatchMetricsFunction: FetchSmartwatchDataTimer triggered at: " + timerInfo);
        
        // 1. Get list of users who have linked their smartwatch accounts.
        // This might involve querying a UserDataEntity table or another table that stores OAuth tokens/consent.
        // For this example, we'll simulate for a default user.
        List<String> userIdsToFetch = Collections.singletonList("defaultUserWithSmartwatch"); 

        for (String userId : userIdsToFetch) {
            context.getLogger().info("Timer: Attempting to fetch data for user: " + userId);
            try {
                List<UserHealthMetricEntity> fetchedMetrics = simulateFetchingGoogleFitData(userId, context);
                 if (fetchedMetrics.isEmpty()) {
                    context.getLogger().info("Timer: No new metrics fetched or simulated for user: " + userId);
                    continue;
                }
                saveHealthMetrics(fetchedMetrics, context);
                context.getLogger().info("Timer: Successfully processed smartwatch data for user " + userId);
            } catch (Exception e) {
                context.getLogger().log(Level.SEVERE, "Timer: Error processing smartwatch data for user " + userId + ": " + e.getMessage(), e);
                // Continue with the next user
            }
        }
    }

    /**
     * Placeholder for Google Fit API integration.
     * This method would handle OAuth, make API calls, and parse the response.
     * @return A list of UserHealthMetricEntity populated with data from the API.
     */
    private List<UserHealthMetricEntity> simulateFetchingGoogleFitData(String userId, ExecutionContext context) throws IOException, GeneralSecurityException {
        context.getLogger().info("Simulating Google Fit API call for user: " + userId);
        
        // --- Conceptual Google Fit API Interaction Steps (Highly Simplified) ---
        // 1. Obtain OAuth2 Credentials for the user. 
        //    This is complex. For a server-side flow, you'd store refresh tokens securely.
        //    For user-delegated permission via an HTTP trigger, an access token might be passed.
        //    The FileDataStoreFactory is problematic in serverless Azure Functions due to temp file system limits and statelessness.
        //    A database or secure store should be used for tokens.

        // Credential credential = getCredentials(); // Placeholder for complex OAuth logic
        // Fitness fitnessService = new Fitness.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
        //        .setApplicationName(APPLICATION_NAME)
        //        .build();

        // 2. Define data sources and data types (e.g., heart rate, steps).
        //    long startTime = ...; // Start of time range
        //    long endTime = System.currentTimeMillis(); // End of time range (now)

        // 3. Make API calls, e.g., to fetch datasets for specific data types.
        //    Example: fetch heart rate data
        //    Dataset heartRateData = fitnessService.users().dataSources()
        //                                .datasets() 
        //                                .get("me", "derived:com.google.heart_rate.bpm:com.google.android.gms:merge_heart_rate_bpm", 
        //                                     startTime + "-" + endTime) // Time range in nanos
        //                                .execute();
        //    processDataset(heartRateData, userId, "HEART_RATE_BPM", "bpm", "GoogleFit");
        
        // --- Simulation --- 
        List<UserHealthMetricEntity> metrics = new ArrayList<>();

        // Simulate Heart Rate
        UserHealthMetricEntity heartRateMetric = new UserHealthMetricEntity();
        heartRateMetric.setUserId(userId);
        heartRateMetric.setMetricType("HEART_RATE_BPM");
        heartRateMetric.setUnit("bpm");
        heartRateMetric.setDataSource("GoogleFitSimulated");
        heartRateMetric.setLastUpdatedFromSource(OffsetDateTime.now(ZoneOffset.UTC));
        heartRateMetric.setLatestValueNumeric(Math.random() * 40 + 60); // Random BPM between 60-100

        UserTimeSeriesDataPointEntity hrDataPoint1 = new UserTimeSeriesDataPointEntity();
        hrDataPoint1.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5));
        hrDataPoint1.setValueNumeric(Math.random() * 10 + 65);
        heartRateMetric.addDataPoint(hrDataPoint1);

        UserTimeSeriesDataPointEntity hrDataPoint2 = new UserTimeSeriesDataPointEntity();
        hrDataPoint2.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(2));
        hrDataPoint2.setValueNumeric(Math.random() * 10 + 70);
        heartRateMetric.addDataPoint(hrDataPoint2);
        metrics.add(heartRateMetric);

        // Simulate Steps
        UserHealthMetricEntity stepsMetric = new UserHealthMetricEntity();
        stepsMetric.setUserId(userId);
        stepsMetric.setMetricType("STEPS_COUNT");
        stepsMetric.setUnit("count");
        stepsMetric.setDataSource("GoogleFitSimulated");
        stepsMetric.setLastUpdatedFromSource(OffsetDateTime.now(ZoneOffset.UTC));
        stepsMetric.setLatestValueNumeric(Math.random() * 5000 + 1000); // Random steps
        metrics.add(stepsMetric);

        return metrics;
    }
    
    /**
     * Helper to get OAuth2 credentials. 
     * WARNING: This is a simplified example for command-line apps and NOT SUITABLE for production serverless functions.
     * In Azure Functions, you need a more robust way to handle OAuth, potentially storing refresh tokens 
     * securely (e.g., in Azure Key Vault or a database) and using them to obtain access tokens.
     * The FileDataStoreFactory is not ideal for serverless environments.
     */
    private Credential getCredentials() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        InputStreamReader clientSecretReader = new InputStreamReader(Objects.requireNonNull(SmartwatchMetricsFunction.class.getResourceAsStream(CREDENTIALS_FILE_PATH)));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);

        // This creates a local file for storing tokens. Not suitable for scalable/stateless Functions.
        // Consider alternatives like Azure Key Vault or DB storage for tokens.
        FileDataStoreFactory FDS_FACTORY = new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, 
                Collections.singletonList("https://www.googleapis.com/auth/fitness.activity.read")) // Example scope
                .setDataStoreFactory(FDS_FACTORY)
                .setAccessType("offline")
                .build();
        // In a real app, this would involve redirecting the user to an auth URL and handling the callback.
        // For a server-side app/Function, you'd likely handle the OAuth code flow or use service accounts if applicable.
        // LocalServerReceiver is for command-line apps.
        // Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver.Builder().setPort(8888).build()).authorize("user");
        // return credential;
        throw new UnsupportedOperationException("OAuth flow not implemented for serverless function. Store and use refresh tokens.");
    }

    private void saveHealthMetrics(List<UserHealthMetricEntity> metrics, ExecutionContext context) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction transaction = null;
        try {
            transaction = em.getTransaction();
            transaction.begin();
            for (UserHealthMetricEntity metric : metrics) {
                // Check if a similar metric type for this user already exists to update it,
                // or always create new entries based on your data model strategy.
                // For simplicity, we merge. You might want more sophisticated logic for time series data.
                
                // Query for existing metric
                UserHealthMetricEntity existingMetric = null;
                try {
                    existingMetric = em.createQuery(
                        "SELECT m FROM UserHealthMetricEntity m WHERE m.userId = :userId AND m.metricType = :metricType AND m.dataSource = :dataSource", 
                        UserHealthMetricEntity.class)
                        .setParameter("userId", metric.getUserId())
                        .setParameter("metricType", metric.getMetricType())
                        .setParameter("dataSource", metric.getDataSource())
                        .getSingleResult();
                } catch (jakarta.persistence.NoResultException nre) {
                    // It's a new metric type for this user/source
                }

                if (existingMetric != null) {
                    // Update existing metric
                    existingMetric.setLastUpdatedFromSource(metric.getLastUpdatedFromSource());
                    existingMetric.setLatestValueNumeric(metric.getLatestValueNumeric());
                    existingMetric.setLatestValueText(metric.getLatestValueText());
                    existingMetric.setUnit(metric.getUnit()); // Unit might change if API updates
                    existingMetric.setRecordedInDbAt(OffsetDateTime.now(ZoneOffset.UTC)); // Update DB record time

                    // For time-series data, decide on merging strategy: replace all, append new, or update existing points.
                    // Simple approach: clear old points and add new ones.
                    existingMetric.getTimeSeriesDataPoints().clear();
                    for(UserTimeSeriesDataPointEntity newPoint : metric.getTimeSeriesDataPoints()) {
                        existingMetric.addDataPoint(newPoint);
                    }
                    em.merge(existingMetric);
                    context.getLogger().info("Updated metric ID " + existingMetric.getMetricId() + " for user " + metric.getUserId() + " and type " + metric.getMetricType());
                } else {
                    // Persist new metric
                    metric.setRecordedInDbAt(OffsetDateTime.now(ZoneOffset.UTC));
                    em.persist(metric);
                    context.getLogger().info("Persisted new metric for user " + metric.getUserId() + " and type " + metric.getMetricType());
                }
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            context.getLogger().log(Level.SEVERE, "Error saving health metrics: " + e.getMessage(), e);
            // Consider how to handle partial failures if processing multiple users/metrics
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
} 