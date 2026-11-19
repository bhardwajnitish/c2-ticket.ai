package com.example.rag.context;

import com.example.rag.retrieval.ScoredChunk;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RagContextBuilder {

    public String buildFromKeepSet(List<ScoredChunk> keepSet) {
        StringBuilder builder = new StringBuilder();
        for (ScoredChunk chunk : keepSet) {
            if (!builder.isEmpty()) {
                builder.append("\n\n");
            }
            builder.append(chunk.content());
        }
        return builder.toString();
    }
}