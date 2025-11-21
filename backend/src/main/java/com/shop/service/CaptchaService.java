package com.shop.service;

import com.shop.util.CaptchaUtil;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 */
@Service
public class CaptchaService {
    
    private final Map<String, CaptchaInfo> captchaStore = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
    
    public CaptchaService() {
        // 每5分钟清理一次过期的验证码
        cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredCaptchas, 5, 5, TimeUnit.MINUTES);
    }
    
    /**
     * 生成验证码
     */
    public CaptchaUtil.CaptchaResult generateCaptcha(String sessionId) {
        CaptchaUtil.CaptchaResult result = CaptchaUtil.generateCaptcha();
        
        // 存储验证码信息（5分钟有效期）
        CaptchaInfo captchaInfo = new CaptchaInfo(result.getCaptchaText(), System.currentTimeMillis() + 5 * 60 * 1000);
        captchaStore.put(sessionId, captchaInfo);
        
        return result;
    }
    
    /**
     * 验证验证码
     */
    public boolean validateCaptcha(String sessionId, String inputCaptcha) {
        if (sessionId == null || inputCaptcha == null) {
            return false;
        }
        
        CaptchaInfo captchaInfo = captchaStore.get(sessionId);
        if (captchaInfo == null) {
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > captchaInfo.getExpireTime()) {
            captchaStore.remove(sessionId);
            return false;
        }
        
        // 验证码不区分大小写
        boolean isValid = captchaInfo.getCaptchaText().equalsIgnoreCase(inputCaptcha);
        
        // 验证后移除验证码（一次性使用）
        if (isValid) {
            captchaStore.remove(sessionId);
        }
        
        return isValid;
    }
    
    /**
     * 清理过期的验证码
     */
    private void cleanupExpiredCaptchas() {
        long currentTime = System.currentTimeMillis();
        captchaStore.entrySet().removeIf(entry -> currentTime > entry.getValue().getExpireTime());
    }
    
    /**
     * 验证码信息类
     */
    private static class CaptchaInfo {
        private String captchaText;
        private long expireTime;
        
        public CaptchaInfo(String captchaText, long expireTime) {
            this.captchaText = captchaText;
            this.expireTime = expireTime;
        }
        
        public String getCaptchaText() {
            return captchaText;
        }
        
        public long getExpireTime() {
            return expireTime;
        }
    }
}