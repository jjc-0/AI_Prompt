package com.ecommerce.agent.controller;

import com.ecommerce.agent.agent.ConversationManager;
import com.ecommerce.agent.model.TranslateRequest;
import com.ecommerce.agent.service.TranslateService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/translate")
public class TranslateController {

    private final TranslateService translateService;
    private final ConversationManager conversationManager;

    public TranslateController(TranslateService translateService,
                               ConversationManager conversationManager) {
        this.translateService = translateService;
        this.conversationManager = conversationManager;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> translate(@Valid @RequestBody TranslateRequest request) {
        long start = System.currentTimeMillis();

        String sessionId = conversationManager.createSession(
                "Translation: " + request.getSourceLanguage() + " → " + request.getTargetLanguage(), "translate");
        String userMsg = "原文: " + request.getText()
                + "\n源语言: " + request.getSourceLanguage()
                + "\n目标语言: " + request.getTargetLanguage()
                + "\n场景: " + request.getContext();
        conversationManager.addMessage(sessionId, "user", userMsg);

        String result = translateService.translate(request).join();
        long elapsed = System.currentTimeMillis() - start;

        conversationManager.addMessage(sessionId, "assistant", result, "deepseek", elapsed);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "result", result,
                "processingTimeMs", elapsed,
                "sourceLanguage", request.getSourceLanguage(),
                "targetLanguage", request.getTargetLanguage(),
                "sessionId", sessionId
        ));
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchTranslate(@RequestBody Map<String, Object> batchRequest) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Batch translation in development"
        ));
    }
}
