package com.tingchenggis.tingcheng.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tingchenggis.tingcheng.entity.Pavilion;
import com.tingchenggis.tingcheng.service.AppUserService;
import com.tingchenggis.tingcheng.service.PavilionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * 应用启动初始化
 *
 * 1. 播种默认账号：管理员 419116 / 注册用户 206004
 * 2. 如 DB 为空，从 classpath 加载样例亭子数据
 * 3. 提示完整数据导入
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final PavilionService pavilionService;
    private final AppUserService appUserService;
    private final ObjectMapper objectMapper;

    public DataInitializer(PavilionService pavilionService, AppUserService appUserService, ObjectMapper objectMapper) {
        this.pavilionService = pavilionService;
        this.appUserService = appUserService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedSamplePavilions();
    }

    private void seedUsers() {
        boolean adminCreated = appUserService.ensureUser("419116", "419116", "ADMIN", "系统管理员");
        boolean userCreated = appUserService.ensureUser("206004", "206004", "USER", "注册用户");
        if (adminCreated || userCreated) {
            logger.warn("================================================================");
            logger.warn("  已创建默认账号（密码已用 BCrypt 加密存储）");
            logger.warn("    管理员 419116 / 默认密码 419116");
            logger.warn("    注册用户 206004 / 默认密码 206004");
            logger.warn("  生产环境请尽快登录后通过「修改密码」功能更改！");
            logger.warn("================================================================");
        }
    }

    private void seedSamplePavilions() {
        long count = 0;
        try {
            count = pavilionService.getAllPavilions().size();
        } catch (Exception e) {
            logger.info("亭子数据表尚未初始化");
            return;
        }

        if (count > 0) {
            logger.info("已加载 {} 个亭子数据，交通路网将按需动态生成", count);
            return;
        }

        // 从 classpath 种子文件加载样例数据
        try (InputStream in = new ClassPathResource("seed/sample-pavilions.json").getInputStream()) {
            List<Pavilion> samples = objectMapper.readValue(in, new TypeReference<List<Pavilion>>() {});
            for (Pavilion p : samples) {
                p.setId(null);
                pavilionService.createPavilion(p);
            }
            logger.warn("================================================================");
            logger.warn("  已自动加载 {} 个样例亭子（琅琊山景区示范数据）", samples.size());
            logger.warn("  完整 228 条数据请通过 POST /thousand-pavilions/import 上传");
            logger.warn("  文件: data/千亭.xlsx");
            logger.warn("================================================================");
        } catch (Exception e) {
            logger.warn("加载样例亭子数据失败: {} （不影响启动，可通过 Excel 导入）", e.getMessage());
        }
    }
}
