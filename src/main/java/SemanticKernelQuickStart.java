import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.google.gson.Gson;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


public class SemanticKernelQuickStart {
    public static Map<String, String> loadEnv() {
        Map<String, String> envMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) continue; // Skip empty lines and comments
                String[] parts = line.split("=", 2);
                if (parts.length < 2) continue; // Skip lines that don't contain key=value pairs
                String key = parts[0].trim();
                String value = parts[1].trim();
                envMap.put(key, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return envMap;
    }

    public static void main(String[] args) {
        Map<String, String> myEnvVariables = loadEnv();
        String AZURE_CLIENT_KEY = myEnvVariables.get("AZURE_OPEN_AI_KEY");

        String CLIENT_ENDPOINT = myEnvVariables.get("AZURE_OPEN_AI_ENDPOINT");

        String MODEL_ID = myEnvVariables.get("AZURE_OPEN_AI_DEPLOYMENT_NAME");


        OpenAIAsyncClient client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(AZURE_CLIENT_KEY))
                .endpoint(CLIENT_ENDPOINT)
                .buildAsyncClient();

// Import the LightsPlugin
        KernelPlugin lightPlugin = KernelPluginFactory.createFromObject(new LightsPlugin(),
                "LightsPlugin");

// Create your AI service client
        ChatCompletionService chatCompletionService = OpenAIChatCompletion.builder()
                .withModelId(MODEL_ID)
                .withOpenAIAsyncClient(client)
                .build();

// Create a kernel with Azure OpenAI chat completion and plugin
        Kernel kernel = Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(lightPlugin)
                .build();

// Add a converter to the kernel to show it how to serialise LightModel objects into a prompt
        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(LightModel.class)
                                .toPromptString(new Gson()::toJson)
                                .build());

// Enable planning
        InvocationContext invocationContext = new InvocationContext.Builder()
                .withReturnMode(InvocationReturnMode.LAST_MESSAGE_ONLY)
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .build();

// Create a history to store the conversation
        ChatHistory history = new ChatHistory();

// Initiate a back-and-forth chat
        Scanner scanner = new Scanner(System.in);
        String userInput;
        do {
            // Collect user input
            System.out.print("User > ");

            userInput = scanner.nextLine();
            // Add user input
            history.addUserMessage(userInput);

            // Prompt AI for response to users input
            List<ChatMessageContent<?>> results = chatCompletionService
                    .getChatMessageContentsAsync(history, kernel, invocationContext)
                    .block();

            for (ChatMessageContent<?> result : results) {
                // Print the results
                if (result.getAuthorRole() == AuthorRole.ASSISTANT && result.getContent() != null) {
                    System.out.println("Assistant > " + result);
                }
                // Add the message from the agent to the chat history
                history.addMessage(result);
            }
        } while (userInput != null && !userInput.isEmpty());
    }
}
