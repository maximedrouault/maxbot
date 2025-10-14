package org.maxbot.back.controller;

import lombok.RequiredArgsConstructor;
import org.maxbot.back.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;


    @GetMapping(value = "/chat", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Flux<String>> chatRequest(@RequestParam String userInput) {
        Flux<String> answer = chatService.chatRequest(userInput);

        return ResponseEntity.ok().body(answer);
    }
}