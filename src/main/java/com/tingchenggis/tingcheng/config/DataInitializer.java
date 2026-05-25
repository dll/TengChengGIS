package com.tingchenggis.tingcheng.config;

import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.service.PavilionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动检查器
 *
 * 检查是否有亭子数据，若无则提示从Excel导入228条真实亭子数据
 * 不再创建任何硬编码的模拟数据
 *
 * @author TingChengGIS
 * @version 2.0.0
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private PavilionService pavilionService;

    @Override
    public void run(String... args) {
        long count = 0;
        try {
            count = pavilionService.getAllPavilions().size();
        } catch (Exception e) {
            logger.info("亭子数据表尚未初始化");
        }

        if (count == 0) {
            logger.info("==============================================");
            logger.info("  当前无亭子数据，请通过 Excel 导入 228 条数据");
            logger.info("  导入接口: POST /thousand-pavilions/import");
            logger.info("  文件: data/凉亭汇总表.xlsx");
            logger.info("==============================================");
        } else {
            logger.info("已加载 {} 个亭子数据，交通路网将按需动态生成", count);
        }
    }
}
