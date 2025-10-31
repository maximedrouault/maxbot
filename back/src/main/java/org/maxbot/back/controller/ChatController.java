package org.maxbot.back.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.maxbot.back.dto.request.ChatRequest;
import org.maxbot.back.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@RateLimiter(name = "globalRateLimiter")
public class ChatController {

    private final ChatService chatService;


    @PostMapping("/chat")
    public ResponseEntity<Flux<String>> chatRequest(@Valid @RequestBody ChatRequest request) {
        Flux<String> answer = chatService.chatRequest(request);

        return ResponseEntity.ok().body(answer);
    }
}