package com.ecommerce.agent.rag;

import com.ecommerce.agent.config.AIConfig;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RAGService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final AIConfig aiConfig;

    public RAGService(EmbeddingStore<TextSegment> embeddingStore,
                      EmbeddingModel embeddingModel,
                      AIConfig aiConfig) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.aiConfig = aiConfig;
    }

    public String augmentPrompt(String userQuery) {
        if (!aiConfig.getRag().isAugmentPrompt()) {
            return null;
        }

        String context = retrieveContext(userQuery);
        if (context == null || context.isBlank()) {
            return null;
        }

        return String.format("""
                以下是与用户问题相关的知识库内容，请在回答时参考这些信息：
                
                ---知识库内容---
                %s
                ---知识库内容结束---
                
                用户问题: %s
                """, context, userQuery);
    }

    public String retrieveContext(String query) {
        return retrieveContext(query, aiConfig.getRag().getMaxResults());
    }

    public String retrieveContext(String query, int maxResults) {
        try {
            Embedding queryEmbedding = embeddingModel.embed(query).content();
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(aiConfig.getRag().getMinScore())
                    .build();
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(searchRequest).matches();

            if (matches.isEmpty()) {
                log.debug("RAG检索: 未找到相关文档片段, query={}", query);
                return null;
            }

            String context = matches.stream()
                    .map(m -> m.embedded().text())
                    .collect(Collectors.joining("\n\n---\n\n"));

            log.debug("RAG检索: {} 条结果, query={}, maxScore={}",
                    matches.size(), truncate(query, 50),
                    matches.get(0).score());

            return context;
        } catch (Exception e) {
            log.warn("RAG检索失败: {}", e.getMessage());
            return null;
        }
    }

    public boolean isAvailable() {
        try {
            embeddingModel.embed("test");
            return true;
        } catch (Exception e) {
            log.warn("RAG嵌入模型不可用: {}", e.getMessage());
            return false;
        }
    }

    public String buildAugmentedSystemPrompt(String baseSystemPrompt, String query) {
        String context = retrieveContext(query);
        if (context == null || context.isBlank()) {
            return baseSystemPrompt;
        }

        return baseSystemPrompt + "\n\n## 参考知识库内容\n" + context
                + "\n\n请参考以上知识库内容，提供更准确、专业的回复。";
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "null";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
