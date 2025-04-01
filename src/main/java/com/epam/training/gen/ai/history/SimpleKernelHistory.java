package com.epam.training.gen.ai.history;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service class for interacting with the AI kernel, maintaining chat history.
 * <p>
 * This service provides a method to process user prompts while preserving chat history.
 * It uses the {@link Kernel} to invoke AI responses based on the user's input and the
 * previous chat context. The conversation history is updated after each interaction.
 */
@Slf4j
@Service
@AllArgsConstructor
public class SimpleKernelHistory {

    private final Kernel kernel;

    public String processWithHistory(Map<String, String> request) {

        var chatHistory = new ChatHistory();

        var response = kernel.invokeAsync(getChat())
                .withArguments(getKernelFunctionArguments(request.get("message"), chatHistory))
                .withPromptExecutionSettings(PromptExecutionSettings.builder()
                        .withTemperature(request.get("temperature") != null ? Double.parseDouble(request.get("temperature")) : 1)
                        .build())
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .block();
        var result = response.getResult();
        chatHistory.addUserMessage(request.get("message"));
        chatHistory.addAssistantMessage(result);
        log.info("AI answer:" + result);
        if (result.isEmpty()) {
            result = "Error: Empty response received from OpenAI.";
        }
        return result;
    }

    /**
     * Creates a kernel function for generating a chat response using a predefined prompt template.
     * <p>
     * The template includes the chat history and the user's message as variables.
     *
     * @return a {@link KernelFunction} for handling chat-based AI interactions
     */
    private KernelFunction<String> getChat() {
        return KernelFunction.<String>createFromPrompt("""
                        {{$chatHistory}}
                        <message role="user">{{$request}}</message>""")
                .build();
    }

    /**
     * Creates the kernel function arguments with the user prompt and chat history.
     *
     * @param prompt the user's input
     * @param chatHistory the current chat history
     * @return a {@link KernelFunctionArguments} instance containing the variables for the AI model
     */
    private KernelFunctionArguments getKernelFunctionArguments(String prompt, ChatHistory chatHistory) {
        return KernelFunctionArguments.builder()
                .withVariable("request", prompt)
                .withVariable("chatHistory", chatHistory)
                .build();
    }
}
