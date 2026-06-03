package com.ecommerce.agent.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class KnowledgeBaseLoader {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final ProductScraper productScraper;

    public KnowledgeBaseLoader(EmbeddingStore<TextSegment> embeddingStore,
                               EmbeddingModel embeddingModel,
                               ProductScraper productScraper) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.productScraper = productScraper;
    }

    @PostConstruct
    public void init() {
        doLoad();
    }

    /**
     * 强制重新加载知识库（含产品爬虫数据）
     */
    public void forceReload() {
        doLoad();
    }

    private void doLoad() {
        log.info("开始加载知识库文档到向量存储...");
        long start = System.currentTimeMillis();

        // 内置业务知识 + 官网产品数据
        List<Document> documents = new ArrayList<>();
        documents.addAll(KnowledgeDocuments.getAllDocuments());
        List<Document> productDocs = productScraper.toDocuments();
        if (!productDocs.isEmpty()) {
            log.info("已加载 {} 篇产品文档到知识库", productDocs.size());
        }
        documents.addAll(productDocs);

        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(documents);

        long elapsed = System.currentTimeMillis() - start;
        log.info("知识库加载完成，共 {} 篇文档（含产品），耗时 {}ms", documents.size(), elapsed);
    }
}
