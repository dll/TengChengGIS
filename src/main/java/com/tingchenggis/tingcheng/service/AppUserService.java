package com.tingchenggis.tingcheng.service;

import com.tingchenggis.tingcheng.entity.AppUser;

import java.util.Optional;

public interface AppUserService {

    /** 校验用户名密码，匹配则返回用户 */
    Optional<AppUser> authenticate(String username, String rawPassword);

    /** 注册新用户，默认角色为 USER */
    AppUser register(String username, String rawPassword, String displayName);

    /** 按用户名查询 */
    Optional<AppUser> findByUsername(String username);

    /** 不存在则创建（用于种子账号），返回是否新建 */
    boolean ensureUser(String username, String rawPassword, String role, String displayName);
}
