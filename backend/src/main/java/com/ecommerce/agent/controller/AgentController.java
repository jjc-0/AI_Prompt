package com.ecommerce.agent.controller;

import com.ecommerce.agent.agent.AgentDispatcher;
import com.ecommerce.agent.agent.ConversationManager;
import com.ecommerce.agent.llm.PromptTemplateManager;
import com.ecommerce.agent.model.*;
import com.ecommerce.agent.service.DemoResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentDispatcher agentDispatcher;
    private final ConversationManager conversationManager;
    private final PromptTemplateManager promptTemplateManager;
    private final DemoResponseService demoResponseService;

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
        return ResponseEntity.ok(Map.of(
            "query", query,
            "results", List.of(Map.of("title", "Display Stand Export Guide", "snippet", "Comprehensive export guide for POP displays.", "score", 0.92)),
            "totalFound", 1
        ));
    }

    @GetMapping("/knowledge/status")
    public ResponseEntity<Map<String, Object>> knowledgeStatus() {
        return ResponseEntity.ok(Map.of(
            "enabled", true,
            "documentCount", 12,
            "indexSize", "3.2 MB",
            "lastUpdated", "2026-06-01"
        ));
    }
}
