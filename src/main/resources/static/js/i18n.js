(function(){ 'use strict';
if (!window.TCG) window.TCG = {};

var LANG_KEY = 'tingcheng_lang';
var current = localStorage.getItem(LANG_KEY) || 'zh';

var zh = {
  'app.title': '亭城 GIS 系统',
  'app.subtitle': '滁州亭文化地理信息系统',
  'lang.zh': '中文',
  'lang.en': 'English',
  'tab.map': '地图',
  'tab.data': '数据',
  'tab.digitize': '数字化',
  'tab.tools': '工具',
  'tab.share': '分享',
  'panel.left': '左面板',
  'panel.right': '右面板',
  'btn.login': '登录',
  'btn.logout': '退出',
  'btn.changePwd': '改密',
  'btn.search': '搜索',
  'btn.refresh': '刷新',
  'btn.import': '导入',
  'pavilion': '亭子',
  'pavilion.list': '亭子列表',
  'pavilion.detail': '亭子详情',
  'pavilion.add': '添加亭子',
  'pavilion.edit': '编辑亭子',
  'pavilion.delete': '删除亭子',
  'pavilion.type': '亭子类型',
  'pavilion.rating': '评分',
  'layer.pavilion': '亭子图层',
  'layer.scenic': '景区图层',
  'layer.admin': '区划图层',
  'layer.route': '路线图层',
  'layer.traffic': '实时路况',
  'login.title': '登录 / 注册',
  'login.tab.login': '登录',
  'login.tab.register': '注册',
  'login.tab.changePwd': '修改密码',
  'login.username': '账号',
  'login.password': '密码',
  'login.oldPwd': '旧密码',
  'login.newPwd': '新密码',
  'login.submit': '提交',
  'login.cancel': '取消',
  'login.success': '登录成功',
  'login.fail': '登录失败',
  'login.logout': '已退出登录',
  'login.notLogged': '未登录',
  'navigate': '导航',
  'navigate.start': '开始导航',
  'navigate.stop': '停止',
  'draw.polygon': '多边形',
  'draw.line': '路径',
  'draw.point': '点',
  'draw.finish': '完成绘制',
  'draw.cancel': '取消绘制',
  'draw.vertex': '顶点编辑',
  'route.optimal': '最优路径',
  'route.clear': '清除路径',
  'route.init': '初始化交通线',
  'route.refresh': '刷新交通线',
  'route.multi': '多模式路网',
  'export.geojson': '导出 GeoJSON',
  'export.excel': '导出 Excel',
  'export.csv': '导出 CSV',
  'export.template.excel': 'Excel 模板',
  'export.template.csv': 'CSV 模板',
  'correction': '坐标纠偏',
  's3.title': '3S 图层',
  's3.wms': '添加 WMS',
  's3.wfs': '添加 WFS',
  's3.clear': '清除图层',
  'voice.start': '语音搜索',
  'voice.stop': '停止语音',
  'camera': '拍照',
  'video': '录像',
  'travel.map': '旅行地图',
  'travel.log': '游记',
  'geofence': '地理围栏',
  'osm.scenic': '导入景区',
  'osm.admin': '导入区划',
};

var en = {
  'app.title': 'TingCheng GIS',
  'app.subtitle': 'Chuzhou Pavilion GIS System',
  'lang.zh': '中文',
  'lang.en': 'English',
  'tab.map': 'Map',
  'tab.data': 'Data',
  'tab.digitize': 'Digitize',
  'tab.tools': 'Tools',
  'tab.share': 'Share',
  'panel.left': 'Panel',
  'panel.right': 'Info',
  'btn.login': 'Login',
  'btn.logout': 'Logout',
  'btn.changePwd': 'Change Pwd',
  'btn.search': 'Search',
  'btn.refresh': 'Refresh',
  'btn.import': 'Import',
  'pavilion': 'Pavilion',
  'pavilion.list': 'Pavilion List',
  'pavilion.detail': 'Pavilion Detail',
  'pavilion.add': 'Add Pavilion',
  'pavilion.edit': 'Edit Pavilion',
  'pavilion.delete': 'Delete Pavilion',
  'pavilion.type': 'Type',
  'pavilion.rating': 'Rating',
  'layer.pavilion': 'Pavilion Layer',
  'layer.scenic': 'Scenic Layer',
  'layer.admin': 'Admin Layer',
  'layer.route': 'Route Layer',
  'layer.traffic': 'Traffic',
  'login.title': 'Login / Register',
  'login.tab.login': 'Login',
  'login.tab.register': 'Register',
  'login.tab.changePwd': 'Change Password',
  'login.username': 'Username',
  'login.password': 'Password',
  'login.oldPwd': 'Old Password',
  'login.newPwd': 'New Password',
  'login.submit': 'Submit',
  'login.cancel': 'Cancel',
  'login.success': 'Login successful',
  'login.fail': 'Login failed',
  'login.logout': 'Logged out',
  'login.notLogged': 'Not logged in',
  'navigate': 'Navigate',
  'navigate.start': 'Start',
  'navigate.stop': 'Stop',
  'draw.polygon': 'Polygon',
  'draw.line': 'Path',
  'draw.point': 'Point',
  'draw.finish': 'Finish Drawing',
  'draw.cancel': 'Cancel',
  'draw.vertex': 'Vertex Edit',
  'route.optimal': 'Optimal Route',
  'route.clear': 'Clear Path',
  'route.init': 'Init Routes',
  'route.refresh': 'Refresh Routes',
  'route.multi': 'Multi-modal',
  'export.geojson': 'Export GeoJSON',
  'export.excel': 'Export Excel',
  'export.csv': 'Export CSV',
  'export.template.excel': 'Excel Template',
  'export.template.csv': 'CSV Template',
  'correction': 'Coordinate Fix',
  's3.title': '3S Layers',
  's3.wms': 'Add WMS',
  's3.wfs': 'Add WFS',
  's3.clear': 'Clear All',
  'voice.start': 'Voice Search',
  'voice.stop': 'Stop',
  'camera': 'Camera',
  'video': 'Video',
  'travel.map': 'Travel Map',
  'travel.log': 'Travel Log',
  'geofence': 'Geo-fence',
  'osm.scenic': 'Import Scenic',
  'osm.admin': 'Import Admin',
};

function setLang(lang) {
  if (lang !== 'zh' && lang !== 'en') lang = 'zh';
  current = lang;
  try { localStorage.setItem(LANG_KEY, lang); } catch(e) {}
  // 触发重绘事件
  if (window.TCG._i18nCallbacks) {
    window.TCG._i18nCallbacks.forEach(function(fn){ try { fn(lang); } catch(e){} });
  }
}

function getLang() { return current; }

function __(key) {
  var dict = current === 'en' ? en : zh;
  return dict[key] || key;
}

// 注册语言切换回调
function onLangChange(fn) {
  if (!window.TCG._i18nCallbacks) window.TCG._i18nCallbacks = [];
  window.TCG._i18nCallbacks.push(fn);
}

// 初始化时自动更新页面标题
function applyLang() {
  document.title = __('app.title');
}

document.addEventListener('DOMContentLoaded', applyLang);

window.TCG.setLang = setLang;
window.TCG.getLang = getLang;
window.TCG.__ = __;
window.TCG.onLangChange = onLangChange;
window.__ = __;
})();
