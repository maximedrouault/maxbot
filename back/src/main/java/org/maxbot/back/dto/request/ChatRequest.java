package org.maxbot.back.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChatRequest(
        @NotNull(message = "Messages cannot be null")
        @NotEmpty(message = "Messages cannot be empty")
        List<Message> messages
) {
    public record Message(
            @NotNull(message = "Content list cannot be null")
            @NotEmpty(message = "Content list cannot be empty")
            @JsonProperty("content")
            List<Content> contents
    ) {
        public record Content(
                @NotBlank(message = "Text cannot be blank")
                @Size(min = 1, max = 10000, message = "Text must be between 1 and 10000 characters")
                String text
        ) {
        }
    }
}
