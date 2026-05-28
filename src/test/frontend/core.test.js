import { describe, it, expect } from 'vitest';

describe('parseWktCoords', () => {
  it('解析 POLYGON WKT', () => {
    const wkt = 'POLYGON((118.3 32.3, 118.4 32.4, 118.5 32.3, 118.3 32.3))';
    const coords = window.parseWktCoords(wkt);
    expect(coords).toHaveLength(4);
    expect(coords[0]).toEqual([118.3, 32.3]);
    expect(coords[1]).toEqual([118.4, 32.4]);
  });

  it('解析 LINESTRING WKT', () => {
    const wkt = 'LINESTRING(118.3 32.3, 118.4 32.4)';
    const coords = window.parseWktCoords(wkt);
    expect(coords).toHaveLength(2);
  });

  it('空字符串返回 null，POINT 也返回坐标', () => {
    expect(window.parseWktCoords('')).toBeNull();
    // POINT 也匹配 ( content ) 模式，返回一个坐标
    const coords = window.parseWktCoords('POINT(1 2)');
    expect(coords).toHaveLength(1);
    expect(coords[0]).toEqual([1, 2]);
  });
});

describe('parseWktPolygon', () => {
  it('解析简单 POLYGON', () => {
    const wkt = 'POLYGON((118.3 32.3, 118.4 32.4, 118.5 32.3, 118.3 32.3))';
    const rings = window.parseWktPolygon(wkt);
    expect(rings).toHaveLength(1);
    expect(rings[0].outer).toHaveLength(4);
    expect(rings[0].outer[0]).toEqual([32.3, 118.3]); // [lat, lng]
  });

  it('解析空值返回空数组', () => {
    expect(window.parseWktPolygon(null)).toEqual([]);
    expect(window.parseWktPolygon('')).toEqual([]);
  });
});

describe('parseWktPolygonSimple', () => {
  it('返回坐标数组的数组', () => {
    const wkt = 'POLYGON((118.3 32.3, 118.4 32.4, 118.5 32.3, 118.3 32.3))';
    const result = window.parseWktPolygonSimple(wkt);
    expect(result).toHaveLength(1);
    expect(result[0]).toHaveLength(4);
    expect(result[0][0]).toEqual([118.3, 32.3]); // [lng, lat]
  });

  it('null 返回 null', () => {
    expect(window.parseWktPolygonSimple(null)).toBeNull();
  });
});

describe('parseWktLineSimple', () => {
  it('解析 LINESTRING', () => {
    const wkt = 'LINESTRING(118.3 32.3, 118.4 32.4)';
    const result = window.parseWktLineSimple(wkt);
    expect(result).toHaveLength(2);
    expect(result[0]).toEqual([118.3, 32.3]);
  });

  it('null 返回 null', () => {
    expect(window.parseWktLineSimple(null)).toBeNull();
  });
});
