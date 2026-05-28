package com.tingchenggis.tingcheng.ai;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private PavilionRepository pavilionRepository;

    @Mock
    private RestTemplate restTemplate;

    private AiService aiService;

    private Pavilion p1;

    @BeforeEach
    void setUp() {
        aiService = new AiService(pavilionRepository, null, restTemplate);
        ReflectionTestUtils.setField(aiService, "activeProvider", "openai");
        ReflectionTestUtils.setField(aiService, "openaiApiKey", "");
        ReflectionTestUtils.setField(aiService, "openaiModel", "gpt-3.5-turbo");
        ReflectionTestUtils.setField(aiService, "openaiApiUrl", "https://api.openai.com/v1/chat/completions");
        ReflectionTestUtils.setField(aiService, "deepseekApiKey", "");
        ReflectionTestUtils.setField(aiService, "zhipuApiKey", "");
        aiService.init();

        p1 = new Pavilion("test", "醉翁亭", null, null, 118.3, 32.3, "HISTORICAL");
        p1.setId(1L);
        p1.setBuiltYear(1047);
        p1.setLocationDesc("琅琊山");
        p1.setDescription("醉翁亭位于滁州市琅琊山麓，是滁州'亭城'文化的象征。");
        p1.setHistoricalSignificance("北宋庆历七年（1047年）建，欧阳修命名并作《醉翁亭记》。");
    }

    @Test
    void chat_fallbackWithoutApiKey() {
        assertFalse(aiService.isAiAvailable());
        String reply = aiService.chat("醉翁亭是谁建造的？");
        assertNotNull(reply);
        assertTrue(reply.contains("欧") || reply.contains("醉翁"));
    }

    @Test
    void chat_generalQuestion() {
        String reply = aiService.chat("滁州有什么好玩的地方？");
        assertNotNull(reply);
    }

    @Test
    void generatePavilionIntroduction() {
        when(pavilionRepository.findById(1L)).thenReturn(Optional.of(p1));

        String intro = aiService.generatePavilionIntroduction(1L);
        assertNotNull(intro);
        assertTrue(intro.contains("醉翁亭"));
    }

    @Test
    void generatePavilionIntroduction_notFound() {
        when(pavilionRepository.findById(99L)).thenReturn(Optional.empty());

        String intro = aiService.generatePavilionIntroduction(99L);
        assertEquals("亭子不存在", intro);
    }

    @Test
    void generateTourRouteAdvice() {
        String advice = aiService.generateTourRouteAdvice(
            java.util.List.of("醉翁亭", "丰乐亭"), "春季", 240);
        assertNotNull(advice);
    }

    @Test
    void status_noApiKey() {
        assertFalse(aiService.isAiAvailable());
    }

    @Test
    void providerName_openai() {
        assertEquals("OpenAI", aiService.getActiveProvider());
    }

    @Test
    void switchToDeepseek() {
        AiService dsService = new AiService(pavilionRepository, null, restTemplate);
        ReflectionTestUtils.setField(dsService, "activeProvider", "deepseek");
        ReflectionTestUtils.setField(dsService, "deepseekApiKey", "");
        dsService.init();
        assertFalse(dsService.isAiAvailable());
    }

    @Test
    void generateCulturalIntroduction() {
        String intro = aiService.generateCulturalIntroduction("醉翁亭", "琅琊山");
        assertNotNull(intro);
        assertTrue(intro.contains("醉翁亭"));
        assertTrue(intro.contains("琅琊山"));
    }

    @Test
    void generateHistoricalStory_withYear() {
        String story = aiService.generateHistoricalStory("醉翁亭", 1047);
        assertNotNull(story);
        assertTrue(story.contains("1047"));
    }

    @Test
    void generateHistoricalStory_withoutYear() {
        String story = aiService.generateHistoricalStory("古亭", null);
        assertNotNull(story);
        assertTrue(story.contains("古亭"));
    }

    @Test
    void generateHistoricalStory_songDynasty() {
        String story = aiService.generateHistoricalStory("亭", 960);
        assertTrue(story.contains("宋代"));
    }

    @Test
    void generateHistoricalStory_mingQing() {
        String story = aiService.generateHistoricalStory("亭", 1500);
        assertTrue(story.contains("明清"));
    }

    @Test
    void generateHistoricalStory_modern() {
        String story = aiService.generateHistoricalStory("亭", 2000);
        assertTrue(story.contains("现代"));
    }

    @Test
    void getCultureOverview() {
        String overview = aiService.getCultureOverview();
        assertNotNull(overview);
        assertTrue(overview.contains("亭城"));
    }

    @Test
    void generateTourismAdvice() {
        String advice = aiService.generateTourismAdvice(List.of("醉翁亭", "丰乐亭"), "春季", "半日");
        assertNotNull(advice);
        assertTrue(advice.contains("醉翁亭"));
    }

    @Test
    void answerQuestion() {
        String answer = aiService.answerQuestion("醉翁亭是谁建的？");
        assertNotNull(answer);
        assertTrue(answer.contains("欧阳修") || answer.contains("醉翁"));
    }
}
