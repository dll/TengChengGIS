package com.tingchenggis.tingcheng.service.impl;

import com.tingchenggis.tingcheng.entity.AppUser;
import com.tingchenggis.tingcheng.exception.BusinessException;
import com.tingchenggis.tingcheng.repository.AppUserRepository;
import com.tingchenggis.tingcheng.service.AppUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AppUserService 边界条件和异常场景测试
 *
 * 包含：
 * 1. 正常认证场景
 * 2. 认证边界条件测试（null、空字符串等）
 * 3. 注册边界条件测试
 * 4. 密码修改边界条件测试
 * 5. 异常场景测试
 */
@ExtendWith(MockitoExtension.class)
class AppUserServiceImplTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AppUserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AppUserServiceImpl(userRepository, passwordEncoder);
    }

    // ==================== 认证测试 ====================

    @Test
    void authenticate_success() {
        AppUser user = new AppUser("testuser", "encodedPassword", "USER", "Test User");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);

        Optional<AppUser> result = service.authenticate("testuser", "rawPassword");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void authenticate_wrongPassword() {
        AppUser user = new AppUser("testuser", "encodedPassword", "USER", "Test User");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        Optional<AppUser> result = service.authenticate("testuser", "wrongPassword");

        assertFalse(result.isPresent());
    }

    @Test
    void authenticate_userNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<AppUser> result = service.authenticate("nonexistent", "password");

        assertFalse(result.isPresent());
    }

    @Test
    void authenticate_nullUsername() {
        Optional<AppUser> result = service.authenticate(null, "password");

        assertFalse(result.isPresent());
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void authenticate_nullPassword() {
        Optional<AppUser> result = service.authenticate("testuser", null);

        assertFalse(result.isPresent());
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void authenticate_emptyUsername() {
        Optional<AppUser> result = service.authenticate("", "password");

        assertFalse(result.isPresent());
    }

    @Test
    void authenticate_emptyPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        Optional<AppUser> result = service.authenticate("testuser", "");

        assertFalse(result.isPresent());
    }

    // ==================== 注册测试 ====================

    @Test
    void register_success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        AppUser result = service.register("newuser", "password", "New User");

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("New User", result.getDisplayName());
        assertEquals("USER", result.getRole());
    }

    @Test
    void register_nullUsername() {
        assertThrows(IllegalArgumentException.class,
            () -> service.register(null, "password", "Display"));
    }

    @Test
    void register_emptyUsername() {
        assertThrows(IllegalArgumentException.class,
            () -> service.register("", "password", "Display"));
    }

    @Test
    void register_blankUsername() {
        assertThrows(IllegalArgumentException.class,
            () -> service.register("   ", "password", "Display"));
    }

    @Test
    void register_nullPassword() {
        assertThrows(IllegalArgumentException.class,
            () -> service.register("user", null, "Display"));
    }

    @Test
    void register_emptyPassword() {
        assertThrows(IllegalArgumentException.class,
            () -> service.register("user", "", "Display"));
    }

    @Test
    void register_passwordLength3() {
        assertThrows(IllegalArgumentException.class,
            () -> service.register("user", "123", "Display"));
    }

    @Test
    void register_passwordLength4() {
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        AppUser result = service.register("user", "1234", "Display");

        assertNotNull(result);
    }

    @Test
    void register_passwordLength100() {
        String longPassword = "a".repeat(100);
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode(longPassword)).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        AppUser result = service.register("user", longPassword, "Display");

        assertNotNull(result);
    }

    @Test
    void register_usernameAlreadyExists() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> service.register("existing", "password", "Display"));
    }

    @Test
    void register_nullDisplayName() {
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        AppUser result = service.register("user", "password", null);

        assertEquals("user", result.getDisplayName());
    }

    @Test
    void register_emptyDisplayName() {
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        AppUser result = service.register("user", "password", "");

        assertEquals("user", result.getDisplayName());
    }

    @Test
    void register_blankDisplayName() {
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        AppUser result = service.register("user", "password", "   ");

        assertEquals("user", result.getDisplayName());
    }

    @Test
    void register_defaultUserRole() {
        // register方法硬编码USER角色
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        AppUser result = service.register("user", "password", "User");

        // register方法硬编码角色为USER
        assertEquals("USER", result.getRole());
    }

    // ==================== 密码修改测试 ====================

    @Test
    void changePassword_success() {
        AppUser user = new AppUser("testuser", "oldEncoded", "USER", "Test User");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "oldEncoded")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newEncoded");

        assertDoesNotThrow(() -> service.changePassword("testuser", "oldPass", "newPass"));

        verify(userRepository).save(user);
        assertEquals("newEncoded", user.getPasswordHash());
    }

    @Test
    void changePassword_nullUsername() {
        assertThrows(BusinessException.class,
            () -> service.changePassword(null, "oldPass", "newPass"));
    }

    @Test
    void changePassword_nullOldPassword() {
        assertThrows(BusinessException.class,
            () -> service.changePassword("testuser", null, "newPass"));
    }

    @Test
    void changePassword_nullNewPassword() {
        assertThrows(BusinessException.class,
            () -> service.changePassword("testuser", "oldPass", null));
    }

    @Test
    void changePassword_newPasswordTooShort() {
        assertThrows(BusinessException.class,
            () -> service.changePassword("testuser", "oldPass", "123"));
    }

    @Test
    void changePassword_wrongOldPassword() {
        AppUser user = new AppUser("testuser", "oldEncoded", "USER", "Test User");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "oldEncoded")).thenReturn(false);

        assertThrows(BusinessException.class,
            () -> service.changePassword("testuser", "wrongPass", "newPass"));
    }

    @Test
    void changePassword_userNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
            () -> service.changePassword("nonexistent", "oldPass", "newPass"));
    }

    // ==================== findByUsername测试 ====================

    @Test
    void findByUsername_found() {
        AppUser user = new AppUser("testuser", "password", "USER", "Test User");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Optional<AppUser> result = service.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void findByUsername_notFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<AppUser> result = service.findByUsername("nonexistent");

        assertFalse(result.isPresent());
    }

    // ==================== ensureUser测试 ====================

    @Test
    void ensureUser_userNotExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        boolean result = service.ensureUser("newuser", "password", "USER", "New User");

        assertTrue(result);
        verify(userRepository).save(any());
    }

    @Test
    void ensureUser_userAlreadyExists() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        boolean result = service.ensureUser("existing", "password", "USER", "Existing User");

        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void ensureUser_withAdminRole() {
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        boolean result = service.ensureUser("admin", "password", "ADMIN", "Admin");

        assertTrue(result);
    }

    @Test
    void ensureUser_nullRole() {
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            AppUser u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        boolean result = service.ensureUser("user", "password", null, "User");

        assertTrue(result);
    }
}
