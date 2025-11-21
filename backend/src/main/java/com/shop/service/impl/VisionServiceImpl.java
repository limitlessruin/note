package com.shop.service.impl;

import com.shop.service.VisionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;
                                                 
import javax.imageio.ImageIO;


@Service
public class VisionServiceImpl implements VisionService {
    @Value("${tongyi.api.key:}")
    private String tongyiApiKey;

    @Value("${tongyi.api.vision-url:https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation}")
    private String tongyiApiUrl;

    private final WebClient webClient;                        

    public VisionServiceImpl() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String analyzeImageUrl(String imageUrl, String question) {
        Map<String, Object> request = buildRequestWithUrl(imageUrl, question);
        return sendRequest(request);
    }

    /**
     * 分析图片内容
     */
    public String analyzeImageBase64(String imageBase64,String question) {
        // 验证图片大小
        validateImageSize(imageBase64);
        
        // 如果图片太大，进行压缩
        String compressedImageBase64 = compressImageIfNeeded(imageBase64);
        
        // 预处理用户问题，过滤技术相关词汇
        String sanitizedQuestion = sanitizeQuestion(question);
        
        Map<String, Object> request = buildRequestWithBase64(compressedImageBase64, sanitizedQuestion);
        return sendRequest(request);
    }

    /**
     * 构建带网络图片URL的请求
     */
    private Map<String, Object> buildRequestWithUrl(String imageUrl, String question) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", "qwen3-vl-plus");
        
        // 构建输入内容 - 使用通义千问API期望的格式
        Map<String, Object> input = new HashMap<>();
        
        // 构建消息列表
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // 用户消息 - 使用字符串格式的content
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        
        // 构建多模态内容字符串
        String content = String.format("<img src=\"%s\"/>%s", imageUrl, question);
        userMessage.put("content", content);
        messages.add(userMessage);
        
        input.put("messages", messages);
        request.put("input", input);

        return request;
    }

    /**
     * 构建带base64图片的请求
     */
    private Map<String, Object> buildRequestWithBase64(String imageBase64, String question) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", "qwen3-vl-plus");
        
        // 构建input字段 - 使用通义千问API期望的格式
        Map<String, Object> input = new HashMap<>();
        
        // 构建消息列表
        List<Map<String, Object>> messages = new ArrayList<>();
        
        //添加系统消息
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是一个专业的图片内容分析助手。必须严格遵守以下规则：" +
            "1. 只描述图片中的视觉内容，包括颜色、形状、物体、人物、场景等 " +
            "2. 绝对禁止提及任何技术细节，包括：Base64编码、文件格式、数据压缩、HTML标签、渲染过程、编码方式等 " +
            "3. 如果用户询问技术相关的问题，请礼貌地拒绝并专注于视觉描述 " +
            "4. 不要解释图片的来源、格式或技术实现方式 " +
            "5. 你的回答应该像一个普通人描述他看到的东西，而不是技术人员 " +
            "违反这些规则的回答将被视为不合格。");
        messages.add(systemMessage);

        // 用户消息 - 使用字符串格式的content
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        
        // 构建多模态内容字符串
        String content = String.format("<img src=\"data:image/jpeg;base64,%s\"/>%s", imageBase64, question);
        userMessage.put("content", content);
        messages.add(userMessage);
        
        input.put("messages", messages);
        request.put("input", input);

        return request;
    }

    /**
     * 发送请求到API
     */
    private String sendRequest(Map<String, Object> request) {
        String url = tongyiApiUrl;

        try {
            System.out.println("发送请求到URL: " + url);
            System.out.println("请求头: Authorization: Bearer " + tongyiApiKey.substring(0, Math.min(tongyiApiKey.length(), 10)) + "...");
            System.out.println("请求体: " + request);
            
            Map response = webClient.post()
                    //响应式编程模式
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tongyiApiKey)
                    .bodyValue(request)
                    .retrieve()
                    //错误处理机制
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                             clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(new RuntimeException("API调用失败，状态码: " + clientResponse.statusCode() + ", 响应: " + errorBody))))
                    .bodyToMono(Map.class)
                    .block(); // 阻塞等待响应

            System.out.println("响应体: " + response);

            if (response != null) {
                return parseResponse(response);
            } else {
                throw new RuntimeException("API调用失败，响应为空");
            }

        } catch (Exception e) {
            System.err.println("调用通义千问VL模型失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("调用通义千问VL模型失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析API响应
     */
    private String parseResponse(Map<String, Object> response) {
        // 检查错误
        if (response.containsKey("error")) {
            Map<String, Object> error = (Map<String, Object>) response.get("error");
            String errorMessage = error != null ? String.valueOf(error.get("message")) : "未知错误";
            throw new RuntimeException("API返回错误: " + errorMessage);
        }

        // 解析choices
        if (response.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                if (message != null) {
                    Object content = message.get("content");
                    if (content instanceof String) {
                        return (String) content;
                    } else if (content instanceof List) {
                        // 处理多模态内容列表
                        return extractTextFromContent((List<Map<String, Object>>) content);
                    }
                }
            }
        }

        // 检查output结构 - 处理新的API响应格式
        if (response.containsKey("output")) {
            Map<String, Object> output = (Map<String, Object>) response.get("output");
            if (output != null && output.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) output.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    if (message != null && message.containsKey("content")) {
                        Object content = message.get("content");
                        System.out.println("解析content: " + content);
                        
                        if (content instanceof String) {
                            return (String) content;
                        } else if (content instanceof List) {
                            // 处理多模态内容列表
                            List<Map<String, Object>> contentList = (List<Map<String, Object>>) content;
                            System.out.println("contentList: " + contentList);
                            
                            if (!contentList.isEmpty()) {
                                Map<String, Object> firstContent = contentList.get(0);
                                System.out.println("firstContent: " + firstContent);
                                
                                if (firstContent.containsKey("text")) {
                                    String text = String.valueOf(firstContent.get("text"));
                                    System.out.println("提取的文本: " + text);
                                    return text;
                                }
                            }
                            return extractTextFromContent(contentList);
                        }
                    }
                }
            }
        }

        throw new RuntimeException("无法解析API响应: " + response);
    }

    /**
     * 从多模态内容中提取文本
     */
    private String extractTextFromContent(List<Map<String, Object>> contentList) {
        return contentList.stream()
                .filter(item -> "text".equals(item.get("type")))
                .map(item -> String.valueOf(item.get("text")))
                .collect(Collectors.joining(" "));
    }

    /**
     * 验证图片大小
     */
    private void validateImageSize(String imageBase64) {
        // API限制的是Base64字符串的长度，不是文件大小
        int maxBase64Length = 129024; // API限制：Base64字符串最大长度
        
        if (imageBase64.length() > maxBase64Length) {
            // 计算实际文件大小用于显示
            int actualFileSize = imageBase64.length() * 3 / 4;
            int maxFileSize = maxBase64Length * 3 / 4;
            
            throw new RuntimeException("图片太大，请选择小于" + 
                String.format("%.2f", maxFileSize / 1024.0) + "KB的图片。当前图片大小: " + 
                String.format("%.2f", actualFileSize / 1024.0) + "KB，最大允许: " + 
                String.format("%.2f", maxFileSize / 1024.0) + "KB");
        }
    }

    /**
     * 如果需要的话压缩图片
     */
    private String compressImageIfNeeded(String imageBase64) {
        // API限制的是Base64字符串的长度，不是文件大小
        int maxBase64Length = 129024; // API限制：Base64字符串最大长度
        
        // 如果Base64数据长度在允许范围内，直接返回
        if (imageBase64.length() <= maxBase64Length) {
            return imageBase64;
        }
        
        try {
            // 1. 将Base64解码为字节数组
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            
            // 2. 读取图片
            BufferedImage originalImage = ImageIO.read(bis);
            bis.close();
            
            if (originalImage == null) {
                throw new RuntimeException("无法解码图片数据");
            }
            
            // 3. 计算压缩比例
            double compressionRatio = (double) maxBase64Length / imageBase64.length();
            
            // 4. 计算新的尺寸（保持宽高比）
            int newWidth = (int) (originalImage.getWidth() * Math.sqrt(compressionRatio));
            int newHeight = (int) (originalImage.getHeight() * Math.sqrt(compressionRatio));
            
            // 确保最小尺寸
            newWidth = Math.max(newWidth, 1);
            newHeight = Math.max(newHeight, 1);
            
            // 5. 创建缩放后的图片
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();
            
            // 6. 将图片重新编码为Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "JPEG", baos);
            byte[] compressedBytes = baos.toByteArray();
            String compressedBase64 = Base64.getEncoder().encodeToString(compressedBytes);
            baos.close();
            
            // 7. 如果压缩后仍然太大，递归压缩
            if (compressedBase64.length() > maxBase64Length) {
                return compressImageIfNeeded(compressedBase64);
            }
            
            System.out.println("图片压缩成功: " + imageBase64.length() + " -> " + compressedBase64.length() + " 字符");
            return compressedBase64;
            
        } catch (Exception e) {
            // 如果压缩失败，返回原始数据并记录错误
            System.err.println("图片压缩失败: " + e.getMessage());
            return imageBase64;
        }
    }
    
    /**
     * 预处理用户问题，过滤技术相关词汇
     */
    private String sanitizeQuestion(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "请描述这张图片的内容";
        }
        
        // 技术相关关键词列表
        String[] technicalKeywords = {
            "base64", "编码", "格式", "压缩", "html", "标签", 
            "数据", "技术", "实现", "渲染", "算法", "代码",
            "api", "接口", "请求", "响应", "服务器", "网络"
        };
        
        String lowerQuestion = question.toLowerCase();
        
        // 检查是否包含技术关键词
        for (String keyword : technicalKeywords) {
            if (lowerQuestion.contains(keyword)) {
                // 如果包含技术关键词，返回一个通用的视觉描述问题
                return "请描述这张图片的视觉内容";
            }
        }
        
        // 如果不包含技术关键词，返回原始问题
        return question;
    }
}
