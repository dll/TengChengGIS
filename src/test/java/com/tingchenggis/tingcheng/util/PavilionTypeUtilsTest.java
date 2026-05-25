package com.tingchenggis.tingcheng.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PavilionTypeUtilsTest {

    @Test
    void normalize_nullReturnsHistorical() {
        assertEquals("HISTORICAL", PavilionTypeUtils.normalize(null));
    }

    @Test
    void normalize_blankReturnsHistorical() {
        assertEquals("HISTORICAL", PavilionTypeUtils.normalize("  "));
    }

    @Test
    void normalize_chineseHistorical() {
        assertEquals("HISTORICAL", PavilionTypeUtils.normalize("历史文化亭"));
    }

    @Test
    void normalize_chineseModern() {
        assertEquals("MODERN", PavilionTypeUtils.normalize("现代景观亭"));
    }

    @Test
    void normalize_chineseCultural() {
        assertEquals("CULTURAL", PavilionTypeUtils.normalize("文化主题亭"));
    }

    @Test
    void normalize_english() {
        assertEquals("HISTORICAL", PavilionTypeUtils.normalize("historical"));
        assertEquals("MODERN", PavilionTypeUtils.normalize("MODERN"));
        assertEquals("CULTURAL", PavilionTypeUtils.normalize("Cultural"));
    }

    @Test
    void normalize_unknown() {
        assertEquals("OTHER", PavilionTypeUtils.normalize("other"));
    }

    @Test
    void toLabel_nullReturnsUnknown() {
        assertEquals("未知类型", PavilionTypeUtils.toLabel(null));
    }

    @Test
    void toLabel_knownTypes() {
        assertEquals("历史文化亭", PavilionTypeUtils.toLabel("HISTORICAL"));
        assertEquals("现代景观亭", PavilionTypeUtils.toLabel("MODERN"));
        assertEquals("文化主题亭", PavilionTypeUtils.toLabel("CULTURAL"));
    }

    @Test
    void inferType_historicalKeywords() {
        assertEquals("HISTORICAL", PavilionTypeUtils.inferType("醉翁亭", null, null));
        assertEquals("HISTORICAL", PavilionTypeUtils.inferType(null, null, "琅琊山"));
        assertEquals("HISTORICAL", PavilionTypeUtils.inferType("丰乐亭", null, null));
        assertEquals("HISTORICAL", PavilionTypeUtils.inferType("古亭", null, null));
        assertEquals("HISTORICAL", PavilionTypeUtils.inferType("遗址亭", null, null));
    }

    @Test
    void inferType_modernStructure() {
        assertEquals("MODERN", PavilionTypeUtils.inferType("新亭", "钢结构", null));
        assertEquals("MODERN", PavilionTypeUtils.inferType("新亭", "玻璃顶", null));
        assertEquals("MODERN", PavilionTypeUtils.inferType("新亭", "塑料", null));
    }

    @Test
    void inferType_defaultCultural() {
        assertEquals("CULTURAL", PavilionTypeUtils.inferType("普通亭", "木结构", null));
        assertEquals("CULTURAL", PavilionTypeUtils.inferType(null, null, null));
    }

    @Test
    void matchesFilter_nullFilter() {
        assertTrue(PavilionTypeUtils.matchesFilter("HISTORICAL", null));
        assertTrue(PavilionTypeUtils.matchesFilter("MODERN", ""));
        assertTrue(PavilionTypeUtils.matchesFilter("CULTURAL", "ALL"));
    }

    @Test
    void matchesFilter_exactMatch() {
        assertTrue(PavilionTypeUtils.matchesFilter("HISTORICAL", "HISTORICAL"));
        assertFalse(PavilionTypeUtils.matchesFilter("HISTORICAL", "MODERN"));
    }

    @Test
    void matchesFilter_normalizedMatch() {
        assertTrue(PavilionTypeUtils.matchesFilter("历史文化亭", "历史文化亭"));
    }
}
