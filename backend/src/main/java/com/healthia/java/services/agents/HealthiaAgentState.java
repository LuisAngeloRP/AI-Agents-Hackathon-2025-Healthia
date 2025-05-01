package com.healthia.java.services.agents;

import com.healthia.java.dtos.azure.AzureImageAnalysisResponse;
import com.healthia.java.dtos.azure.AzureMedicalTextAnalysisResponse;
import com.healthia.java.models.UserData;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.UpdateChannel;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HealthiaAgentState extends AgentState {

    public static final String USER_INPUT = "userInput";
    public static final String IMAGE_PATH = "imagePath";
    public static final String USER_DATA = "userData";
    public static final String SELECTED_AGENT_NAME = "selectedAgentName";
    public static final String AGENT_RESPONSE = "agentResponse";
    public static final String IS_IMAGE_PROCESSING = "isImageProcessing";
    public static final String AZURE_IMAGE_ANALYSIS_RESPONSE = "azureImageAnalysisResponse";
    public static final String AZURE_MEDICAL_TEXT_ANALYSIS_RESPONSE = "azureMedicalTextAnalysisResponse";

    public static final Map<String, Channel> SCHEMA;

    static {
        SCHEMA = new HashMap<>();
        SCHEMA.put(USER_INPUT, new UpdateChannel(String.class));
        SCHEMA.put(IMAGE_PATH, new UpdateChannel(Path.class));
        SCHEMA.put(USER_DATA, new UpdateChannel(UserData.class));
        SCHEMA.put(SELECTED_AGENT_NAME, new UpdateChannel(String.class));
        SCHEMA.put(AGENT_RESPONSE, new UpdateChannel(String.class));
        SCHEMA.put(IS_IMAGE_PROCESSING, new UpdateChannel(Boolean.class));
        SCHEMA.put(AZURE_IMAGE_ANALYSIS_RESPONSE, new UpdateChannel(AzureImageAnalysisResponse.class));
        SCHEMA.put(AZURE_MEDICAL_TEXT_ANALYSIS_RESPONSE, new UpdateChannel(AzureMedicalTextAnalysisResponse.class));
    }


    public HealthiaAgentState(Map<String, Object> initData) {
        super(initData);
    }

    public HealthiaAgentState() {
        super(new HashMap<>());
    }

    public Optional<String> getUserInput() {
        return value(USER_INPUT);
    }

    public HealthiaAgentState setUserInput(String userInput) {
        this.data().put(USER_INPUT, userInput);
        return this;
    }

    public Optional<Path> getImagePath() {
        return value(IMAGE_PATH);
    }

    public HealthiaAgentState setImagePath(Path imagePath) {
        this.data().put(IMAGE_PATH, imagePath);
        return this;
    }

    public Optional<UserData> getUserData() {
        return value(USER_DATA);
    }

    public HealthiaAgentState setUserData(UserData userData) {
        this.data().put(USER_DATA, userData);
        return this;
    }

    public Optional<String> getSelectedAgentName() {
        return value(SELECTED_AGENT_NAME);
    }

    public HealthiaAgentState setSelectedAgentName(String selectedAgentName) {
        this.data().put(SELECTED_AGENT_NAME, selectedAgentName);
        return this;
    }

    public Optional<String> getAgentResponse() {
        return value(AGENT_RESPONSE);
    }

    public HealthiaAgentState setAgentResponse(String agentResponse) {
        this.data().put(AGENT_RESPONSE, agentResponse);
        return this;
    }

    public Optional<Boolean> isImageProcessing() { return value(IS_IMAGE_PROCESSING); }

    public HealthiaAgentState setIsImageProcessing(boolean isImageProcessing) {
        this.data().put(IS_IMAGE_PROCESSING, isImageProcessing);
        return this;
    }

    public Optional<AzureImageAnalysisResponse> getAzureImageAnalysisResponse() {
        return value(AZURE_IMAGE_ANALYSIS_RESPONSE);
    }

    public HealthiaAgentState setAzureImageAnalysisResponse(AzureImageAnalysisResponse response) {
        this.data().put(AZURE_IMAGE_ANALYSIS_RESPONSE, response);
        return this;
    }

    public Optional<AzureMedicalTextAnalysisResponse> getAzureMedicalTextAnalysisResponse() {
        return value(AZURE_MEDICAL_TEXT_ANALYSIS_RESPONSE);
    }

    public HealthiaAgentState setAzureMedicalTextAnalysisResponse(AzureMedicalTextAnalysisResponse response) {
        this.data().put(AZURE_MEDICAL_TEXT_ANALYSIS_RESPONSE, response);
        return this;
    }
} 