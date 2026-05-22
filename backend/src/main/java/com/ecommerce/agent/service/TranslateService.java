package com.ecommerce.agent.service;

import com.ecommerce.agent.config.AIConfig;
import com.ecommerce.agent.llm.MultiModelOrchestrator;
import com.ecommerce.agent.llm.PromptTemplateManager;
import com.ecommerce.agent.model.TranslateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class TranslateService {

    private final PromptTemplateManager promptManager;
    private final MultiModelOrchestrator orchestrator;
    private final AIConfig aiConfig;
    private final DemoResponseService demoService;

    public TranslateService(PromptTemplateManager promptManager, MultiModelOrchestrator orchestrator,
                            AIConfig aiConfig, DemoResponseService demoService) {
        this.promptManager = promptManager;
        this.orchestrator = orchestrator;
        this.aiConfig = aiConfig;
        this.demoService = demoService;
    }

    public CompletableFuture<String> translate(TranslateRequest request) {
        if (!aiConfig.isDeepSeekKeyConfigured()) {
            log.warn("DeepSeek API Key 未配置，使用演示模式");
            return CompletableFuture.completedFuture(
                    demoService.generateTranslationDemo(request.getText(),
                            request.getSourceLanguage(), request.getTargetLanguage()));
        }

        Map<String, String> vars = new HashMap<>();
        vars.put("sourceLanguage", request.getSourceLanguage() != null ? request.getSourceLanguage() : "中文");
        vars.put("targetLanguage", request.getTargetLanguage() != null ? request.getTargetLanguage() : "英文");
        vars.put("context", request.getContext() != null ? request.getContext() : "跨境电商商品信息");
        vars.put("text", request.getText());

        String systemPrompt = promptManager.renderTemplate("translation-ecommerce", vars);

        if (request.isEcommerceLocalization()) {
            String enhancedPrompt = systemPrompt + """

                    ## 电商本地化强化要求
                    - 数字、单位需要转换为目标市场的惯用格式
                    - 价格符号需转换为目标货币符号
                    - 尺寸单位需本地化 (inch↔cm, lb↔kg)
                    - 注意目标市场的广告法规用词限制
                    - 保留品牌名和专有名词不翻译
                    """;
            systemPrompt = enhancedPrompt;
        }

        return orchestrator.reasoning(systemPrompt, request.getText());
    }
}
