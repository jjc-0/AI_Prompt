package com.ecommerce.agent.service;

import com.ecommerce.agent.agent.AgentDispatcher;
import com.ecommerce.agent.agent.ConversationManager;
import com.ecommerce.agent.model.AgentRequest;
import com.ecommerce.agent.model.AgentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class WeChatBotService {

    private static final String ILINK_BASE_URL = "https://ilinkai.weixin.qq.com";
    private static final String ILINK_APP_ID = "bot";
    private static final String ILINK_APP_CLIENT_VERSION = "132100";

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AgentDispatcher agentDispatcher;
    private final ConversationManager conversationManager;

    private Thread pollThread;
    private volatile boolean running = false;
    private String botToken;
    private String accountId;
    private String getUpdatesBuf = "";

    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    public WeChatBotService(AgentDispatcher agentDispatcher, ConversationManager conversationManager) {
        this.agentDispatcher = agentDispatcher;
        this.conversationManager = conversationManager;
    }

    public synchronized void start(String token, String accountId) {
        if (running) return;
        this.botToken = token;
        this.accountId = accountId;
        this.getUpdatesBuf = "";
        this.running = true;
        pollThread = new Thread(this::pollLoop, "wechat-poll");
        pollThread.setDaemon(true);
        pollThread.start();
        log.info("微信Bot消息轮询已启动");
    }

    @PreDestroy
    public synchronized void stop() {
        running = false;
        if (pollThread != null) pollThread.interrupt();
    }

    private void pollLoop() {
        while (running) {
            try {
                List<Map<String, Object>> messages = fetchUpdates();
                if (!messages.isEmpty()) log.info("[微信轮询] 收到 {} 条新消息", messages.size());
                for (Map<String, Object> msg : messages) processMessage(msg);
            } catch (InterruptedException e) { break;
            } catch (Exception e) { log.warn("轮询异常: {}", e.getMessage()); try { Thread.sleep(3000); } catch (InterruptedException ie) { break; } }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchUpdates() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("get_updates_buf", getUpdatesBuf);
        body.put("base_info", baseInfo());

        HttpRequest req = request("ilink/bot/getupdates", body, 40);

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            log.warn("[getupdates] HTTP {} body: {}", resp.statusCode(), resp.body().substring(0, Math.min(200, resp.body().length())));
        }
        Map<String, Object> data = objectMapper.readValue(resp.body(), Map.class);

        Object errcode = data.get("errcode");
        if (errcode instanceof Number n && n.intValue() != 0) {
            log.warn("[getupdates] API错误: errcode={}, errmsg={}", errcode, data.get("errmsg"));
        }

        Object buf = data.get("next_get_updates_buf");
        if (buf instanceof String s && !s.isBlank()) getUpdatesBuf = s;

        Object msgs = data.get("messages");
        if (msgs instanceof List) return (List<Map<String, Object>>) msgs;
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private void processMessage(Map<String, Object> msg) {
        try {
            String fromUserId = toString(msg.get("from_user_id"));
            if (fromUserId.isBlank()) return;

            // 提取 item_list 中的文本
            String text = "";
            Object items = msg.get("item_list");
            if (items instanceof List list) {
                for (Object item : list) {
                    if (item instanceof Map m) {
                        Object type = m.get("type");
                        if (type instanceof Number n && n.intValue() == 1) {
                            Object ti = m.get("text_item");
                            if (ti instanceof Map tm) text += toString(tm.get("text"));
                        }
                    }
                }
            }
            if (text.isBlank()) return;

            log.info("[微信] {}: {}", fromUserId, text);

            String sessionId = userSessions.computeIfAbsent(fromUserId, k -> {
                String sid = "wx_" + fromUserId.substring(0, Math.min(8, fromUserId.length()));
                String title = "微信 · " + fromUserId.substring(0, Math.min(8, fromUserId.length()));
                return conversationManager.createSession(sid, title, "wechat");
            });

            AgentRequest req = AgentRequest.builder()
                    .sessionId(sessionId)
                    .message(text)
                    .taskType("chat")
                    .enableTools(true)
                    .build();

            AgentResponse agentResp = agentDispatcher.dispatch(req);
            String reply = agentResp.getMessage();
            if (reply == null || reply.isBlank()) reply = "智能体暂无响应";

            log.info("[微信回复] {}", reply.substring(0, Math.min(80, reply.length())));
            sendMessage(fromUserId, reply);

        } catch (Exception e) {
            log.error("处理微信消息失败", e);
        }
    }

    /**
     * 发送文本消息，对齐插件 buildTextMessageReq() 格式:
     * { msg: { from_user_id, to_user_id, client_id, message_type, message_state, item_list }, base_info }
     */
    private void sendMessage(String toUserId, String text) throws Exception {
        Map<String, Object> textItem = new LinkedHashMap<>();
        textItem.put("type", 1);
        textItem.put("text_item", Map.of("text", text));

        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("from_user_id", "");
        msg.put("to_user_id", toUserId);
        msg.put("client_id", "jc-claw-" + randomHex(8));
        msg.put("message_type", 2);  // BOT
        msg.put("message_state", 2); // FINISH
        msg.put("item_list", List.of(textItem));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("msg", msg);
        body.put("base_info", baseInfo());

        HttpRequest req = request("ilink/bot/sendmessage", body, 15);
        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            log.warn("[sendmessage] HTTP {} body: {}", resp.statusCode(), resp.body().substring(0, Math.min(200, resp.body().length())));
        } else {
            log.info("[微信发送成功] {}", text.substring(0, Math.min(50, text.length())));
        }
    }

    /** 构建带 iLink 头的 HttpRequest */
    private HttpRequest request(String endpoint, Map<String, Object> body, long timeoutSec) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(ILINK_BASE_URL + "/" + endpoint))
                .timeout(Duration.ofSeconds(timeoutSec))
                .header("Content-Type", "application/json")
                .header("AuthorizationType", "ilink_bot_token")
                .header("X-WECHAT-UIN", randomWechatUin())
                .header("iLink-App-Id", ILINK_APP_ID)
                .header("iLink-App-ClientVersion", ILINK_APP_CLIENT_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
        if (botToken != null && !botToken.isEmpty()) {
            b.header("Authorization", "Bearer " + botToken);
        }
        return b.build();
    }

    private Map<String, Object> baseInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("channel_version", "2.4.4");
        info.put("bot_agent", "JC-ClawBot");
        return info;
    }

    private static String randomHex(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) sb.append(Integer.toHexString(ThreadLocalRandom.current().nextInt(16)));
        return sb.toString();
    }

    private static String randomWechatUin() {
        int v = ThreadLocalRandom.current().nextInt();
        return Base64.getEncoder().encodeToString(String.valueOf(v).getBytes());
    }

    private static String toString(Object o) {
        return o instanceof String s ? s : o != null ? o.toString() : "";
    }

    public boolean isRunning() { return running; }
}
