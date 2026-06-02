package com.ecommerce.agent.controller;

import com.ecommerce.agent.agent.AgentDispatcher;
import com.ecommerce.agent.agent.ConversationManager;
import com.ecommerce.agent.model.AgentRequest;
import com.ecommerce.agent.model.AgentResponse;
import com.ecommerce.agent.service.DemoResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AgentDispatcher agentDispatcher;
    private final ConversationManager conversationManager;
    private final DemoResponseService demoResponseService;

    @PostMapping("/market")
    public ResponseEntity<Map<String, Object>> analyzeMarket(@RequestBody Map<String, Object> body) {
        String productName = (String) body.getOrDefault("productName", "");
        String targetCountry = (String) body.getOrDefault("targetCountry", "US");

        long start = System.currentTimeMillis();

        String message = String.format("""
            Analyze the B2B export market opportunity for the following product:
            Product: %s
            Target Market: %s
            
            Provide detailed analysis covering: market size, competitive landscape, pricing strategy,
            entry recommendations, regulatory requirements, and opportunity rating.
            """, productName, targetCountry);

        AgentRequest request = AgentRequest.builder()
            .message(message)
            .taskType("analysis")
            .parameters(Map.of("targetCountry", targetCountry))
            .enableTools(true)
            .build();

        AgentResponse response;
        try {
            response = agentDispatcher.dispatch(request);
        } catch (Exception e) {
            String fallback = demoResponseService.generateAnalysisDemo(productName, targetCountry);
            String sessionId = conversationManager.createSession("Analysis", "analysis");
            return ResponseEntity.ok(Map.of(
                "sessionId", sessionId,
                "result", fallback,
                "processingTimeMs", System.currentTimeMillis() - start
            ));
        }

        return ResponseEntity.ok(Map.of(
            "sessionId", response.getSessionId(),
            "result", response.getMessage(),
            "processingTimeMs", response.getProcessingTimeMs()
        ));
    }

    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> getTools() {
        Map<String, Object> tools = new LinkedHashMap<>();
        tools.put("market_data", Map.of("name", "Market Data", "description", "Real-time market insights", "enabled", true));
        tools.put("currency_convert", Map.of("name", "Currency Converter", "description", "Exchange rate lookup", "enabled", true));
        tools.put("competitor_analysis", Map.of("name", "Competitor Analysis", "description", "Competitive landscape", "enabled", true));
        tools.put("seo_keywords", Map.of("name", "SEO Keywords", "description", "Platform keyword optimization", "enabled", true));
        tools.put("web_scraper", Map.of("name", "Web Scraper", "description", "Market data collection", "enabled", false));
        return ResponseEntity.ok(tools);
    }
}
