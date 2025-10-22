package org.maxbot.back.controller;

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
public class EmbeddingController {

    private final EmbeddingService embeddingService;


    @PostMapping("/uploadPdf")
    public ResponseEntity<String> pdfToVectorStore(@RequestParam MultipartFile file) {
        embeddingService.pdfToVectorStore(file);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/deleteByIds")
    public ResponseEntity<Void> deleteByIds(@RequestBody List<String> idList) {
        embeddingService.deleteDocument(idList);

        return ResponseEntity.noContent().build();
    }
}
