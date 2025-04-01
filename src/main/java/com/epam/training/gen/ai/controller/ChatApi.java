package com.epam.training.gen.ai.controller;

import com.epam.training.gen.ai.history.SimpleKernelHistory;
import com.epam.training.gen.ai.service.SemanticKernelImageGenerator;
import com.microsoft.semantickernel.Kernel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ChatApi {

    @Autowired
    private SimpleKernelHistory simpleKernelHistory;

    @Autowired
    private SemanticKernelImageGenerator semanticKernelImageGenerator;


    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) throws IOException, InterruptedException {
        Map<String, String> jsonResponse = new HashMap<>();
        if (request.get("model") != null && request.get("model").toLowerCase().startsWith("dall")) {
            String response = semanticKernelImageGenerator.generateImage(request.get("message"), request.get("model"));
            jsonResponse.put("response", response);
        } else {
            String response = simpleKernelHistory.processWithHistory(request);
            jsonResponse.put("response", response);
        }
        return jsonResponse;
    }
}