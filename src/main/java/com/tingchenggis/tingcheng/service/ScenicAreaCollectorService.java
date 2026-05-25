package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.ScenicAreaCollector;

import java.util.List;
import java.util.Map;

public interface ScenicAreaCollectorService {

    ScenicAreaCollector createCollector(ScenicAreaCollector collector);

    List<ScenicAreaCollector> getCollectorsByScenicAreaId(Long scenicAreaId);

    void deleteCollectorsByScenicAreaId(Long scenicAreaId);

    Map<Long, Long> getCollectorCountByScenicAreaIds();
}
