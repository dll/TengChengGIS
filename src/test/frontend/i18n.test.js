import { describe, it, expect, beforeEach } from 'vitest';

describe('i18n', () => {
  beforeEach(() => {
    localStorage.removeItem('tingcheng_lang');
    window.TCG.setLang('zh');
  });

  it('默认语言为中文', () => {
    expect(window.TCG.getLang()).toBe('zh');
  });

  it('__ 查找中文翻译', () => {
    expect(window.TCG.__('app.title')).toBe('亭城 GIS 系统');
    expect(window.TCG.__('login.success')).toBe('登录成功');
  });

  it('__ 切换英文后返回英文', () => {
    window.TCG.setLang('en');
    expect(window.TCG.__('app.title')).toBe('TingCheng GIS');
    expect(window.TCG.__('login.success')).toBe('Login successful');
  });

  it('__ 不存在的 key 返回 key 本身', () => {
    expect(window.TCG.__('nonexistent.key')).toBe('nonexistent.key');
  });

  it('setLang 保存到 localStorage', () => {
    window.TCG.setLang('en');
    expect(localStorage.getItem('tingcheng_lang')).toBe('en');
  });

  it('onLangChange 回调在 setLang 时触发', () => {
    let called = false;
    window.TCG.onLangChange(function(lang) {
      called = true;
      expect(lang).toBe('en');
    });
    window.TCG.setLang('en');
    expect(called).toBe(true);
  });

  it('全局 __ 函数可用', () => {
    expect(window.__('app.title')).toBe('亭城 GIS 系统');
    window.TCG.setLang('en');
    expect(window.__('app.title')).toBe('TingCheng GIS');
  });

  it('toggleLang 切换语言并更新按钮文本', () => {
    var btn = document.getElementById('langToggle');
    expect(btn.textContent).toBe('🌐 EN');
    window.toggleLang();
    expect(window.TCG.getLang()).toBe('en');
    expect(btn.textContent).toBe('🌐 中');
    window.toggleLang();
    expect(window.TCG.getLang()).toBe('zh');
    expect(btn.textContent).toBe('🌐 EN');
  });
});
