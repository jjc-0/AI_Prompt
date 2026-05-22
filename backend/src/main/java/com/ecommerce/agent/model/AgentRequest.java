package com.ecommerce.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequest {
    private String sessionId;
    private String message;
    private String taskType;
    private Map<String, Object> parameters;
    private List<ConversationMessage> history;
    private boolean enableTools;
    private String preferredModel;
}
