package org.maxbot.back.service;

import org.maxbot.back.dto.request.ChatRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    @Value("${ai.system-prompt}")
    private String aiSystemPrompt;

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .build();
    }

    public Flux<String> chatRequest(ChatRequest request) {
        return chatClient.prompt()
                .system(aiSystemPrompt)
                .user(request.toString())
                .stream()
                .content();
    }
}