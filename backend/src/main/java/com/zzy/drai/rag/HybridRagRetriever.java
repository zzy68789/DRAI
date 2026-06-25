package com.zzy.drai.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class HybridRagRetriever {
    private static final double BM25_K1 = 1.5d;
    private static final double BM25_B = 0.75d;

    private final VectorDocumentStore vectorDocumentStore;
    private final double relevanceThreshold;
    private final List<RagDocumentChunk> lexicalChunks = new CopyOnWriteArrayList<>();

    public HybridRagRetriever(
            VectorDocumentStore vectorDocumentStore,
            @Value("${drai.rag.relevance-threshold:0.2}") double relevanceThreshold
    ) {
        this.vectorDocumentStore = vectorDocumentStore;
        this.relevanceThreshold = relevanceThreshold;
    }

    public void index(List<RagDocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }
        lexicalChunks.addAll(chunks);
        vectorDocumentStore.add(chunks);
    }

    public List<RagDocument> retrieve(String query, int topK) {
        if (query == null || query.isBlank() || topK <= 0) {
            return List.of();
        }
        int candidateLimit = Math.max(topK * 2, topK);
        return merge(bm25(query, candidateLimit), vectorDocumentStore.query(query, candidateLimit), topK);
    }

    public void clear() {
        lexicalChunks.clear();
        vectorDocumentStore.clear();
    }

    private List<RagDocument> bm25(String query, int topK) {
        List<String> queryTerms = tokenize(query);
        if (queryTerms.isEmpty() || lexicalChunks.isEmpty()) {
            return List.of();
        }
        Map<String, Integer> documentFrequency = documentFrequency(queryTerms);
        double averageLength = lexicalChunks.stream()
                .mapToInt(chunk -> Math.max(1, tokenize(chunk.content()).size()))
                .average()
                .orElse(1.0d);

        List<ScoredChunk> scoredChunks = new ArrayList<>();
        for (RagDocumentChunk chunk : lexicalChunks) {
            double score = bm25Score(queryTerms, tokenize(chunk.content()), documentFrequency, averageLength);
            if (score > 0) {
                scoredChunks.add(new ScoredChunk(chunk, score));
            }
        }
        double maxScore = scoredChunks.stream().mapToDouble(ScoredChunk::score).max().orElse(1.0d);
        return scoredChunks.stream()
                .map(scored -> new RagDocument(scored.chunk().source(), scored.chunk().content(), round(scored.score() / maxScore)))
                .filter(doc -> doc.score() >= relevanceThreshold)
                .sorted(Comparator.comparingDouble(RagDocument::score).reversed())
                .limit(topK)
                .toList();
    }

    private double bm25Score(
            List<String> queryTerms,
            List<String> documentTerms,
            Map<String, Integer> documentFrequency,
            double averageLength
    ) {
        Map<String, Long> termFrequency = new HashMap<>();
        for (String term : documentTerms) {
            termFrequency.merge(term, 1L, Long::sum);
        }
        double score = 0.0d;
        int documentCount = lexicalChunks.size();
        int documentLength = Math.max(1, documentTerms.size());
        for (String queryTerm : queryTerms) {
            long frequency = termFrequency.getOrDefault(queryTerm, 0L);
            if (frequency == 0) {
                continue;
            }
            int df = documentFrequency.getOrDefault(queryTerm, 0);
            double idf = Math.log(1 + (documentCount - df + 0.5d) / (df + 0.5d));
            double denominator = frequency + BM25_K1 * (1 - BM25_B + BM25_B * documentLength / averageLength);
            score += idf * (frequency * (BM25_K1 + 1)) / denominator;
        }
        return score;
    }

    private Map<String, Integer> documentFrequency(List<String> queryTerms) {
        Map<String, Integer> df = new HashMap<>();
        Set<String> uniqueQueryTerms = new HashSet<>(queryTerms);
        for (RagDocumentChunk chunk : lexicalChunks) {
            Set<String> documentTerms = new HashSet<>(tokenize(chunk.content()));
            for (String queryTerm : uniqueQueryTerms) {
                if (documentTerms.contains(queryTerm)) {
                    df.merge(queryTerm, 1, Integer::sum);
                }
            }
        }
        return df;
    }

    private List<RagDocument> merge(List<RagDocument> bm25Results, List<RagDocument> vectorResults, int topK) {
        Map<String, RagDocument> merged = new LinkedHashMap<>();
        for (RagDocument doc : bm25Results) {
            putBest(merged, doc);
        }
        for (RagDocument doc : vectorResults) {
            if (doc.score() >= relevanceThreshold) {
                putBest(merged, doc);
            }
        }
        return merged.values().stream()
                .sorted(Comparator.comparingDouble(RagDocument::score).reversed())
                .limit(topK)
                .toList();
    }

    private void putBest(Map<String, RagDocument> merged, RagDocument doc) {
        String key = normalizeKey(doc);
        RagDocument existing = merged.get(key);
        if (existing == null || doc.score() > existing.score()) {
            merged.put(key, doc);
        }
    }

    private String normalizeKey(RagDocument doc) {
        return doc.source().toLowerCase(Locale.ROOT).trim()
                + "::"
                + doc.content().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String[] pieces = text.toLowerCase(Locale.ROOT).split("[\\s,，。；;：:、()（）\\[\\]{}<>《》\"'`]+");
        List<String> terms = new ArrayList<>();
        for (String piece : pieces) {
            if (!piece.isBlank()) {
                terms.add(piece);
            }
        }
        return terms;
    }

    private double round(double value) {
        return Math.round(value * 1_000_000d) / 1_000_000d;
    }

    private record ScoredChunk(RagDocumentChunk chunk, double score) {
    }
}
