package com.healthia.java.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Bean
    public OpenAIClient openAIClient() {
        // Configures using the OPENAI_API_KEY environment variable by default
        // Add .organization() or .project() if needed and configured via env vars
        // Ensure OPENAI_API_KEY environment variable is set
        return OpenAIOkHttpClient.fromEnv();
    }
} 