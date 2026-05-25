package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import com.tingchenggis.tingcheng.service.PavilionCollectorService;
import com.tingchenggis.tingcheng.service.PavilionService;
import com.tingchenggis.tingcheng.service.PavilionStats;
import com.tingchenggis.tingcheng.service.ThousandPavilionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 亭子业务逻辑实现类
 *
 * 实现亭子相关的业务操作
 *
 * @author TingChengGIS
 * @version 1.0.0
 */
@Service
@Transactional
public class PavilionServiceImpl implements PavilionService {

    private static final Logger logger = LoggerFactory.getLogger(PavilionServiceImpl.class);

    private final PavilionRepository pavilionRepository;
    private final ThousandPavilionsService thousandPavilionsService;
    private final PavilionCollectorService collectorService;

    public PavilionServiceImpl(PavilionRepository pavilionRepository,
                               ThousandPavilionsService thousandPavilionsService,
                               PavilionCollectorService collectorService) {
        this.pavilionRepository = pavilionRepository;
        this.thousandPavilionsService = thousandPavilionsService;
        this.collectorService = collectorService;
    }

    @Override
    public Pavilion createPavilion(Pavilion pavilion) {
        logger.info("Creating new pavilion: {}", pavilion.getName());
        pavilion.setId(null);

        if (pavilion.getLatitude() == null || pavilion.getLongitude() == null) {
            pavilion.setLatitude(0.0);
            pavilion.setLongitude(0.0);
            pavilion.setGeomWkt("POINT(0 0)");
        }

        Pavilion savedPavilion = pavilionRepository.save(pavilion);
        logger.info("Successfully created pavilion with ID: {}", savedPavilion.getId());
        return savedPavilion;
    }

    @Override
    public Optional<Pavilion> getPavilionById(Long id) {
        logger.info("根据ID获取亭子: {}", id);
        Optional<Pavilion> pavilion = pavilionRepository.findById(id);
        if (pavilion.isPresent()) {
            logger.debug("找到亭子: {}", pavilion.get().getName());
        } else {
            logger.warn("未找到ID为 {} 的亭子", id);
        }
        return pavilion;
    }

    @Override
    public Page<Pavilion> getAllPavilions(Pageable pageable) {
        logger.info("获取分页亭子列表，页码: {}, 大小: {}", pageable.getPageNumber(), pageable.getPageSize());
        return pavilionRepository.findAll(pageable);
    }

    @Override
    public List<Pavilion> getAllPavilions() {
        logger.info("获取所有亭子列表");
        return pavilionRepository.findAll();
    }

    @Override
    public Pavilion updatePavilion(Long id, Pavilion pavilion) {
        logger.info("Updating pavilion with ID: {}", id);

        Optional<Pavilion> existingPavilionOpt = pavilionRepository.findById(id);
        if (!existingPavilionOpt.isPresent()) {
            throw new RuntimeException("Pavilion not found with id: " + id);
        }

        Pavilion existingPavilion = existingPavilionOpt.get();
        existingPavilion.setName(pavilion.getName());
        existingPavilion.setChineseName(pavilion.getChineseName());
        existingPavilion.setDescription(pavilion.getDescription());
        existingPavilion.setHistoricalSignificance(pavilion.getHistoricalSignificance());
        existingPavilion.setGeomWkt(pavilion.getGeomWkt());
        existingPavilion.setLongitude(pavilion.getLongitude());
        existingPavilion.setLatitude(pavilion.getLatitude());
        existingPavilion.setPavilionType(pavilion.getPavilionType());
        existingPavilion.setConstructionPeriod(pavilion.getConstructionPeriod());
        existingPavilion.setArchitecturalStyle(pavilion.getArchitecturalStyle());
        existingPavilion.setAreaSize(pavilion.getAreaSize());
        existingPavilion.setVisitorRating(pavilion.getVisitorRating());
        existingPavilion.setIsOpenToPublic(pavilion.getIsOpenToPublic());
        existingPavilion.setTicketPrice(pavilion.getTicketPrice());
        existingPavilion.setBuiltYear(pavilion.getBuiltYear());
        existingPavilion.setLastRenovationYear(pavilion.getLastRenovationYear());
        existingPavilion.setStructure(pavilion.getStructure());
        existingPavilion.setTopStyle(pavilion.getTopStyle());
        existingPavilion.setStreet(pavilion.getStreet());
        existingPavilion.setNotes(pavilion.getNotes());
        existingPavilion.setLocationDesc(pavilion.getLocationDesc());

        Pavilion updatedPavilion = pavilionRepository.save(existingPavilion);
        logger.info("Successfully updated pavilion with ID: {}", updatedPavilion.getId());
        return updatedPavilion;
    }

    @Override
    public void deletePavilion(Long id) {
        logger.info("Deleting pavilion with ID: {}", id);
        if (!pavilionRepository.existsById(id)) {
            throw new RuntimeException("Pavilion not found with id: " + id);
        }
        collectorService.deleteCollectorsByPavilionId(id);
        pavilionRepository.deleteById(id);
        logger.info("Successfully deleted pavilion with ID: {}", id);
    }

    @Override
    public List<Pavilion> findByPavilionType(String pavilionType) {
        logger.info("根据亭子类型查询: {}", pavilionType);
        List<Pavilion> pavilions = pavilionRepository.findByPavilionType(pavilionType);
        logger.debug("找到 {} 个亭子", pavilions.size());
        return pavilions;
    }

    @Override
    public List<Pavilion> findByNameContaining(String name) {
        logger.info("根据名称模糊查询: {}", name);
        return pavilionRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Pavilion> findByBuiltYearBetween(Integer startYear, Integer endYear) {
        logger.info("根据年代区间查询: {} - {}", startYear, endYear);
        return pavilionRepository.findByBuiltYearBetween(startYear, endYear);
    }

    @Override
    public List<Pavilion> findByVisitorRatingGreaterThanEqual(Double minRating) {
        logger.info("根据评分查询，最低评分: {}", minRating);
        return pavilionRepository.findByVisitorRatingGreaterThanEqual(minRating);
    }

    @Override
    public List<Pavilion> findByGeographicRange(String wktText) {
        logger.info("根据地理位置范围查询: {}", wktText);
        return pavilionRepository.findByGeographicRange(wktText);
    }

    @Override
    public List<Pavilion> findOpenToPublic() {
        logger.info("查询开放参观的亭子");
        return pavilionRepository.findByIsOpenToPublicTrue();
    }

    @Override
    public List<Pavilion> findByArchitecturalStyle(String architecturalStyle) {
        logger.info("根据建筑风格查询亭子: {}", architecturalStyle);
        return pavilionRepository.findByArchitecturalStyle(architecturalStyle);
    }

    @Override
    public PavilionStats getStats() {
        logger.debug("Calculating pavilion statistics");

        PavilionStats stats = new PavilionStats();
        stats.setTotalPavilions((long) pavilionRepository.findAll().size());
        stats.setHistoricalPavilions((long) pavilionRepository.findByPavilionType("HISTORICAL").size());
        stats.setModernPavilions((long) pavilionRepository.findByPavilionType("MODERN").size());
        stats.setCulturalPavilions((long) pavilionRepository.findByPavilionType("CULTURAL").size());

        List<Pavilion> allPavilions = pavilionRepository.findAll();
        if (!allPavilions.isEmpty()) {
            double sumRatings = allPavilions.stream()
                    .mapToDouble(p -> p.getVisitorRating() != null ? p.getVisitorRating() : 0.0)
                    .sum();
            stats.setAverageRating(sumRatings / allPavilions.size());
        } else {
            stats.setAverageRating(0.0);
        }

        Pavilion mostPopular = allPavilions.stream()
                .max((p1, p2) -> {
                    Double rating1 = p1.getVisitorRating() != null ? p1.getVisitorRating() : 0.0;
                    Double rating2 = p2.getVisitorRating() != null ? p2.getVisitorRating() : 0.0;
                    return rating1.compareTo(rating2);
                })
                .orElse(null);

        stats.setMostPopularPavilion(mostPopular != null ? mostPopular.getName() : "N/A");

        return stats;
    }

    @Override
    public List<Pavilion> recommendPavilions(String userId, String preferences) {
        logger.info("Generating recommendations for user: {} with preferences: {}", userId, preferences);

        if (preferences.contains("historical")) {
            return findByPavilionType("HISTORICAL");
        } else if (preferences.contains("modern")) {
            return findByPavilionType("MODERN");
        } else if (preferences.contains("cultural")) {
            return findByPavilionType("CULTURAL");
        } else {
            return findByVisitorRatingGreaterThanEqual(4.0);
        }
    }

    @Override
    public double calculateDistance(Long pavilionId1, Long pavilionId2) {
        return thousandPavilionsService.calculateDistance(pavilionId1, pavilionId2);
    }

    @Override
    public List<Long> getOptimalTraversalRoute() {
        return thousandPavilionsService.getOptimalTraversalRoute();
    }

    @Override
    public Map<String, Object> getSmartTourPlan(String startId, String endId, int duration, String preference) {
        return thousandPavilionsService.getSmartTourPlan(startId, endId, duration, preference);
    }

    @Override
    public Map<String, Object> getNearbyFacilities(Long pavilionId, double radiusKm) {
        return thousandPavilionsService.getNearbyFacilities(pavilionId, radiusKm);
    }

    @Override
    public Map<String, Object> getWeatherInfo() {
        return thousandPavilionsService.getWeatherInfo();
    }

    @Override
    public Map<String, Object> generateShareableRoute(List<Long> pavilionIds, String routeName) {
        return thousandPavilionsService.generateShareableRoute(pavilionIds, routeName);
    }

    @Override
    public Map<String, Object> getVRExperience(Long pavilionId) {
        return thousandPavilionsService.getVRExperience(pavilionId);
    }
}
