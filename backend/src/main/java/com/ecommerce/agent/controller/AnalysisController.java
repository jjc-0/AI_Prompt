package com.ecommerce.agent.controller;

import com.ecommerce.agent.agent.ConversationManager;
import com.ecommerce.agent.service.AnalysisService;
import com.ecommerce.agent.tool.ToolRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final ToolRegistry toolRegistry;
    private final ConversationManager conversationManager;

    public AnalysisController(AnalysisService analysisService, ToolRegistry toolRegistry,
                               ConversationManager conversationManager) {
        this.analysisService = analysisService;
        this.toolRegistry = toolRegistry;
        this.conversationManager = conversationManager;
    }

    @PostMapping("/market")
    public ResponseEntity<Map<String, Object>> analyzeMarket(@RequestBody Map<String, String> request) {
        long start = System.currentTimeMillis();

        String productName = request.get("productName");
        String sessionId = conversationManager.createSession(
                "Analysis: " + productName + " → " + request.get("targetCountry"), "analysis");
        String userMsg = "分析产品: " + productName
                + "\n目标市场: " + request.get("targetCountry")
                + "\n品类: " + request.getOrDefault("category", "")
                + "\n价格: " + request.getOrDefault("priceRange", "");
        conversationManager.addMessage(sessionId, "user", userMsg);

        String result = analysisService.analyzeMarket(
                productName,
                request.get("productDescription"),
                request.get("priceRange"),
                request.get("category"),
                request.get("targetCountry")
        ).join();
        long elapsed = System.currentTimeMillis() - start;

        conversationManager.addMessage(sessionId, "assistant", result, "deepseek-reasoning", elapsed);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "result", result,
                "processingTimeMs", elapsed,
                "model", "deepseek-reasoning",
                "sessionId", sessionId
        ));
    }

    @GetMapping("/tools")
    public ResponseEntity<List<Map<String, Object>>> getAvailableTools() {
        return ResponseEntity.ok(toolRegistry.getToolDefinitionsForLLM());
    }
}
