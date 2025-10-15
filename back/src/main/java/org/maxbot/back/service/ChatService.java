package org.maxbot.back.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
@SessionScope
public class ChatService {

    @Value("${PROMPT_SYSTEM_TEMPLATE}")
    private String promptSystemTemplate;

    private final ChatClient chatClient;
    private final UUID conversationId;

    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .build();
        this.conversationId = UUID.randomUUID();
    }


    public Flux<String> chatRequest(String userInput) {
        return this.chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .system(promptSystemTemplate)
                .user(userInput)
                .stream()
                .content();
    }
}