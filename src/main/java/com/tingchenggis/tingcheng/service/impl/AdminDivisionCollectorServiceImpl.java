package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.AdminDivisionCollector;
import com.tingchenggis.tingcheng.repository.AdminDivisionCollectorRepository;
import com.tingchenggis.tingcheng.service.AdminDivisionCollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminDivisionCollectorServiceImpl implements AdminDivisionCollectorService {

    private static final Logger logger = LoggerFactory.getLogger(AdminDivisionCollectorServiceImpl.class);

    private final AdminDivisionCollectorRepository collectorRepository;

    public AdminDivisionCollectorServiceImpl(AdminDivisionCollectorRepository collectorRepository) {
        this.collectorRepository = collectorRepository;
    }

    @Override
    public AdminDivisionCollector createCollector(AdminDivisionCollector collector) {
        return collectorRepository.save(collector);
    }

    @Override
    public List<AdminDivisionCollector> getCollectorsByAdminDivisionId(Long adminDivisionId) {
        return collectorRepository.findByAdminDivisionIdOrderByCollectionTimeDesc(adminDivisionId);
    }

    @Override
    public void deleteCollectorsByAdminDivisionId(Long adminDivisionId) {
        collectorRepository.deleteByAdminDivisionId(adminDivisionId);
    }

    @Override
    public Map<Long, Long> getCollectorCountByAdminDivisionIds() {
        List<Object[]> rows = collectorRepository.countGroupByAdminDivisionId();
        Map<Long, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }
}
