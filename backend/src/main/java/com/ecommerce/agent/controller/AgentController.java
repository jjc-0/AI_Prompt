package com.ecommerce.agent.controller;

import com.ecommerce.agent.agent.AgentDispatcher;
import com.ecommerce.agent.agent.ConversationManager;
import com.ecommerce.agent.config.AIConfig;
import com.ecommerce.agent.llm.MultiModelOrchestrator;
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
    private final MultiModelOrchestrator orchestrator;
    private final AIConfig aiConfig;
    private final PromptTemplateManager promptTemplateManager;
    private final DemoResponseService demoResponseService;
    private final RAGService ragService;
    private final KnowledgeDocumentRepository knowledgeDocRepo;
    private final ProductRepository productRepo;
    private final KnowledgeBaseLoader knowledgeBaseLoader;

    public AgentController(AgentDispatcher agentDispatcher,
                           ConversationManager conversationManager,
                           MultiModelOrchestrator orchestrator,
                           AIConfig aiConfig,
                           PromptTemplateManager promptTemplateManager,
                           DemoResponseService demoResponseService,
                           RAGService ragService,
                           KnowledgeDocumentRepository knowledgeDocRepo,
                           ProductRepository productRepo,
                           KnowledgeBaseLoader knowledgeBaseLoader) {
        this.agentDispatcher = agentDispatcher;
        this.conversationManager = conversationManager;
        this.orchestrator = orchestrator;
        this.aiConfig = aiConfig;
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
            .taskType("chat")
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
            String title = (String) s.get("title");
            if (opType == null || opType.isBlank()) {
                opType = inferType(title);
            }
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

    @PostMapping("/session/{sessionId}/auto-title")
    public ResponseEntity<Map<String, Object>> autoTitle(@PathVariable String sessionId) {
        List<ConversationRecord> records = conversationManager.getDBHistory(sessionId);
        if (records.isEmpty()) {
            return ResponseEntity.ok(Map.of("success", false, "title", "空对话"));
        }

        // Build conversation summary from first few messages
        StringBuilder sb = new StringBuilder();
        int max = Math.min(records.size(), 6);
        for (int i = 0; i < max; i++) {
            ConversationRecord r = records.get(i);
            String content = r.getContent();
            if (content != null) {
                int limit = Math.min(content.length(), 100);
                sb.append(r.getRole()).append(": ").append(content, 0, limit).append("\n");
            }
        }

        if (!aiConfig.isDeepSeekKeyConfigured()) {
            String fallback = sb.length() > 30 ? sb.substring(0, 30).replace('\n', ' ') : sb.toString().replace('\n', ' ');
            conversationManager.updateSessionTitle(sessionId, fallback);
            return ResponseEntity.ok(Map.of("success", true, "title", fallback, "note", "AI未配置，使用摘要"));
        }

        try {
            String systemPrompt = """
                    你是对话标题生成器。根据对话内容生成简短标题。
                    要求：10个汉字或20个英文字符以内，只返回标题，不要任何引号、标点或解释。
                    如果是翻译任务用"翻译: "开头，分析任务用"分析: "开头，文案任务用"文案: "开头。
                    """;
            String title = orchestrator.reasoning(systemPrompt,
                    "为以下对话生成标题:\n" + sb.toString()).get();
            if (title != null && !title.isBlank()) {
                title = title.trim().replaceAll("^[\"']|[\"']$", "");
                if (title.length() > 30) title = title.substring(0, 30);
                conversationManager.updateSessionTitle(sessionId, title);
                return ResponseEntity.ok(Map.of("success", true, "title", title));
            }
        } catch (Exception e) {
            log.warn("AI auto-title failed: {}", e.getMessage());
        }
        return ResponseEntity.ok(Map.of("success", false, "title", "自动命名失败"));
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

    /**
     * Infer operationType from session title for old records
     * where operationType was not stored (null in DB). New records
     * created after the fix already have the correct value.
     */
    private String inferType(String title) {
        if (title == null) return "chat";
        String t = title.toLowerCase();
        if (t.contains("inquiry") || t.contains("scoring") || t.contains("询盘")) return "inquiry";
        if (t.contains("translat") || t.contains("翻译"))               return "translate";
        if (t.contains("copywrit") || t.contains("文案") || t.contains("email")) return "copywriting";
        if (t.contains("analysis") || t.contains("分析") || t.contains("market")
                || t.contains("seo") || t.contains("competitor"))       return "analysis";
        if (t.contains("image") || t.contains("识图") || t.contains("recognition")) return "image-recognition";
        return "chat";
    }
}
