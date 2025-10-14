package org.maxbot.back.controller;

import lombok.RequiredArgsConstructor;
import org.maxbot.back.service.EmbeddingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class EmbeddingController {

    private final EmbeddingService embeddingService;


    @PostMapping(value = "/embedding/upload")
    public ResponseEntity<String> pdfToVectorStore(@RequestParam MultipartFile file) {
        embeddingService.pdfToVectorStore(file);

        return ResponseEntity.ok().body("File uploaded successfully");
    }
}
