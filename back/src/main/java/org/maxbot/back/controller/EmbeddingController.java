package org.maxbot.back.controller;

import lombok.RequiredArgsConstructor;
import org.maxbot.back.service.EmbeddingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EmbeddingController {

    private final EmbeddingService embeddingService;


    @PostMapping("/embedding/upload")
    public ResponseEntity<String> pdfToVectorStore(@RequestParam MultipartFile file) {
        embeddingService.pdfToVectorStore(file);

        return ResponseEntity.ok().body("File uploaded successfully");
    }

}
