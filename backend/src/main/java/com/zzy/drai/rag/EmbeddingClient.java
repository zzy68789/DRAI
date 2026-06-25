package com.zzy.drai.rag;

import java.util.List;

public interface EmbeddingClient {
    List<Double> embed(String text);
}
