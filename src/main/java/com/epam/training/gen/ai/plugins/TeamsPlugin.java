package com.epam.training.gen.ai.plugins;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;


import java.time.format.DateTimeFormatter;

@Slf4j
public class TeamsPlugin {

    @DefineKernelFunction(
            name = "send_message",
            description = "Get the current time")
    public String send_message(
            @KernelFunctionParameter(
                    name = "message",
                    description = "the message to send",
                    required = true)
            String message) {
        log.info("Sending message: " + message);
        return "Message sent: " + message;
    }
}