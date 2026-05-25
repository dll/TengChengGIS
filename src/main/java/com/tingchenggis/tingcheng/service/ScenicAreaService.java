package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.ScenicArea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ScenicAreaService {

    ScenicArea createScenicArea(ScenicArea scenicArea);

    Optional<ScenicArea> getScenicAreaById(Long id);

    Page<ScenicArea> getAllScenicAreas(Pageable pageable);

    List<ScenicArea> getAllScenicAreas();

    ScenicArea updateScenicArea(Long id, ScenicArea scenicArea);

    void deleteScenicArea(Long id);

    List<ScenicArea> findByAreaType(String areaType);

    List<ScenicArea> findByNameContaining(String name);

    List<ScenicArea> findByGeographicRange(String wktText);

    Map<String, Object> getStats();
}
