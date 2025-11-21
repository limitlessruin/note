package com.shop.controller;

import com.shop.service.CaptchaService;
import com.shop.util.CaptchaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 验证码控制器
 */
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {
    
    @Autowired
    private CaptchaService captchaService;
    
    /**
     * 获取验证码图片
     */
    @GetMapping("/image")
    public ResponseEntity<Map<String, Object>> getCaptchaImage(@RequestParam(value = "sessionId", required = false) String sessionId) {
        // 如果没有提供sessionId，生成一个新的
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        
        CaptchaUtil.CaptchaResult result = captchaService.generateCaptcha(sessionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("sessionId", sessionId);
        response.put("captchaImage", result.getCaptchaImage());
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 验证验证码
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCaptcha(
            @RequestParam String sessionId,
            @RequestParam String captcha) {
        
        boolean isValid = captchaService.validateCaptcha(sessionId, captcha);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", isValid);
        response.put("message", isValid ? "验证码正确" : "验证码错误");
        
        return ResponseEntity.ok(response);
    }
}