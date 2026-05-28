package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.AppUser;
import com.tingchenggis.tingcheng.repository.AppUserRepository;
import com.tingchenggis.tingcheng.service.AppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class AppUserServiceImpl implements AppUserService {

    private static final Logger logger = LoggerFactory.getLogger(AppUserServiceImpl.class);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserServiceImpl(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) return Optional.empty();
        return userRepository.findByUsername(username)
            .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()));
    }

    @Override
    public AppUser register(String username, String rawPassword, String displayName) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (rawPassword == null || rawPassword.length() < 4) {
            throw new IllegalArgumentException("密码长度不能少于4位");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        AppUser user = new AppUser(username, passwordEncoder.encode(rawPassword), "USER",
            displayName != null && !displayName.isBlank() ? displayName : username);
        AppUser saved = userRepository.save(user);
        logger.info("注册新用户: {} (id={})", username, saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean ensureUser(String username, String rawPassword, String role, String displayName) {
        if (userRepository.existsByUsername(username)) {
            return false;
        }
        AppUser user = new AppUser(username, passwordEncoder.encode(rawPassword),
            role != null ? role : "USER",
            displayName != null ? displayName : username);
        userRepository.save(user);
        logger.info("已创建种子账号: username={}, role={}", username, role);
        return true;
    }
}
