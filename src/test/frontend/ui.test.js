import { describe, it, expect } from 'vitest';

describe('levelLabel', () => {
  it('CITY → 市', () => {
    expect(window.levelLabel('CITY')).toBe('市');
  });

  it('STREET → 街道', () => {
    expect(window.levelLabel('STREET')).toBe('街道');
  });

  it('未知 code 原样返回', () => {
    expect(window.levelLabel('VILLAGE')).toBe('VILLAGE');
  });

  it('null 返回 未知', () => {
    expect(window.levelLabel(null)).toBe('未知');
  });

  it('undefined 返回 未知', () => {
    expect(window.levelLabel(undefined)).toBe('未知');
  });
});

describe('pointInPolygon (ray-casting)', () => {
  const square = [[0, 0], [10, 0], [10, 10], [0, 10]];

  it('点在多边形内部', () => {
    expect(window.pointInPolygon([5, 5], square)).toBe(true);
  });

  it('点在多边形外部', () => {
    expect(window.pointInPolygon([15, 5], square)).toBe(false);
  });

  it('点在边上（非顶点）视为内部', () => {
    expect(window.pointInPolygon([5, 0], square)).toBe(true);
    expect(window.pointInPolygon([0, 5], square)).toBe(true);
  });

  it('点在顶点上可能为 false（射线算法局限）', () => {
    // 顶点 (0,0) 被射线命中返回 true，(10,10) 被跳过返回 false
    expect(window.pointInPolygon([10, 10], square)).toBe(false);
  });

  it('复杂多边形（凹形）', () => {
    // 凹多边形: (0,0)→(10,0)→(10,10)→(5,5)→(0,10)
    const concave = [[0, 0], [10, 0], [10, 10], [5, 5], [0, 10]];
    expect(window.pointInPolygon([2, 2], concave)).toBe(true);   // 底部区域
    expect(window.pointInPolygon([1, 9], concave)).toBe(false);  // 左上方（凹口内部已被排除）
    expect(window.pointInPolygon([8, 9], concave)).toBe(false);  // 右上方（被 cut line 裁掉）
  });

  it('三角形内部', () => {
    const tri = [[0, 0], [10, 0], [5, 10]];
    expect(window.pointInPolygon([5, 3], tri)).toBe(true);
    expect(window.pointInPolygon([0, 5], tri)).toBe(false);
  });
});
