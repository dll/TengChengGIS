(function(){ 'use strict';
if (!window.TCG) window.TCG = {};

var TCG = window.TCG;
var toast = window.toast;
var tLabel = window.tLabel;
var tStyle = window.tStyle;
var tileUrl = window.tileUrl;
var tileSubs = window.tileSubs;
var attribution = window.attribution;
var needGcj = window.needGcj;
var leafCoord = window.leafCoord;
var pavLeafCoord = window.pavLeafCoord;
var escHtml = window.escHtml;
var gcj02 = window.gcj02;
var gcj02Wgs = window.gcj02Wgs;
var repositionForChinese = window.repositionForChinese;
var updateZoom = window.updateZoom;
var calcDist = window.calcDist;

var routesInitializing = false, routesInited = false;

function initMaps() {
    TCG.lmap = L.map('leafletContainer').setView([32.3123, 118.3178], 13);
    TCG.tileLyr = L.tileLayer(tileUrl('gaode'), { subdomains: tileSubs('gaode'), attribution: attribution('gaode'), maxZoom: 18 }).addTo(TCG.lmap);
    TCG.s3LayerGroup = L.layerGroup().addTo(TCG.lmap);
    TCG.routePolyLyr = L.layerGroup().addTo(TCG.lmap);

    TCG.viewer = new Cesium.Viewer('cesiumContainer', {
        imageryProvider: new Cesium.OpenStreetMapImageryProvider({ url: 'https://a.tile.openstreetmap.org/' }),
        baseLayerPicker: false, geocoder: false, homeButton: false, sceneModePicker: false,
        navigationHelpButton: false, animation: false, timeline: false, fullscreenButton: false,
        infoBox: false, selectionIndicator: false
    });
    TCG.viewer.camera.flyTo({ destination: Cesium.Cartesian3.fromDegrees(118.3178, 32.3123, 10000), duration: 0 });

    TCG.lmap.on('zoomend', updateZoom);
    TCG.lmap.on('click', onMapClick);
    if (TCG.viewer) TCG.viewer.camera.changed.addEventListener(updateZoom);
    setTimeout(loadPavilions, 500);
}

function switchMap(prov) {
    if (prov===TCG.mapProv && TCG.tileLyr) return; TCG.mapProv = prov;
    if (TCG.tileLyr) TCG.lmap.removeLayer(TCG.tileLyr);
    if (prov!=='gaode' && TCG.trafficLyr) { TCG.lmap.removeLayer(TCG.trafficLyr); TCG.trafficLyr=null; document.getElementById('chkTraffic').checked=false; }
    TCG.tileLyr = L.tileLayer(tileUrl(prov), { subdomains: tileSubs(prov), attribution: attribution(prov), maxZoom: 18 }).addTo(TCG.lmap);
    if (prov==='gaode'||prov==='tencent'||prov==='weipian') repositionForChinese();
}

function onMapClick(e) {
    var latlng = e.latlng;
    var lng = latlng.lng, lat = latlng.lat;
    if (TCG.measureActive) {
        addMeasurePoint(latlng, lng, lat);
        return;
    }
    if (TCG.addOnMapActive) {
        TCG.editTarget = null;
        document.getElementById('pavModalTitle').textContent = '在地图上添加亭子';
        document.getElementById('pavForm').reset();
        document.getElementById('fLng').value = lng.toFixed(6);
        document.getElementById('fLat').value = lat.toFixed(6);
        document.getElementById('collList').innerHTML = '';
        window.switchPavTab('basic');
        TCG.pavModal.show();
        TCG.addOnMapActive = false;
        TCG.lmap.getContainer().style.cursor = '';
        document.getElementById('btnAddOnMap').style.background = 'rgba(255,255,255,.08)';
        return;
    }
}

function toggleView() {
    var leafDiv = document.getElementById('leafletContainer'), csDiv = document.getElementById('cesiumContainer'), btn = document.getElementById('toggleViewBtn');
    if (TCG.is3D) {
        leafDiv.style.display='block'; csDiv.style.display='none'; btn.textContent='3D'; TCG.is3D=false;
    } else {
        leafDiv.style.display='none'; csDiv.style.display='block'; btn.textContent='2D'; TCG.is3D=true;
        if (TCG.pavs.length>0) showOnCesium();
    }
}

function resetView() { if (TCG.is3D&&TCG.viewer) TCG.viewer.camera.flyTo({destination:Cesium.Cartesian3.fromDegrees(118.3178,32.3123,10000),duration:1.5}); else TCG.lmap.setView([32.3123,118.3178],13); }
function zoomIn() { if (TCG.is3D&&TCG.viewer) TCG.viewer.camera.zoomIn(); else TCG.lmap.zoomIn(); updateZoom(); }
function zoomOut() { if (TCG.is3D&&TCG.viewer) TCG.viewer.camera.zoomOut(); else TCG.lmap.zoomOut(); updateZoom(); }

async function loadPavilions() {
    try {
        var resp = await fetch('/thousand-pavilions/locations?includeCollectorCounts=true');
        TCG.pavs = await resp.json();

        for (var k in TCG.markers) { if (TCG.lmap.hasLayer(TCG.markers[k])) TCG.lmap.removeLayer(TCG.markers[k]); }
        TCG.markers = {};
        for (var k in TCG.csMarkers) { TCG.viewer.entities.removeById(k); }
        TCG.csMarkers = {};
        if (TCG.clusterLayer) { TCG.lmap.removeLayer(TCG.clusterLayer); TCG.clusterLayer = null; }

        var useCluster = TCG.clusterEnabled && TCG.pavs.length > 50;
        if (useCluster) TCG.clusterLayer = L.markerClusterGroup({
            chunkedLoading: true, maxClusterRadius: 50, spiderfyOnMaxZoom: true,
            iconCreateFunction: function(cluster) {
                var count = cluster.getChildCount();
                var size = count < 10 ? 'small' : count < 50 ? 'medium' : 'large';
                return L.divIcon({ html: '<div style="background:rgba(52,152,219,.8);color:#fff;border-radius:50%;width:'+(size==='small'?30:size==='medium'?36:42)+'px;height:'+(size==='small'?30:size==='medium'?36:42)+'px;display:flex;align-items:center;justify-content:center;font-weight:bold;font-size:'+(size==='small'?11:size==='medium'?13:15)+'px;border:2px solid rgba(255,255,255,.6)">'+count+'</div>', className: '', iconSize: [42,42] });
            }
        });

        TCG.pavs.forEach(function(p) {
            if (!p.latitude || !p.longitude) return;
            var st = tStyle(p.type);
            var icon = L.divIcon({ className: 'pav-marker '+st.cls, html: '<div style="background:'+st.color+';width:22px;height:22px;border-radius:50%;display:flex;align-items:center;justify-content:center;color:#fff;font-weight:bold;font-size:11px;">'+p.chineseName.charAt(0)+'</div>', iconSize: [22,22], iconAnchor: [11,11] });
            var leaf = pavLeafCoord(p);
            var m = L.marker(leaf, {icon: icon})
                .bindPopup('<b>'+p.chineseName+'</b><br>'+tLabel(p.type)+'<br>评分:'+(p.visitorRating||'暂无'));
            m.on('click', function(e){ window.showDetail(p); window.toggleSel(p); });
            m._wgs = {lat: p.latitude, lng: p.longitude};
            m._pav = p;
            TCG.markers[p.id] = m;
            if (useCluster) TCG.clusterLayer.addLayer(m);
            else m.addTo(TCG.lmap);

            var cColor = Cesium.Color[tStyle(p.type).cesium];
            var ent = TCG.viewer.entities.add({
                id: p.id.toString(), position: Cesium.Cartesian3.fromDegrees(p.longitude,p.latitude,10),
                point: { pixelSize:10, color:cColor, outlineColor:Cesium.Color.WHITE, outlineWidth:2, heightReference:Cesium.HeightReference.CLAMP_TO_GROUND },
                label: { text:p.chineseName, font:'14pt monospace', fillColor:Cesium.Color.WHITE, outlineColor:Cesium.Color.BLACK, outlineWidth:2, horizontalOrigin:Cesium.HorizontalOrigin.LEFT, verticalOrigin:Cesium.VerticalOrigin.BOTTOM, eyeOffset:new Cesium.Cartesian3(-20,0,0), heightReference:Cesium.HeightReference.CLAMP_TO_GROUND }
            });
            TCG.csMarkers[p.id] = ent;
        });

        if (useCluster && TCG.clusterLayer) TCG.lmap.addLayer(TCG.clusterLayer);

        toast('加载了 '+TCG.pavs.length+' 个亭子', 'success');
        loadTransportRoutes();
    } catch(e) { toast('加载失败: '+e.message, 'danger'); }
}

function clearRouteLayers() {
    [TCG.busLyr,TCG.taxiLyr,TCG.ebikeLyr,TCG.bikeLyr,TCG.walkLyr].forEach(function(l){ if(l) TCG.lmap.removeLayer(l); });
    TCG.busLyr=L.layerGroup(); TCG.taxiLyr=L.layerGroup(); TCG.ebikeLyr=L.layerGroup(); TCG.bikeLyr=L.layerGroup(); TCG.walkLyr=L.layerGroup();
    if (TCG.csRoutes) { TCG.csRoutes.forEach(function(r){ if(TCG.viewer&&TCG.viewer.entities) TCG.viewer.entities.remove(r); }); TCG.csRoutes=[]; }
}

async function loadTransportRoutes() {
    try {
        var resp = await fetch('/transport-routes'), result = await resp.json();
        if (!result.success||!result.data||result.data.length===0) { if (!routesInitializing) initRoutes(); return; }
        var routes = result.data;
        clearRouteLayers();

        var colors = { BUS:'#3498db', TAXI:'#f39c12', E_BIKE:'#2ecc71', BICYCLE:'#e67e22', WALKING:'#e74c3c' };
        var widths = { BUS:5, TAXI:4, E_BIKE:3, BICYCLE:3, WALKING:2 };
        var icons = { BUS:'[Bus]', TAXI:'[Taxi]', E_BIKE:'[E-Bike]', BICYCLE:'[Bike]', WALKING:'[Walk]' };
        var lyrMap = { BUS:TCG.busLyr, TAXI:TCG.taxiLyr, E_BIKE:TCG.ebikeLyr, BICYCLE:TCG.bikeLyr, WALKING:TCG.walkLyr };

        routes.forEach(function(r) {
            if (!r.fromPavilionId||!r.toPavilionId) return;
            var fp = TCG.pavs.find(function(p){return p.id===r.fromPavilionId});
            var tp = TCG.pavs.find(function(p){return p.id===r.toPavilionId});
            if (!fp||!tp) return;

            var mode = r.transportMode||'WALKING', c = colors[mode]||'#95a5a6', w = widths[mode]||3;
            var coords = [], wgsCoords = [];
            if (r.waypoints) {
                try {
                    coords = JSON.parse(r.waypoints).map(function(wp){wgsCoords.push([wp[1],wp[0]]); return leafCoord(wp[0],wp[1]);});
                } catch(e){}
            }
            if (coords.length===0) {
                wgsCoords = [[fp.latitude, fp.longitude], [tp.latitude, tp.longitude]];
                coords = [leafCoord(fp.longitude,fp.latitude), leafCoord(tp.longitude,tp.latitude)];
            }

            var dash = mode==='WALKING'?'5,8':mode==='BICYCLE'?'8,4':null;
            var pl = L.polyline(coords, { color:c, weight:w, opacity:0.85, smoothFactor:0.5, dashArray:dash });
            pl._wgsCoords = wgsCoords;
            var isLoop = r.fromPavilionId===r.toPavilionId, fareStr = r.estimatedFare>0?'<tr><td>费用</td><td style="text-align:right">¥'+r.estimatedFare.toFixed(1)+'</td></tr>':'';
            pl.bindPopup(
                '<div style="padding:8px;min-width:200px">'+
                '<h6 style="color:'+c+';border-bottom:2px solid '+c+';padding-bottom:4px;margin:0 0 6px">'+(icons[mode]||'')+' '+r.routeName+'</h6>'+
                '<table style="width:100%;font-size:11px">'+
                '<tr><td>距离</td><td style="text-align:right">'+r.distanceKm+' km</td></tr>'+
                '<tr><td>耗时</td><td style="text-align:right">'+r.travelTimeMinutes+' 分钟</td></tr>'+
                '<tr><td>道路</td><td style="text-align:right">'+(r.roadType||'-')+'</td></tr>'+
                fareStr+
                (r.trafficCondition?'<tr><td>路况</td><td style="text-align:right">'+r.trafficCondition+'</td></tr>':'')+
                (isLoop?'<tr><td colspan="2" style="text-align:center;color:#e74c3c">环线</td></tr>':'')+
                '</table>'+
                (r.routeDescription?'<p style="margin-top:6px;font-size:11px;color:#999">'+r.routeDescription+'</p>':'')+
                '</div>'
            );
            pl.on('mouseover', function(){ this.setStyle({weight:w+2,opacity:1}); this.bringToFront(); });
            pl.on('mouseout', function(){ this.setStyle({weight:w,opacity:.85}); });
            (lyrMap[mode]||TCG.walkLyr).addLayer(pl);

            if (TCG.viewer&&TCG.viewer.entities&&coords.length>0) {
                var pos = []; coords.forEach(function(c){pos.push(c[1],c[0]);});
                var ent = TCG.viewer.entities.add({ polyline: { positions: Cesium.Cartesian3.fromDegreesArray(pos), width:w, material: new Cesium.PolylineDashMaterialProperty({color:Cesium.Color.fromCssColorString(c),gapColor:Cesium.Color.WHITE.withAlpha(.2)}), clampToGround:true } });
                TCG.csRoutes.push(ent);
            }
        });

        TCG.busLyr.addTo(TCG.lmap); TCG.taxiLyr.addTo(TCG.lmap); TCG.ebikeLyr.addTo(TCG.lmap); TCG.bikeLyr.addTo(TCG.lmap); TCG.walkLyr.addTo(TCG.lmap);
    } catch(e) { toast('交通线加载失败: '+e.message, 'danger'); }
}

async function initRoutes() {
    if (routesInitializing) return;
    routesInitializing = true;
    try {
        var resp = await fetch('/transport-routes/build-network', { method:'POST' }), result = await resp.json();
        if (result.success) {
            if (!routesInited) { toast('交通线初始化成功', 'success'); routesInited = true; }
            clearRouteLayers(); routesInited = true;
            setTimeout(function(){ routesInitializing = false; loadTransportRoutes(); }, 500);
        } else {
            routesInitializing = false;
            toast('初始化失败: '+result.message, 'danger');
        }
    } catch(e) { routesInitializing = false; toast('初始化失败: '+e.message, 'danger'); }
}

var CHK_IDS = { bus:'chkBus', taxi:'chkTaxi', ebike:'chkEBike', bike:'chkBicycle', walk:'chkWalking' };
function toggleMode(mode) {
    var map = { bus: TCG.busLyr, taxi: TCG.taxiLyr, ebike: TCG.ebikeLyr, bike: TCG.bikeLyr, walk: TCG.walkLyr };
    var lyr = map[mode]; if (!lyr) return;
    var chk = document.getElementById(CHK_IDS[mode]);
    if (chk.checked) lyr.addTo(TCG.lmap); else TCG.lmap.removeLayer(lyr);
}

function toggleTraffic() {
    var chk = document.getElementById('chkTraffic') || document.getElementById('chkTraffic2');
    if (!chk) return;
    if (TCG.mapProv!=='gaode') { toast('实时路况仅高德标准地图可用', 'warning'); chk.checked=false; return; }
    if (chk.checked) {
        TCG.trafficLyr = L.tileLayer('https://tm.amap.com/trafficengine/mapabc/traffictile?v=1.0&t=1&x={x}&y={y}&z={z}', { subdomains:['1','2','3','4'], opacity:.7, maxZoom:18 }).addTo(TCG.lmap);
        toast('实时路况已开启', 'info');
    } else { if (TCG.trafficLyr) { TCG.lmap.removeLayer(TCG.trafficLyr); TCG.trafficLyr=null; } }
}

function togglePavLayer() {
    var v = document.getElementById('chkPavilion').checked;
    if (TCG.clusterLayer && TCG.clusterEnabled) {
        if (v) TCG.lmap.addLayer(TCG.clusterLayer); else TCG.lmap.removeLayer(TCG.clusterLayer);
    } else {
        for (var k in TCG.markers) { if (v) TCG.markers[k].addTo(TCG.lmap); else TCG.lmap.removeLayer(TCG.markers[k]); }
    }
    for (var k in TCG.csMarkers) { if (TCG.viewer&&TCG.viewer.entities.getById(k)) TCG.viewer.entities.getById(k).show=v; }
}

function toggleCluster() {
    TCG.clusterEnabled = document.getElementById('chkCluster').checked;
    loadPavilions();
}

function toggleAttrLayer() {
    var v = document.getElementById('chkAttraction').checked;
    if (TCG.pathLyr) { if (v) TCG.pathLyr.addTo(TCG.lmap); else TCG.lmap.removeLayer(TCG.pathLyr); }
    if (TCG.csPath) TCG.csPath.show = v;
}

function showOnCesium() {
    if (!TCG.viewer) return;
    TCG.pavs.forEach(function(p){
        if (!p.latitude||!p.longitude||TCG.viewer.entities.getById(p.id.toString())) return;
        var c = Cesium.Color[tStyle(p.type).cesium];
        TCG.csMarkers[p.id] = TCG.viewer.entities.add({
            id: p.id.toString(), position: Cesium.Cartesian3.fromDegrees(p.longitude,p.latitude,10),
            point: { pixelSize:10, color:c, outlineColor:Cesium.Color.WHITE, outlineWidth:2, heightReference:Cesium.HeightReference.CLAMP_TO_GROUND },
            label: { text:p.chineseName, font:'14pt monospace', fillColor:Cesium.Color.WHITE, outlineColor:Cesium.Color.BLACK, outlineWidth:2, horizontalOrigin:Cesium.HorizontalOrigin.LEFT, verticalOrigin:Cesium.VerticalOrigin.BOTTOM, eyeOffset:new Cesium.Cartesian3(-20,0,0), heightReference:Cesium.HeightReference.CLAMP_TO_GROUND }
        });
    });
}

function toggleScenicLayer() {
    var chk = document.getElementById('chkScenic');
    if (!TCG.scenicPolyLyr) return;
    if (chk.checked) { if (!TCG.lmap.hasLayer(TCG.scenicPolyLyr)) TCG.scenicPolyLyr.addTo(TCG.lmap); }
    else TCG.lmap.removeLayer(TCG.scenicPolyLyr);
}

function toggleAdminLayer() {
    var chk = document.getElementById('chkAdmin');
    if (!TCG.adminPolyLyr) return;
    if (chk.checked) { if (!TCG.lmap.hasLayer(TCG.adminPolyLyr)) TCG.adminPolyLyr.addTo(TCG.lmap); }
    else TCG.lmap.removeLayer(TCG.adminPolyLyr);
}

function toggleRouteLayer() {
    var chk = document.getElementById('chkRoute');
    if (chk.checked) { if (!TCG.lmap.hasLayer(TCG.routePolyLyr)) TCG.routePolyLyr.addTo(TCG.lmap); }
    else TCG.lmap.removeLayer(TCG.routePolyLyr);
}

function parseWktCoords(wkt) {
    var m = wkt.match(/\(\(([^)]+)\)\)/);
    if (!m) m = wkt.match(/\(([^)]+)\)/);
    if (!m) return null;
    return m[1].split(',').map(function(s){
        var parts = s.trim().split(/\s+/);
        return [parseFloat(parts[0]), parseFloat(parts[1])];
    });
}

function zoomToWkt(wkt) {
    try {
        var coords = parseWktCoords(wkt);
        if (!coords || !coords.length) return;
        var bounds = L.latLngBounds(coords.map(function(c){ return [c[1], c[0]]; }));
        TCG.lmap.fitBounds(bounds, {padding:[30,30]});
    } catch(e) { console.warn('zoomToWkt error', e); }
}

function parseWktPolygon(wkt) {
    if (!wkt) return [];
    var rings = [];
    var polyType = wkt.startsWith('MULTIPOLYGON') ? 'MULTIPOLYGON' : 'POLYGON';
    var content = wkt.replace(/^(MULTIPOLYGON|POLYGON)\s*/, '');
    var ringMatches = content.match(/\(\([^)]+\)\)/g) || content.match(/\([^)]+\)/g);
    if (!ringMatches) {
        var clean = content.replace(/[()]/g, '').trim();
        var pts = clean.split(',').map(function(p){
            var xy = p.trim().split(/\s+/);
            return [parseFloat(xy[1]), parseFloat(xy[0])];
        }).filter(function(p){ return !isNaN(p[0]) && !isNaN(p[1]); });
        return pts.length > 0 ? [{outer: pts, inner: []}] : [];
    }
    for (var i = 0; i < ringMatches.length; i++) {
        var ring = ringMatches[i].replace(/[()]/g, '').trim();
        var pts = ring.split(',').map(function(p){
            var xy = p.trim().split(/\s+/);
            return [parseFloat(xy[1]), parseFloat(xy[0])];
        }).filter(function(p){ return !isNaN(p[0]) && !isNaN(p[1]); });
        if (pts.length >= 3) {
            if (i === 0 || ringMatches.length === 1) {
                rings.push({outer: pts, inner: []});
            }
        }
    }
    return rings;
}

function parseWktPolygonSimple(wkt) {
    if (!wkt) return null;
    var rings = [];
    var m = wkt.match(/\(\(([^)]+)\)\)/);
    if (m) {
        var ringStr = m[1];
        var pts = ringStr.split(',').map(function(s){
            var parts = s.trim().split(/\s+/);
            return [parseFloat(parts[0]), parseFloat(parts[1])];
        });
        rings.push(pts);
    }
    return rings;
}

function parseWktLineSimple(wkt) {
    if (!wkt) return null;
    var m = wkt.match(/\(([^)]+)\)/);
    if (m) {
        return m[1].split(',').map(function(s){
            var parts = s.trim().split(/\s+/);
            return [parseFloat(parts[0]), parseFloat(parts[1])];
        });
    }
    return null;
}

function renderS3Table() {
    var body = document.getElementById('s3TblBody');
    document.getElementById('s3Total').textContent = TCG.s3Layers.length;
    if (!TCG.s3Layers.length) {
        body.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#888;padding:20px">暂无图层，点击上方「+ 添加WMS」或「+ 添加WFS」添加</td></tr>';
        return;
    }
    var html = '';
    TCG.s3Layers.forEach(function(l, i){
        var statusIcon = l.visible ? '<span style="color:#2ecc71">● 显示</span>' : '<span style="color:#888">○ 隐藏</span>';
        var srcInfo = l.sourceInfo || (l.type === 'wms' ? 'WMS服务' : 'WFS服务');
        html += '<tr style="border-bottom:1px solid rgba(255,255,255,.05)">'
            + '<td style="padding:5px;text-align:center">' + (i+1) + '</td>'
            + '<td style="padding:5px"><b>' + l.displayName + '</b><br><span style="color:#666;font-size:10px">' + l.layerName + '</span></td>'
            + '<td style="padding:5px"><span class="type-badge" style="background:'+(l.type==='wms'?'rgba(255,152,0,.2);color:#ff9800':'rgba(46,204,113,.2);color:#2ecc71')+'">' + l.type.toUpperCase() + '</span></td>'
            + '<td style="padding:5px;font-size:10px;max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap" title="'+l.url+'">' + l.url + '</td>'
            + '<td style="padding:5px">' + statusIcon + '</td>'
            + '<td style="padding:5px;font-size:10px;color:#888">' + srcInfo + '</td>'
            + '<td style="padding:5px">'
            + '<button class="btn btn-sm" style="font-size:9px;background:rgba(52,152,219,.15);color:#3498db;padding:1px 5px;margin-right:2px" onclick="toggleS3Layer('+l.id+')">'+(l.visible?'隐藏':'显示')+'</button>'
            + '<button class="btn btn-sm" style="font-size:9px;background:rgba(231,76,60,.15);color:#e74c3c;padding:1px 5px" onclick="removeS3Layer('+l.id+')">删除</button>'
            + '</td></tr>';
    });
    body.innerHTML = html;
    renderS3LayerList();
}

function renderS3LayerList() {
    var list = document.getElementById('s3LayerList');
    if (!TCG.s3Layers.length) {
        list.innerHTML = '<p style="color:#888;font-size:10px">暂无图层，点击「+ 添加WMS/WFS」或查看「推荐服务」</p>';
        return;
    }
    var html = '';
    TCG.s3Layers.forEach(function(l){
        html += '<div class="ctrl-row" style="font-size:10px">'
            + '<input type="checkbox" '+(l.visible?'checked':'')+' onchange="toggleS3Layer('+l.id+')">'
            + '<label style="cursor:pointer" onclick="showS3LayerInfo('+l.id+')" title="点击查看详情">'
            + '<span class="mode-dot" style="background:'+(l.type==='wms'?'#ff9800':'#2ecc71')+';width:6px;height:6px"></span>'
            + l.displayName
            + ' <span style="color:#666">('+l.type.toUpperCase()+')</span>'
            + '</label></div>';
    });
    list.innerHTML = html;
}

function toggleS3Layer(id) {
    var l = TCG.s3Layers.find(function(x){ return x.id === id; });
    if (!l) return;
    l.visible = !l.visible;
    if (l.layerObj) {
        if (l.type === 'wms' && l.layerObj._map) {
            if (l.visible) l.layerObj.addTo(l.map || TCG.lmap);
            else l.map.removeLayer(l.layerObj);
        } else if (l.type === 'wfs' && l.layerObj) {
            if (l.visible) l.layerObj.addTo(TCG.s3LayerGroup);
            else TCG.s3LayerGroup.removeLayer(l.layerObj);
        }
    }
    renderS3Table();
}

function removeS3Layer(id) {
    if (!confirm('确认删除图层？')) return;
    var l = TCG.s3Layers.find(function(x){ return x.id === id; });
    if (!l) return;
    if (l.type === 'wms' && l.layerObj) {
        l.map.removeLayer(l.layerObj);
    } else if (l.type === 'wfs' && l.layerObj) {
        TCG.s3LayerGroup.removeLayer(l.layerObj);
    }
    TCG.s3Layers = TCG.s3Layers.filter(function(x){ return x.id !== id; });
    renderS3Table();
}

function showS3LayerInfo(id) {
    var l = TCG.s3Layers.find(function(x){ return x.id === id; });
    if (!l) return;
    var info = [
        '名称: ' + l.displayName,
        '图层: ' + l.layerName,
        '类型: ' + l.type.toUpperCase(),
        '服务: ' + l.url,
        '来源: ' + (l.sourceInfo || '用户添加'),
        '状态: ' + (l.visible ? '显示中' : '已隐藏'),
    ].join('\n');
    toast(info.replace(/\n/g, ' · '), 'info');
}

function filterS3Table() { renderS3Table(); }

function clearAllS3Layers() {
    if (!confirm('确认清除所有3S图层？')) return;
    TCG.s3Layers.forEach(function(l){
        if (l.type === 'wms' && l.layerObj) l.map.removeLayer(l.layerObj);
        else if (l.type === 'wfs' && l.layerObj) TCG.s3LayerGroup.removeLayer(l.layerObj);
    });
    TCG.s3Layers = [];
    renderS3Table();
}

function openAddWmsModal() {
    if (!TCG.addWmsModal) TCG.addWmsModal = new bootstrap.Modal(document.getElementById('addWmsModal'));
    document.getElementById('wmsUrl').value = 'http://localhost:8080/geoserver/wms?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetCapabilities';
    document.getElementById('wmsLoadStatus').textContent = '';
    document.getElementById('wmsLayerSelect').innerHTML = '<option value="">— 先获取图层列表 —</option>';
    document.getElementById('wmsLayerInfo').style.display = 'none';
    document.getElementById('addWmsBtn').disabled = true;
    TCG.wmsCapabilitiesCache = null;
    TCG.addWmsModal.show();
}

async function fetchWmsLayers() {
    var url = document.getElementById('wmsUrl').value.trim();
    if (!url) { toast('请输入WMS服务地址', 'warning'); return; }
    var status = document.getElementById('wmsLoadStatus');
    status.textContent = '加载中...';
    try {
        var resp = await fetch('/ogc/wms/capabilities', {
            method:'POST', headers:{'Content-Type':'application/json'},
            body: JSON.stringify({url: url})
        });
        var data = await resp.json();
        if (!data.success || !data.service || !data.service.length) {
            status.textContent = '未获取到图层';
            toast('获取失败: ' + (data.message||'无数据'), 'danger');
            return;
        }
        TCG.wmsCapabilitiesCache = data;
        var sel = document.getElementById('wmsLayerSelect');
        sel.innerHTML = '';
        data.service.forEach(function(l){
            var opt = document.createElement('option');
            opt.value = l.name;
            opt.textContent = l.title + ' (' + l.name + ')';
            opt.title = l.abstract || '';
            sel.appendChild(opt);
        });
        status.textContent = '共 ' + data.service.length + ' 个图层';
        document.getElementById('addWmsBtn').disabled = false;
        var info = document.getElementById('wmsLayerInfo');
        var svc = data.service[0];
        info.style.display = 'block';
        info.innerHTML = '<b>服务:</b> ' + (svc._serviceTitle||'未知') + '<br>'
            + (svc._serviceAbstract ? '<b>简介:</b> ' + svc._serviceAbstract : '');
        toast('获取到 ' + data.service.length + ' 个WMS图层', 'success');
    } catch(e) {
        status.textContent = '加载失败';
        toast('获取失败: ' + e.message, 'danger');
    }
}

function confirmAddWms() {
    var url = document.getElementById('wmsUrl').value.trim();
    var sel = document.getElementById('wmsLayerSelect');
    var layerName = sel.value;
    if (!layerName) { toast('请选择一个图层', 'warning'); return; }
    var style = document.getElementById('wmsStyle').value.trim() || '';
    var opacity = parseFloat(document.getElementById('wmsOpacity').value);
    var displayName = sel.options[sel.selectedIndex].text.split(' (')[0] || layerName;

    var baseUrl = url.includes('?') ? url.substring(0, url.indexOf('?')) : url;

    var wmsLayer = L.tileLayer.wms(baseUrl, {
        layers: layerName,
        format: 'image/png',
        transparent: true,
        opacity: opacity,
        version: '1.1.1',
        styles: style || '',
        attribution: displayName + ' (WMS)'
    }).addTo(TCG.lmap);

    var l = {
        id: TCG.s3NextId++,
        type: 'wms',
        url: url,
        displayName: displayName,
        layerName: layerName,
        style: style,
        opacity: opacity,
        visible: true,
        layerObj: wmsLayer,
        map: TCG.lmap,
        sourceInfo: 'WMS - ' + (TCG.wmsCapabilitiesCache?.service?.[0]?._serviceTitle || '自定义')
    };
    TCG.s3Layers.push(l);
    TCG.addWmsModal.hide();
    renderS3Table();
    toast('WMS图层已添加: ' + displayName, 'success');
}

function openAddWfsModal() {
    if (!TCG.addWfsModal) TCG.addWfsModal = new bootstrap.Modal(document.getElementById('addWfsModal'));
    document.getElementById('wfsUrl').value = 'http://localhost:8080/geoserver/wfs?SERVICE=WFS&VERSION=2.0.0&REQUEST=GetCapabilities';
    document.getElementById('wfsLoadStatus').textContent = '';
    document.getElementById('wfsTypeSelect').innerHTML = '<option value="">— 先获取要素类型 —</option>';
    document.getElementById('wfsLayerInfo').style.display = 'none';
    document.getElementById('addWfsBtn').disabled = true;
    TCG.addWfsModal.show();
}

async function fetchWfsTypes() {
    var url = document.getElementById('wfsUrl').value.trim();
    if (!url) { toast('请输入WFS服务地址', 'warning'); return; }
    var status = document.getElementById('wfsLoadStatus');
    status.textContent = '正在解析服务...';
    try {
        var resp = await fetch('/ogc/wfs/capabilities', {
            method:'POST', headers:{'Content-Type':'application/json'},
            body: JSON.stringify({url: url})
        });
        var data = await resp.json();
        if (!data.success) {
            status.textContent = '获取失败，请手动输入';
            toast('WFS Capabilities获取失败: ' + (data.message||''), 'warning');
            return;
        }
        var types = data.types || [];
        var sel = document.getElementById('wfsTypeSelect');
        sel.innerHTML = '';
        if (!types.length) {
            sel.innerHTML = '<option value="">— 未解析到要素类型 —</option>';
            status.textContent = '未解析到要素类型，请手动输入';
            toast('WFS Capabilities解析无结果，请手动输入TypeName', 'warning');
            document.getElementById('addWfsBtn').disabled = false;
            return;
        }
        types.forEach(function(t){
            var opt = document.createElement('option');
            opt.value = t.name;
            opt.textContent = t.title + ' (' + t.name + ')' + (t.srs ? ' [' + t.srs + ']' : '');
            sel.appendChild(opt);
        });
        status.textContent = '共 ' + types.length + ' 个要素类型';
        document.getElementById('addWfsBtn').disabled = false;
        document.getElementById('wfsLayerInfo').style.display = 'block';
        var info = '已解析 ' + types.length + ' 个要素类型';
        if (data._serviceTitle) info += ' · 服务: ' + data._serviceTitle;
        document.getElementById('wfsLayerInfo').innerHTML = info + '<br>选择或手动输入TypeName，将获取GeoJSON并显示';
        toast('获取到 ' + types.length + ' 个要素类型', 'success');
    } catch(e) {
        status.textContent = '请求失败，请手动输入';
        toast('WFS获取失败: ' + e.message + '，可手动输入TypeName', 'warning');
        document.getElementById('addWfsBtn').disabled = false;
    }
}

async function confirmAddWfs() {
    var url = document.getElementById('wfsUrl').value.trim();
    var sel = document.getElementById('wfsTypeSelect');
    var manual = document.getElementById('wfsTypeManual').value.trim();
    var typeName = manual || sel.value;
    if (!typeName) { toast('请选择或输入要素类型', 'warning'); return; }
    var color = document.getElementById('wfsColor').value;
    var opacity = parseFloat(document.getElementById('wfsOpacity').value);
    var displayName = manual || (sel.options[sel.selectedIndex] ? sel.options[sel.selectedIndex].text.split(' (')[0] : typeName) || typeName;

    var wfsUrl = url;
    if (wfsUrl.includes('REQUEST=GetCapabilities')) {
        wfsUrl = wfsUrl.replace(/REQUEST=GetCapabilities/i, 'REQUEST=GetFeature');
    } else {
        var baseUrl = url.includes('?') ? url.substring(0, url.indexOf('?')) : url;
        wfsUrl = baseUrl + '?SERVICE=WFS&VERSION=2.0.0&REQUEST=GetFeature&TYPENAMES='
            + encodeURIComponent(typeName) + '&OUTPUTFORMAT=application/json&COUNT=500';
    }
    if (!wfsUrl.includes('TYPENAMES=') && !wfsUrl.includes('typeName=')) {
        wfsUrl += (wfsUrl.includes('?') ? '&' : '?') + 'TYPENAMES=' + encodeURIComponent(typeName) + '&OUTPUTFORMAT=application/json&COUNT=500';
    }

    toast('正在获取WFS数据...', 'info');
    try {
        var resp = await fetch('/ogc/wfs/features', {
            method:'POST', headers:{'Content-Type':'application/json'},
            body: JSON.stringify({url: wfsUrl})
        });
        var data = await resp.json();
        if (!data.success || !data.data) {
            toast('获取WFS数据失败: ' + (data.message||''), 'danger');
            return;
        }
        var geojson = data.data;
        var geoLayer = L.geoJSON(geojson, {
            style: { color: color, fillColor: color, fillOpacity: opacity, weight: 2 },
            pointToLayer: function(feat, ll){
                return L.circleMarker(ll, { radius: 6, fillColor: color, color: '#fff', weight: 1, fillOpacity: opacity });
            },
            onEachFeature: function(feat, layer){
                if (feat.properties) {
                    var html = '<div style="font-size:11px">';
                    Object.keys(feat.properties).slice(0, 8).forEach(function(k){
                        html += '<b>' + k + ':</b> ' + feat.properties[k] + '<br>';
                    });
                    html += '</div>';
                    layer.bindPopup(html);
                }
            }
        });
        TCG.s3LayerGroup.addLayer(geoLayer);

        var l = {
            id: TCG.s3NextId++,
            type: 'wfs',
            url: url,
            displayName: displayName,
            layerName: typeName,
            color: color,
            opacity: opacity,
            visible: true,
            layerObj: geoLayer,
            sourceInfo: 'WFS - ' + typeName + ' (' + (geojson.features?.length||0) + ' 个要素)'
        };
        TCG.s3Layers.push(l);
        TCG.addWfsModal.hide();
        renderS3Table();
        toast('WFS图层已添加: ' + displayName + ' (' + (geojson.features?.length||0) + ' 个要素)', 'success');
    } catch(e) {
        toast('WFS加载失败: ' + e.message, 'danger');
    }
}

async function loadS3Presets() {
    toast('正在获取推荐服务...', 'info');
    try {
        var resp = await fetch('/ogc/presets');
        var data = await resp.json();
        if (!data.success || !data.data) { toast('获取失败', 'danger'); return; }
        var msg = data.data.map(function(s){ return s.name + ' (' + s.type + ')'; }).join(' · ');
        toast('推荐服务: ' + msg + ' — 请通过「+ 添加WMS」手动添加', 'info');
        data.data.forEach(function(svc){
            toast('📡 ' + svc.name + ' [' + svc.type + ']: ' + svc.description + (svc.note ? ' ('+svc.note+')' : ''), 'info', null, 12000);
        });
        if (data.data.length > 0) {
            document.getElementById('wmsUrl').value = data.data[0].url;
        }
    } catch(e) { toast('获取推荐服务失败: ' + e.message, 'danger'); }
}

function initDefaultS3Layers() {
    var defaults = [
        {
            url: 'https://ows.terrestris.de/osm/service',
            layerName: 'OSM_WMS',
            displayName: 'OSM 标准地图(WMS)',
            type: 'wms',
            style: '',
            opacity: 0.6,
            sourceInfo: 'terrestris.de 公共WMS服务'
        },
        {
            url: 'https://ows.terrestris.de/osm/service',
            layerName: 'TOPO-WMS',
            displayName: 'OSM 地形图(WMS)',
            type: 'wms',
            style: '',
            opacity: 0.5,
            sourceInfo: 'terrestris.de 公共WMS服务'
        }
    ];
    var count = 0;
    defaults.forEach(function(cfg){
        try {
            var baseUrl = cfg.url;
            var wmsLayer = L.tileLayer.wms(baseUrl, {
                layers: cfg.layerName,
                format: 'image/png',
                transparent: true,
                opacity: cfg.opacity,
                version: '1.1.1',
                styles: cfg.style || '',
                attribution: cfg.displayName
            }).addTo(TCG.lmap);
            TCG.s3Layers.push({
                id: TCG.s3NextId++,
                type: 'wms',
                url: cfg.url,
                displayName: cfg.displayName,
                layerName: cfg.layerName,
                style: cfg.style,
                opacity: cfg.opacity,
                visible: true,
                layerObj: wmsLayer,
                map: TCG.lmap,
                sourceInfo: cfg.sourceInfo
            });
            count++;
        } catch(e) {
            console.warn('默认3S图层加载失败:', cfg.layerName, e);
        }
    });
    if (count > 0) {
        renderS3Table();
        toast('已加载 ' + count + ' 个默认3S图层，可在「3S图层」中管理', 'success');
    }
}

async function correctCoordinates() {
    if (!confirm('将为所有亭子计算 GCJ-02 坐标(用于高德/腾讯地图)。继续?')) return;
    toast('正在批量纠偏...', 'info');
    try {
        var resp = await fetch('/coordinate/correct-pavilions?force=true', {method:'POST'});
        var data = await resp.json();
        if (data.success) {
            toast('纠偏完成: 更新 '+data.updated+' 条 / 共 '+data.total+' 条', 'success');
            loadPavilions();
        } else toast('纠偏失败', 'danger');
    } catch(e) { toast('纠偏失败: '+e.message, 'danger'); }
}

async function buildMultiModal() {
    if (!confirm('多模式路网将为每对亭子构建多种交通方式(driving/cycling/foot)。耗时较长且会清除现有路网。继续?')) return;
    toast('多模式路网构建中...(可能需要数分钟)', 'info');
    try {
        var resp = await fetch('/transport-routes/build-multi-modal', {method:'POST'});
        var data = await resp.json();
        if (data.success) {
            toast('已创建 '+data.data.routesCreated+' 条多模式路线', 'success');
            loadTransportRoutes();
        } else toast('构建失败: '+(data.message||''), 'danger');
    } catch(e) { toast('构建失败: '+e.message, 'danger'); }
}

async function osmImportScenic() {
    if (!confirm('将从OpenStreetMap导入滁州景区数据（含琅琊山风景区边界），继续？')) return;
    toast('正在从OSM导入景区数据...', 'info');
    try {
        var resp = await fetch('/osm/import/scenic', {method:'POST'});
        var data = await resp.json();
        if (data.success) {
            var r = data.data || {};
            toast('景区导入完成！新增 ' + (r.created||0) + ' 条，失败 ' + (r.errors||0) + ' 条', r.created > 0 ? 'success' : 'warning');
            window.loadScenicAreas();
        } else toast('导入失败: ' + (data.message||''), 'danger');
    } catch(e) { toast('导入失败: ' + e.message, 'danger'); }
}

async function osmImportAdmin() {
    if (!confirm('将从OpenStreetMap导入滁州市区划数据（市→区→街道三级），继续？')) return;
    toast('正在从OSM导入区划数据...', 'info');
    try {
        var resp = await fetch('/osm/import/admin', {method:'POST'});
        var data = await resp.json();
        if (data.success) {
            var r = data.data || {};
            toast('区划导入完成！新增 ' + (r.created||0) + ' 条，失败 ' + (r.errors||0) + ' 条', r.created > 0 ? 'success' : 'warning');
            window.loadAdminDivisions();
        } else toast('导入失败: ' + (data.message||''), 'danger');
    } catch(e) { toast('导入失败: ' + e.message, 'danger'); }
}

var editVertices = [];
var editTargetPoly = null;
var editTargetType = null;
var editTargetId = null;
var editVertexGroup = null;

function enableVertexEdit(type, id, wkt) {
    disableVertexEdit();
    editTargetType = type; editTargetId = id;
    var lyr = type === 'scenic' ? TCG.scenicPolyLyr : TCG.adminPolyLyr;
    if (!lyr) return;
    editVertexGroup = L.layerGroup().addTo(TCG.lmap);
    var rings = parseWktPolygon(wkt);
    if (!rings || !rings.length) return;
    var outer = rings[0].outer;
    if (!outer || outer.length < 3) return;
    editVertices = outer.map(function(p){ return [p[0], p[1]]; });
    editVertices.forEach(function(pt, idx){
        var gc = needGcj() ? gcj02(pt[0], pt[1]) : [pt[0], pt[1]];
        var marker = L.circleMarker([gc[1], gc[0]], {
            radius: 6, color: '#e74c3c', fillColor: '#fff', fillOpacity: 1, weight: 2
        }).addTo(editVertexGroup);
        marker.dragging.enable();
        marker._wgsIdx = idx;
        marker.on('drag', function(){ onVertexDrag(); });
        marker.on('dragend', function(){ onVertexDragEnd(); });
    });
    editTargetPoly = lyr;
    toast('进入边界编辑模式 - 拖拽红点调整边界', 'info');
}

function onVertexDrag() {
    if (!editVertexGroup) return;
    var newCoords = [];
    editVertexGroup.eachLayer(function(m){
        var ll = m.getLatLng();
        var nc = needGcj();
        var wgs = nc ? gcj02Wgs(ll.lng, ll.lat) : [ll.lng, ll.lat];
        newCoords.push({lng: wgs[0], lat: wgs[1], idx: m._wgsIdx});
    });
    newCoords.sort(function(a,b){ return a.idx - b.idx; });
    var pts = newCoords.map(function(p){ return [p.lng, p.lat]; });
    var wkt = 'POLYGON((' + pts.map(function(p){ return p[0]+' '+p[1]; }).join(', ') + ', ' + pts[0][0]+' '+pts[0][1] + '))';
    if (editTargetPoly) {
        editTargetPoly.eachLayer(function(lyr){
            if (lyr instanceof L.Polygon) {
                var ll = pts.map(function(p){ var g = needGcj()?gcj02(p[0],p[1]):[p[0],p[1]]; return [g[1],g[0]]; });
                lyr.setLatLngs(ll);
            }
        });
    }
}

function onVertexDragEnd() {
    if (!editTargetType || !editTargetId) return;
    var pts = [];
    editVertexGroup.eachLayer(function(m){
        pts.push({lng: null, lat: null, idx: m._wgsIdx, marker: m});
    });
    pts.sort(function(a,b){ return a.idx - b.idx; });
    pts.forEach(function(p){
        var ll = p.marker.getLatLng();
        var nc = needGcj();
        var wgs = nc ? gcj02Wgs(ll.lng, ll.lat) : [ll.lng, ll.lat];
        p.lng = wgs[0]; p.lat = wgs[1];
    });
    var coords = pts.map(function(p){ return [p.lng, p.lat]; });
    coords.push(coords[0]);
    var wkt = 'POLYGON((' + coords.map(function(p){ return p[0]+' '+p[1]; }).join(', ') + '))';
    var url = editTargetType === 'scenic' ? '/scenic-areas/' + editTargetId : '/admin-divisions/' + editTargetId;
    fetch(url, { method:'PUT', headers:{'Content-Type':'application/json'}, body: JSON.stringify({geomWkt: wkt}) })
        .then(function(r){ return r.json(); })
        .then(function(d){ if (d.success !== false) toast('边界已更新', 'success'); })
        .catch(function(e){ toast('保存失败: ' + e.message, 'danger'); });
}

function disableVertexEdit() {
    if (editVertexGroup) { TCG.lmap.removeLayer(editVertexGroup); editVertexGroup = null; }
    editVertices = []; editTargetPoly = null; editTargetType = null; editTargetId = null;
}

function startDraw(mode, targetFieldOrCb) {
    if (TCG.drawActive) cancelDraw();
    TCG.drawActive = true; TCG.drawMode = mode; TCG.drawPoints = [];
    var isField = typeof targetFieldOrCb === 'string';
    var fieldId = isField ? targetFieldOrCb : null;
    var cancelBtn = fieldId ? document.getElementById(fieldId === 'rfWkt' ? 'rfDrawCancel' : (fieldId === 'sfWkt' ? 'sfDrawCancel' : 'afDrawCancel')) : null;
    if (cancelBtn) cancelBtn.style.display = 'inline-block';
    var finishBtn = document.getElementById('finishDrawBtn');
    if (!finishBtn) {
        finishBtn = document.createElement('div');
        finishBtn.id = 'finishDrawBtn';
        finishBtn.style.cssText = 'position:absolute;bottom:80px;left:50%;transform:translateX(-50%);z-index:2000;padding:8px 24px;background:#27ae60;color:#fff;border:none;border-radius:20px;cursor:pointer;font-size:14px;box-shadow:0 2px 12px rgba(0,0,0,.5);display:none';
        finishBtn.textContent = '✅ 完成绘制';
        finishBtn.onclick = function(){ window.manualFinishDraw(); };
        document.getElementById('mapwrap').appendChild(finishBtn);
    }
    finishBtn.style.display = 'block';
    TCG.drawCallback = function(wkt, isManual){
        if (isField) {
            document.getElementById(fieldId).value = wkt;
        } else if (typeof targetFieldOrCb === 'function') {
            targetFieldOrCb(wkt, isManual);
        }
        TCG.drawActive = false; TCG.drawMode = null; TCG.drawPoints = [];
        if (cancelBtn) cancelBtn.style.display = 'none';
        if (finishBtn) finishBtn.style.display = 'none';
        if (TCG.drawLayer) { TCG.lmap.removeLayer(TCG.drawLayer); TCG.drawLayer = null; }
        if (!isManual) toast('绘制完成', 'success');
    };
    toast('点击地图添加' + (mode === 'line' ? '路径点' : '多边形顶点') + '，点击「完成绘制」保存', 'info');
    TCG.lmap.on('click', onDrawClick);
    TCG.lmap.on('dblclick', onDrawDblClick);
}

function manualFinishDraw() {
    if (!TCG.drawActive || TCG.drawPoints.length < 2) {
        toast('至少需要 2 个点', 'warning'); return;
    }
    var wkt;
    if (TCG.drawMode === 'polygon') {
        var pts = TCG.drawPoints.map(function(p){ return p[0] + ' ' + p[1]; }).join(', ');
        wkt = 'POLYGON((' + pts + ', ' + TCG.drawPoints[0][0] + ' ' + TCG.drawPoints[0][1] + '))';
    } else {
        var pts = TCG.drawPoints.map(function(p){ return p[0] + ' ' + p[1]; }).join(', ');
        wkt = 'LINESTRING(' + pts + ')';
    }
    TCG.lmap.off('click', onDrawClick);
    TCG.lmap.off('dblclick', onDrawDblClick);
    if (TCG.drawCallback) TCG.drawCallback(wkt, true);
    if (TCG.digitPendingType) {
        var name = prompt('请输入' + (TCG.digitPendingType === 'scenic' ? '景区' : TCG.digitPendingType === 'admin' ? '区划' : '路线') + '名称:');
        if (name && name.trim()) {
            saveDigitizedLayer(TCG.digitPendingType, wkt, name.trim());
        }
        TCG.digitPendingType = null;
    }
}

function onDrawClick(e) {
    if (!TCG.drawActive) return;
    TCG.drawPoints.push([e.latlng.lng, e.latlng.lat]);
    updateDrawLayer();
}

function onDrawDblClick(e) {
    TCG.lmap.stop();
}

function updateDrawLayer() {
    if (TCG.drawLayer) TCG.lmap.removeLayer(TCG.drawLayer);
    if (TCG.drawPoints.length < 2) return;
    var ll = TCG.drawPoints.map(function(p){ return [p[1], p[0]]; });
    if (TCG.drawMode === 'polygon') {
        TCG.drawLayer = L.polygon(ll, {color:'#ff9800', weight:2, fillOpacity:0.2}).addTo(TCG.lmap);
    } else {
        TCG.drawLayer = L.polyline(ll, {color:'#e67e22', weight:3, dashArray:'8,6'}).addTo(TCG.lmap);
    }
}

function cancelDraw() {
    TCG.drawActive = false; TCG.drawMode = null; TCG.drawPoints = [];
    TCG.digitPendingType = null;
    if (TCG.drawLayer) { TCG.lmap.removeLayer(TCG.drawLayer); TCG.drawLayer = null; }
    TCG.lmap.off('click', onDrawClick);
    TCG.lmap.off('dblclick', onDrawDblClick);
    var finishBtn = document.getElementById('finishDrawBtn');
    if (finishBtn) finishBtn.style.display = 'none';
    ['rfDrawCancel', 'sfDrawCancel', 'afDrawCancel'].forEach(function(id){
        var el = document.getElementById(id);
        if (el) el.style.display = 'none';
    });
    toast('已取消绘制', 'info');
}

function onDigitSelect(val) {
    if (!val) return;
    window.switchTab('map');
    TCG.digitPendingType = val;
    if (val === 'spot') {
        toast('点击地图上位置添加景点', 'info');
        TCG.lmap.once('click', function(e){
            var lng = e.latlng.lng, lat = e.latlng.lat;
            var name = prompt('请输入景点名称:');
            if (name && name.trim()) {
                saveDigitizedLayer('spot', null, name.trim(), lng, lat);
            }
            TCG.digitPendingType = null;
        });
    } else if (val === 'route') {
        startDraw('line', function(wkt, isManual){
            if (!isManual) return;
            var name = prompt('请输入路线名称:');
            if (name && name.trim()) saveDigitizedLayer('route', wkt, name.trim());
            TCG.digitPendingType = null;
        });
    } else {
        startDraw('polygon', function(wkt, isManual){
            if (!isManual) return;
            var name = prompt('请输入' + (val === 'scenic' ? '景区' : '区划') + '名称:');
            if (name && name.trim()) saveDigitizedLayer(val, wkt, name.trim());
            TCG.digitPendingType = null;
        });
    }
}

async function saveDigitizedLayer(type, wkt, name, lng, lat) {
    toast('正在保存...', 'info');
    try {
        var body = {};
        if (type === 'scenic') {
            body = { name: name, chineseName: name, geomWkt: wkt, areaType: '风景区', isOpenToPublic: true };
            var resp = await fetch('/scenic-areas', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) });
            if (resp.ok) { toast('景区「' + name + '」已保存', 'success'); window.loadScenicAreas(); }
            else { var e = await resp.json(); toast('保存失败: ' + (e.message || resp.status), 'danger'); }
        } else if (type === 'admin') {
            body = { name: name, chineseName: name, geomWkt: wkt, adminLevel: 'STREET' };
            var resp = await fetch('/admin-divisions', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) });
            if (resp.ok) { toast('区划「' + name + '」已保存', 'success'); window.loadAdminDivisions(); }
            else { var e = await resp.json(); toast('保存失败: ' + (e.message || resp.status), 'danger'); }
        } else if (type === 'route') {
            body = { name: name, geomWkt: wkt, routeType: 'HIKING', difficulty: 'EASY', color: '#e67e22' };
            var resp = await fetch('/tourism-routes', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) });
            if (resp.ok) { toast('路线「' + name + '」已保存', 'success'); window.loadTourismRoutes(); }
            else { var e = await resp.json(); toast('保存失败: ' + (e.message || resp.status), 'danger'); }
        } else if (type === 'spot') {
            body = { name: name, chineseName: name, longitude: lng, latitude: lat, areaType: '观景点', isOpenToPublic: true };
            var resp = await fetch('/scenic-areas', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) });
            if (resp.ok) { toast('景点「' + name + '」已保存', 'success');
                var icon = L.divIcon({ className:'pav-marker', html:'<div style="background:#e74c3c;width:14px;height:14px;border-radius:50%;border:2px solid #fff;box-shadow:0 1px 4px rgba(0,0,0,.4)"></div>', iconSize:[14,14], iconAnchor:[7,7] });
                var nc = needGcj() ? gcj02(lng, lat) : [lng, lat];
                L.marker([nc[1], nc[0]], {icon:icon}).addTo(TCG.s3LayerGroup).bindPopup('<b>'+name+'</b><br>观景点');
                window.loadScenicAreas();
            }
            else { var e = await resp.json(); toast('保存失败: ' + (e.message || resp.status), 'danger'); }
        }
    } catch(e) { toast('保存失败: ' + e.message, 'danger'); }
}

window.initMaps = initMaps;
window.switchMap = switchMap;
window.onMapClick = onMapClick;
window.toggleView = toggleView;
window.resetView = resetView;
window.zoomIn = zoomIn;
window.zoomOut = zoomOut;
window.loadPavilions = loadPavilions;
window.clearRouteLayers = clearRouteLayers;
window.loadTransportRoutes = loadTransportRoutes;
window.initRoutes = initRoutes;
window.toggleMode = toggleMode;
window.toggleTraffic = toggleTraffic;
window.togglePavLayer = togglePavLayer;
window.toggleCluster = toggleCluster;
window.toggleAttrLayer = toggleAttrLayer;
window.toggleScenicLayer = toggleScenicLayer;
window.toggleAdminLayer = toggleAdminLayer;
window.toggleRouteLayer = toggleRouteLayer;
window.showOnCesium = showOnCesium;
window.renderS3Table = renderS3Table;
window.renderS3LayerList = renderS3LayerList;
window.toggleS3Layer = toggleS3Layer;
window.removeS3Layer = removeS3Layer;
window.showS3LayerInfo = showS3LayerInfo;
window.filterS3Table = filterS3Table;
window.clearAllS3Layers = clearAllS3Layers;
window.openAddWmsModal = openAddWmsModal;
window.fetchWmsLayers = fetchWmsLayers;
window.confirmAddWms = confirmAddWms;
window.openAddWfsModal = openAddWfsModal;
window.fetchWfsTypes = fetchWfsTypes;
window.confirmAddWfs = confirmAddWfs;
window.loadS3Presets = loadS3Presets;
window.initDefaultS3Layers = initDefaultS3Layers;
window.correctCoordinates = correctCoordinates;
window.buildMultiModal = buildMultiModal;
window.osmImportScenic = osmImportScenic;
window.osmImportAdmin = osmImportAdmin;
window.enableVertexEdit = enableVertexEdit;
window.disableVertexEdit = disableVertexEdit;
window.startDraw = startDraw;
window.manualFinishDraw = manualFinishDraw;
window.cancelDraw = cancelDraw;
window.onDigitSelect = onDigitSelect;
window.parseWktCoords = parseWktCoords;
window.zoomToWkt = zoomToWkt;
window.parseWktPolygon = parseWktPolygon;
window.parseWktPolygonSimple = parseWktPolygonSimple;
window.parseWktLineSimple = parseWktLineSimple;
})();
