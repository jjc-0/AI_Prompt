package com.ecommerce.agent.controller;

import com.ecommerce.agent.llm.PromptTemplateManager;
import com.ecommerce.agent.model.PromptTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/copywriting/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final PromptTemplateManager promptTemplateManager;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getTemplates(@RequestParam(required = false) String category) {
        List<PromptTemplate> templates;
        if (category != null && !category.isEmpty()) {
            templates = promptTemplateManager.getTemplatesByCategory(category);
        } else {
            templates = promptTemplateManager.getAllTemplates();
        }
        return ResponseEntity.ok(templates.stream().map(this::toMap).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTemplate(@PathVariable String id) {
        PromptTemplate t = promptTemplateManager.getTemplate(id);
        if (t == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toMap(t));
    }

    @PostMapping("/{id}/preview")
    public ResponseEntity<Map<String, Object>> previewTemplate(@PathVariable String id,
                                                                @RequestBody Map<String, String> variables) {
        try {
            String rendered = promptTemplateManager.renderTemplate(id, variables);
            return ResponseEntity.ok(Map.of("rendered", rendered));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> toMap(PromptTemplate t) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", t.getId());
        map.put("name", t.getName());
        map.put("description", t.getDescription());
        map.put("category", t.getCategory());
        map.put("template", t.getTemplate());
        map.put("variables", t.getVariables() != null ? t.getVariables() : List.of());
        map.put("targetPlatform", t.getTargetPlatform());
        map.put("active", t.isActive());
        map.put("hitRate", 0.85);
        map.put("version", "1.0");
        return map;
    }
}
