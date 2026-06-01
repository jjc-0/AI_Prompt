package com.ecommerce.agent.controller;

import com.ecommerce.agent.agent.ConversationManager;
import com.ecommerce.agent.model.CopyWritingRequest;
import com.ecommerce.agent.llm.PromptTemplateManager;
import com.ecommerce.agent.model.PromptTemplate;
import com.ecommerce.agent.service.CopyWritingService;
import com.ecommerce.agent.service.SessionTitleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/copywriting")
public class CopyWritingController {

    private final CopyWritingService copyWritingService;
    private final PromptTemplateManager promptManager;
    private final ConversationManager conversationManager;
    private final SessionTitleService titleService;

    public CopyWritingController(CopyWritingService copyWritingService,
                                  PromptTemplateManager promptManager,
                                  ConversationManager conversationManager,
                                  SessionTitleService titleService) {
        this.copyWritingService = copyWritingService;
        this.promptManager = promptManager;
        this.conversationManager = conversationManager;
        this.titleService = titleService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@Valid @RequestBody CopyWritingRequest request) {
        long start = System.currentTimeMillis();

        String sessionId = conversationManager.createSession(null, "copywriting");
        String userMsg = "产品: " + request.getProductName()
                + "\n特点: " + request.getSellingPoints()
                + "\n市场: " + request.getTargetCountry()
                + "\n平台: " + request.getPlatform();
        conversationManager.addMessage(sessionId, "user", userMsg);
        titleService.autoTitle(sessionId, userMsg);

        String result = copyWritingService.generateCopy(request).join();
        long elapsed = System.currentTimeMillis() - start;

        conversationManager.addMessage(sessionId, "assistant", result, "deepseek", elapsed);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "result", result,
                "processingTimeMs", elapsed,
                "model", "deepseek",
                "sessionId", sessionId
        ));
    }

    @PostMapping("/generate/collaborative")
    public ResponseEntity<Map<String, Object>> generateCollaborative(@Valid @RequestBody CopyWritingRequest request) {
        return generate(request);
    }

    @GetMapping("/templates")
    public ResponseEntity<List<PromptTemplate>> getTemplates(
            @RequestParam(required = false) String category) {
        List<PromptTemplate> templates;
        if (category != null) {
            templates = promptManager.getTemplatesByCategory(category);
        } else {
            templates = promptManager.getAllTemplates();
        }
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<PromptTemplate> getTemplate(@PathVariable String id) {
        PromptTemplate template = promptManager.getTemplate(id);
        if (template == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(template);
    }

    @PostMapping("/templates/{id}/preview")
    public ResponseEntity<Map<String, Object>> previewTemplate(
            @PathVariable String id,
            @RequestBody Map<String, String> variables) {
        String rendered = promptManager.renderTemplate(id, variables);
        return ResponseEntity.ok(Map.of(
                "templateId", id,
                "rendered", rendered,
                "variables", variables
        ));
    }
}
