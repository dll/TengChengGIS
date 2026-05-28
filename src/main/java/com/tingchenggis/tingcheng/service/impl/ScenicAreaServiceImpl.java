package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.ScenicArea;
import com.tingchenggis.tingcheng.repository.ScenicAreaRepository;
import com.tingchenggis.tingcheng.service.ScenicAreaCollectorService;
import com.tingchenggis.tingcheng.service.ScenicAreaService;
import com.tingchenggis.tingcheng.util.GeoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ScenicAreaServiceImpl implements ScenicAreaService {

    private static final Logger logger = LoggerFactory.getLogger(ScenicAreaServiceImpl.class);

    private final ScenicAreaRepository scenicAreaRepository;
    private final ScenicAreaCollectorService collectorService;

    public ScenicAreaServiceImpl(ScenicAreaRepository scenicAreaRepository,
                                  ScenicAreaCollectorService collectorService) {
        this.scenicAreaRepository = scenicAreaRepository;
        this.collectorService = collectorService;
    }

    @Override
    public ScenicArea createScenicArea(ScenicArea scenicArea) {
        logger.info("Creating scenic area: {}", scenicArea.getName());
        scenicArea.setId(null);
        return scenicAreaRepository.save(scenicArea);
    }

    @Override
    public Optional<ScenicArea> getScenicAreaById(Long id) {
        return scenicAreaRepository.findById(id);
    }

    @Override
    public Page<ScenicArea> getAllScenicAreas(Pageable pageable) {
        return scenicAreaRepository.findAll(pageable);
    }

    @Override
    public List<ScenicArea> getAllScenicAreas() {
        return scenicAreaRepository.findAll();
    }

    @Override
    public ScenicArea updateScenicArea(Long id, ScenicArea scenicArea) {
        ScenicArea existing = scenicAreaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ScenicArea not found: " + id));
        existing.setName(scenicArea.getName());
        existing.setChineseName(scenicArea.getChineseName());
        existing.setDescription(scenicArea.getDescription());
        existing.setAreaType(scenicArea.getAreaType());
        existing.setAreaSize(scenicArea.getAreaSize());
        existing.setGeomWkt(scenicArea.getGeomWkt());
        existing.setBoundaryWkt(scenicArea.getBoundaryWkt());
        existing.setLongitude(scenicArea.getLongitude());
        existing.setLatitude(scenicArea.getLatitude());
        existing.setAddress(scenicArea.getAddress());
        existing.setOpeningHours(scenicArea.getOpeningHours());
        existing.setTicketPrice(scenicArea.getTicketPrice());
        existing.setVisitorRating(scenicArea.getVisitorRating());
        existing.setIsOpenToPublic(scenicArea.getIsOpenToPublic());
        existing.setNotes(scenicArea.getNotes());
        return scenicAreaRepository.save(existing);
    }

    @Override
    public void deleteScenicArea(Long id) {
        if (!scenicAreaRepository.existsById(id))
            throw new RuntimeException("ScenicArea not found: " + id);
        collectorService.deleteCollectorsByScenicAreaId(id);
        scenicAreaRepository.deleteById(id);
    }

    @Override
    public List<ScenicArea> findByAreaType(String areaType) {
        return scenicAreaRepository.findByAreaType(areaType);
    }

    @Override
    public List<ScenicArea> findByNameContaining(String name) {
        return scenicAreaRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<ScenicArea> findByGeographicRange(String wktText) {
        double[] bbox = GeoUtils.parseWktBbox(wktText);
        if (bbox == null) {
            return List.of();
        }
        return scenicAreaRepository.findByGeographicRange(bbox[0], bbox[1], bbox[2], bbox[3]);
    }

    @Override
    public Map<String, Object> getStats() {
        List<ScenicArea> all = scenicAreaRepository.findAll();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", all.size());

        Map<String, Long> typeCounts = new LinkedHashMap<>();
        for (ScenicArea sa : all) {
            String t = sa.getAreaType() != null ? sa.getAreaType() : "未分类";
            typeCounts.merge(t, 1L, Long::sum);
        }
        stats.put("byType", typeCounts);

        double avgRating = all.stream()
            .mapToDouble(sa -> sa.getVisitorRating() != null ? sa.getVisitorRating() : 0)
            .average().orElse(0);
        stats.put("averageRating", Math.round(avgRating * 10.0) / 10.0);

        long openCount = all.stream().filter(sa -> Boolean.TRUE.equals(sa.getIsOpenToPublic())).count();
        stats.put("openToPublic", openCount);

        return stats;
    }
}
