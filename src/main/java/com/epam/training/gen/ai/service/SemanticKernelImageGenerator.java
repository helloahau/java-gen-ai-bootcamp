package com.epam.training.gen.ai.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class SemanticKernelImageGenerator {
    @Value("${client-azureopenai-api-url}")
    private String API_URL;  // Replace with the actual API URL
    // possible to use Stability AI(stability.stable-diffusion-xl, stability.sd3-large-v1, stability.stable-image-ultra-v1), OpenAI(dall-e-3)

    @Value("${client-azureopenai-key}")
    private String API_KEY;  // Replace with your actual API key


    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    private final ObjectMapper objectMapper;

    public SemanticKernelImageGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String generateImage(String prompt, String model) throws IOException, InterruptedException {
        var requestBody = new HashMap<>();
        requestBody.put("messages", new Object[]{Map.of("role", "user", "content", prompt)});
        requestBody.put("max_tokens", 1000);

        var requestJson = objectMapper.writeValueAsString(requestBody);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(API_URL, model)))
                .header("Content-Type", "application/json")
                .header("Api-Key", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            var jsonResponse = objectMapper.readTree(response.body());
            return jsonResponse.at("/choices/0/message/custom_content/attachments/1/url").asText();
        } else {
            throw new RuntimeException("Failed to generate image: " + response.body());
        }
    }
}
