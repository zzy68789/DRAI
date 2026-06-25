package com.zzy.drai.rag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class RagService {
    private final PdfTextExtractor pdfTextExtractor;
    private final VectorDocumentStore vectorDocumentStore;
    private final TextChunker textChunker;

    @Autowired
    public RagService(PdfTextExtractor pdfTextExtractor, VectorDocumentStore vectorDocumentStore) {
        this(pdfTextExtractor, vectorDocumentStore, new TextChunker(500, 50));
    }

    RagService(PdfTextExtractor pdfTextExtractor, VectorDocumentStore vectorDocumentStore, TextChunker textChunker) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.vectorDocumentStore = vectorDocumentStore;
        this.textChunker = textChunker;
    }

    public int process(List<MultipartFile> files) {
        clear();
        int stored = 0;
        List<RagDocumentChunk> chunks = new ArrayList<>();
        for (MultipartFile file : files) {
            List<RagDocumentChunk> fileChunks = processOne(file);
            chunks.addAll(fileChunks);
            stored += fileChunks.size();
        }
        vectorDocumentStore.add(chunks);
        return stored;
    }

    public List<RagDocument> retrieve(String query, int topK) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return vectorDocumentStore.query(query, topK);
    }

    public void clear() {
        vectorDocumentStore.clear();
    }

    private List<RagDocumentChunk> processOne(MultipartFile file) {
        if (file.isEmpty()) {
            return List.of();
        }
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("MVP 仅支持 PDF 文件");
        }
        try {
            String text = pdfTextExtractor.extract(file.getBytes());
            List<String> rawChunks = textChunker.chunk(text);
            List<RagDocumentChunk> chunks = new ArrayList<>(rawChunks.size());
            for (int i = 0; i < rawChunks.size(); i++) {
                chunks.add(new RagDocumentChunk(file.getOriginalFilename(), i, rawChunks.get(i)));
            }
            return chunks;
        } catch (IOException e) {
            throw new IllegalArgumentException("读取上传文件失败: " + e.getMessage(), e);
        }
    }
}
