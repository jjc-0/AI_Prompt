package com.ecommerce.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpenClawService {

    private static final String ILINK_BASE_URL = "https://ilinkai.weixin.qq.com";
    private static final String ILINK_APP_ID = "bot";
    private static final String ILINK_APP_CLIENT_VERSION = "132100"; // 0x020404
    private static final String BOT_TYPE = "3";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String currentQrcode; // 当前登录的 qrcode token，用于轮询

    /** 发起微信扫码绑定 — 直接调用 iLink API */
    public Map<String, Object> startLogin(String openclawHome) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();

        // 调用 get_bot_qrcode
        String url = ILINK_BASE_URL + "/ilink/bot/get_bot_qrcode?bot_type=" + BOT_TYPE;
        String body = "{\"local_token_list\":[]}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("iLink-App-Id", ILINK_APP_ID)
                .header("iLink-App-ClientVersion", ILINK_APP_CLIENT_VERSION)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        log.info("请求 iLink QR: {}", url);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        log.info("iLink QR 响应 status={}, body={}", response.statusCode(), response.body());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = objectMapper.readValue(response.body(), Map.class);

        String qrcode = (String) data.get("qrcode");
        String qrcodeUrl = (String) data.get("qrcode_img_content");

        if (qrcodeUrl == null || qrcodeUrl.isBlank()) {
            result.put("success", false);
            result.put("error", "iLink 返回数据异常: " + response.body());
            return result;
        }

        this.currentQrcode = qrcode;

        result.put("success", true);
        result.put("qrUrl", qrcodeUrl);
        result.put("status", "binding");
        result.put("message", "请用手机微信扫描二维码完成绑定");
        log.info("获取到二维码 URL: {}", qrcodeUrl);
        return result;
    }

    /** 查询绑定状态 — 轮询 iLink get_qrcode_status */
    public Map<String, Object> checkStatus(String openclawHome) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("connected", false);

        if (currentQrcode == null) {
            return result;
        }

        try {
            String url = ILINK_BASE_URL + "/ilink/bot/get_qrcode_status?qrcode="
                    + URLEncoder.encode(currentQrcode, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("iLink-App-Id", ILINK_APP_ID)
                    .header("iLink-App-ClientVersion", ILINK_APP_CLIENT_VERSION)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(response.body(), Map.class);

            String status = (String) data.get("status");
            log.debug("iLink QR 状态: {}", status);

            if ("confirmed".equals(status)) {
                result.put("connected", true);
                result.put("botToken", data.get("bot_token"));
                result.put("accountId", data.get("ilink_bot_id"));
                result.put("baseUrl", data.get("baseurl"));
                result.put("userId", data.get("ilink_user_id"));
                currentQrcode = null; // 清除
            } else if ("binded_redirect".equals(status)) {
                result.put("connected", true); // 已绑定过
                result.put("alreadyConnected", true);
                currentQrcode = null;
            } else if ("expired".equals(status)) {
                result.put("expired", true);
                currentQrcode = null;
            }

        } catch (Exception e) {
            log.debug("轮询 iLink 状态失败: {}", e.getMessage());
        }

        return result;
    }

    /** 取消绑定 */
    public void cancelLogin() {
        currentQrcode = null;
    }
}
