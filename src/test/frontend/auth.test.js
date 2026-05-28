import { describe, it, expect, beforeEach } from 'vitest';

describe('auth.js — renderAuth', () => {
  beforeEach(() => {
    if (window.__clearAuth) window.__clearAuth();
    document.getElementById('authUser').innerHTML = '';
    document.getElementById('btnLogin').style.display = '';
    document.getElementById('btnLogout').style.display = '';
    const tabPwdLi = document.getElementById('tabPwdLi');
    if (tabPwdLi) tabPwdLi.style.display = 'none';
  });

  it('未登录时显示「未登录」和登录按钮', () => {
    window.renderAuth();
    expect(document.getElementById('authUser').innerHTML).toContain('未登录');
    expect(document.getElementById('btnLogin').style.display).not.toBe('none');
    expect(document.getElementById('btnLogout').style.display).toBe('none');
  });

  it('登录后显示用户名和退出按钮', () => {
    window.__setAuth('test-token', { username: 'testuser', displayName: '测试用户', role: 'USER' });
    expect(document.getElementById('authUser').innerHTML).toContain('测试用户');
    expect(document.getElementById('authUser').innerHTML).toContain('USER');
    expect(document.getElementById('btnLogin').style.display).toBe('none');
    expect(document.getElementById('btnLogout').style.display).not.toBe('none');
  });

  it('管理员登录后显示改密链接', () => {
    window.__setAuth('admin-token', { username: 'admin', displayName: '管理员', role: 'ADMIN' });
    expect(document.getElementById('authUser').innerHTML).toContain('🔑改密');
  });

  it('登录后密码修改 tab 显示', () => {
    const tabPwdLi = document.getElementById('tabPwdLi');
    expect(tabPwdLi.style.display).toBe('none');
    window.__setAuth('t', { username: 'u', role: 'USER' });
    expect(tabPwdLi.style.display).toBe('');
  });
});
