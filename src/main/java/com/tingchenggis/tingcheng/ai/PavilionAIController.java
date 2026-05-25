package com.tingchenggis.tingcheng.ai;

import com.tingchenggis.tingcheng.service.PavilionService;
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

    private final PavilionAIService pavilionAIService;
    private final PavilionService pavilionService;

    public PavilionAIController(PavilionAIService pavilionAIService, PavilionService pavilionService) {
        this.pavilionAIService = pavilionAIService;
        this.pavilionService = pavilionService;
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
            
            String introduction = pavilionAIService.generateCulturalIntroduction(pavilionName, location);
            
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
            
            String story = pavilionAIService.generateHistoricalStory(pavilionName, constructionYear);
            
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
            
            String answer = pavilionAIService.answerQuestion(question);
            
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
            
            String advice = pavilionAIService.generateTourismAdvice(pavilions, season, duration);
            
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
            
            StringBuilder overview = new StringBuilder();
            overview.append("【滁州'亭城'文化概览】\n\n");
            overview.append("滁州，位于安徽省东部，是一座拥有悠久历史的文化名城。因其深厚的'亭文化'积淀，素有'亭城'之美誉。\n\n");
            overview.append("一、历史渊源：\n");
            overview.append("滁州的'亭文化'始于北宋，因欧阳修任滁州知州时写下千古名篇《醉翁亭记》而声名远播。文中'醉翁之意不在酒，在乎山水之间也'的名句，使得醉翁亭成为'天下第一亭'。\n\n");
            overview.append("二、代表亭子：\n");
            overview.append("1. 醉翁亭：位于琅琊山麓，是滁州'亭城'文化的象征\n");
            overview.append("2. 丰乐亭：同样由欧阳修建造，与醉翁亭并称为'姐妹亭'\n");
            overview.append("3. 琅琊亭：位于琅琊山顶，俯瞰滁州全景\n");
            overview.append("4. 现代景观亭：近年来新建的体现传统文化与现代设计相结合的景观亭\n\n");
            overview.append("三、文化内涵：\n");
            overview.append("滁州的亭子不仅是休憩之所，更是文人雅士聚会论道、吟诗作赋的文化场所，体现了人与自然和谐共生的理念。\n\n");
            overview.append("四、当代发展：\n");
            overview.append("今天的滁州在保护历史文化遗产的同时，积极发展现代旅游产业，将'亭文化'融入城市建设，打造独具特色的'亭城'品牌。");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", overview.toString());
            
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