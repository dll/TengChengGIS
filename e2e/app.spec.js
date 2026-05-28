import { test, expect } from '@playwright/test';

test.describe('TingChengGIS 首页', () => {
  test('首页加载成功，显示地图容器', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveTitle(/亭城/);
    // Leaflet 容器和 Cesium 容器都存在
    await expect(page.locator('#leafletContainer')).toBeVisible();
    await expect(page.locator('#cesiumContainer')).toBeVisible();
  });

  test('topbar 包含登录按钮和核心操作按钮', async ({ page }) => {
    await page.goto('/');
    await expect(page.getByTitle('登录')).toBeVisible();
    await expect(page.getByTitle('显示/隐藏左面板')).toBeVisible();
  });

  test('点击登录按钮弹出登录弹窗', async ({ page }) => {
    await page.goto('/');
    await page.getByTitle('登录').click();
    await expect(page.locator('#loginModal')).toBeVisible();
    await expect(page.locator('#loginUsername')).toBeVisible();
  });

  test('登录流程：使用管理员账号登录', async ({ page }) => {
    await page.goto('/');
    await page.getByTitle('登录').click();
    await page.fill('#loginUsername', '419116');
    await page.fill('#loginPassword', '419116');
    await page.click('#btnSubmitAuth');
    // 登录成功后弹窗关闭，显示用户名
    await expect(page.locator('#authUser')).toContainText('系统管理员');
    await expect(page.getByTitle('退出登录')).toBeVisible();
  });

  test('左面板可切换显示/隐藏', async ({ page }) => {
    await page.goto('/');
    const ctrlPanel = page.locator('#ctrlPanel');
    await expect(ctrlPanel).toBeVisible();
    await page.getByTitle('显示/隐藏左面板').click();
    await expect(ctrlPanel).not.toBeVisible();
    await page.getByTitle('显示/隐藏左面板').click();
    await expect(ctrlPanel).toBeVisible();
  });
});
