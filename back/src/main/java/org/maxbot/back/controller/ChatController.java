package org.maxbot.back.controller;

import lombok.RequiredArgsConstructor;
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
public class ChatController {

    private final ChatService chatService;


    @PostMapping("/chat")
    public ResponseEntity<Flux<String>> chatRequest(@RequestBody String userInput) {
        Flux<String> answer = chatService.chatRequest(userInput);

        return ResponseEntity.ok().body(answer);
    }
}