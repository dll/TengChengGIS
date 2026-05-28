// ── jsdom 环境初始化 ──
import { readFileSync } from 'fs';
import { resolve, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const STATIC = resolve(__dirname, '../../main/resources/static');

// 1. 创建必备 DOM 元素
document.body.innerHTML = `
  <div id="mapwrap">
    <div id="leafletContainer"></div>
    <div id="cesiumContainer"></div>
  </div>
  <div id="toastBox"></div>
  <div id="zoomLabel">13</div>
  <button id="langToggle">🌐 EN</button>
  <span id="authUser">未登录</span>
  <button id="btnLogin"></button>
  <button id="btnLogout"></button>
  <div id="s3TblBody"></div>
  <div id="s3Total">0</div>
  <div id="s3LayerList"></div>
  <input type="checkbox" id="chkPavilion" checked />
  <input type="checkbox" id="chkAttraction" checked />
  <input type="checkbox" id="chkRoute" checked />
  <input type="checkbox" id="chkScenic" checked />
  <input type="checkbox" id="chkAdmin" checked />
  <input type="checkbox" id="chkTraffic" />
  <input type="checkbox" id="chkBus" checked />
  <input type="checkbox" id="chkTaxi" checked />
  <input type="checkbox" id="chkEBike" checked />
  <input type="checkbox" id="chkBicycle" checked />
  <input type="checkbox" id="chkWalking" checked />
  <button class="admin-only" style="display:none">多模式路网</button>
  <button class="admin-only" style="display:none">坐标纠偏</button>
  <div id="loginModal">
    <ul class="nav nav-tabs">
      <li><a class="nav-link active" href="#tabLoginPane"></a></li>
      <li><a class="nav-link" href="#tabRegPane"></a></li>
      <li id="tabPwdLi" style="display:none"><a class="nav-link" href="#tabPwdPane"></a></li>
    </ul>
    <div class="tab-content">
      <div id="tabLoginPane"></div>
      <div id="tabRegPane"></div>
      <div id="tabPwdPane">
        <input id="pwdOld" />
        <input id="pwdNew" />
        <div id="pwdMsg"></div>
      </div>
    </div>
  </div>
`;

// 2. Mock Leaflet (L)
class LClass {
  constructor() { this._layers = []; }
  addTo() { return this; }
  removeLayer() { return this; }
  addLayer() { return this; }
  eachLayer(fn) { this._layers.forEach(fn); }
  setLatLngs() { return this; }
  getLatLngs() { return []; }
  bindPopup() { return this; }
  on() { return this; }
  off() { return this; }
  setStyle() { return this; }
  bringToFront() { return this; }
  getContainer() { return { style: {} }; }
  getZoom() { return 13; }
  setView() { return this; }
  fitBounds() { return this; }
  zoomIn() { return this; }
  zoomOut() { return this; }
}
class LeafletMap extends LClass {
  constructor() { super(); this._events = {}; }
  on(e, fn) { this._events[e] = fn; }
  off(e) { delete this._events[e]; }
  hasLayer() { return false; }
  removeLayer() { return this; }
}
class LeafletLayerGroup extends LClass {}
class LeafletMarker extends LClass {
  constructor(ll, opts) { super(); this._latlng = ll; this._opts = opts; }
  setLatLng(ll) { this._latlng = ll; }
  getLatLng() { return this._latlng; }
  dragging = { enable() {} };
}
class LeafletDivIcon { constructor(opts) { Object.assign(this, opts); } }
class LeafletPolyline extends LClass {
  constructor(coords, opts) { super(); this._coords = coords; this._opts = opts; }
  getLatLngs() { return this._coords; }
}
class LeafletPolygon extends LeafletPolyline {}
class LeafletCircleMarker extends LClass {
  constructor(ll, opts) { super(); this._latlng = ll; this._opts = opts; }
  setLatLng(ll) { this._latlng = ll; }
  getLatLng() { return this._latlng; }
  dragging = { enable() {} };
}
class LeafletTileLayer extends LClass {
  constructor(url, opts) { super(); this._url = url; this._opts = opts; }
}
class LeafletTileLayerWms extends LeafletTileLayer {}
class LeafletLatLngBounds {
  constructor(coords) { this._coords = coords; }
  extend() { return this; }
}
class LeafletGeoJSON extends LClass {}

const L = {
  map: () => new LeafletMap(),
  tileLayer: (url, opts) => new LeafletTileLayer(url, opts),
  tileLayer: { wms: (url, opts) => new LeafletTileLayerWms(url, opts) },
  layerGroup: () => new LeafletLayerGroup(),
  marker: (ll, opts) => new LeafletMarker(ll, opts),
  divIcon: (opts) => new LeafletDivIcon(opts),
  polyline: (coords, opts) => new LeafletPolyline(coords, opts),
  polygon: (coords, opts) => new LeafletPolygon(coords, opts),
  circleMarker: (ll, opts) => new LeafletCircleMarker(ll, opts),
  latLngBounds: (coords) => new LeafletLatLngBounds(coords),
  geoJSON: (data, opts) => new LeafletGeoJSON(),
  DomEvent: { on() {}, off() {} },
};
// Fix the tileLayer.wms assignment
L.tileLayer.wms = (url, opts) => new LeafletTileLayerWms(url, opts);
globalThis.L = L;

// 3. Mock Cesium (最小化 stub)
globalThis.Cesium = {
  Viewer: class {
    constructor() {
      this.camera = {
        positionCartographic: { height: 10000 },
        flyTo() {},
        zoomIn() {},
        zoomOut() {},
        changed: { addEventListener() {} },
      };
      this.entities = {
        add() { return {}; },
        remove() {},
        getById() { return null; },
      };
    }
  },
  Cartesian3: { fromDegrees() { return {}; } },
  Color: {
    WHITE: {}, BLACK: {}, fromCssColorString() { return {}; },
    RED: {}, GREEN: {}, BLUE: {}, YELLOW: {}, ORANGE: {}, PURPLE: {},
  },
  HeightReference: { CLAMP_TO_GROUND: {} },
  HorizontalOrigin: { LEFT: {}, BOTTOM: {} },
  VerticalOrigin: { BOTTOM: {} },
  PolylineDashMaterialProperty: class { constructor(opts) { Object.assign(this, opts); } },
  OpenStreetMapImageryProvider: class { constructor(opts) {} },
};

// 4. Mock bootstrap
globalThis.bootstrap = {
  Modal: class {
    static getOrCreateInstance() { return new this(); }
    constructor() {}
    show() {}
    hide() {}
  },
  Tab: class { constructor() {} show() {} },
};

// 5. Mock localStorage
const store = {};
globalThis.localStorage = {
  getItem: (k) => store[k] ?? null,
  setItem: (k, v) => { store[k] = String(v); },
  removeItem: (k) => { delete store[k]; },
  clear: () => { Object.keys(store).forEach(k => delete store[k]); },
};

// 6. 加载前端 JS 模块（顺序依赖）
function loadScript(filename) {
  const code = readFileSync(resolve(STATIC, filename), 'utf-8');
  try {
    (new Function(code))();
  } catch (e) {
    console.warn(`[setup] ${filename} 加载警告:`, e.message);
  }
}

// 按依赖顺序加载
loadScript('js/config.js');
loadScript('js/i18n.js');
loadScript('js/utils.js');
loadScript('js/core.js');
loadScript('js/auth.js');
loadScript('js/features.js');
loadScript('js/init.js');
loadScript('js/ui.js');
