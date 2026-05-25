package com.tingchenggis.tingcheng.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI服务类
 * 
 * 提供与滁州亭城文化和《醉翁亭记》相关的AI功能
 * 
 * @author TingChengGIS
 * @version 1.0.0
 */
@Service
public class PavilionAIService {

    private static final Logger logger = LoggerFactory.getLogger(PavilionAIService.class);

    public PavilionAIService() {
        logger.info("Initializing Pavilion AI Service for Chuzhou Tingcheng cultural analysis");
    }

    /**
     * 根据《醉翁亭记》文化背景生成亭子介绍
     * 
     * @param pavilionName 亭子名称
     * @param location 地理位置
     * @return 文化背景丰富的介绍文本
     */
    public String generateCulturalIntroduction(String pavilionName, String location) {
        logger.info("Generating cultural introduction for pavilion: {} at {}", pavilionName, location);
        
        // 这里是一个模拟实现，实际使用时应该调用真实的AI服务
        StringBuilder intro = new StringBuilder();
        intro.append("【").append(pavilionName).append("】\n\n");
        intro.append("此亭坐落于").append(location).append("，承袭滁州千年文脉，与欧阳修《醉翁亭记》之雅韵相映成辉。\n\n");
        intro.append("正如文中所述'环滁皆山也'，此亭亦处青山绿水之间，四时之景不同，而乐亦无穷也。\n\n");
        intro.append("登临此亭，可观山川之美，可感文人墨客之雅趣，诚为滁州'亭城'文化之又一佳景。\n\n");
        intro.append("春来花满径，夏至柳成荫，秋风送爽意，冬雪覆琼林。四季更迭，各有其趣，正应了'醉翁之意不在酒，在乎山水之间也'之妙境。");
        
        return intro.toString();
    }

    /**
     * 生成历史背景故事
     * 
     * @param pavilionName 亭子名称
     * @param constructionYear 建造年份
     * @return 历史背景故事
     */
    public String generateHistoricalStory(String pavilionName, Integer constructionYear) {
        logger.info("Generating historical story for pavilion: {} built in {}", pavilionName, constructionYear);
        
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

    /**
     * AI智能问答服务
     * 
     * @param question 用户问题
     * @return AI生成的答案
     */
    public String answerQuestion(String question) {
        logger.info("Answering question: {}", question);
        
        // 检测是否与滁州亭城文化相关的问题
        if (question.toLowerCase().contains("醉翁亭") || question.toLowerCase().contains("欧阳修")) {
            return generateOuyangxiuResponse(question);
        } else if (question.toLowerCase().contains("亭") && question.toLowerCase().contains("滁州")) {
            return generateTingchengResponse(question);
        } else {
            return generateGeneralResponse(question);
        }
    }

    /**
     * 生成与欧阳修和《醉翁亭记》相关的回答
     * 
     * @param question 用户问题
     * @return 与欧阳修相关的回答
     */
    private String generateOuyangxiuResponse(String question) {
        StringBuilder response = new StringBuilder();
        response.append("关于欧阳修与《醉翁亭记》，这可是滁州文化的瑰宝。\n\n");
        response.append("欧阳修（1007-1072），字永叔，号醉翁，北宋著名文学家，唐宋八大家之一。\n\n");
        response.append("《醉翁亭记》作于庆历六年（1046年），当时欧阳修被贬为滁州知州。文中'醉翁之意不在酒，在乎山水之间也'一句传诵千古。\n\n");
        response.append("醉翁亭位于滁州市西南琅琊山麓，始建于北宋庆历七年（1047年），被誉为'天下第一亭'，是滁州'亭城'文化的象征。\n\n");
        response.append("您想了解关于醉翁亭的哪些方面呢？比如它的建筑特色、历史变迁，还是与欧阳修相关的其他典故？");
        
        return response.toString();
    }

    /**
     * 生成与滁州亭城文化相关的回答
     * 
     * @param question 用户问题
     * @return 与亭城相关的回答
     */
    private String generateTingchengResponse(String question) {
        StringBuilder response = new StringBuilder();
        response.append("滁州素有'亭城'之美誉，这源于深厚的历史文化底蕴。\n\n");
        response.append("滁州的'亭文化'可以追溯到北宋时期，欧阳修任滁州知州时所写的《醉翁亭记》使滁州闻名天下。\n\n");
        response.append("如今的滁州不仅保留了历史悠久的醉翁亭、丰乐亭等古迹，还新建了许多具有现代特色的景观亭，形成了独特的'亭城'风貌。\n\n");
        response.append("滁州的亭子不仅是休憩之所，更是文化的载体，体现了人与自然和谐共生的理念。\n\n");
        response.append("如果您对某个具体的亭子感兴趣，我可以为您详细介绍其历史背景和文化内涵。");
        
        return response.toString();
    }

    /**
     * 生成一般性回答
     * 
     * @param question 用户问题
     * @return 一般性回答
     */
    private String generateGeneralResponse(String question) {
        StringBuilder response = new StringBuilder();
        response.append("感谢您的提问：").append(question).append("\n\n");
        response.append("滁州'亭城'文化博大精深，融合了自然景观与人文情怀。无论您是对历史古迹感兴趣，还是想了解现代景观，滁州的亭子都能为您提供独特的体验。\n\n");
        response.append("如果您有关于滁州亭子的具体问题，比如位置、历史、建筑特色等，欢迎随时询问，我将尽力为您解答。");
        
        return response.toString();
    }

    /**
     * 生成游览建议
     * 
     * @param pavilions 亭子列表
     * @param season 季节
     * @param duration 游览时长
     * @return 游览路线建议
     */
    public String generateTourismAdvice(List<String> pavilions, String season, String duration) {
        logger.info("Generating tourism advice for pavilions: {} in {} for {}", pavilions, season, duration);
        
        StringBuilder advice = new StringBuilder();
        advice.append("【滁州亭城游览建议】\n\n");
        advice.append("根据您的需求，为您规划了一条").append(season).append("时节的").append(duration).append("游览路线：\n\n");
        
        advice.append("上午行程：\n");
        if (pavilions.size() > 0) {
            advice.append("- 首先前往").append(pavilions.get(0)).append("，感受古朴典雅的文化氛围\n");
        }
        
        advice.append("中午休息：在附近品尝滁州特色美食\n\n");
        
        advice.append("下午行程：\n");
        if (pavilions.size() > 1) {
            advice.append("- 参观").append(pavilions.get(1)).append("，体验不同的建筑风格\n");
        }
        if (pavilions.size() > 2) {
            advice.append("- 最后游览").append(pavilions.get(2)).append("，结束愉快的亭城之旅\n");
        }
        
        advice.append("\n温馨提示：").append(season).append("时节滁州气候宜人，适合户外活动，请合理安排时间，享受'环滁皆山'的美景。");
        
        return advice.toString();
    }
}