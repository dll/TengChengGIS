package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.ai.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/chat")
    public Map<String, Object> chat(@RequestParam String message) {
        logger.info("AI chat: {}", message);
        String reply = aiService.chat(message);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("reply", reply);
        result.put("aiAvailable", aiService.isAiAvailable());
        return result;
    }

    @GetMapping("/pavilion/{pavilionId}")
    public Map<String, Object> getPavilionIntroduction(@PathVariable Long pavilionId) {
        logger.info("AI introduction for pavilion: {}", pavilionId);
        String intro = aiService.generatePavilionIntroduction(pavilionId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("introduction", intro);
        result.put("aiAvailable", aiService.isAiAvailable());
        return result;
    }

    @PostMapping("/tour-advice")
    public Map<String, Object> getTourAdvice(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> names = (List<String>) request.getOrDefault("pavilionNames", List.of());
        String season = (String) request.getOrDefault("season", "春季");
        int duration = (int) request.getOrDefault("durationMinutes", 240);

        logger.info("AI tour advice: {} pavilions, season={}, duration={}", names.size(), season, duration);
        String advice = aiService.generateTourRouteAdvice(names, season, duration);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("advice", advice);
        result.put("aiAvailable", aiService.isAiAvailable());
        return result;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("aiAvailable", aiService.isAiAvailable());
        result.put("provider", aiService.getActiveProvider());
        result.put("type", aiService.isAiAvailable() ? aiService.getActiveProvider().toLowerCase() : "template");
        return result;
    }
}
