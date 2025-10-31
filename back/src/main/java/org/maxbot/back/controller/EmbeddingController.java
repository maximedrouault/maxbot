package org.maxbot.back.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.maxbot.back.service.EmbeddingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/embedding")
@RateLimiter(name = "globalRateLimiter")
public class EmbeddingController {

    private final EmbeddingService embeddingService;


    @PostMapping("/uploadPdf")
    public ResponseEntity<Void> pdfToVectorStore(@RequestParam MultipartFile file) {
        embeddingService.pdfToVectorStore(file);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/deleteByIds")
    public ResponseEntity<Void> deleteByIds(@RequestBody List<String> idList) {
        embeddingService.deleteDocumentByIds(idList);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/deleteByFileName")
    public ResponseEntity<Void> deleteByFileName(@RequestParam String fileName) {
        embeddingService.deleteDocumentByFileName(fileName);

        return ResponseEntity.noContent().build();
    }
}
