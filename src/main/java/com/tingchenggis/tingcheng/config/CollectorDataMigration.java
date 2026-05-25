package com.tingchenggis.tingcheng.config;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.entity.PavilionCollector;
import com.tingchenggis.tingcheng.repository.PavilionRepository;
import com.tingchenggis.tingcheng.service.PavilionCollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据迁移：为现有亭子创建默认采集记录
 *
 * @author TingChengGIS
 * @version 1.0.0
 */
@Component
public class CollectorDataMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CollectorDataMigration.class);

    private final PavilionRepository pavilionRepository;
    private final PavilionCollectorService collectorService;

    public CollectorDataMigration(PavilionRepository pavilionRepository,
                                   PavilionCollectorService collectorService) {
        this.pavilionRepository = pavilionRepository;
        this.collectorService = collectorService;
    }

    @Override
    public void run(String... args) {
        try {
            List<Pavilion> pavilions = pavilionRepository.findAll();
            if (pavilions.isEmpty()) {
                return;
            }

            var countMap = collectorService.getCollectorCountByPavilionIds();
            int created = 0;

            for (Pavilion pavilion : pavilions) {
                if (countMap.containsKey(pavilion.getId())) {
                    continue;
                }

                PavilionCollector collector = new PavilionCollector();
                collector.setPavilionId(pavilion.getId());
                collector.setCollectorName("系统导入");
                collector.setCollectionTool("Excel批量导入");
                collector.setDataSource("Excel批量导入");
                collector.setCollectionTime(LocalDateTime.now());
                collector.setNotes("数据迁移自动创建");
                collectorService.createCollector(collector);
                created++;
            }

            if (created > 0) {
                logger.info("数据迁移完成：为 {} 个亭子创建了默认采集记录", created);
            }
        } catch (Exception e) {
            logger.warn("采集记录迁移跳过：{}", e.getMessage());
        }
    }
}
