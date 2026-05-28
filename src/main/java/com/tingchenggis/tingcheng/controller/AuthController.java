package com.tingchenggis.tingcheng.controller;

import com.tingchenggis.tingcheng.entity.AppUser;
import com.tingchenggis.tingcheng.security.JwtUtil;
import com.tingchenggis.tingcheng.service.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AppUserService userService;

    public AuthController(JwtUtil jwtUtil, AppUserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> resp = new LinkedHashMap<>();
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            resp.put("success", false);
            resp.put("message", "用户名或密码不能为空");
            return ResponseEntity.badRequest().body(resp);
        }

        Optional<AppUser> userOpt = userService.authenticate(
            request.getUsername().trim(), request.getPassword());
        if (userOpt.isEmpty()) {
            resp.put("success", false);
            resp.put("message", "用户名或密码错误");
            return ResponseEntity.status(401).body(resp);
        }

        AppUser user = userOpt.get();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        resp.put("success", true);
        resp.put("token", token);
        resp.put("username", user.getUsername());
        resp.put("displayName", user.getDisplayName());
        resp.put("role", user.getRole());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            AppUser saved = userService.register(
                request.getUsername(), request.getPassword(), request.getDisplayName());
            String token = jwtUtil.generateToken(saved.getUsername(), saved.getRole());
            resp.put("success", true);
            resp.put("message", "注册成功");
            resp.put("token", token);
            resp.put("username", saved.getUsername());
            resp.put("displayName", saved.getDisplayName());
            resp.put("role", saved.getRole());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(resp);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication auth) {
        Map<String, Object> resp = new LinkedHashMap<>();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            resp.put("success", false);
            resp.put("message", "未登录");
            return ResponseEntity.status(401).body(resp);
        }
        String username = String.valueOf(auth.getPrincipal());
        Optional<AppUser> userOpt = userService.findByUsername(username);
        resp.put("success", true);
        resp.put("username", username);
        resp.put("displayName", userOpt.map(AppUser::getDisplayName).orElse(username));
        resp.put("role", auth.getAuthorities().stream()
            .findFirst().map(GrantedAuthority::getAuthority).orElse("ROLE_USER")
            .replaceFirst("^ROLE_", ""));
        return ResponseEntity.ok(resp);
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            Authentication auth, @RequestBody ChangePasswordRequest request) {
        Map<String, Object> resp = new LinkedHashMap<>();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            resp.put("success", false);
            resp.put("message", "未登录");
            return ResponseEntity.status(401).body(resp);
        }
        try {
            String username = String.valueOf(auth.getPrincipal());
            userService.changePassword(username, request.getOldPassword(), request.getNewPassword());
            resp.put("success", true);
            resp.put("message", "密码修改成功");
            return ResponseEntity.ok(resp);
        } catch (com.tingchenggis.tingcheng.exception.BusinessException e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(resp);
        }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String displayName;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }
}
