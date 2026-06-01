package com.ecommerce.agent.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.concurrent.CompletionException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<Map<String, Object>> handleCompletionException(CompletionException ex) {
        Throwable cause = ex.getCause();
        String message = cause != null ? cause.getMessage() : ex.getMessage();
        log.error("异步执行异常: {}", message, cause);

        if (message != null && (message.contains("401") || message.contains("403") || message.contains("auth"))) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "success", false,
                    "error", "API_KEY_NOT_CONFIGURED",
                    "message", "DeepSeek API Key 未配置或无效。请在 application-secrets.yml 中填写有效的 DEEPSEEK_API_KEY。",
                    "hint", "编辑 backend/src/main/resources/application-secrets.yml，替换 DEEPSEEK_API_KEY"
            ));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "AI_SERVICE_ERROR",
                "message", message != null ? message : "AI服务调用失败"
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "error", "VALIDATION_ERROR",
                "message", message
        ));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("运行时异常: {}", ex.getMessage(), ex);

        String message = ex.getMessage();
        if (message != null && (message.contains("401") || message.contains("Unauthorized") || message.contains("auth"))) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "success", false,
                    "error", "API_KEY_NOT_CONFIGURED",
                    "message", "DeepSeek API Key 未配置或无效。请在 application-secrets.yml 中填写有效的 DEEPSEEK_API_KEY。",
                    "hint", "编辑 backend/src/main/resources/application-secrets.yml"
            ));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "INTERNAL_ERROR",
                "message", message != null ? message : "服务器内部错误"
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("未捕获异常: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "error", "UNKNOWN_ERROR",
                "message", ex.getMessage() != null ? ex.getMessage() : "未知错误"
        ));
    }
}
