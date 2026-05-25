package com.tingchenggis.tingcheng.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PavilionAIServiceTest {

    private PavilionAIService service;

    @BeforeEach
    void setUp() {
        service = new PavilionAIService();
    }

    @Test
    void generateCulturalIntroduction() {
        String intro = service.generateCulturalIntroduction("醉翁亭", "琅琊山");
        assertNotNull(intro);
        assertTrue(intro.contains("醉翁亭"));
        assertTrue(intro.contains("琅琊山"));
        assertTrue(intro.contains("醉翁之意不在酒"));
    }

    @Test
    void generateHistoricalStory_withYear() {
        String story = service.generateHistoricalStory("醉翁亭", 1047);
        assertNotNull(story);
        assertTrue(story.contains("1047"));
        assertTrue(story.contains("醉翁亭"));
    }

    @Test
    void generateHistoricalStory_withoutYear() {
        String story = service.generateHistoricalStory("古亭", null);
        assertNotNull(story);
        assertTrue(story.contains("古亭"));
    }

    @Test
    void generateHistoricalStory_songDynasty() {
        String story = service.generateHistoricalStory("亭", 960);
        assertTrue(story.contains("宋代"));
    }

    @Test
    void generateHistoricalStory_mingQing() {
        String story = service.generateHistoricalStory("亭", 1500);
        assertTrue(story.contains("明清"));
    }

    @Test
    void generateHistoricalStory_modern() {
        String story = service.generateHistoricalStory("亭", 2000);
        assertTrue(story.contains("现代"));
    }

    @Test
    void answerQuestion_zuiweng() {
        String answer = service.answerQuestion("醉翁亭是谁建的？");
        assertNotNull(answer);
        assertTrue(answer.contains("欧阳修"));
        assertTrue(answer.contains("醉翁亭记"));
    }

    @Test
    void answerQuestion_ouyangxiu() {
        String answer = service.answerQuestion("欧阳修的生平？");
        assertNotNull(answer);
        assertTrue(answer.contains("欧阳修"));
    }

    @Test
    void answerQuestion_tingcheng() {
        String answer = service.answerQuestion("滁州有哪些亭子？");
        assertNotNull(answer);
        assertTrue(answer.contains("亭城"));
    }

    @Test
    void answerQuestion_general() {
        String answer = service.answerQuestion("你好");
        assertNotNull(answer);
    }

    @Test
    void generateTourismAdvice() {
        String advice = service.generateTourismAdvice(List.of("醉翁亭", "丰乐亭"), "春季", "半日");
        assertNotNull(advice);
        assertTrue(advice.contains("醉翁亭"));
        assertTrue(advice.contains("丰乐亭"));
        assertTrue(advice.contains("春季"));
    }

    @Test
    void generateTourismAdvice_singlePavilion() {
        String advice = service.generateTourismAdvice(List.of("醉翁亭"), "秋季", "一日");
        assertNotNull(advice);
        assertTrue(advice.contains("醉翁亭"));
    }
}
