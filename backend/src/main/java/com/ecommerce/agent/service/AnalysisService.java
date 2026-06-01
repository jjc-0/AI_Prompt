package com.ecommerce.agent.service;

import com.ecommerce.agent.config.AIConfig;
import com.ecommerce.agent.llm.MultiModelOrchestrator;
import com.ecommerce.agent.llm.PromptTemplateManager;
import com.ecommerce.agent.rag.RAGService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AnalysisService {

    private final PromptTemplateManager promptManager;
    private final MultiModelOrchestrator orchestrator;
    private final AIConfig aiConfig;
    private final DemoResponseService demoService;
    private final RAGService ragService;

    public AnalysisService(PromptTemplateManager promptManager, MultiModelOrchestrator orchestrator,
                           AIConfig aiConfig, DemoResponseService demoService,
                           RAGService ragService) {
        this.promptManager = promptManager;
        this.orchestrator = orchestrator;
        this.aiConfig = aiConfig;
        this.demoService = demoService;
        this.ragService = ragService;
    }

    public CompletableFuture<String> analyzeMarket(String productName, String productDescription,
                                                    String priceRange, String category, String targetCountry) {
        if (!aiConfig.isDeepSeekKeyConfigured()) {
            log.warn("DeepSeek API Key 未配置，使用演示模式");
            return CompletableFuture.completedFuture(
                    demoService.generateAnalysisDemo(productName, targetCountry));
        }

        Map<String, String> vars = new HashMap<>();
        vars.put("productName", productName);
        vars.put("productDescription", productDescription != null ? productDescription : "无");
        vars.put("priceRange", priceRange != null ? priceRange : "未提供");
        vars.put("category", category != null ? category : "未提供");
        vars.put("targetCountry", targetCountry != null ? targetCountry : "US");

        String systemPrompt = promptManager.renderTemplate("analysis-market", vars);
        String ragAugmentedPrompt = ragService.buildAugmentedSystemPrompt(systemPrompt,
                String.format("展示架市场分析 %s %s", productName, targetCountry));

        String userMessage = String.format(
                "请分析商品「%s」在%s市场的适销性。品类: %s, 价格区间: %s。请给出详细的结构化分析报告。",
                productName, targetCountry, category != null ? category : "未指定", priceRange);

        return orchestrator.reasoning(ragAugmentedPrompt, userMessage);
    }
}
