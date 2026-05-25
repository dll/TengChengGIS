package com.tingchenggis.tingcheng.repository;

import com.tingchenggis.tingcheng.entity.Pavilion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PavilionRepositoryTest {

    @Autowired
    private PavilionRepository pavilionRepository;

    private Pavilion p1, p2, p3;

    @BeforeEach
    void setUp() {
        pavilionRepository.deleteAll();

        p1 = new Pavilion("zuiweng", "醉翁亭", "desc1", "POINT(118.3 32.3)", 118.3, 32.3, "HISTORICAL");
        p1.setBuiltYear(1047);
        p1.setVisitorRating(4.8);
        p1.setIsOpenToPublic(true);
        p1.setArchitecturalStyle("宋代");

        p2 = new Pavilion("fengle", "丰乐亭", "desc2", "POINT(118.4 32.4)", 118.4, 32.4, "HISTORICAL");
        p2.setBuiltYear(1046);
        p2.setVisitorRating(4.5);
        p2.setIsOpenToPublic(true);
        p2.setArchitecturalStyle("宋代");

        p3 = new Pavilion("modern", "现代亭", "desc3", "POINT(118.5 32.5)", 118.5, 32.5, "MODERN");
        p3.setBuiltYear(2020);
        p3.setVisitorRating(3.0);
        p3.setIsOpenToPublic(false);
        p3.setArchitecturalStyle("现代");

        pavilionRepository.saveAll(List.of(p1, p2, p3));
    }

    @Test
    void findByPavilionType() {
        List<Pavilion> result = pavilionRepository.findByPavilionType("HISTORICAL");
        assertEquals(2, result.size());
    }

    @Test
    void findByNameContainingIgnoreCase() {
        List<Pavilion> result = pavilionRepository.findByNameContainingIgnoreCase("zui");
        assertEquals(1, result.size());
        assertEquals("醉翁亭", result.get(0).getChineseName());
    }

    @Test
    void findByNameContainingIgnoreCase_partial() {
        List<Pavilion> result = pavilionRepository.findByNameContainingIgnoreCase("zui");
        assertEquals(1, result.size());
    }

    @Test
    void findByBuiltYearBetween() {
        List<Pavilion> result = pavilionRepository.findByBuiltYearBetween(1000, 2000);
        assertEquals(2, result.size());
    }

    @Test
    void findByBuiltYearBetween_empty() {
        List<Pavilion> result = pavilionRepository.findByBuiltYearBetween(2001, 2025);
        assertEquals(1, result.size());
    }

    @Test
    void findByVisitorRatingGreaterThanEqual() {
        List<Pavilion> result = pavilionRepository.findByVisitorRatingGreaterThanEqual(4.0);
        assertEquals(2, result.size());
    }

    @Test
    void findByIsOpenToPublicTrue() {
        List<Pavilion> result = pavilionRepository.findByIsOpenToPublicTrue();
        assertEquals(2, result.size());
    }

    @Test
    void findByArchitecturalStyle() {
        List<Pavilion> result = pavilionRepository.findByArchitecturalStyle("现代");
        assertEquals(1, result.size());
    }

    @Test
    void saveAndFindById() {
        Pavilion saved = pavilionRepository.save(p1);
        Pavilion found = pavilionRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("醉翁亭", found.getChineseName());
    }

    @Test
    void deleteById() {
        List<Pavilion> before = pavilionRepository.findAll();
        pavilionRepository.deleteById(p1.getId());
        List<Pavilion> after = pavilionRepository.findAll();
        assertEquals(before.size() - 1, after.size());
    }

    @Test
    void findAll() {
        List<Pavilion> all = pavilionRepository.findAll();
        assertEquals(3, all.size());
    }
}
