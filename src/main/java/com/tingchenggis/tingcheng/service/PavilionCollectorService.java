package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.PavilionCollector;

import java.util.List;
import java.util.Map;

/**
 * 采集记录服务接口
 *
 * @author TingChengGIS
 * @version 1.0.0
 */
public interface PavilionCollectorService {

    PavilionCollector createCollector(PavilionCollector collector);

    List<PavilionCollector> getCollectorsByPavilionId(Long pavilionId);

    void deleteCollectorsByPavilionId(Long pavilionId);

    Map<Long, Long> getCollectorCountByPavilionIds();
}
