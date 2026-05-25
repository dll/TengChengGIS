package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.ScenicAreaCollector;
import com.tingchenggis.tingcheng.repository.ScenicAreaCollectorRepository;
import com.tingchenggis.tingcheng.service.ScenicAreaCollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScenicAreaCollectorServiceImpl implements ScenicAreaCollectorService {

    private static final Logger logger = LoggerFactory.getLogger(ScenicAreaCollectorServiceImpl.class);

    private final ScenicAreaCollectorRepository collectorRepository;

    public ScenicAreaCollectorServiceImpl(ScenicAreaCollectorRepository collectorRepository) {
        this.collectorRepository = collectorRepository;
    }

    @Override
    public ScenicAreaCollector createCollector(ScenicAreaCollector collector) {
        return collectorRepository.save(collector);
    }

    @Override
    public List<ScenicAreaCollector> getCollectorsByScenicAreaId(Long scenicAreaId) {
        return collectorRepository.findByScenicAreaIdOrderByCollectionTimeDesc(scenicAreaId);
    }

    @Override
    public void deleteCollectorsByScenicAreaId(Long scenicAreaId) {
        collectorRepository.deleteByScenicAreaId(scenicAreaId);
    }

    @Override
    public Map<Long, Long> getCollectorCountByScenicAreaIds() {
        List<Object[]> rows = collectorRepository.countGroupByScenicAreaId();
        Map<Long, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }
}
