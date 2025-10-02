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
    private String SYSTEM_TEMPLATE;


    @GetMapping("/chat")
    public Object chat(@RequestBody String userInput) {

        return this.chatClient.prompt()
                .system(SYSTEM_TEMPLATE)
                .user(userInput)
                .call()
                .content();
    }
}
