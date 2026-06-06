package com.ecommerce.agent.rag;

import com.ecommerce.agent.model.KnowledgeDocument;
import com.ecommerce.agent.repository.KnowledgeDocumentRepository;
import com.ecommerce.agent.repository.ProductRepository;
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
    private final KnowledgeDocumentRepository knowledgeDocRepo;
    private final ProductRepository productRepo;

    public KnowledgeBaseLoader(EmbeddingStore<TextSegment> embeddingStore,
                               EmbeddingModel embeddingModel,
                               ProductScraper productScraper,
                               KnowledgeDocumentRepository knowledgeDocRepo,
                               ProductRepository productRepo) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.productScraper = productScraper;
        this.knowledgeDocRepo = knowledgeDocRepo;
        this.productRepo = productRepo;
    }

    @PostConstruct
    public void init() {
        seedDefaultDocuments();
        doLoad();
    }

    /**
     * 首次启动时将硬编码文档写入 MySQL，之后从 MySQL 读取
     */
    public void seedDefaultDocuments() {
        if (knowledgeDocRepo.count() > 0) {
            log.info("知识库文档已存在于 MySQL，跳过初始化");
            return;
        }
        log.info("首次启动，初始化知识库文档到 MySQL...");
        List<Document> docs = KnowledgeDocuments.getAllDocuments();
        for (Document doc : docs) {
            String title = extractTitle(doc.text());
            String category = guessCategory(title);
            knowledgeDocRepo.save(KnowledgeDocument.builder()
                    .title(title)
                    .content(doc.text())
                    .category(category)
                    .enabled(true)
                    .build());
        }
        log.info("已写入 {} 篇文档到 MySQL", docs.size());
    }

    /**
     * 强制重新加载知识库（含产品数据）
     */
    public void forceReload() {
        doLoad();
    }

    private void doLoad() {
        log.info("开始加载知识库文档到向量存储...");
        long start = System.currentTimeMillis();

        List<Document> documents = new ArrayList<>();

        // 1. 从 MySQL 加载业务知识文档
        List<KnowledgeDocument> knowledgeDocs = knowledgeDocRepo.findByEnabledTrueOrderByTitleAsc();
        for (KnowledgeDocument kd : knowledgeDocs) {
            documents.add(Document.from(kd.getTitle() + "\n\n" + kd.getContent()));
            documents.get(documents.size() - 1).metadata().put("source", "knowledge_documents");
        }
        log.info("从 MySQL 加载 {} 篇知识文档", knowledgeDocs.size());

        // 2. 从 MySQL 加载产品数据
        List<com.ecommerce.agent.model.Product> products = productRepo.findByEnabledTrueOrderByNameAsc();
        for (com.ecommerce.agent.model.Product p : products) {
            String md = toProductMarkdown(p);
            documents.add(Document.from(md));
            documents.get(documents.size() - 1).metadata().put("source", "products");
        }
        log.info("从 MySQL 加载 {} 个产品", products.size());

        if (documents.isEmpty()) {
            log.warn("MySQL 中无数据，使用硬编码文档作为后备");
            documents.addAll(KnowledgeDocuments.getAllDocuments());
            documents.addAll(productScraper.toDocuments());
        }

        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(splitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(documents);

        long elapsed = System.currentTimeMillis() - start;
        log.info("知识库加载完成，共 {} 篇文档，耗时 {}ms", documents.size(), elapsed);
    }

    private String extractTitle(String text) {
        if (text == null || text.isBlank()) return "Untitled";
        String trimmed = text.trim();
        int newline = trimmed.indexOf('\n');
        String firstLine = newline > 0 ? trimmed.substring(0, newline) : trimmed.substring(0, Math.min(80, trimmed.length()));
        return firstLine.replaceAll("^#+\\s*", "").trim();
    }

    private String guessCategory(String title) {
        String t = title.toLowerCase();
        if (t.contains("公司")) return "company";
        if (t.contains("产品") || t.contains("规格")) return "product";
        if (t.contains("市场")) return "market";
        if (t.contains("法规") || t.contains("认证") || t.contains("合规")) return "compliance";
        if (t.contains("物流") || t.contains("运输")) return "logistics";
        if (t.contains("展会") || t.contains("展览")) return "trade_show";
        if (t.contains("术语")) return "terminology";
        if (t.contains("平台") || t.contains("b2b")) return "platform";
        if (t.contains("邮件") || t.contains("询盘")) return "email";
        if (t.contains("本地") || t.contains("化指南")) return "localization";
        return "general";
    }

    private String toProductMarkdown(com.ecommerce.agent.model.Product p) {
        StringBuilder sb = new StringBuilder();
        sb.append("## ").append(p.getName() != null ? p.getName() : "未知产品").append("\n\n");
        if (p.getSku() != null && !p.getSku().isBlank()) {
            sb.append("- **型号/SKU**: ").append(p.getSku()).append("\n");
        }
        if (p.getPrice() != null && !p.getPrice().isBlank()) {
            sb.append("- **价格**: ").append(p.getPrice()).append("\n");
        }
        if (p.getCategory() != null && !p.getCategory().isBlank()) {
            sb.append("- **分类**: ").append(p.getCategory()).append("\n");
        }
        if (p.getUrl() != null && !p.getUrl().isBlank()) {
            sb.append("- **产品链接**: ").append(p.getUrl()).append("\n");
        }
        if (p.getDescription() != null && !p.getDescription().isBlank()) {
            sb.append("\n**产品描述**:\n").append(p.getDescription()).append("\n");
        }
        sb.append("\n---\n");
        return sb.toString();
    }
}
