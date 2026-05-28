import { describe, it, expect, beforeEach } from 'vitest';

// 这些函数由 setup.js 加载 utils.js 后暴露在 window 上
const gcj02 = () => window.gcj02;
const gcj02Wgs = () => window.gcj02Wgs;
const escHtml = () => window.escHtml;
const calcDist = () => window.calcDist;
const tileUrl = () => window.tileUrl;
const needGcj = () => window.needGcj;
const leafCoord = () => window.leafCoord;
const pavLeafCoord = () => window.pavLeafCoord;

describe('escHtml', () => {
  it('转义 HTML 特殊字符', () => {
    expect(window.escHtml('<script>alert("xss")</script>'))
      .toBe('&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;');
  });

  it('null/undefined 返回空字符串', () => {
    expect(window.escHtml(null)).toBe('');
    expect(window.escHtml(undefined)).toBe('');
  });

  it('普通字符串不变', () => {
    expect(window.escHtml('hello 世界')).toBe('hello 世界');
  });
});

describe('calcDist', () => {
  it('同一点距离为 0', () => {
    const d = window.calcDist(118.3, 32.3, 118.3, 32.3);
    expect(d).toBeCloseTo(0, 5);
  });

  it('滁州到南京约 55km', () => {
    const d = window.calcDist(118.3, 32.3, 118.8, 32.05);
    expect(d).toBeGreaterThan(45);
    expect(d).toBeLessThan(65);
  });

  it('对称性', () => {
    const d1 = window.calcDist(118.0, 32.0, 119.0, 33.0);
    const d2 = window.calcDist(119.0, 33.0, 118.0, 32.0);
    expect(d1).toBeCloseTo(d2, 5);
  });
});

describe('gcj02 / gcj02Wgs', () => {
  it('gcj02 对已知点产生偏移', () => {
    const g = window.gcj02(118.3, 32.3);
    expect(g[0]).not.toBeCloseTo(118.3, 3);
    expect(g[1]).not.toBeCloseTo(32.3, 3);
  });

  it('gcj02Wgs 是 gcj02 的逆运算（精度 ~10m）', () => {
    const origLng = 118.3178, origLat = 32.3123;
    const g = window.gcj02(origLng, origLat);
    const w = window.gcj02Wgs(g[0], g[1]);
    expect(w[0]).toBeCloseTo(origLng, 4);
    expect(w[1]).toBeCloseTo(origLat, 4);
  });
});

describe('tileUrl', () => {
  it('高德返回正确的 URL', () => {
    const url = window.tileUrl('gaode');
    expect(url).toMatch(/^https:\/\/webrd0[1-4]\.is\.autonavi\.com\/appmaptile/);
  });

  it('OSM 返回标准瓦片 URL', () => {
    const url = window.tileUrl('osm');
    expect(url).toMatch(/tile\.openstreetmap\.org/);
  });

  it('腾讯返回正确的 URL', () => {
    const url = window.tileUrl('tencent');
    expect(url).toMatch(/rt[0-3]\.map\.gtimg\.com\/tile/);
  });
});

describe('needGcj', () => {
  beforeEach(() => {
    window.TCG.mapProv = 'gaode';
  });

  it('gaode 返回 true', () => {
    window.TCG.mapProv = 'gaode';
    expect(window.needGcj()).toBe(true);
  });

  it('tencent 返回 true', () => {
    window.TCG.mapProv = 'tencent';
    expect(window.needGcj()).toBe(true);
  });

  it('osm 返回 false', () => {
    window.TCG.mapProv = 'osm';
    expect(window.needGcj()).toBe(false);
  });
});

describe('leafCoord', () => {
  it('非 GCJ 模式下返回 [lat, lng]', () => {
    window.TCG.mapProv = 'osm';
    const c = window.leafCoord(118.3, 32.3);
    expect(c[0]).toBe(32.3);
    expect(c[1]).toBe(118.3);
  });

  it('GCJ 模式下返回 GCJ-02 [lat, lng]', () => {
    window.TCG.mapProv = 'gaode';
    const c = window.leafCoord(118.3, 32.3);
    // 应该有偏移，且格式为 [lat, lng]
    expect(c[0]).not.toBe(32.3);
    expect(typeof c[0]).toBe('number');
  });
});

describe('pavLeafCoord', () => {
  it('GCJ 模式下优先使用 *Gcj 字段', () => {
    window.TCG.mapProv = 'gaode';
    const p = { longitude: 118.3, latitude: 32.3, longitudeGcj: 118.32, latitudeGcj: 32.32 };
    const c = window.pavLeafCoord(p);
    expect(c[0]).toBe(32.32);
    expect(c[1]).toBe(118.32);
  });

  it('无 Gcj 字段时回退到 leafCoord', () => {
    window.TCG.mapProv = 'osm';
    const p = { longitude: 118.3, latitude: 32.3 };
    const c = window.pavLeafCoord(p);
    expect(c[0]).toBe(32.3);
    expect(c[1]).toBe(118.3);
  });
});
