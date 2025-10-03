package org.maxbot.back.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Value("${SYSTEM_TEMPLATE}")
    private String systemTemplate;


    @GetMapping("/chat")
    public String chat(@RequestBody String userInput) {

        return this.chatClient.prompt()
                .system(systemTemplate)
                .user(userInput)
                .call()
                .content();
    }
}
