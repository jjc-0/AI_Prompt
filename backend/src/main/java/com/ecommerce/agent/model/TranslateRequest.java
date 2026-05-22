package com.ecommerce.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslateRequest {
    private String text;
    private String sourceLanguage;
    private String targetLanguage;
    private String context;
    private boolean ecommerceLocalization;
}
