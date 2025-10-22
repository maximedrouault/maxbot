package org.maxbot.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final VectorStore vectorStore;


    public void pdfToVectorStore(MultipartFile file) {
        try {
            List<Document> documents = readPdfFile(file);
            List<Document> chunks = documentsToChunks(documents);
            chunksToVectorStore(chunks);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process PDF file to Vector store", e);
        }
    }

    public void deleteDocument(List<String> idList) {
        try {
            vectorStore.delete(idList);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete documents from Vector store", e);
        }
    }


    private List<Document> readPdfFile(MultipartFile file) {
        PagePdfDocumentReader pdfDocumentReader = new PagePdfDocumentReader(file.getResource());

        return pdfDocumentReader.read();
    }

    private List<Document> documentsToChunks(List<Document> documents) {
        TextSplitter textSplitter = new TokenTextSplitter();

        return textSplitter.apply(documents);
    }

    private void chunksToVectorStore(List<Document> chunks) {
        vectorStore.add(chunks);
    }
}
