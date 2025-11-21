package com.shop.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码工具类
 */
public class CaptchaUtil {
    
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int CAPTCHA_LENGTH = 4;
    private static final int IMAGE_WIDTH = 120;
    private static final int IMAGE_HEIGHT = 40;
    
    /**
     * 生成验证码图片和验证码文本
     */
    public static CaptchaResult generateCaptcha() {
        String captchaText = generateRandomText();
        BufferedImage image = createCaptchaImage(captchaText);
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
            return new CaptchaResult(captchaText, "data:image/png;base64," + base64Image);
        } catch (IOException e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }
    
    /**
     * 生成随机验证码文本
     */
    private static String generateRandomText() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            sb.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        return sb.toString();
    }
    
    /**
     * 创建验证码图片
     */
    private static BufferedImage createCaptchaImage(String text) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        // 设置背景色
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        
        // 设置字体
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        
        // 绘制验证码文本
        Random random = new Random();
        for (int i = 0; i < text.length(); i++) {
            // 随机颜色
            g2d.setColor(new Color(random.nextInt(128), random.nextInt(128), random.nextInt(128)));
            
            // 随机位置偏移
            int x = 20 + i * 20 + random.nextInt(5);
            int y = 25 + random.nextInt(10);
            
            g2d.drawString(String.valueOf(text.charAt(i)), x, y);
        }
        
        // 添加干扰线
        for (int i = 0; i < 5; i++) {
            g2d.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            int x1 = random.nextInt(IMAGE_WIDTH);
            int y1 = random.nextInt(IMAGE_HEIGHT);
            int x2 = random.nextInt(IMAGE_WIDTH);
            int y2 = random.nextInt(IMAGE_HEIGHT);
            g2d.drawLine(x1, y1, x2, y2);
        }
        
        // 添加噪点
        for (int i = 0; i < 50; i++) {
            int x = random.nextInt(IMAGE_WIDTH);
            int y = random.nextInt(IMAGE_HEIGHT);
            image.setRGB(x, y, random.nextInt(0xFFFFFF));
        }
        
        g2d.dispose();
        return image;
    }
    
    /**
     * 验证码结果类
     */
    public static class CaptchaResult {
        private String captchaText;
        private String captchaImage;
        
        public CaptchaResult(String captchaText, String captchaImage) {
            this.captchaText = captchaText;
            this.captchaImage = captchaImage;
        }
        
        public String getCaptchaText() {
            return captchaText;
        }
        
        public String getCaptchaImage() {
            return captchaImage;
        }
    }
}