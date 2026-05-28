import { test, expect } from '@playwright/test';

test.describe('TingChengGIS 首页', () => {

  test.beforeEach(async ({ page }) => {
    // 收集浏览器控制台错误以便调试
    page.on('console', msg => {
      if (msg.type() === 'error') console.error('  BROWSER ERROR:', msg.text());
    });
    page.on('pageerror', err => console.error('  PAGE ERROR:', err.message));
  });

  test('首页加载成功，显示地图容器', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle', timeout: 30000 });
    // 等待 JS 初始化完成
    await page.waitForTimeout(2000);
    // 标题包含亭城或 GIS（中英文均可）
    await expect(page).toHaveTitle(/亭城|GIS/);
    // Leaflet 容器存在
    await expect(page.locator('#leafletContainer')).toBeAttached();
    // Cesium 容器存在（不检查可见性，因 Cesium 可能因 WebGL 失败）
    await expect(page.locator('#cesiumContainer')).toBeAttached();
  });

  test('topbar 包含登录按钮和核心操作按钮', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle', timeout: 30000 });
    await page.waitForTimeout(1500);
    // 登录按钮可能由 i18n 动态设置，用 id 兜底
    const loginBtn = page.locator('#btnLogin');
    await expect(loginBtn).toBeVisible({ timeout: 10000 });
    await expect(page.getByTitle('显示/隐藏左面板')).toBeVisible({ timeout: 5000 });
  });

  test('点击登录按钮弹出登录弹窗', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle', timeout: 30000 });
    await page.waitForTimeout(1500);
    await page.locator('#btnLogin').click();
    await expect(page.locator('#loginModal')).toBeVisible({ timeout: 5000 });
    await expect(page.locator('#loginUsername')).toBeVisible({ timeout: 5000 });
  });

  test('登录流程：使用管理员账号登录', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle', timeout: 30000 });
    await page.waitForTimeout(1500);
    await page.locator('#btnLogin').click();
    await page.fill('#loginUsername', '419116');
    await page.fill('#loginPassword', '419116');
    await page.click('#btnSubmitAuth');
    // 等待登录完成（弹窗关闭、用户信息更新）
    await page.waitForTimeout(1000);
    await expect(page.locator('#authUser')).toContainText('系统管理员', { timeout: 10000 });
    await expect(page.locator('#btnLogout')).toBeVisible({ timeout: 5000 });
  });

  test('左面板可切换显示/隐藏', async ({ page }) => {
    await page.goto('/', { waitUntil: 'networkidle', timeout: 30000 });
    const ctrlPanel = page.locator('#ctrlPanel');
    await expect(ctrlPanel).toBeVisible({ timeout: 10000 });
    await page.getByTitle('显示/隐藏左面板').click();
    await expect(ctrlPanel).not.toBeVisible({ timeout: 5000 });
    await page.getByTitle('显示/隐藏左面板').click();
    await expect(ctrlPanel).toBeVisible({ timeout: 5000 });
  });
});
