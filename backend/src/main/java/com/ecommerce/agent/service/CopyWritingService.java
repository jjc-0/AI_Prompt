package com.ecommerce.agent.service;

import com.ecommerce.agent.config.AIConfig;
import com.ecommerce.agent.llm.MultiModelOrchestrator;
import com.ecommerce.agent.llm.PromptTemplateManager;
import com.ecommerce.agent.model.CopyWritingRequest;
import com.ecommerce.agent.util.CountryLanguageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class CopyWritingService {

    private final PromptTemplateManager promptManager;
    private final MultiModelOrchestrator orchestrator;
    private final AIConfig aiConfig;
    private final DemoResponseService demoService;

    public CopyWritingService(PromptTemplateManager promptManager, MultiModelOrchestrator orchestrator,
                              AIConfig aiConfig, DemoResponseService demoService) {
        this.promptManager = promptManager;
        this.orchestrator = orchestrator;
        this.aiConfig = aiConfig;
        this.demoService = demoService;
    }

    public CompletableFuture<String> generateCopy(CopyWritingRequest request) {
        if (!aiConfig.isDeepSeekKeyConfigured()) {
            log.warn("DeepSeek API Key 未配置，使用演示模式");
            return CompletableFuture.completedFuture(
                    demoService.generateCopywritingDemo(
                            request.getProductName(), request.getSellingPoints(),
                            request.getPlatform(), request.getTargetCountry()));
        }

        String templateId = resolveTemplateId(request.getPlatform());
        String targetLang = request.getLanguage() != null && !request.getLanguage().isBlank()
                ? request.getLanguage()
                : CountryLanguageUtil.resolveLanguage(request.getTargetCountry());

        Map<String, String> vars = new HashMap<>();
        vars.put("productName", request.getProductName());
        vars.put("sellingPoints", request.getSellingPoints());
        vars.put("targetCountry", CountryLanguageUtil.resolveCountryName(request.getTargetCountry()));
        vars.put("language", targetLang);
        vars.put("style", request.getStyle() != null ? request.getStyle() : "专业且有吸引力");

        String systemPrompt = promptManager.renderTemplate(templateId, vars);
        String userMessage = buildUserMessage(request);

        return orchestrator.reasoning(systemPrompt, userMessage);
    }

    public CompletableFuture<String> generateCopyWithCollab(CopyWritingRequest request) {
        return generateCopy(request);
    }

    private String resolveTemplateId(String platform) {
        if ("email".equalsIgnoreCase(platform) || "inquiry".equalsIgnoreCase(platform)) {
            return "inquiry-reply";
        }
        return "copywriting-amazon";
    }

    private String buildUserMessage(CopyWritingRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("请为以下商品生成").append(request.getPlatform() != null ? request.getPlatform() : "Amazon").append("平台的文案。\n\n");
        sb.append("商品名: ").append(request.getProductName()).append("\n");
        sb.append("卖点: ").append(request.getSellingPoints()).append("\n");
        sb.append("目标国家: ").append(CountryLanguageUtil.resolveCountryName(request.getTargetCountry())).append("\n");
        if (request.getExtraParams() != null && !request.getExtraParams().isEmpty()) {
            sb.append("额外信息:\n");
            request.getExtraParams().forEach((k, v) -> sb.append("- ").append(k).append(": ").append(v).append("\n"));
        }
        return sb.toString();
    }
}
