# HealthIA Backend

## Overview

The HealthIA backend is a Java-based system built with Spring Boot and Maven. It utilizes Azure Functions for serverless capabilities and integrates with Azure Blob Storage and OpenAI services. The backend is structured as a multi-module Maven project.

## Project Structure

The backend consists of the following main modules:

-   `healthia-app`: This is the core Spring Boot application. It handles main API requests, business logic, and orchestrates calls to Azure Functions and other services.
    -   `src/main/java/com/healthia/java/`:
        -   `HealthiaJavaApplication.java`: The main entry point for the Spring Boot application.
        -   `config/`: Contains configuration classes for external services like Azure Blob Storage (`AzureBlobConfig.java`) and OpenAI (`OpenAIConfig.java`).
        -   `controllers/`: Defines the REST API endpoints.
            -   `ChatbotController.java`: Handles chat-related functionalities, likely interacting with AI agents.
            -   `ImageAnalysisController.java`: Manages image analysis requests, potentially for food logging or medical image processing.
        -   `dtos/`: Data Transfer Objects used for request and response payloads, particularly for Azure function interactions.
            -   `azure/`: Contains DTOs specific to Azure Function calls (e.g., `AzureImageAnalysisRequest.java`, `AzureMedicalTextAnalysisResponse.java`).
        -   `models/`: Domain models representing various entities in the application (e.g., `AnalisisPlato.java`, `ChatRequest.java`, `UserData.java`).
        -   `services/`: Contains the business logic of the application.
            -   `AzureFunctionClientService.java`: A client service to interact with the deployed Azure Functions.
            -   `BlobStorageService.java`: Service for interacting with Azure Blob Storage (e.g., uploading, downloading files).
            -   `ImageAnalysisService.java`: Handles the logic for image analysis.
            -   `MealPlanGeneratorService.java`: Responsible for generating meal plans, likely using OpenAI.
            -   `OpenAIService.java`: A generic service to interact with OpenAI APIs.
            -   `agents/`: Implements various AI agents using a framework (possibly LangGraph based on `langgraph4j` dependency).
                -   `ExerciseAgent.java`: Agent focused on exercise-related queries or tasks.
                -   `HealthiaAgentState.java`: Represents the state for the agents.
                -   `MedicalAgent.java`: Agent specialized in medical information or queries.
                -   `NutritionAgent.java`: Agent dedicated to nutrition-related advice and information.
                -   `SupervisorService.java`: Likely an orchestrator or router for the different agents.
-   `azure-functions`: This module contains individual Azure Functions, which are serverless components designed for specific tasks.
    -   `pom.xml`: Manages dependencies and build configurations specific to the Azure Functions. It includes dependencies for Azure Functions Java library, Spring Boot (possibly for some functions), Jackson, MySQL Connector, and OpenAI.
    -   `dashboard-metrics-function/`: Azure Function related to dashboard metrics.
    -   `exercise-function/`: Azure Function for exercise-related processing.
    -   `image-analysis-function/`: Azure Function dedicated to image analysis.
        -   `entities/`, `repositories/`, `util/`
    -   `medical-function/`: Azure Function for medical data processing or queries.
        -   `entities/`, `repositories/`
    -   `nutrition-function/`: Azure Function for nutrition-related tasks.
        -   `entities/`, `util/`
    -   `smartwatch-function/`: Azure Function for processing data from smartwatches.
        -   `entities/`, `util/`
-   `src/`: This directory in the parent `backend` module seems to be a remnant or an older structure and might contain parts of the Spring Boot application that were not fully migrated to the `healthia-app` module. The current main Spring Boot application resides in `healthia-app`. However, crucial components like `HealthiaJavaApplication.java`, controllers, models, and services (including agents) are located here. **This is a potential point of confusion and could be refactored for better clarity by moving all Spring Boot application code into the `healthia-app` module.**
-   `data_usuario/`: This directory's purpose is unclear from the structure alone. It might be used for storing user-specific data locally, for testing, or for initial data seeding.
-   `pom.xml`: The parent POM file for the entire backend, managing common dependencies, properties, and build configurations for all sub-modules. It defines `healthia-app` and `azure-functions` as its modules. It includes `langgraph4j-core` as a direct dependency.

## Functionality

The backend provides the following key functionalities:

1.  **Chatbot Services**: Through `ChatbotController` and various AI agents (`ExerciseAgent`, `MedicalAgent`, `NutritionAgent` orchestrated by `SupervisorService`), the system can engage in conversations with users, providing information and assistance related to health, exercise, and nutrition. It likely uses OpenAI for generating responses.
2.  **Image Analysis**: The `ImageAnalysisController` and `ImageAnalysisService`, along with the `image-analysis-function` (Azure Function), allow users to upload images for analysis. This could be used for:
    -   Food recognition and nutritional analysis of meals.
    -   Potentially, analysis of medical images (though this would require strict compliance and validation).
3.  **Meal Plan Generation**: The `MealPlanGeneratorService` suggests an ability to create personalized meal plans for users, probably leveraging OpenAI's capabilities.
4.  **Data Storage**: Azure Blob Storage is used for storing binary data like images (`BlobStorageService`, `AzureBlobConfig`).
5.  **Azure Function Integration**: The system leverages Azure Functions for specific, potentially intensive or event-driven tasks. The `AzureFunctionClientService` in the `healthia-app` module is responsible for invoking these functions. Each function appears to be self-contained with its own entities and, in some cases, repositories (suggesting direct database interaction from functions like `image-analysis-function` and `medical-function`).
6.  **User Data Management**: The `UserData` model suggests that user-specific information is stored and processed.

## Building and Running

The project uses Maven for dependency management and building.

### 1. Prerequisites

*   Java JDK 17 or later.
*   Apache Maven.
*   Azure CLI: For managing Azure resources and deployments.
*   Azure Functions Core Tools: For local development and testing of Azure Functions.
*   An active Azure Subscription.
*   OpenAI API Key: For services utilizing OpenAI models.
*   (Optional) Azurite: For local Azure Storage emulation if you need to test Blob Storage interactions locally.
*   (Optional) Docker: If you plan to containerize the Spring Boot application.

### 2. Azure Services Setup

The following Azure services need to be provisioned:

*   **Azure App Service (or other compute for Spring Boot)**: To host the main `healthia-app` Spring Boot application. Alternatives include Azure Kubernetes Service (AKS) or Azure Container Instances (ACI) if deploying as a container.
*   **Azure Function App**: Create a Function App in Azure to host the individual functions from the `azure-functions` module. You will deploy each function (e.g., `nutrition-function`, `image-analysis-function`) to this Function App.
    *   **Runtime Stack**: Java 17.
    *   **Hosting Plan**: Consumption (Serverless) or Premium plan based on requirements.
*   **Azure Blob Storage**: Used by `BlobStorageService` for storing images and potentially other binary data. Create a Storage Account and a container within it.
*   **Azure Database for MySQL (or other relational database)**: If your Azure Functions (e.g., `image-analysis-function`, `medical-function` based on presence of `repositories`) interact with a database, provision an Azure Database for MySQL instance.
    *   Create the necessary database schema and tables.
    *   Configure firewall rules to allow access from your Azure Functions and, for development, from your local machine.
*   **Azure Key Vault (Recommended)**: For securely storing secrets like API keys (OpenAI, Azure services connection strings, database credentials). Your Spring Boot application and Azure Functions should be configured to retrieve secrets from Key Vault using Managed Identities.
*   **Azure Application Insights (Recommended)**: For monitoring and logging both the Spring Boot application and Azure Functions.

### 3. Configuration

#### a. Spring Boot Application (`healthia-app` and `backend/src`)

*   **`application.properties`** (or `application.yml`) in `healthia-app/src/main/resources/` (and potentially `backend/src/main/resources/` if still used):
    *   Configure database connection details (if the Spring Boot app directly accesses a DB).
    *   Azure Blob Storage connection string and container name.
    *   OpenAI API Key and any relevant endpoints or model IDs.
    *   URLs for your deployed Azure Functions (e.g., `azure.function.nutrition.url`, `azure.function.imageanalysis.url`).
    *   **Recommendation**: Use Spring profiles (e.g., `dev`, `prod`) to manage environment-specific configurations. For production, source sensitive values from Azure Key Vault or environment variables set by the hosting service.

#### b. Azure Functions (`azure-functions/*`)

*   **`local.settings.json`**: For each function module (e.g., `backend/azure-functions/nutrition-function/local.settings.json`). This file is used for local development and **should not be committed to source control if it contains secrets**.
    *   `AzureWebJobsStorage`: Connection string for Azure Storage (used by the Functions host, can be Azurite for local).
    *   `FUNCTIONS_WORKER_RUNTIME`: `java`
    *   Database connection string (e.g., `DB_CONNECTION_STRING`, `DB_USER`, `DB_PASSWORD`).
    *   OpenAI API Key (e.g., `OPENAI_API_KEY`).
    *   Any other function-specific settings.
*   **Application Settings in Azure Portal**: When deployed to Azure, these settings must be configured in the Function App's "Configuration" section (or managed via Key Vault references).
    *   This includes database connection strings, OpenAI API keys, Azure Storage connection strings, etc.
*   **`pom.xml` for each function**: May contain plugin configurations for deployment, including the Function App name and resource group.

### 4. Building the Backend

Navigate to the root `backend` directory and run:

```bash
mvn clean install
```
This command will compile the code, run tests, and build JAR files for the Spring Boot application and each Azure Function.

### 5. Running Locally

#### a. Running the Spring Boot Application

Assuming the main application code is consolidated or correctly configured in the parent `pom.xml` or `healthia-app/pom.xml`:

From the `backend` directory (if `spring-boot-maven-plugin` is configured in parent POM):
```bash
mvn spring-boot:run -pl healthia-app
```
Or, from the `backend/healthia-app` directory:
```bash
mvn spring-boot:run
```
The application will typically start on port 8080 (or as configured in `application.properties`). Ensure all necessary configurations (like OpenAI keys, Azure Function URLs if calling deployed functions, or local function URLs) are set in `application.properties` or as environment variables.

#### b. Running Azure Functions Locally

For each Azure Function module (e.g., `dashboard-metrics-function`, `nutrition-function`):

1.  Navigate to the function's directory:
    ```bash
    cd backend/azure-functions/your-function-name
    ```
2.  Ensure `local.settings.json` is configured with necessary connection strings and API keys.
3.  Start the function using Azure Functions Core Tools:
    ```bash
    func host start
    ```
    Or, if you prefer to use the Maven plugin (this might also build first):
    ```bash
    mvn azure-functions:run
    ```
Each function will run on a different port (e.g., 7071, 7072, etc.). The Spring Boot application needs to be configured to call these local function URLs if you are testing the integration locally.

### 6. Deployment to Azure

#### a. Deploying the Spring Boot Application

*   **To Azure App Service**:
    *   Can be done via the Azure CLI, Maven plugin for Azure App Service (`mvn azure-webapp:deploy`), or by setting up a CI/CD pipeline (e.g., GitHub Actions, Azure DevOps).
    *   Ensure all necessary Application Settings (environment variables, Key Vault references) are configured in the App Service.
*   **As a Container**:
    *   Build a Docker image of the Spring Boot application.
    *   Push the image to a container registry (e.g., Azure Container Registry).
    *   Deploy to Azure Kubernetes Service (AKS), Azure Container Instances (ACI), or Azure Web App for Containers.

#### b. Deploying Azure Functions

For each Azure Function module:

1.  Navigate to the function's directory:
    ```bash
    cd backend/azure-functions/your-function-name
    ```
2.  Use the Maven plugin to deploy:
    ```bash
    mvn azure-functions:deploy
    ```
    You might need to configure the `pom.xml` with the Azure Function App name, resource group, and other deployment details, or provide them as command-line parameters.
3.  Ensure all Application Settings are correctly configured in the Azure Function App post-deployment.

## Dependencies

-   Spring Boot: Core framework for the main application.
-   Azure SDKs: For Azure Functions and Azure Blob Storage.
-   OpenAI Java Library: For interacting with OpenAI APIs.
-   LangGraph4j: A library for building stateful, multi-actor applications with LLMs, used for the agentic services.
-   Jackson: For JSON serialization/deserialization.
-   Lombok: To reduce boilerplate code.
-   MySQL Connector: Suggests some Azure Functions might be interacting with a MySQL database.

## Potential Issues and Areas for Improvement

1.  **Project Structure Clarity**:
    -   The location of the main Spring Boot application code (`HealthiaJavaApplication.java`, controllers, core services, models) is currently in `backend/src/main/java/...` while there's also a `backend/healthia-app/src/main/java/...` which contains `AzureFunctionClientService.java` and related DTOs. This is confusing. **RECOMMENDATION**: Consolidate all Spring Boot application code (entry point, controllers, services, models, configs currently in `backend/src/`) into the `healthia-app` module to make it the single source for the main application. The `backend/src` directory could then be removed or used only for truly shared code if any (though Maven modules are the standard way to share).
2.  **Azure Function Responsibilities & Database Access**:
    -   Some Azure Functions (e.g., `image-analysis-function`, `medical-function`) have `repositories`, suggesting they directly access a database. While this is possible, it can sometimes lead to a more complex architecture if the main application also needs to interact with the same data or manage transactions across services. The division of responsibilities between the main app and the functions regarding data ownership should be clear.
    -   The `nutrition-function` and `smartwatch-function` have `entities` but no `repositories` visible in the provided structure. It's unclear how they persist or retrieve data if needed.
3.  **Missing Implementations (Inferred)**:
    -   **Authentication and Authorization**: There's no explicit mention or visible structure for user authentication and authorization. This is a critical component for any application handling user data, especially sensitive health information.
    -   **Error Handling and Logging**: While Spring Boot and Azure Functions provide some default capabilities, a robust and centralized error handling and logging strategy is crucial for production systems.
    -   **Testing**: The structure includes `src/test/java` in the parent `backend` but the extent and coverage of tests are not visible. Comprehensive unit, integration, and potentially end-to-end tests are necessary.
    -   **Configuration Management**: While `OpenAIConfig.java` and `AzureBlobConfig.java` exist, a more detailed strategy for managing configurations (e.g., API keys, connection strings) for different environments (dev, staging, prod) should be in place (e.g., using Spring profiles, Azure App Configuration).
    -   **API Documentation**: No tools like Swagger/OpenAPI are immediately visible. Documenting the REST APIs is important for frontend integration and maintainability.
4.  **`data_usuario/` Directory**: The purpose and usage of this directory should be documented. If it's for temporary data or specific deployment scenarios, this should be clarified.
5.  **Inter-service Communication Security**: If Azure Functions are called over HTTP, ensure these calls are secured (e.g., using Azure Function Keys, Managed Identities, or VNet integration).
6.  **Dependencies in `azure-functions/pom.xml` vs. Individual Function POMs**: The parent `azure-functions/pom.xml` manages dependencies for all function apps. It's worth checking if each individual function app (e.g., `nutrition-function/pom.xml`) also declares these or if they solely rely on the parent. The current view doesn't show individual function POMs, but they typically exist.
7.  **`langgraph4j` Version**: The parent `pom.xml` mentions `<langgraph4j.version>1.5.13</langgraph4j.version>` and `<artifactId>langgraph4j-core</artifactId>`. Ensure this is the intended library and version for the agentic framework.

## Further Steps

-   Clarify the intended structure for the Spring Boot application (`healthia-app` vs. `backend/src`).
-   Document the data flow, especially between the main application and the Azure Functions.
-   Detail the purpose of each Azure Function and how it's triggered.
-   Review and implement security best practices, especially for authentication, authorization, and data protection.
-   Expand on testing strategies and coverage.

This README provides a high-level overview based on the directory structure and POM files. A deeper dive into the code of each service and function would be necessary for a more exhaustive analysis. 