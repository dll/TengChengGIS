(function(){ 'use strict';
if (!window.TCG) window.TCG = {};

function gcj02(lng, lat) {
    const PI = Math.PI, a = 6378245, ee = 0.00669342162296594323;
    function tLat(x, y) {
        let r = -100 + 2*x + 3*y + 0.2*y*y + 0.1*x*y + 0.2*Math.sqrt(Math.abs(x));
        r += (20*Math.sin(6*x*PI) + 20*Math.sin(2*x*PI))*2/3;
        r += (20*Math.sin(y*PI) + 40*Math.sin(y/3*PI))*2/3;
        r += (160*Math.sin(y/12*PI) + 320*Math.sin(y*PI/30))*2/3;
        return r;
    }
    function tLng(x, y) {
        let r = 300 + x + 2*y + 0.1*x*x + 0.1*x*y + 0.1*Math.sqrt(Math.abs(x));
        r += (20*Math.sin(6*x*PI) + 20*Math.sin(2*x*PI))*2/3;
        r += (20*Math.sin(x*PI) + 40*Math.sin(x/3*PI))*2/3;
        r += (150*Math.sin(x/12*PI) + 300*Math.sin(x/30*PI))*2/3;
        return r;
    }
    const dLat = tLat(lng-105, lat-35), dLng = tLng(lng-105, lat-35);
    const rad = lat/180*PI;
    let m = Math.sin(rad); m = 1 - ee*m*m;
    const s = Math.sqrt(m);
    return [lng + (dLng*180)/(a/s*Math.cos(rad)*PI), lat + (dLat*180)/((a*(1-ee))/(m*s)*PI)];
}

function gcj02Wgs(lng, lat) {
    var g = gcj02(lng, lat);
    return [lng*2 - g[0], lat*2 - g[1]];
}

function escHtml(s) {
    if (!s) return '';
    return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function toast(msg, type) {
    var box = document.getElementById('toastBox');
    var colors = { success: '#2ecc71', danger: '#e74c3c', warning: '#f39c12', info: '#4a86e8' };
    var el = document.createElement('div'); el.className='toast-msg';
    el.style.borderLeftColor = colors[type]||'#4a86e8'; el.textContent = msg;
    box.appendChild(el); setTimeout(function(){ el.remove(); }, 4000);
}

function tLabel(t) { return window.TCG.TYPE_LAB[t] || t || '未知'; }
function tStyle(t) { return window.TCG.TYPE_STYLE[t] || window.TCG.TYPE_STYLE.DEFAULT; }

function tileUrl(p) {
    const subs = p==='gaode'||p==='weipian'?['1','2','3','4']:p==='tencent'?['0','1','2','3']:['a','b','c'];
    const s = subs[Math.floor(Math.random()*subs.length)];
    if (p==='gaode') return 'https://webrd0'+s+'.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}';
    if (p==='weipian') return 'https://webst0'+s+'.is.autonavi.com/appmaptile?style=6&x={x}&y={y}&z={z}';
    if (p==='tencent') return 'https://rt'+s+'.map.gtimg.com/tile?z={z}&x={x}&y={y}&type=vector&styleid=0';
    return 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
}
function tileSubs(p) { return p==='gaode'||p==='weipian'?['1','2','3','4']:p==='tencent'?['0','1','2','3']:['a','b','c']; }
function attribution(p) {
    if (p==='gaode') return '&copy; 高德';
    if (p==='weipian') return '&copy; 高德影像';
    if (p==='tencent') return '&copy; 腾讯';
    return '&copy; OSM';
}

function needGcj() { return window.TCG.mapProv==='gaode'||window.TCG.mapProv==='tencent'||window.TCG.mapProv==='weipian'; }
function leafCoord(lng, lat) {
    if (needGcj()) { var g=gcj02(lng,lat); return [g[1],g[0]]; }
    return [lat,lng];
}
function pavLeafCoord(p) {
    if (needGcj() && p.longitudeGcj != null && p.latitudeGcj != null) {
        return [p.latitudeGcj, p.longitudeGcj];
    }
    return leafCoord(p.longitude, p.latitude);
}

function toggleSection(id) { document.getElementById(id).classList.toggle('collapsed'); }

function updateZoom() {
    var z = window.TCG.is3D && window.TCG.viewer ? Math.max(1, Math.min(20, Math.round(18 - Math.log2(window.TCG.viewer.camera.positionCartographic.height/1000)))) : window.TCG.lmap.getZoom();
    document.getElementById('zoomLabel').textContent = z;
}

function repositionForChinese() {
    var nc = needGcj();
    for (var k in window.TCG.markers) {
        var m = window.TCG.markers[k], wgs = m._wgs, p = m._pav;
        if (!wgs) continue;
        var c;
        if (nc && p && p.longitudeGcj != null && p.latitudeGcj != null) {
            c = [p.longitudeGcj, p.latitudeGcj];
        } else {
            c = nc ? gcj02(wgs.lng, wgs.lat) : [wgs.lng, wgs.lat];
        }
        m.setLatLng([c[1], c[0]]);
    }
    if (window.TCG.pathLyr) {
        var wgsCoords = window.TCG.pathLyr._wgsCoords || window.TCG.pathLyr.getLatLngs();
        window.TCG.pathLyr.setLatLngs(wgsCoords.map(function(c){ var g=nc?gcj02(c.lng,c.lat):[c.lng,c.lat]; return [g[1],g[0]]; }));
    }
    [window.TCG.busLyr,window.TCG.taxiLyr,window.TCG.ebikeLyr,window.TCG.bikeLyr,window.TCG.walkLyr].forEach(function(lyr){
        if (!lyr) return;
        lyr.eachLayer(function(pl){
            var wgsCoords = pl._wgsCoords || pl.getLatLngs();
            pl.setLatLngs(wgsCoords.map(function(c){ var g=nc?gcj02(c.lng,c.lat):[c.lng,c.lat]; return [g[1],g[0]]; }));
        });
    });
}

function calcDist(lon1,lat1,lon2,lat2) {
    var R=6371, dLat=(lat2-lat1)*Math.PI/180, dLon=(lon2-lon1)*Math.PI/180;
    var a=Math.sin(dLat/2)*Math.sin(dLat/2)+Math.cos(lat1*Math.PI/180)*Math.cos(lat2*Math.PI/180)*Math.sin(dLon/2)*Math.sin(dLon/2);
    return R*2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
}

window.gcj02 = gcj02;
window.gcj02Wgs = gcj02Wgs;
window.escHtml = escHtml;
window.toast = toast;
window.tLabel = tLabel;
window.tStyle = tStyle;
window.tileUrl = tileUrl;
window.tileSubs = tileSubs;
window.attribution = attribution;
window.needGcj = needGcj;
window.leafCoord = leafCoord;
window.pavLeafCoord = pavLeafCoord;
window.toggleSection = toggleSection;
window.updateZoom = updateZoom;
window.repositionForChinese = repositionForChinese;
window.calcDist = calcDist;
})();
