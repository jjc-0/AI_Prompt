package com.ecommerce.agent.controller;

import com.ecommerce.agent.agent.AgentDispatcher;
import com.ecommerce.agent.agent.ConversationManager;
import com.ecommerce.agent.llm.PromptTemplateManager;
import com.ecommerce.agent.model.*;
import com.ecommerce.agent.rag.KnowledgeBaseLoader;
import com.ecommerce.agent.rag.RAGService;
import com.ecommerce.agent.repository.KnowledgeDocumentRepository;
import com.ecommerce.agent.repository.ProductRepository;
import com.ecommerce.agent.service.DemoResponseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentDispatcher agentDispatcher;
    private final ConversationManager conversationManager;
    private final PromptTemplateManager promptTemplateManager;
    private final DemoResponseService demoResponseService;
    private final RAGService ragService;
    private final KnowledgeDocumentRepository knowledgeDocRepo;
    private final ProductRepository productRepo;
    private final KnowledgeBaseLoader knowledgeBaseLoader;

    public AgentController(AgentDispatcher agentDispatcher,
                           ConversationManager conversationManager,
                           PromptTemplateManager promptTemplateManager,
                           DemoResponseService demoResponseService,
                           RAGService ragService,
                           KnowledgeDocumentRepository knowledgeDocRepo,
                           ProductRepository productRepo,
                           KnowledgeBaseLoader knowledgeBaseLoader) {
        this.agentDispatcher = agentDispatcher;
        this.conversationManager = conversationManager;
        this.promptTemplateManager = promptTemplateManager;
        this.demoResponseService = demoResponseService;
        this.ragService = ragService;
        this.knowledgeDocRepo = knowledgeDocRepo;
        this.productRepo = productRepo;
        this.knowledgeBaseLoader = knowledgeBaseLoader;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        String sessionId = (String) body.getOrDefault("sessionId", null);
        String message = (String) body.getOrDefault("message", "");
        boolean enableTools = Boolean.TRUE.equals(body.get("enableTools"));

        AgentRequest request = AgentRequest.builder()
            .sessionId(sessionId)
            .message(message)
            .enableTools(enableTools)
            .build();

        AgentResponse response = agentDispatcher.dispatch(request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", response.getSessionId());
        result.put("message", response.getMessage());
        result.put("modelUsed", response.getModelUsed());
        result.put("processingTimeMs", response.getProcessingTimeMs());

        List<Map<String, Object>> tcList = new ArrayList<>();
        if (response.getToolCalls() != null) {
            for (var tc : response.getToolCalls()) {
                Map<String, Object> tcm = new LinkedHashMap<>();
                tcm.put("toolName", tc.getToolName());
                tcm.put("output", tc.getOutput());
                tcm.put("durationMs", tc.getDurationMs());
                tcList.add(tcm);
            }
        }
        result.put("toolCalls", tcList);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/chat/tools")
    public ResponseEntity<Map<String, Object>> chatWithTools(@RequestBody Map<String, Object> body) {
        body.put("enableTools", true);
        return chat(body);
    }

    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> getSessions(@RequestParam(required = false) String type) {
        List<Map<String, Object>> sessions;
        if (type != null && !type.isEmpty()) {
            sessions = conversationManager.getSessionList(type);
        } else {
            sessions = conversationManager.getSessionList();
        }

        List<Map<String, Object>> enriched = sessions.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>(s);
            String sid = (String) s.get("sessionId");
            String opType = (String) s.get("operationType");
            if (opType == null) opType = "agent";
            m.put("type", opType);
            m.put("modelUsed", "deepseek-chat");

            List<ConversationRecord> records = conversationManager.getDBHistory(sid);
            if (!records.isEmpty()) {
                ConversationRecord last = records.get(records.size() - 1);
                m.put("modelUsed", last.getModelUsed() != null ? last.getModelUsed() : "deepseek-chat");
            }
            return m;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("sessions", enriched));
    }

    @GetMapping("/session/{sessionId}/history")
    public ResponseEntity<Map<String, Object>> getHistory(@PathVariable String sessionId) {
        List<ConversationMessage> history = conversationManager.getHistory(sessionId);
        return ResponseEntity.ok(Map.of("records", history.stream().map(msg -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("role", msg.getRole());
            m.put("content", msg.getContent());
            m.put("toolName", msg.getToolName());
            m.put("toolResult", msg.getToolResult());
            m.put("timestamp", msg.getTimestamp());
            return m;
        }).collect(Collectors.toList())));
    }

    @GetMapping("/session/{sessionId}/history/db")
    public ResponseEntity<Map<String, Object>> getDBHistory(@PathVariable String sessionId) {
        List<ConversationRecord> records = conversationManager.getDBHistory(sessionId);
        return ResponseEntity.ok(Map.of("records", records.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("role", r.getRole());
            m.put("content", r.getContent());
            m.put("toolName", r.getToolName());
            m.put("toolResult", r.getToolResult());
            m.put("processingTimeMs", r.getProcessingTimeMs());
            m.put("modelUsed", r.getModelUsed());
            m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList())));
    }

    @PutMapping("/session/{sessionId}/title")
    public ResponseEntity<Map<String, Object>> updateTitle(@PathVariable String sessionId,
                                                            @RequestBody Map<String, String> body) {
        conversationManager.updateSessionTitle(sessionId, body.get("title"));
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/session/{sessionId}/clear")
    public ResponseEntity<Map<String, Object>> clearSession(@PathVariable String sessionId) {
        conversationManager.clearSession(sessionId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/knowledge/search")
    public ResponseEntity<Map<String, Object>> searchKnowledge(@RequestBody Map<String, Object> body) {
        String query = (String) body.getOrDefault("query", "");
        int maxResults = body.containsKey("maxResults") ? ((Number) body.get("maxResults")).intValue() : 5;
        String context = ragService.retrieveContext(query, maxResults);
        if (context == null || context.isBlank()) {
            return ResponseEntity.ok(Map.of(
                "query", query,
                "results", List.of(),
                "totalFound", 0
            ));
        }
        String[] segments = context.split("\n\n---\n\n");
        List<Map<String, Object>> results = new ArrayList<>();
        for (String seg : segments) {
            results.add(Map.of("snippet", seg.substring(0, Math.min(200, seg.length())) + "...", "fullText", seg));
        }
        return ResponseEntity.ok(Map.of(
            "query", query,
            "results", results,
            "totalFound", results.size()
        ));
    }

    @GetMapping("/knowledge/status")
    public ResponseEntity<Map<String, Object>> knowledgeStatus() {
        boolean available = ragService.isAvailable();
        long docCount = knowledgeDocRepo.count();
        long productCount = productRepo.count();
        return ResponseEntity.ok(Map.of(
            "enabled", available,
            "storeType", "MySQL（持久化存储）",
            "knowledgeDocumentCount", docCount,
            "productCount", productCount,
            "dataSource", "MySQL → knowledge_documents + products 表",
            "topics", knowledgeDocRepo.findByEnabledTrueOrderByTitleAsc()
                    .stream().map(KnowledgeDocument::getTitle).collect(Collectors.toList())
        ));
    }

    /**
     * 查看所有 RAG 知识库文档内容（从 MySQL）
     */
    @GetMapping("/knowledge/documents")
    public ResponseEntity<Map<String, Object>> listDocuments() {
        List<KnowledgeDocument> docs = knowledgeDocRepo.findByEnabledTrueOrderByTitleAsc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (KnowledgeDocument kd : docs) {
            result.add(Map.of(
                "id", kd.getId(),
                "title", kd.getTitle(),
                "category", kd.getCategory() != null ? kd.getCategory() : "",
                "content", kd.getContent(),
                "length", kd.getContent().length(),
                "enabled", kd.isEnabled(),
                "createdAt", kd.getCreatedAt() != null ? kd.getCreatedAt().toString() : "",
                "updatedAt", kd.getUpdatedAt() != null ? kd.getUpdatedAt().toString() : ""
            ));
        }
        return ResponseEntity.ok(Map.of(
            "total", result.size(),
            "source", "MySQL",
            "documents", result
        ));
    }

    /**
     * 查询 MySQL 中的产品列表（分页）
     */
    @GetMapping("/knowledge/products")
    public ResponseEntity<Map<String, Object>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageResult = productRepo.findAll(
                org.springframework.data.domain.PageRequest.of(page, size,
                        org.springframework.data.domain.Sort.by("createdAt").descending()));
        List<Map<String, Object>> items = new ArrayList<>();
        for (com.ecommerce.agent.model.Product p : pageResult.getContent()) {
            items.add(Map.of(
                "id", p.getId(),
                "name", p.getName(),
                "url", p.getUrl() != null ? p.getUrl() : "",
                "imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "",
                "price", p.getPrice() != null ? p.getPrice() : "",
                "sku", p.getSku() != null ? p.getSku() : "",
                "description", p.getDescription() != null
                        ? p.getDescription().substring(0, Math.min(300, p.getDescription().length())) : "",
                "category", p.getCategory() != null ? p.getCategory() : "",
                "createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : ""
            ));
        }
        return ResponseEntity.ok(Map.of(
            "total", pageResult.getTotalElements(),
            "page", page,
            "size", size,
            "totalPages", pageResult.getTotalPages(),
            "items", items
        ));
    }

    /**
     * 重新加载知识库（从 MySQL 重新构建向量索引）
     */
    @PostMapping("/knowledge/reload")
    public ResponseEntity<Map<String, Object>> reloadKnowledge() {
        knowledgeBaseLoader.forceReload();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "已从 MySQL 重新加载知识库到向量存储"
        ));
    }
}
