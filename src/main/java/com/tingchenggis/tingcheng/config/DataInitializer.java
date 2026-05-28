package com.tingchenggis.tingcheng.config;

import com.tingchenggis.tingcheng.service.AppUserService;
import com.tingchenggis.tingcheng.service.PavilionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 应用启动检查器
 *
 * 1. 检查亭子数据是否已初始化
 * 2. 播种默认账号：管理员 419116 / 注册用户 206004（密码与账号相同）
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final PavilionService pavilionService;
    private final AppUserService appUserService;

    public DataInitializer(PavilionService pavilionService, AppUserService appUserService) {
        this.pavilionService = pavilionService;
        this.appUserService = appUserService;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedPavilionsHint();
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

    private void seedPavilionsHint() {
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
            logger.info("  文件: data/千亭.xlsx");
            logger.info("==============================================");
        } else {
            logger.info("已加载 {} 个亭子数据，交通路网将按需动态生成", count);
        }
    }
}
