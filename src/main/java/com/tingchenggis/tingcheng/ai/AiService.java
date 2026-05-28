package com.tingchenggis.tingcheng.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    private final PavilionRepository pavilionRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${tingcheng.ai.active-provider:openai}")
    private String activeProvider;

    @Value("${tingcheng.ai.openai-api-key:}")
    private String openaiApiKey;
    @Value("${tingcheng.ai.openai-api-url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;
    @Value("${tingcheng.ai.openai-model:gpt-3.5-turbo}")
    private String openaiModel;

    @Value("${tingcheng.ai.deepseek-api-key:}")
    private String deepseekApiKey;
    @Value("${tingcheng.ai.deepseek-api-url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;
    @Value("${tingcheng.ai.deepseek-model:deepseek-chat}")
    private String deepseekModel;

    @Value("${tingcheng.ai.zhipu-api-key:}")
    private String zhipuApiKey;
    @Value("${tingcheng.ai.zhipu-api-url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String zhipuApiUrl;
    @Value("${tingcheng.ai.zhipu-model:glm-4}")
    private String zhipuModel;

    private String resolvedApiKey;
    private String resolvedApiUrl;
    private String resolvedModel;
    private String resolvedProviderName;
    private boolean aiAvailable;

    private static final String SYSTEM_PROMPT = "你是一个滁州亭城GIS系统的智能导游助手。" +
        "你的任务是回答关于滁州亭子、醉翁亭记、欧阳修以及滁州历史文化的问题。" +
        "请用中文回答，语气亲切专业，内容翔实有据。";

    public AiService(PavilionRepository pavilionRepository, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.pavilionRepository = pavilionRepository;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    void init() {
        switch (activeProvider) {
            case "deepseek" -> {
                resolvedApiKey = deepseekApiKey;
                resolvedApiUrl = deepseekApiUrl;
                resolvedModel = deepseekModel;
                resolvedProviderName = "DeepSeek";
            }
            case "zhipu" -> {
                resolvedApiKey = zhipuApiKey;
                resolvedApiUrl = zhipuApiUrl;
                resolvedModel = zhipuModel;
                resolvedProviderName = "ZhiPu";
            }
            default -> {
                resolvedApiKey = openaiApiKey;
                resolvedApiUrl = openaiApiUrl;
                resolvedModel = openaiModel;
                resolvedProviderName = "OpenAI";
            }
        }
        if (resolvedApiKey != null && !resolvedApiKey.isBlank() && !resolvedApiKey.equals("sk-your-key-here")) {
            aiAvailable = true;
            logger.info("AI service initialized: provider={}, model={}, url={}",
                resolvedProviderName, resolvedModel, resolvedApiUrl);
        } else {
            logger.info("No API key configured for active provider '{}', AI service will use template fallback",
                activeProvider);
            aiAvailable = false;
        }
    }

    public String chat(String userMessage) {
        if (!aiAvailable) {
            return templateResponse(userMessage);
        }
        try {
            return callOpenAI(List.of(
                message("system", SYSTEM_PROMPT),
                message("user", userMessage)
            ), 500);
        } catch (Exception e) {
            logger.error("AI chat failed: {}", e.getMessage());
            return templateResponse(userMessage);
        }
    }

    public String generatePavilionIntroduction(Long pavilionId) {
        Optional<Pavilion> opt = pavilionRepository.findById(pavilionId);
        if (opt.isEmpty()) return "亭子不存在";

        Pavilion p = opt.get();
        String prompt = String.format(
            "请以导游的口吻介绍滁州的亭子「%s」。它位于%s，建于%s，类型是%s。" +
            "请从建筑特色、历史文化、参观建议三方面介绍，200字左右。",
            p.getChineseName(),
            p.getLocationDesc() != null ? p.getLocationDesc() : "滁州",
            p.getBuiltYear() != null ? p.getBuiltYear() + "年" : "未知年份",
            p.getPavilionType() != null ? p.getPavilionType() : "未分类"
        );

        if (!aiAvailable) {
            return fallbackIntroduction(p);
        }

        try {
            return callOpenAI(List.of(
                message("system", SYSTEM_PROMPT),
                message("user", prompt)
            ), 400);
        } catch (Exception e) {
            logger.error("AI introduction failed: {}", e.getMessage());
            return fallbackIntroduction(p);
        }
    }

    public String generateTourRouteAdvice(List<String> pavilionNames, String season, int durationMinutes) {
        String prompt = String.format(
            "请为滁州亭城游览规划一条路线，包含以下亭子：%s。季节：%s，游览时长：%d分钟。" +
            "请给出具体的行程安排、时间分配和温馨提示。",
            String.join("、", pavilionNames), season, durationMinutes
        );

        if (!aiAvailable) {
            return String.format(
                "【%s游览建议】\n\n推荐游览路线：%s\n预计时长：%d分钟\n建议顺序参观，每个亭子停留约30分钟。",
                season, String.join(" → ", pavilionNames), durationMinutes
            );
        }

        try {
            return callOpenAI(List.of(
                message("system", SYSTEM_PROMPT),
                message("user", prompt)
            ), 600);
        } catch (Exception e) {
            logger.error("AI tour advice failed: {}", e.getMessage());
            return "AI服务暂时不可用，请稍后再试。";
        }
    }

    public boolean isAiAvailable() {
        return aiAvailable;
    }

    public String getActiveProvider() {
        return resolvedProviderName;
    }

    public String generateCulturalIntroduction(String pavilionName, String location) {
        if (aiAvailable) {
            String prompt = String.format(
                "请以导游口吻介绍滁州的亭子「%s」（位于%s），从建筑特色、历史文化、参观建议三方面介绍，200字左右。",
                pavilionName, location);
            try {
                return callOpenAI(List.of(
                    message("system", SYSTEM_PROMPT),
                    message("user", prompt)
                ), 400);
            } catch (Exception e) {
                logger.warn("AI cultural introduction failed, using template: {}", e.getMessage());
            }
        }
        return "【" + pavilionName + "】\n\n" +
            "此亭坐落于" + location + "，承袭滁州千年文脉，与欧阳修《醉翁亭记》之雅韵相映成辉。\n" +
            "正如文中所述'环滁皆山也'，此亭亦处青山绿水之间，四时之景不同，而乐亦无穷也。\n\n" +
            "登临此亭，可观山川之美，可感文人墨客之雅趣，诚为滁州'亭城'文化之又一佳景。\n\n" +
            "春来花满径，夏至柳成荫，秋风送爽意，冬雪覆琼林。";
    }

    public String generateHistoricalStory(String pavilionName, Integer constructionYear) {
        if (aiAvailable) {
            String prompt = String.format(
                "请讲述滁州亭子「%s」的历史故事（始建于%d年），包含建造背景、历史变迁、文化意义，200字左右。",
                pavilionName, constructionYear != null ? constructionYear : 0);
            try {
                return callOpenAI(List.of(
                    message("system", SYSTEM_PROMPT),
                    message("user", prompt)
                ), 400);
            } catch (Exception e) {
                logger.warn("AI historical story failed, using template: {}", e.getMessage());
            }
        }
        StringBuilder story = new StringBuilder();
        story.append("【").append(pavilionName).append("的历史故事】\n\n");
        if (constructionYear != null) {
            story.append("此亭始建于").append(constructionYear).append("年");
            if (constructionYear <= 1000) {
                story.append("，正值宋代文风鼎盛之时，与欧阳修醉翁亭相呼应，承载着深厚的文化底蕴。");
            } else if (constructionYear <= 1900) {
                story.append("，见证了明清两代的兴衰更替，是古代文人雅士聚会论道之所。");
            } else {
                story.append("，体现了现代滁州对传统文化的传承与发扬。");
            }
        } else {
            story.append("此亭虽无确切建造年份记载，但其造型古朴典雅，必为滁州悠久亭文化之一脉相承。");
        }
        story.append("\n\n历代文人墨客多有题咏，留下了诸多珍贵的文化印记。");
        return story.toString();
    }

    public String answerQuestion(String question) {
        return chat(question);
    }

    public String generateTourismAdvice(List<String> pavilions, String season, String duration) {
        return generateTourRouteAdvice(pavilions, season, 120);
    }

    public String getCultureOverview() {
        return "【滁州'亭城'文化概览】\n\n" +
            "滁州，位于安徽省东部，是一座拥有悠久历史的文化名城。因其深厚的'亭文化'积淀，素有'亭城'之美誉。\n\n" +
            "一、历史渊源：\n" +
            "滁州的'亭文化'始于北宋，因欧阳修任滁州知州时写下千古名篇《醉翁亭记》而声名远播。" +
            "文中'醉翁之意不在酒，在乎山水之间也'的名句，使得醉翁亭成为'天下第一亭'。\n\n" +
            "二、代表亭子：\n" +
            "1. 醉翁亭：位于琅琊山麓，是滁州'亭城'文化的象征\n" +
            "2. 丰乐亭：同样由欧阳修建造，与醉翁亭并称为'姐妹亭'\n" +
            "3. 琅琊亭：位于琅琊山顶，俯瞰滁州全景\n" +
            "4. 现代景观亭：近年来新建的体现传统文化与现代设计相结合的景观亭\n\n" +
            "三、文化内涵：\n" +
            "滁州的亭子不仅是休憩之所，更是文人雅士聚会论道、吟诗作赋的文化场所，体现了人与自然和谐共生的理念。\n\n" +
            "四、当代发展：\n" +
            "今天的滁州在保护历史文化遗产的同时，积极发展现代旅游产业，将'亭文化'融入城市建设，打造独具特色的'亭城'品牌。";
    }

    private String callOpenAI(List<ObjectNode> messages, int maxTokens) {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", resolvedModel);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", 0.7);
        ArrayNode messagesArray = requestBody.putArray("messages");
        messages.forEach(messagesArray::add);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resolvedApiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
            resolvedApiUrl, HttpMethod.POST, entity, JsonNode.class
        );

        JsonNode choice = response.getBody().get("choices").get(0);
        return choice.get("message").get("content").asText();
    }

    private ObjectNode message(String role, String content) {
        ObjectNode msg = objectMapper.createObjectNode();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }

    private String templateResponse(String question) {
        if (question.contains("醉翁亭") || question.contains("欧阳修")) {
            return "关于欧阳修与《醉翁亭记》：欧阳修（1007-1072），字永叔，号醉翁，北宋著名文学家。" +
                "《醉翁亭记》作于庆历六年（1046年），当时欧阳修被贬为滁州知州。" +
                "文中'醉翁之意不在酒，在乎山水之间也'传诵千古。" +
                "醉翁亭位于滁州市西南琅琊山麓，始建于北宋庆历七年（1047年），被誉为'天下第一亭'。";
        }
        if (question.contains("亭") && question.contains("滁州")) {
            return "滁州素有'亭城'之美誉。滁州的'亭文化'可追溯到北宋，" +
                "欧阳修任滁州知州时所写《醉翁亭记》使滁州闻名天下。" +
                "如今的滁州保留了醉翁亭、丰乐亭等古迹，也新建了许多现代景观亭。";
        }
        return "感谢您的提问。滁州'亭城'文化融合了自然景观与人文情怀。" +
            "如有具体亭子的疑问，欢迎随时询问。";
    }

    private String fallbackIntroduction(Pavilion p) {
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(p.getChineseName()).append("】\n\n");
        sb.append("此亭坐落于").append(p.getLocationDesc() != null ? p.getLocationDesc() : "滁州");
        if (p.getBuiltYear() != null) {
            sb.append("，始建于").append(p.getBuiltYear()).append("年");
        }
        sb.append("。").append(p.getDescription() != null ? p.getDescription() : "");
        sb.append("\n\n").append(p.getHistoricalSignificance() != null ? p.getHistoricalSignificance() : "");
        return sb.toString();
    }
}
