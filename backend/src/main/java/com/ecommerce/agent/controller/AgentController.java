package com.ecommerce.agent.controller;

import com.ecommerce.agent.agent.AgentDispatcher;
import com.ecommerce.agent.agent.ConversationManager;
import com.ecommerce.agent.model.AgentRequest;
import com.ecommerce.agent.model.AgentResponse;
import com.ecommerce.agent.model.ConversationRecord;
import com.ecommerce.agent.rag.RAGService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentDispatcher agentDispatcher;
    private final ConversationManager conversationManager;
    private final RAGService ragService;

    public AgentController(AgentDispatcher agentDispatcher,
                           ConversationManager conversationManager,
                           RAGService ragService) {
        this.agentDispatcher = agentDispatcher;
        this.conversationManager = conversationManager;
        this.ragService = ragService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentResponse> chat(@Valid @RequestBody AgentRequest request) {
        AgentResponse response = agentDispatcher.dispatch(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat/tools")
    public ResponseEntity<AgentResponse> chatWithTools(@Valid @RequestBody AgentRequest request) {
        request.setEnableTools(true);
        AgentResponse response = agentDispatcher.dispatch(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/knowledge/search")
    public ResponseEntity<Map<String, Object>> searchKnowledge(@RequestBody Map<String, String> body) {
        String query = body.get("query");
        int maxResults = body.containsKey("maxResults")
                ? Integer.parseInt(body.get("maxResults"))
                : 5;
        String context = ragService.retrieveContext(query, maxResults);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "query", query,
                "resultCount", context != null ? context.split("\n\n---\n\n").length : 0,
                "context", context != null ? context : "未找到相关内容"
        ));
    }

    @GetMapping("/knowledge/status")
    public ResponseEntity<Map<String, Object>> knowledgeStatus() {
        return ResponseEntity.ok(Map.of(
                "available", ragService.isAvailable(),
                "model", "all-MiniLM-L6-v2 (local)",
                "store", "InMemory"
        ));
    }

    @GetMapping("/session/{sessionId}/history")
    public ResponseEntity<Map<String, Object>> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "messages", conversationManager.getHistory(sessionId),
                "contextSummary", conversationManager.getContextSummary(sessionId)
        ));
    }

    @GetMapping("/session/{sessionId}/history/db")
    public ResponseEntity<Map<String, Object>> getDBHistory(@PathVariable String sessionId) {
        List<ConversationRecord> records = conversationManager.getDBHistory(sessionId);
        return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "total", records.size(),
                "records", records
        ));
    }

    @GetMapping("/sessions")
    public ResponseEntity<Map<String, Object>> getSessionList(
            @RequestParam(required = false) String type) {
        List<Map<String, Object>> sessions;
        if (type != null && !type.isBlank()) {
            sessions = conversationManager.getSessionList(type);
        } else {
            sessions = conversationManager.getSessionList();
        }
        return ResponseEntity.ok(Map.of(
                "total", sessions.size(),
                "sessions", sessions,
                "filterType", type != null ? type : "all"
        ));
    }

    @PutMapping("/session/{sessionId}/title")
    public ResponseEntity<Map<String, String>> updateSessionTitle(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {
        String title = body.get("title");
        conversationManager.updateSessionTitle(sessionId, title);
        return ResponseEntity.ok(Map.of("sessionId", sessionId, "title", title));
    }

    @PostMapping("/session/{sessionId}/clear")
    public ResponseEntity<Map<String, String>> clearSession(@PathVariable String sessionId) {
        conversationManager.clearSession(sessionId);
        return ResponseEntity.ok(Map.of("sessionId", sessionId, "status", "cleared"));
    }
}
