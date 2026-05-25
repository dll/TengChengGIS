package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.PavilionCollector;
import com.tingchenggis.tingcheng.repository.PavilionCollectorRepository;
import com.tingchenggis.tingcheng.service.PavilionCollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 采集记录服务实现
 *
 * @author TingChengGIS
 * @version 1.0.0
 */
@Service
public class PavilionCollectorServiceImpl implements PavilionCollectorService {

    private static final Logger logger = LoggerFactory.getLogger(PavilionCollectorServiceImpl.class);

    private final PavilionCollectorRepository collectorRepository;

    public PavilionCollectorServiceImpl(PavilionCollectorRepository collectorRepository) {
        this.collectorRepository = collectorRepository;
    }

    @Override
    public PavilionCollector createCollector(PavilionCollector collector) {
        return collectorRepository.save(collector);
    }

    @Override
    public List<PavilionCollector> getCollectorsByPavilionId(Long pavilionId) {
        return collectorRepository.findByPavilionIdOrderByCollectionTimeDesc(pavilionId);
    }

    @Override
    public void deleteCollectorsByPavilionId(Long pavilionId) {
        collectorRepository.deleteByPavilionId(pavilionId);
    }

    @Override
    public Map<Long, Long> getCollectorCountByPavilionIds() {
        List<Object[]> rows = collectorRepository.countGroupByPavilionId();
        Map<Long, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }
}
