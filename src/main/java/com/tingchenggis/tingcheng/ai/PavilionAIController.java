package com.tingchenggis.tingcheng.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI控制器
 * 
 * 提供与AI相关的亭城文化服务
 * 
 * @author TingChengGIS
 * @version 1.0.0
 */
@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class PavilionAIController {

    private static final Logger logger = LoggerFactory.getLogger(PavilionAIController.class);

    private final AiService aiService;

    public PavilionAIController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * 生成亭子文化介绍
     * 
     * @param pavilionName 亭子名称
     * @param location 地理位置
     * @return 文化介绍文本
     */
    @GetMapping("/culture-intro")
    public ResponseEntity<Map<String, Object>> generateCultureIntro(
            @RequestParam String pavilionName,
            @RequestParam String location) {
        try {
            logger.info("Generating culture introduction for pavilion: {} at {}", pavilionName, location);
            
            String introduction = aiService.generateCulturalIntroduction(pavilionName, location);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", introduction);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating culture introduction for pavilion: " + pavilionName, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "生成文化介绍失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 生成历史背景故事
     * 
     * @param pavilionName 亭子名称
     * @param constructionYear 建造年份
     * @return 历史故事文本
     */
    @GetMapping("/historical-story")
    public ResponseEntity<Map<String, Object>> generateHistoricalStory(
            @RequestParam String pavilionName,
            @RequestParam(required = false) Integer constructionYear) {
        try {
            logger.info("Generating historical story for pavilion: {} built in {}", pavilionName, constructionYear);
            
            String story = aiService.generateHistoricalStory(pavilionName, constructionYear);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", story);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating historical story for pavilion: " + pavilionName, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "生成历史故事失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * AI智能问答
     * 
     * @param question 用户问题
     * @return AI生成的答案
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askQuestion(@RequestBody Map<String, String> requestBody) {
        try {
            String question = requestBody.get("question");
            logger.info("Processing AI question: {}", question);
            
            String answer = aiService.answerQuestion(question);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", answer);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing AI question", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理问题失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 生成游览建议
     * 
     * @param pavilions 亭子列表
     * @param season 季节
     * @param duration 游览时长
     * @return 游览路线建议
     */
    @PostMapping("/tourism-advice")
    public ResponseEntity<Map<String, Object>> generateTourismAdvice(
            @RequestBody Map<String, Object> requestBody) {
        try {
            @SuppressWarnings("unchecked")
            List<String> pavilions = (List<String>) requestBody.get("pavilions");
            String season = (String) requestBody.get("season");
            String duration = (String) requestBody.get("duration");
            
            logger.info("Generating tourism advice for pavilions: {} in {} for {}", pavilions, season, duration);
            
            String advice = aiService.generateTourismAdvice(pavilions, season, duration);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", advice);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating tourism advice", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "生成游览建议失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取滁州亭城文化概览
     * 
     * @return 滁州亭城文化概览
     */
    @GetMapping("/culture-overview")
    public ResponseEntity<Map<String, Object>> getCultureOverview() {
        try {
            logger.info("Generating culture overview for Chuzhou Tingcheng");
            
            String overview = aiService.getCultureOverview();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", overview);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating culture overview", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "生成文化概览失败: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}