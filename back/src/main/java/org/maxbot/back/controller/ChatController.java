package org.maxbot.back.controller;

import lombok.RequiredArgsConstructor;
import org.maxbot.back.dto.response.ChatResponse;
import org.maxbot.back.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;


    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chatRequest(@RequestBody String userInput) {
        ChatResponse answer = chatService.chatRequest(userInput);

        return ResponseEntity.ok().body(answer);
    }
}