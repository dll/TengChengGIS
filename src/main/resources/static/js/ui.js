(function(){ 'use strict';
if (!window.TCG) window.TCG = {};

var TCG = window.TCG;
var toast = window.toast;
var tLabel = window.tLabel;
var tStyle = window.tStyle;
var escHtml = window.escHtml;
var leafCoord = window.leafCoord;
var pavLeafCoord = window.pavLeafCoord;
var needGcj = window.needGcj;
var gcj02 = window.gcj02;
var calcDist = window.calcDist;
var repositionForChinese = window.repositionForChinese;

var searchActive = false;
var measureActive = false, measurePts = [], measureLine = null, measureMarkers = [];
var addOnMapActive = false;
var tblSortField = null, tblSortAsc = true;
var selScenic = null, selAdmin = null;
var scenicModal = null, adminModal = null, scenicEditTarget = null, adminEditTarget = null;
var voiceRecognition = null, isVoiceListening = false, voiceBtnEl = null;

var routeEditTarget = null, selRoute = null;
var routeModalObj = null;

function toggleTbDropdown(id) {
    var content = document.querySelector('#' + id + ' .tb-drop-content');
    if (!content) return;
    var isShow = content.classList.contains('show');
    document.querySelectorAll('.tb-drop-content.show').forEach(function(el){ el.classList.remove('show'); });
    if (!isShow) content.classList.add('show');
}

function togglePanel(panelId) {
    var el = document.getElementById(panelId);
    if (!el) return;
    el.classList.toggle('panel-hidden');
    var btnId = panelId === 'ctrlPanel' ? 'toggleCtrlPanel' : 'toggleInfoPanel';
    var btn = document.getElementById(btnId);
    if (btn) btn.style.opacity = el.classList.contains('panel-hidden') ? '0.5' : '1';
}

function toggleSel(p) {
    var idx = TCG.sel.findIndex(function(x){return x.id===p.id});
    if (idx>-1) TCG.sel.splice(idx,1); else TCG.sel.push(p);
    updateSelDisplay();
    syncTableHighlight(p.id);
}

function updateSelDisplay() {
    var div = document.getElementById('selList');
    if (TCG.sel.length===0) { div.innerHTML='<p style="color:#888">暂未选择</p>'; return; }
    var h = ''; TCG.sel.forEach(function(p){
        h+='<div class="sel-item"><span>'+p.chineseName+'</span><span class="rm" onclick="removeSel('+p.id+')">&times;</span></div>';
    }); div.innerHTML = h;
}

function removeSel(id) { TCG.sel = TCG.sel.filter(function(p){return p.id!==id}); updateSelDisplay(); }

async function showDetail(p) {
    var div = document.getElementById('pavDetail');
    var ext = '';
    try {
        var resp = await fetch('/thousand-pavilions/multimedia/'+p.id), mm = await resp.json();
        if (mm&&mm.imageGallery&&mm.imageGallery.length>0) {
            ext+='<div style="margin-top:6px">';
            mm.imageGallery.forEach(function(img,i){
                ext+='<img src="'+img+'" style="width:100%;border-radius:3px;margin-bottom:3px;cursor:pointer" onclick="showImg(\''+img+'\')">';
            });
            ext+='</div>';
        }
        if (mm&&mm.textGuide) ext+='<p style="font-size:10px;color:#999;margin-top:4px">'+mm.textGuide+'</p>';
    } catch(e){}
    div.innerHTML = '<div><b style="font-size:13px">'+p.chineseName+'</b> <small style="color:#888">('+(p.name||'')+')</small>'+
        '<p style="margin:4px 0;font-size:11px">'+tLabel(p.type)+' | 评分:'+(p.visitorRating||'暂无')+'<br>坐标:('+p.longitude.toFixed(4)+','+p.latitude.toFixed(4)+')</p>'+
        (p.description?'<p style="font-size:10px;color:#aaa">'+(p.description||'').substring(0,120)+'...</p>':'')+
        ext+'</div>';
}

window.showImg = function(src) {
    var old = document.getElementById('imgModal'); if (old) old.remove();
    document.body.insertAdjacentHTML('beforeend','<div class="modal fade" id="imgModal" tabindex="-1"><div class="modal-dialog modal-lg"><div class="modal-content"><div class="modal-header"><h5 class="modal-title">图片预览</h5><button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button></div><div class="modal-body text-center"><img src="'+src+'" class="img-fluid"></div></div></div></div>');
    var m = new bootstrap.Modal(document.getElementById('imgModal')); m.show();
    document.getElementById('imgModal').addEventListener('hidden.bs.modal', function h(){ this.remove(); this.removeEventListener('hidden.bs.modal',h); });
};

async function showBestPath() {
    if (TCG.sel.length<2) { toast('请至少选择2个亭子', 'warning'); return; }
    try {
        var ids = TCG.sel.map(function(p){return p.id}), params = new URLSearchParams();
        ids.forEach(function(id){params.append('pavilionIds',id)});
        var resp = await fetch('/pavilions-gis/optimal-path?'+params.toString()), data = await resp.json();
        if (TCG.pathLyr) { TCG.lmap.removeLayer(TCG.pathLyr); TCG.pathLyr=null; }
        if (TCG.csPath) { TCG.viewer.entities.remove(TCG.csPath); TCG.csPath=null; }
        if (data.success&&data.path&&data.path.length>0) {
            var coords = data.path.map(function(pt){return [pt.latitude,pt.longitude]});
            TCG.pathLyr = L.polyline(coords, {color:'red',weight:3}).addTo(TCG.lmap);
            var pos = data.path.map(function(pt){return Cesium.Cartesian3.fromDegrees(pt.longitude,pt.latitude,100)});
            TCG.csPath = TCG.viewer.entities.add({ polyline:{positions:pos,width:3,material:Cesium.Color.RED.withAlpha(.8),clampToGround:true} });
            if (!TCG.is3D) TCG.lmap.fitBounds(TCG.pathLyr.getBounds());
            toast('最优路径: '+data.pathLength.toFixed(2)+'km', 'success');
        } else toast('路径规划失败', 'danger');
    } catch(e) { toast('路径规划失败: '+e.message, 'danger'); }
}

function clearPath() {
    if (TCG.pathLyr) { TCG.lmap.removeLayer(TCG.pathLyr); TCG.pathLyr=null; }
    if (TCG.csPath) { TCG.viewer.entities.remove(TCG.csPath); TCG.csPath=null; }
}

function startNav() {
    if (TCG.sel.length<2) { toast('请至少选择2个亭子', 'warning'); return; }
    if (TCG.navActive) { toast('导航已在进行', 'info'); return; }
    var ptGuide = document.getElementById('chkPointGuide').checked, lnGuide = document.getElementById('chkLineGuide').checked;
    if (!ptGuide&&!lnGuide) { toast('请启用一种播报方式', 'warning'); return; }
    TCG.navActive=true; TCG.navIdx=0;
    if (ptGuide) doPointGuide();
    if (lnGuide) doLineGuide();
    toast('导航已开始', 'success');
}

function stopNav() {
    TCG.navActive=false; TCG.navIdx=0;
    if ('speechSynthesis' in window) speechSynthesis.cancel();
    toast('导航已停止', 'info');
}

function doPointGuide() {
    if (!TCG.navActive||TCG.navIdx>=TCG.sel.length) { if (TCG.navIdx>=TCG.sel.length) toast('播报完成', 'success'); stopNav(); return; }
    var p = TCG.sel[TCG.navIdx], txt = '到达'+p.chineseName+'。';
    if (p.description) txt += p.description.substring(0,50)+'。';
    if (p.builtYear) txt += '建于'+p.builtYear+'年。';
    txt += '类型是'+tLabel(p.type)+'。';
    speak(txt, function(){ TCG.navIdx++; setTimeout(function(){ if (TCG.navActive) doPointGuide(); }, 5000); });
}

function doLineGuide() {
    if (TCG.sel.length<2) return;
    var f = TCG.sel[0], t = TCG.sel[TCG.sel.length-1], dist = 0;
    for (var i=0;i<TCG.sel.length-1;i++) { dist+=calcDist(TCG.sel[i].longitude,TCG.sel[i].latitude,TCG.sel[i+1].longitude,TCG.sel[i+1].latitude); }
    speak('从'+f.chineseName+'到'+t.chineseName+'，经过'+TCG.sel.length+'个亭子，约'+dist.toFixed(1)+'公里。');
}

function speak(text, onEnd) {
    if ('speechSynthesis' in window) {
        var u = new SpeechSynthesisUtterance(text); u.lang='zh-CN'; u.rate=.9;
        if (onEnd) u.onend=onEnd;
        speechSynthesis.speak(u);
    } else { toast(text, 'info'); if (onEnd) setTimeout(onEnd, 3000); }
}

function toggleSearch() {
    searchActive = !searchActive;
    document.getElementById('searchPanel').style.display = searchActive ? 'block' : 'none';
    if (searchActive) { document.getElementById('searchInput').focus(); document.getElementById('searchInput').value=''; document.getElementById('searchResults').innerHTML=''; }
    document.getElementById('btnSearch').style.background = searchActive ? 'rgba(138,180,248,.25)' : 'rgba(255,255,255,.08)';
}

function closeSearch() { searchActive = false; document.getElementById('searchPanel').style.display = 'none'; document.getElementById('btnSearch').style.background = 'rgba(255,255,255,.08)'; }

function doSearch() {
    var q = document.getElementById('searchInput').value.trim().toLowerCase();
    if (!q) { toast('请输入搜索关键词', 'warning'); return; }
    var results = TCG.pavs.filter(function(p){
        return (p.chineseName && p.chineseName.toLowerCase().indexOf(q) >= 0)
            || (p.street && p.street.toLowerCase().indexOf(q) >= 0)
            || (p.locationDesc && p.locationDesc.toLowerCase().indexOf(q) >= 0)
            || (p.structure && p.structure.toLowerCase().indexOf(q) >= 0);
    });
    var h = '';
    if (results.length === 0) {
        h = '<p style="color:#888;padding:8px;font-size:11px">未找到匹配结果</p>';
    } else {
        results.forEach(function(p){
            var latlng = leafCoord(p.longitude, p.latitude);
            h += '<div class="search-result" onclick="gotoSearchResult('+p.id+')" style="padding:6px 8px;margin:2px 0;background:rgba(255,255,255,.04);border-radius:4px;cursor:pointer;font-size:11px">';
            h += '<div style="font-weight:bold;color:#8ab4f8">' + escHtml(p.chineseName || p.name) + '</div>';
            h += '<div style="color:#aaa;font-size:10px">';
            if (p.street) h += escHtml(p.street) + ' | ';
            if (p.structure) h += escHtml(p.structure) + ' | ';
            h += '类型: ' + tLabel(p.type);
            h += '</div>';
            if (p.description) h += '<div style="color:#888;font-size:10px;margin-top:2px">' + escHtml(p.description.substring(0,80)) + '</div>';
            h += '</div>';
        });
    }
    h += '<p style="color:#666;font-size:10px;padding:4px 8px">共 ' + results.length + ' 条结果，点击定位</p>';
    document.getElementById('searchResults').innerHTML = h;
}

function gotoSearchResult(id) {
    var p = TCG.pavs.find(function(x){ return x.id === id; });
    if (!p) return;
    if (TCG.is3D && TCG.viewer) {
        TCG.viewer.camera.flyTo({ destination: Cesium.Cartesian3.fromDegrees(p.longitude, p.latitude, 2000), duration: 1.5 });
    } else {
        var c = leafCoord(p.longitude, p.latitude);
        TCG.lmap.setView(c, 16);
    }
    showPavilionDetail(p);
    closeSearch();
}

function showPavilionDetail(p) {
    var st = tStyle(p.pavilionType || p.type);
    var h = '<div style="padding:4px 0">';
    h += '<div style="display:flex;align-items:center;gap:6px"><span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:'+st.color+'"></span><b style="font-size:13px">'+escHtml(p.chineseName||p.name)+'</b></div>';
    h += '<table style="width:100%;font-size:10px;margin-top:6px;color:#bbb">';
    if (p.structure) h += '<tr><td style="color:#888;width:60px">结构</td><td>'+escHtml(p.structure)+'</td></tr>';
    if (p.topStyle) h += '<tr><td style="color:#888">风格</td><td>'+escHtml(p.topStyle)+'</td></tr>';
    if (p.street) h += '<tr><td style="color:#888">街道</td><td>'+escHtml(p.street)+'</td></tr>';
    if (p.locationDesc) h += '<tr><td style="color:#888">位置</td><td>'+escHtml(p.locationDesc)+'</td></tr>';
    if (p.builtYear) h += '<tr><td style="color:#888">建造</td><td>'+p.builtYear+'年</td></tr>';
    if (p.visitorRating) h += '<tr><td style="color:#888">评分</td><td>'+p.visitorRating+'</td></tr>';
    if (p.description) h += '<tr><td style="color:#888">描述</td><td style="word-break:break-all">'+escHtml(p.description.substring(0,100))+'</td></tr>';
    h += '<tr><td style="color:#888">坐标</td><td>'+p.longitude.toFixed(6)+', '+p.latitude.toFixed(6)+'</td></tr>';
    h += '</table>';
    h += '<div style="margin-top:6px;display:flex;gap:4px">';
    h += '<button onclick="selFromDetail('+p.id+')" style="padding:3px 10px;background:#2d5aa0;border:none;color:#fff;border-radius:3px;cursor:pointer;font-size:10px">选中</button>';
    h += '<button onclick="showCollectorInfo('+p.id+')" style="padding:3px 10px;background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.15);color:#ccc;border-radius:3px;cursor:pointer;font-size:10px">采集</button>';
    h += '<button onclick="closeInfo()" style="padding:3px 10px;background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.15);color:#ccc;border-radius:3px;cursor:pointer;font-size:10px">关闭</button>';
    h += '</div></div>';
    document.getElementById('pavDetail').innerHTML = h;
    document.getElementById('infoPanel').classList.add('visible');
}

function selFromDetail(id) {
    if (TCG.sel.indexOf(id) < 0) TCG.sel.push(id);
    updateSelDisplay();
    toast('已选中', 'success');
}

function closeInfo() { document.getElementById('infoPanel').classList.remove('visible'); }

function toggleMeasure() {
    measureActive = !measureActive;
    if (measureActive) {
        document.getElementById('measurePanel').style.display = 'block';
        document.getElementById('btnMeasure').style.background = 'rgba(243,156,18,.25)';
    } else {
        stopMeasure();
    }
}

function stopMeasure() {
    if (!measureActive) return;
    measureActive = false;
    document.getElementById('measurePanel').style.display = 'none';
    document.getElementById('btnMeasure').style.background = 'rgba(255,255,255,.08)';
    clearMeasure();
}

function clearMeasure() {
    if (measureLine) { TCG.lmap.removeLayer(measureLine); measureLine = null; }
    measureMarkers.forEach(function(m){ TCG.lmap.removeLayer(m); });
    measureMarkers = [];
    measurePts = [];
    document.getElementById('measureDist').textContent = '0';
    document.getElementById('measurePts').textContent = '0';
}

function addMeasurePoint(latlng, lng, lat) {
    measurePts.push({ latlng: latlng, lng: lng, lat: lat });
    var marker = L.circleMarker(latlng, { radius: 5, color: '#f39c12', fillColor: '#f39c12', fillOpacity: 0.8 }).addTo(TCG.lmap);
    var idx = measurePts.length;
    marker.bindTooltip(idx.toString(), { permanent: true, direction: 'top', offset: [0, -8], className: 'measure-label' });
    measureMarkers.push(marker);
    if (measurePts.length >= 2) {
        if (measureLine) TCG.lmap.removeLayer(measureLine);
        measureLine = L.polyline(measurePts.map(function(p){ return p.latlng; }), { color: '#f39c12', weight: 2, dashArray: '5,5' }).addTo(TCG.lmap);
    }
    var total = 0;
    for (var i = 1; i < measurePts.length; i++) {
        total += calcDist(measurePts[i-1].lng, measurePts[i-1].lat, measurePts[i].lng, measurePts[i].lat);
    }
    var displayDist = total < 1 ? (total * 1000).toFixed(0) + ' m' : total.toFixed(3) + ' km';
    document.getElementById('measureDist').textContent = displayDist;
    document.getElementById('measurePts').textContent = measurePts.length;
}

function toggleAddOnMap() {
    addOnMapActive = !addOnMapActive;
    document.getElementById('btnAddOnMap').style.background = addOnMapActive ? 'rgba(46,204,113,.25)' : 'rgba(255,255,255,.08)';
    if (addOnMapActive) {
        toast('请在地图上点击选择位置', 'info');
        TCG.lmap.getContainer().style.cursor = 'crosshair';
    } else {
        TCG.lmap.getContainer().style.cursor = '';
    }
}

function switchTab(tab) {
    var btns = document.querySelectorAll('#tabBar .tab-btn');
    btns.forEach(function(b){ b.classList.remove('active'); });
    var mapView = document.getElementById('mapwrap');
    var pavView = document.getElementById('dataView');
    var scenicView = document.getElementById('scenicDataView');
    var adminView = document.getElementById('adminDataView');
    var s3View = document.getElementById('s3DataView');
    var routeView = document.getElementById('routeDataView');
    [pavView, scenicView, adminView, s3View, routeView].forEach(function(v){ v.classList.remove('show'); });
    mapView.style.display = 'none';

    if (tab === 'map') {
        btns[0].classList.add('active');
        mapView.style.display = '';
    } else if (tab === 'pavilion') {
        btns[1].classList.add('active');
        pavView.classList.add('show');
        document.getElementById('statsDashboard').style.display = '';
        renderDataTable();
        renderStats();
    } else if (tab === 'scenic') {
        btns[2].classList.add('active');
        scenicView.classList.add('show');
        document.getElementById('scenicStatsDash').style.display = '';
        window.loadScenicAreas();
    } else if (tab === 'admin') {
        btns[3].classList.add('active');
        adminView.classList.add('show');
        document.getElementById('adminStatsDash').style.display = '';
        window.loadAdminDivisions();
    } else if (tab === '3s') {
        btns[5].classList.add('active');
        s3View.classList.add('show');
        window.renderS3Table();
    } else if (tab === 'route') {
        btns[4].classList.add('active');
        routeView.classList.add('show');
        window.loadTourismRoutes();
    }
}

function renderDataTable(filter) {
    var tbody = document.getElementById('tblBody');
    var rows = TCG.pavs.slice();
    if (filter) {
        var q = filter.toLowerCase();
        rows = rows.filter(function(p){
            return (p.chineseName||'').toLowerCase().includes(q) ||
                   (p.locationDesc||'').toLowerCase().includes(q) ||
                   (p.structure||'').toLowerCase().includes(q) ||
                   (p.topStyle||'').toLowerCase().includes(q) ||
                   (p.street||'').toLowerCase().includes(q);
        });
    }
    if (tblSortField) {
        rows.sort(function(a,b){
            var va = a[tblSortField], vb = b[tblSortField];
            if (va == null) va = ''; if (vb == null) vb = '';
            if (typeof va === 'string') va = va.toLowerCase();
            if (typeof vb === 'string') vb = vb.toLowerCase();
            if (va < vb) return tblSortAsc ? -1 : 1;
            if (va > vb) return tblSortAsc ? 1 : -1;
            return 0;
        });
    }
    var h = '';
    rows.forEach(function(p){
        var inSel = TCG.sel.findIndex(function(x){return x.id===p.id}) > -1;
        var st = tStyle(p.type);
        h += '<tr data-pid="'+p.id+'" class="'+(inSel?'sel-row':'')+'" onclick="selectFromTable('+p.id+')">';
        h += '<td class="col-id">'+p.id+'</td>';
        h += '<td class="col-name">'+escHtml(p.chineseName||p.name||'')+'</td>';
        h += '<td class="col-struct">'+escHtml(p.structure||'')+'</td>';
        h += '<td class="col-style" title="'+escHtml(p.topStyle||'')+'">'+escHtml(p.topStyle||'')+'</td>';
        h += '<td class="col-loc" title="'+escHtml(p.locationDesc||'')+'">'+escHtml(p.locationDesc||'')+'</td>';
        h += '<td class="col-street">'+escHtml(p.street||'')+'</td>';
        h += '<td class="col-area">'+(p.areaSize!=null?p.areaSize:'')+'</td>';
        h += '<td class="col-rating">'+(p.visitorRating!=null?p.visitorRating:'')+'</td>';
        h += '<td class="col-type"><span class="type-badge '+st.cls+'">'+(p.typeLabel||p.type||'')+'</span></td>';
        var cc = p.collectorCount || 0;
        h += '<td class="col-collector"><span class="collector-badge" onclick="event.stopPropagation();showCollectorInfo('+p.id+')" title="'+cc+'条采集记录">'+cc+'条</span></td>';
        h += '</tr>';
    });
    tbody.innerHTML = h;
    document.getElementById('tblTotal').textContent = rows.length;
    document.getElementById('tblSelCnt').textContent = TCG.sel.length;
}

function syncTableHighlight(id) {
    var dataView = document.getElementById('dataView');
    if (!dataView.classList.contains('show')) return;
    var rows = document.querySelectorAll('#tblBody tr');
    rows.forEach(function(tr){
        var pid = parseInt(tr.getAttribute('data-pid'));
        var inSel = TCG.sel.findIndex(function(x){return x.id===pid}) > -1;
        if (inSel) tr.classList.add('sel-row'); else tr.classList.remove('sel-row');
    });
    document.getElementById('tblSelCnt').textContent = TCG.sel.length;
    var target = document.querySelector('#tblBody tr[data-pid="'+id+'"]');
    if (target) { target.classList.add('active-row'); setTimeout(function(){ target.classList.remove('active-row'); }, 1500); target.scrollIntoView({block:'center',behavior:'smooth'}); }
}

function selectFromTable(id) {
    var p = TCG.pavs.find(function(x){return x.id===id});
    if (!p) return;
    var idx = TCG.sel.findIndex(function(x){return x.id===id});
    if (idx>-1) TCG.sel.splice(idx,1); else TCG.sel.push(p);
    updateSelDisplay();
    showDetail(p);
    syncTableHighlight(id);
    if (!TCG.is3D && TCG.lmap && p.latitude && p.longitude) {
        TCG.lmap.setView([p.latitude, p.longitude], TCG.lmap.getZoom()<15?15:TCG.lmap.getZoom());
    } else if (TCG.is3D && TCG.viewer) {
        TCG.viewer.camera.flyTo({destination:Cesium.Cartesian3.fromDegrees(p.longitude,p.latitude,500),duration:1});
    }
}

function filterTable() {
    var q = document.getElementById('tblSearch').value;
    renderDataTable(q);
}

function tblSort(field) {
    if (tblSortField === field) tblSortAsc = !tblSortAsc;
    else { tblSortField = field; tblSortAsc = true; }
    document.querySelectorAll('#dataTable thead th').forEach(function(th){ th.classList.remove('sorted'); });
    var colMap = {id:0,chineseName:1,structure:2,topStyle:3,locationDesc:4,street:5,areaSize:6,visitorRating:7,type:8,collectorCount:9};
    var th = document.querySelectorAll('#dataTable thead th')[colMap[field]];
    if (th) { th.classList.add('sorted'); th.querySelector('.sort-arrow').textContent = tblSortAsc?'▲':'▼'; }
    renderDataTable(document.getElementById('tblSearch').value);
}

function tblClearSel() {
    TCG.sel = [];
    updateSelDisplay();
    renderDataTable(document.getElementById('tblSearch').value);
}

function exportTableCSV() {
    var q = document.getElementById('tblSearch').value;
    var rows = TCG.pavs.slice();
    if (q) {
        var fq = q.toLowerCase();
        rows = rows.filter(function(p){
            return (p.chineseName||'').toLowerCase().includes(fq) ||
                   (p.locationDesc||'').toLowerCase().includes(fq) ||
                   (p.structure||'').toLowerCase().includes(fq) ||
                   (p.topStyle||'').toLowerCase().includes(fq) ||
                   (p.street||'').toLowerCase().includes(fq);
        });
    }
    var csv = '﻿ID,名称,结构,风格(平立面),位置,街道,面积(m²),评分,类型,采集记录\n';
    rows.forEach(function(p){
        csv += [p.id, '"'+(p.chineseName||'')+'"', '"'+(p.structure||'')+'"', '"'+(p.topStyle||'')+'"',
            '"'+(p.locationDesc||'')+'"', '"'+(p.street||'')+'"', p.areaSize||'', p.visitorRating||'',
            '"'+(p.typeLabel||p.type||'')+'"', p.collectorCount||0].join(',') + '\n';
    });
    var blob = new Blob([csv], {type:'text/csv;charset=utf-8'});
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url; a.download = 'pavilion_data.csv';
    document.body.appendChild(a); a.click(); document.body.removeChild(a);
    URL.revokeObjectURL(url);
    toast('已导出 '+rows.length+' 条数据', 'success');
}

function renderStats() {
    if (TCG.pavs.length === 0) return;
    function countBy(arr, field) {
        var map = {};
        arr.forEach(function(p){
            var val = p[field];
            if (!val || (typeof val === 'string' && val.trim() === '')) val = '未分类';
            map[val] = (map[val] || 0) + 1;
        });
        return map;
    }
    function renderBarChart(cid, title, mapObs) {
        var entries = Object.entries(mapObs).sort(function(a,b){ return b[1] - a[1]; });
        var max = entries.length > 0 ? entries[0][1] : 1;
        var h = '<h6>'+title+' ('+entries.length+'种)</h6>';
        var topN = entries.slice(0, 15);
        topN.forEach(function(e){
            var pct = Math.round(e[1] / max * 100);
            h += '<div class="stat-bar-row">' +
                '<span class="bar-label" title="'+escHtml(e[0])+'">'+escHtml(e[0])+'</span>' +
                '<span class="bar-track"><span class="bar-fill" style="width:'+pct+'%"></span></span>' +
                '<span class="bar-cnt">'+e[1]+'</span>' +
                '</div>';
        });
        if (entries.length > 15)
            h += '<span class="stat-toggle" onclick="this.style.display=\'none\';var c=document.getElementById(\''+cid+'\');c.innerHTML=c._fullHtml">显示全部('+entries.length+'项)</span>';
        document.getElementById(cid).innerHTML = h;
        if (entries.length > 15) {
            var fullH = '<h6>'+title+' ('+entries.length+'种)</h6>';
            entries.forEach(function(e){
                var pct = Math.round(e[1] / max * 100);
                fullH += '<div class="stat-bar-row">' +
                    '<span class="bar-label" title="'+escHtml(e[0])+'">'+escHtml(e[0])+'</span>' +
                    '<span class="bar-track"><span class="bar-fill" style="width:'+pct+'%"></span></span>' +
                    '<span class="bar-cnt">'+e[1]+'</span></div>';
            });
            document.getElementById(cid)._fullHtml = fullH;
        }
    }
    var structMap = countBy(TCG.pavs, 'structure');
    var styleMap = countBy(TCG.pavs, 'topStyle');
    var streetMap = countBy(TCG.pavs, 'street');
    var typeMap = countBy(TCG.pavs, 'typeLabel');
    renderBarChart('chartStructure', '结构分布', structMap);
    renderBarChart('chartTopStyle', '风格/平立面分布', styleMap);
    renderBarChart('chartType', '类型分布', typeMap);
    renderBarChart('chartStreet', '街道分布', streetMap);
    document.getElementById('statsOverview').innerHTML =
        '<div class="stat-card"><div class="stat-num">'+TCG.pavs.length+'</div><div class="stat-label">总亭子数</div></div>' +
        '<div class="stat-card"><div class="stat-num" style="color:#e74c3c">'+(structMap?Object.keys(structMap).length:0)+'</div><div class="stat-label">结构类型</div></div>' +
        '<div class="stat-card"><div class="stat-num" style="color:#2ecc71">'+(styleMap?Object.keys(styleMap).length:0)+'</div><div class="stat-label">风格类型</div></div>' +
        '<div class="stat-card"><div class="stat-num" style="color:#f39c12">'+(streetMap?Object.keys(streetMap).length:0)+'</div><div class="stat-label">街道数量</div></div>' +
        '<div class="stat-card"><div class="stat-num" style="color:#3498db">'+(typeMap?Object.keys(typeMap).length:0)+'</div><div class="stat-label">亭子类型</div></div>';
    fetch('/pavilions/stats').then(function(r){return r.json()}).then(function(d){
        if (d.success && d.data) {
            var avgEl = document.createElement('div');
            avgEl.className = 'stat-card';
            avgEl.innerHTML = '<div class="stat-num" style="color:#9b59b6">'+(d.data.averageRating?d.data.averageRating.toFixed(1):'-')+'</div><div class="stat-label">平均评分</div>';
            document.getElementById('statsOverview').appendChild(avgEl);
        }
    }).catch(function(){});
}

async function showCollectorInfo(pavilionId) {
    try {
        var pav = TCG.pavs.find(function(p){ return p.id === pavilionId; });
        var resp = await fetch('/thousand-pavilions/' + pavilionId + '/collectors');
        var data = await resp.json();
        var colls = data.data || [];
        var h = '<div style="padding:4px 0">';
        h += '<b style="font-size:13px">' + (pav ? escHtml(pav.chineseName || pav.name) : '亭子#' + pavilionId) + '</b>';
        h += '<p style="font-size:10px;color:#888;margin:2px 0">采集记录: ' + colls.length + '条</p>';
        colls.forEach(function(c, i){
            h += '<div style="background:rgba(255,255,255,.04);border-radius:4px;padding:7px 8px;margin:4px 0;font-size:11px">';
            h += '<b>#' + (i+1) + ' ' + escHtml(c.collectorName || '未知') + '</b>';
            h += '<table style="width:100%;font-size:10px;margin-top:3px;color:#bbb">';
            h += '<tr><td style="color:#888">采集工具</td><td>' + escHtml(c.collectionTool || '-') + '</td></tr>';
            h += '<tr><td style="color:#888">数据来源</td><td>' + escHtml(c.dataSource || '-') + '</td></tr>';
            h += '<tr><td style="color:#888">采集时间</td><td>' + (c.collectionTime ? new Date(c.collectionTime).toLocaleString('zh-CN') : '-') + '</td></tr>';
            if (c.accuracy != null) h += '<tr><td style="color:#888">精度/误差</td><td>' + c.accuracy + ' m</td></tr>';
            if (c.notes) h += '<tr><td style="color:#888">备注</td><td>' + escHtml(c.notes) + '</td></tr>';
            h += '</table></div>';
        });
        if (colls.length === 0) h += '<p style="color:#888;font-size:11px">暂无采集记录</p>';
        h += '</div>';
        document.getElementById('pavDetail').innerHTML = h;
        document.getElementById('infoPanel').classList.add('visible');
    } catch(e) {
        toast('获取采集记录失败: ' + e.message, 'danger');
    }
}

var _origLoadPavilions = window.loadPavilions;
window.loadPavilions = function() {
    var r = _origLoadPavilions();
    if (document.getElementById('dataView').classList.contains('show')) {
        renderDataTable();
        renderStats();
    }
    return r;
};

async function loadScenicAreas() {
    try {
        var resp = await fetch('/scenic-areas/locations?includeCollectorCounts=true');
        TCG.scenicAreas = await resp.json();
        renderScenicTable();
        renderScenicStats();
        renderScenicPolygons();
    } catch(e) { toast('加载景区失败: '+e.message, 'danger'); }
}

function renderScenicPolygons() {
    if (!TCG.lmap) return;
    if (TCG.scenicPolyLyr) TCG.lmap.removeLayer(TCG.scenicPolyLyr);
    TCG.scenicPolyLyr = L.layerGroup();
    TCG.scenicAreas.forEach(function(sa){
        if (!sa.geomWkt) return;
        var rings = window.parseWktPolygon(sa.geomWkt);
        rings.forEach(function(ring){
            if (!ring.outer || ring.outer.length < 3) return;
            var coords = ring.outer.map(function(p){
                return leafCoord(p[1], p[0]);
            });
            var poly = L.polygon(coords, {
                color: '#27ae60', weight: 2, opacity: 0.8,
                fillColor: '#27ae60', fillOpacity: 0.25
            });
            poly.bindPopup('<b>'+escHtml(sa.chineseName||sa.name)+'</b><br>类型:'+(sa.areaType||'')+'<br>面积:'+(sa.areaSize||'')+' km²');
            poly.on('click', function(){ showScenicDetail(sa); });
            poly._wgsCoords = ring.outer.map(function(p){ return [p[0], p[1]]; });
            TCG.scenicPolyLyr.addLayer(poly);
        });
    });
    TCG.scenicPolyLyr.addTo(TCG.lmap);
    toast('景区多边形: '+TCG.scenicAreas.length+'个', 'info');
}

function showScenicDetail(sa) {
    var h = '<b style="font-size:13px">'+escHtml(sa.chineseName||sa.name)+'</b>';
    h += '<table style="width:100%;font-size:11px;margin-top:4px;color:#bbb">';
    h += '<tr><td style="color:#888">类型</td><td>'+escHtml(sa.areaType||'')+'</td></tr>';
    h += '<tr><td style="color:#888">面积</td><td>'+(sa.areaSize||'')+' km²</td></tr>';
    h += '<tr><td style="color:#888">评分</td><td>'+(sa.visitorRating||'')+'</td></tr>';
    h += '<tr><td style="color:#888">采集记录</td><td><span class="collector-badge" style="cursor:pointer" onclick="showScenicCollectorInfo('+sa.id+')">'+(sa.collectorCount||0)+'条</span></td></tr>';
    h += '</table>';
    document.getElementById('pavDetail').innerHTML = h;
    document.getElementById('infoPanel').classList.add('visible');
}

async function showScenicCollectorInfo(scenicAreaId) {
    try {
        var sa = TCG.scenicAreas.find(function(s){ return s.id === scenicAreaId; });
        var resp = await fetch('/scenic-areas/'+scenicAreaId+'/collectors');
        var data = await resp.json();
        var colls = data.data || [];
        var h = '<b style="font-size:13px">'+(sa?escHtml(sa.chineseName||sa.name):'景区#'+scenicAreaId)+'</b>';
        h += '<p style="font-size:10px;color:#888;margin:2px 0">采集记录: '+colls.length+'条</p>';
        colls.forEach(function(c,i){
            h += '<div style="background:rgba(255,255,255,.04);border-radius:4px;padding:7px 8px;margin:4px 0;font-size:11px">';
            h += '<b>#'+(i+1)+' '+escHtml(c.collectorName||'未知')+'</b>';
            h += '<table style="width:100%;font-size:10px;margin-top:3px;color:#bbb">';
            h += '<tr><td style="color:#888">工具</td><td>'+escHtml(c.collectionTool||'-')+'</td></tr>';
            h += '<tr><td style="color:#888">来源</td><td>'+escHtml(c.dataSource||'-')+'</td></tr>';
            h += '<tr><td style="color:#888">时间</td><td>'+(c.collectionTime?new Date(c.collectionTime).toLocaleString('zh-CN'):'-')+'</td></tr>';
            if(c.accuracy!=null) h += '<tr><td style="color:#888">精度</td><td>'+c.accuracy+' m</td></tr>';
            if(c.notes) h += '<tr><td style="color:#888">备注</td><td>'+escHtml(c.notes)+'</td></tr>';
            h += '</table></div>';
        });
        if(colls.length===0) h += '<p style="color:#888;font-size:11px">暂无采集记录</p>';
        document.getElementById('pavDetail').innerHTML = h;
    } catch(e) { toast('获取采集记录失败: '+e.message, 'danger'); }
}

function renderScenicTable() {
    var tbody = document.getElementById('scenicTblBody');
    var q = (document.getElementById('scenicSearch')||{}).value||'';
    var rows = TCG.scenicAreas.slice();
    if (q) {
        var lq = q.toLowerCase();
        rows = rows.filter(function(s){
            return (s.chineseName||'').toLowerCase().includes(lq) || (s.name||'').toLowerCase().includes(lq) || (s.areaType||'').toLowerCase().includes(lq) || (s.address||'').toLowerCase().includes(lq);
        });
    }
    var h = '';
    rows.forEach(function(sa){
        h += '<tr class="'+(selScenic&&selScenic.id===sa.id?'row-sel':'')+'" onclick="selScenicRow('+sa.id+',event)">';
        h += '<td class="col-id">'+sa.id+'</td>';
        h += '<td class="col-name">'+escHtml(sa.chineseName||sa.name||'')+'</td>';
        h += '<td class="col-type">'+escHtml(sa.areaType||'')+'</td>';
        h += '<td class="col-area">'+(sa.areaSize!=null?sa.areaSize:'')+'</td>';
        h += '<td class="col-loc" title="'+escHtml(sa.address||'')+'">'+escHtml(sa.address||'')+'</td>';
        h += '<td class="col-rating">'+(sa.visitorRating!=null?sa.visitorRating:'')+'</td>';
        h += '<td class="col-collector"><span class="collector-badge" onclick="event.stopPropagation();showScenicCollectorInfo('+sa.id+')">'+(sa.collectorCount||0)+'条</span></td>';
        h += '</tr>';
    });
    tbody.innerHTML = h;
    document.getElementById('scenicTotal').textContent = rows.length;
}

function selScenicRow(id, ev) {
    var found = TCG.scenicAreas.find(function(s){ return s.id===id; });
    if (!found) return;
    selScenic = found; selAdmin = null;
    updateScenicBtnState(); updateAdminBtnState();
    renderScenicTable();
}

function renderScenicStats() {
    if (TCG.scenicAreas.length===0) return;
    var typeMap = {};
    TCG.scenicAreas.forEach(function(s){
        var t = s.areaType||'未分类';
        typeMap[t] = (typeMap[t]||0)+1;
    });
    var entries = Object.entries(typeMap).sort(function(a,b){ return b[1]-a[1]; });
    var max = entries.length>0?entries[0][1]:1;
    var h = '<h6>景区类型 ('+entries.length+'种)</h6>';
    entries.slice(0,15).forEach(function(e){
        var pct = Math.round(e[1]/max*100);
        h += '<div class="stat-bar-row"><span class="bar-label">'+escHtml(e[0])+'</span><span class="bar-track"><span class="bar-fill" style="width:'+pct+'%"></span></span><span class="bar-cnt">'+e[1]+'</span></div>';
    });
    document.getElementById('scenicChartType').innerHTML = h;
    document.getElementById('scenicStatsOverview').innerHTML =
        '<div class="stat-card"><div class="stat-num">'+TCG.scenicAreas.length+'</div><div class="stat-label">总景区数</div></div>'+
        '<div class="stat-card"><div class="stat-num" style="color:#27ae60">'+Object.keys(typeMap).length+'</div><div class="stat-label">景区类型</div></div>';
}

function filterScenicTable() { renderScenicTable(); }

function exportScenicCSV() {
    var csv = '﻿ID,名称,类型,面积(km²),地址,评分,采集记录\n';
    TCG.scenicAreas.forEach(function(s){
        csv += [s.id,'"'+escHtml(s.chineseName||s.name||'')+'"','"'+escHtml(s.areaType||'')+'"',s.areaSize||'','"'+escHtml(s.address||'')+'"',s.visitorRating||'',s.collectorCount||0].join(',')+'\n';
    });
    var blob = new Blob([csv],{type:'text/csv;charset=utf-8'});
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a'); a.href=url; a.download='scenic_area_data.csv';
    document.body.appendChild(a); a.click(); document.body.removeChild(a);
    URL.revokeObjectURL(url);
    toast('已导出 '+TCG.scenicAreas.length+' 条景区数据', 'success');
}

function flyToScenic(id) {
    var sa = TCG.scenicAreas.find(function(s){return s.id===id});
    if (!sa) return;
    showScenicDetail(sa);
    if (!TCG.is3D && TCG.lmap && sa.latitude && sa.longitude) {
        TCG.lmap.setView([sa.latitude, sa.longitude], 14);
    }
}

async function loadAdminDivisions() {
    try {
        var resp = await fetch('/admin-divisions/locations?includeCollectorCounts=true');
        TCG.adminDivisions = await resp.json();
        if (TCG.adminTreeMode) renderAdminTree();
        else renderAdminTable();
        renderAdminStats();
        renderAdminPolygons();
    } catch(e) { toast('加载区划失败: '+e.message, 'danger'); }
}

function renderAdminPolygons() {
    if (!TCG.lmap) return;
    if (TCG.adminPolyLyr) TCG.lmap.removeLayer(TCG.adminPolyLyr);
    TCG.adminPolyLyr = L.layerGroup();
    var levelColors = {CITY:'#e74c3c', DISTRICT:'#3498db', COUNTY:'#f39c12', STREET:'#2ecc71', TOWN:'#9b59b6'};
    TCG.adminDivisions.forEach(function(ad){
        if (!ad.geomWkt) return;
        var rings = window.parseWktPolygon(ad.geomWkt);
        var color = levelColors[ad.adminLevel] || '#8e44ad';
        rings.forEach(function(ring){
            if (!ring.outer || ring.outer.length < 3) return;
            var coords = ring.outer.map(function(p){
                return leafCoord(p[1], p[0]);
            });
            var poly = L.polygon(coords, {
                color: color, weight: 2, opacity: 0.8,
                fillColor: color, fillOpacity: 0.2, dashArray: '4,3'
            });
            poly.bindPopup('<b>'+escHtml(ad.chineseName||ad.name)+'</b><br>级别:'+(ad.adminLevel||'')+'<br>人口:'+(ad.population||'')+'<br>上级:'+(ad.parentName||'无'));
            poly._wgsCoords = ring.outer.map(function(p){ return [p[0], p[1]]; });
            TCG.adminPolyLyr.addLayer(poly);
        });
    });
    TCG.adminPolyLyr.addTo(TCG.lmap);
}

function renderAdminTable() {
    var tbody = document.getElementById('adminTblBody');
    var q = (document.getElementById('adminSearch')||{}).value||'';
    var rows = TCG.adminDivisions.slice();
    if (q) {
        var lq = q.toLowerCase();
        rows = rows.filter(function(a){
            return (a.chineseName||'').toLowerCase().includes(lq) || (a.name||'').toLowerCase().includes(lq) || (a.adminLevel||'').toLowerCase().includes(lq);
        });
    }
    var h = '';
    rows.forEach(function(ad){
        h += '<tr class="'+(selAdmin&&selAdmin.id===ad.id?'row-sel':'')+'" onclick="selAdminRow('+ad.id+',event)">';
        h += '<td class="col-name">'+escHtml(ad.chineseName||ad.name||'')+'</td>';
        h += '<td class="col-level"><span class="level-badge '+(ad.adminLevel||'')+'">'+levelLabel(ad.adminLevel)+'</span></td>';
        h += '<td class="col-loc">'+escHtml(ad.parentName||'无')+'</td>';
        h += '<td class="col-area">'+(ad.areaSize!=null?ad.areaSize:'')+'</td>';
        h += '<td class="col-rating">'+(ad.population!=null?ad.population:'')+'</td>';
        h += '<td class="col-id">'+escHtml(ad.adminCode||'')+'</td>';
        h += '<td class="col-collector"><span class="collector-badge" onclick="event.stopPropagation();showAdminCollectorInfo('+ad.id+')">'+(ad.collectorCount||0)+'条</span></td>';
        h += '</tr>';
    });
    tbody.innerHTML = h;
    document.getElementById('adminTotal').textContent = rows.length;
}

function selAdminRow(id, ev) {
    var found = TCG.adminDivisions.find(function(a){ return a.id===id; });
    if (!found) return;
    selAdmin = found; selScenic = null;
    updateScenicBtnState(); updateAdminBtnState();
    renderAdminTable();
}

function renderAdminTree() {
    var tbody = document.getElementById('adminTblBody');
    var roots = TCG.adminDivisions.filter(function(a){ return !a.parentId; });
    var childrenMap = {};
    TCG.adminDivisions.forEach(function(a){
        if (a.parentId) {
            if (!childrenMap[a.parentId]) childrenMap[a.parentId] = [];
            childrenMap[a.parentId].push(a);
        }
    });
    var h = '';
    function renderNode(node, depth) {
        var indent = '', i;
        for (i=0;i<depth;i++) indent += '<span class="tree-indent"></span>';
        var hasChildren = childrenMap[node.id] && childrenMap[node.id].length > 0;
        var toggleId = 'treeToggle_'+node.id;
        h += '<tr class="'+(depth>0?'tree-row-child':'')+'" data-admin-id="'+node.id+'" data-depth="'+depth+'">';
        h += '<td class="col-name">'+indent+(hasChildren?'<span class="tree-toggle" id="'+toggleId+'" onclick="event.stopPropagation();toggleTreeNode('+node.id+')">▼</span>':'<span class="tree-indent"></span>')+escHtml(node.chineseName||node.name||'')+'</td>';
        h += '<td class="col-level"><span class="level-badge '+(node.adminLevel||'')+'">'+levelLabel(node.adminLevel)+'</span></td>';
        h += '<td class="col-loc">'+escHtml(node.parentName||'无')+'</td>';
        h += '<td class="col-area">'+(node.areaSize!=null?node.areaSize:'')+'</td>';
        h += '<td class="col-rating">'+(node.population!=null?node.population:'')+'</td>';
        h += '<td class="col-id">'+escHtml(node.adminCode||'')+'</td>';
        h += '<td class="col-collector"><span class="collector-badge" onclick="event.stopPropagation();showAdminCollectorInfo('+node.id+')">'+(node.collectorCount||0)+'条</span></td>';
        h += '</tr>';
        if (hasChildren) {
            childrenMap[node.id].forEach(function(child){ renderNode(child, depth+1); });
        }
    }
    roots.forEach(function(r){ renderNode(r, 0); });
    tbody.innerHTML = h;
    document.getElementById('adminTotal').textContent = TCG.adminDivisions.length;
}

function toggleAdminTree() {
    TCG.adminTreeMode = !TCG.adminTreeMode;
    var btn = document.querySelector('#adminToolbar button');
    btn.textContent = TCG.adminTreeMode ? '列表' : '树形';
    if (TCG.adminTreeMode) renderAdminTree();
    else renderAdminTable();
}

function toggleTreeNode(nodeId) {
    var depth = parseInt(document.querySelector('tr[data-admin-id="'+nodeId+'"]').getAttribute('data-depth'));
    var rows = document.querySelectorAll('#adminTblBody tr');
    var found = false;
    var toggle = document.getElementById('treeToggle_'+nodeId);
    var collapsed = toggle.textContent === '▼';
    toggle.textContent = collapsed ? '▶' : '▼';
    rows.forEach(function(row){
        var rid = parseInt(row.getAttribute('data-admin-id'));
        if (rid === nodeId) { found = true; return; }
        if (found) {
            var d = parseInt(row.getAttribute('data-depth'));
            if (d <= depth) { found = false; return; }
            if (collapsed) row.style.display = 'none';
            else row.style.display = '';
        }
    });
}

function renderAdminStats() {
    if (TCG.adminDivisions.length===0) return;
    var levelMap = {};
    TCG.adminDivisions.forEach(function(a){
        var l = a.adminLevel||'未知';
        levelMap[l] = (levelMap[l]||0)+1;
    });
    var entries = Object.entries(levelMap).sort(function(a,b){ return b[1]-a[1]; });
    var max = entries.length>0?entries[0][1]:1;
    var h = '<h6>层级分布</h6>';
    entries.forEach(function(e){
        var pct = Math.round(e[1]/max*100);
        h += '<div class="stat-bar-row"><span class="bar-label">'+levelLabel(e[0])+'</span><span class="bar-track"><span class="bar-fill" style="width:'+pct+'%"></span></span><span class="bar-cnt">'+e[1]+'</span></div>';
    });
    document.getElementById('adminChartLevel').innerHTML = h;
    var totalPop = TCG.adminDivisions.reduce(function(s,a){ return s+(a.population||0); }, 0);
    document.getElementById('adminStatsOverview').innerHTML =
        '<div class="stat-card"><div class="stat-num">'+TCG.adminDivisions.length+'</div><div class="stat-label">总区划数</div></div>'+
        '<div class="stat-card"><div class="stat-num" style="color:#8e44ad">'+Object.keys(levelMap).length+'</div><div class="stat-label">层级类型</div></div>'+
        '<div class="stat-card"><div class="stat-num" style="color:#f39c12">'+(totalPop>0?(totalPop/10000).toFixed(0)+'万':'-')+'</div><div class="stat-label">总人口</div></div>';
}

function levelLabel(lv) {
    var map = {CITY:'市', DISTRICT:'区/县', COUNTY:'县', STREET:'街道', TOWN:'镇'};
    return map[lv] || lv || '未知';
}

async function showAdminCollectorInfo(adminDivId) {
    try {
        var ad = TCG.adminDivisions.find(function(a){ return a.id === adminDivId; });
        var resp = await fetch('/admin-divisions/'+adminDivId+'/collectors');
        var data = await resp.json();
        var colls = data.data || [];
        var h = '<b style="font-size:13px">'+(ad?escHtml(ad.chineseName||ad.name):'区划#'+adminDivId)+'</b>';
        h += '<p style="font-size:10px;color:#888;margin:2px 0">采集记录: '+colls.length+'条</p>';
        colls.forEach(function(c,i){
            h += '<div style="background:rgba(255,255,255,.04);border-radius:4px;padding:7px 8px;margin:4px 0;font-size:11px">';
            h += '<b>#'+(i+1)+' '+escHtml(c.collectorName||'未知')+'</b>';
            h += '<table style="width:100%;font-size:10px;margin-top:3px;color:#bbb">';
            h += '<tr><td style="color:#888">工具</td><td>'+escHtml(c.collectionTool||'-')+'</td></tr>';
            h += '<tr><td style="color:#888">来源</td><td>'+escHtml(c.dataSource||'-')+'</td></tr>';
            h += '<tr><td style="color:#888">时间</td><td>'+(c.collectionTime?new Date(c.collectionTime).toLocaleString('zh-CN'):'-')+'</td></tr>';
            if(c.accuracy!=null) h += '<tr><td style="color:#888">精度</td><td>'+c.accuracy+' m</td></tr>';
            if(c.notes) h += '<tr><td style="color:#888">备注</td><td>'+escHtml(c.notes)+'</td></tr>';
            h += '</table></div>';
        });
        if(colls.length===0) h += '<p style="color:#888;font-size:11px">暂无采集记录</p>';
        document.getElementById('pavDetail').innerHTML = h;
    } catch(e) { toast('获取采集记录失败: '+e.message, 'danger'); }
}

function filterAdminTable() {
    if (TCG.adminTreeMode) renderAdminTree();
    else renderAdminTable();
}

function exportAdminCSV() {
    var csv = '﻿名称,级别,上级区划,面积(km²),人口,区划代码,采集记录\n';
    TCG.adminDivisions.forEach(function(a){
        csv += ['"'+escHtml(a.chineseName||a.name||'')+'"', levelLabel(a.adminLevel), '"'+escHtml(a.parentName||'')+'"', a.areaSize||'', a.population||'', '"'+escHtml(a.adminCode||'')+'"', a.collectorCount||0].join(',')+'\n';
    });
    var blob = new Blob([csv],{type:'text/csv;charset=utf-8'});
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a'); a.href=url; a.download='admin_division_data.csv';
    document.body.appendChild(a); a.click(); document.body.removeChild(a);
    URL.revokeObjectURL(url);
    toast('已导出 '+TCG.adminDivisions.length+' 条区划数据', 'success');
}

function flyToAdmin(id) {
    var ad = TCG.adminDivisions.find(function(a){return a.id===id});
    if (!ad) return;
    var h = '<b style="font-size:13px">'+escHtml(ad.chineseName||ad.name)+'</b>';
    h += '<table style="width:100%;font-size:11px;margin-top:4px;color:#bbb">';
    h += '<tr><td style="color:#888">级别</td><td>'+levelLabel(ad.adminLevel)+'</td></tr>';
    h += '<tr><td style="color:#888">上级</td><td>'+escHtml(ad.parentName||'无')+'</td></tr>';
    h += '<tr><td style="color:#888">人口</td><td>'+(ad.population||'')+'</td></tr>';
    h += '<tr><td style="color:#888">采集记录</td><td><span class="collector-badge" style="cursor:pointer" onclick="showAdminCollectorInfo('+ad.id+')">'+(ad.collectorCount||0)+'条</span></td></tr>';
    h += '</table>';
    document.getElementById('pavDetail').innerHTML = h;
    document.getElementById('infoPanel').classList.add('visible');
    if (!TCG.is3D && TCG.lmap && ad.geomWkt) {
        var rings = window.parseWktPolygon(ad.geomWkt);
        if (rings.length>0 && rings[0].outer.length>0) {
            var c = rings[0].outer[0];
            TCG.lmap.setView(c, 13);
        }
    }
}

var scenicModal, adminModal, scenicEditTarget, adminEditTarget;
function openScenicModal() {
    scenicEditTarget = null; document.getElementById('scenicModalTitle').textContent = '添加景区';
    document.getElementById('scenicForm').reset(); scenicModal.show();
}

function editCurScenic() {
    if (!selScenic) return;
    scenicEditTarget = selScenic; document.getElementById('scenicModalTitle').textContent = '编辑景区';
    document.getElementById('sfName').value = selScenic.chineseName || selScenic.name || '';
    document.getElementById('sfType').value = selScenic.areaType || '风景区';
    document.getElementById('sfDesc').value = selScenic.description || '';
    document.getElementById('sfLng').value = selScenic.longitude || '';
    document.getElementById('sfLat').value = selScenic.latitude || '';
    document.getElementById('sfAddr').value = selScenic.address || '';
    document.getElementById('sfPrice').value = selScenic.ticketPrice || 0;
    document.getElementById('sfRating').value = selScenic.visitorRating || '';
    document.getElementById('sfWkt').value = selScenic.geomWkt || '';
    document.getElementById('sfOpen').checked = selScenic.isOpenToPublic !== false;
    scenicModal.show();
}

async function saveScenic() {
    var body = {
        name: document.getElementById('sfName').value.trim(),
        chineseName: document.getElementById('sfName').value.trim(),
        areaType: document.getElementById('sfType').value,
        description: document.getElementById('sfDesc').value.trim() || null,
        longitude: parseFloat(document.getElementById('sfLng').value) || null,
        latitude: parseFloat(document.getElementById('sfLat').value) || null,
        address: document.getElementById('sfAddr').value.trim() || null,
        ticketPrice: parseFloat(document.getElementById('sfPrice').value) || 0,
        visitorRating: parseFloat(document.getElementById('sfRating').value) || null,
        geomWkt: document.getElementById('sfWkt').value.trim() || null,
        isOpenToPublic: document.getElementById('sfOpen').checked
    };
    if (!body.name) { toast('请输入名称', 'danger'); return; }
    var url = '/scenic-areas', method = 'POST';
    if (scenicEditTarget) { url += '/' + scenicEditTarget.id; method = 'PUT'; }
    var resp = await fetch(url, { method: method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
    if (resp.ok) { toast(scenicEditTarget ? '景区更新成功' : '景区添加成功', 'success'); scenicModal.hide(); loadScenicAreas(); }
    else { var err = await resp.json(); toast('保存失败: ' + (err.message || resp.status), 'error'); }
}

async function delCurScenic() {
    if (!selScenic) return;
    if (!confirm('确定删除景区「' + (selScenic.chineseName || selScenic.name) + '」？')) return;
    var resp = await fetch('/scenic-areas/' + selScenic.id, { method: 'DELETE' });
    if (resp.ok) { toast('景区删除成功', 'success'); selScenic = null; updateScenicBtnState(); loadScenicAreas(); }
    else { var err = await resp.json(); toast('删除失败: ' + (err.message || resp.status), 'error'); }
}

function updateScenicBtnState() {
    var disabled = !selScenic;
    document.getElementById('scenicEditBtn').disabled = disabled;
    document.getElementById('scenicBoundBtn').disabled = disabled || !selScenic.geomWkt;
    document.getElementById('scenicDelBtn').disabled = disabled;
}

function editCurScenicBoundary() {
    if (!selScenic || !selScenic.geomWkt) { toast('该景区无边界数据', 'warning'); return; }
    switchTab('map');
    setTimeout(function(){
        window.enableVertexEdit('scenic', selScenic.id, selScenic.geomWkt);
        window.zoomToWkt(selScenic.geomWkt);
    }, 500);
}

function openAdminModal() {
    adminEditTarget = null; document.getElementById('adminModalTitle').textContent = '添加区划';
    document.getElementById('adminForm').reset(); adminModal.show();
}

function editCurAdmin() {
    if (!selAdmin) return;
    adminEditTarget = selAdmin; document.getElementById('adminModalTitle').textContent = '编辑区划';
    document.getElementById('afName').value = selAdmin.chineseName || selAdmin.name || '';
    document.getElementById('afLevel').value = selAdmin.adminLevel || 'STREET';
    document.getElementById('afParentId').value = selAdmin.parentId || '';
    document.getElementById('afCode').value = selAdmin.adminCode || '';
    document.getElementById('afArea').value = selAdmin.areaSize || '';
    document.getElementById('afPop').value = selAdmin.population || '';
    document.getElementById('afWkt').value = selAdmin.geomWkt || '';
    document.getElementById('afNotes').value = selAdmin.notes || '';
    adminModal.show();
}

async function saveAdmin() {
    var body = {
        name: document.getElementById('afName').value.trim(),
        chineseName: document.getElementById('afName').value.trim(),
        adminLevel: document.getElementById('afLevel').value,
        parentId: document.getElementById('afParentId').value ? parseInt(document.getElementById('afParentId').value) : null,
        adminCode: document.getElementById('afCode').value.trim() || null,
        areaSize: parseFloat(document.getElementById('afArea').value) || null,
        population: parseInt(document.getElementById('afPop').value) || null,
        geomWkt: document.getElementById('afWkt').value.trim() || null,
        notes: document.getElementById('afNotes').value.trim() || null
    };
    if (!body.name) { toast('请输入名称', 'danger'); return; }
    var url = '/admin-divisions', method = 'POST';
    if (adminEditTarget) { url += '/' + adminEditTarget.id; method = 'PUT'; }
    var resp = await fetch(url, { method: method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
    if (resp.ok) { toast(adminEditTarget ? '区划更新成功' : '区划添加成功', 'success'); adminModal.hide(); loadAdminDivisions(); }
    else { var err = await resp.json(); toast('保存失败: ' + (err.message || resp.status), 'error'); }
}

async function delCurAdmin() {
    if (!selAdmin) return;
    if (!confirm('确定删除区划「' + (selAdmin.chineseName || selAdmin.name) + '」？')) return;
    var resp = await fetch('/admin-divisions/' + selAdmin.id, { method: 'DELETE' });
    if (resp.ok) { toast('区划删除成功', 'success'); selAdmin = null; updateAdminBtnState(); loadAdminDivisions(); }
    else { var err = await resp.json(); toast('删除失败: ' + (err.message || resp.status), 'error'); }
}

function updateAdminBtnState() {
    var disabled = !selAdmin;
    document.getElementById('adminEditBtn').disabled = disabled;
    document.getElementById('adminBoundBtn').disabled = disabled || !selAdmin.geomWkt;
    document.getElementById('adminDelBtn').disabled = disabled;
}

function editCurAdminBoundary() {
    if (!selAdmin || !selAdmin.geomWkt) { toast('该区划无边界数据', 'warning'); return; }
    switchTab('map');
    setTimeout(function(){
        window.enableVertexEdit('admin', selAdmin.id, selAdmin.geomWkt);
        window.zoomToWkt(selAdmin.geomWkt);
    }, 500);
}

function onDataSelect(val) {
    document.getElementById('dataSelect').value = '';
    if (!val) return;
    if (val === 'loadPavilions') window.loadPavilions();
    else if (val === 'importData') window.showImportDialog();
    else if (val === 'savedPlans') window.openSavedPlans();
}

function startVoiceSearch() {
    var SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
    if (!SpeechRecognition) {
        toast('您的浏览器不支持语音识别，请使用Chrome', 'warning');
        return;
    }
    if (isVoiceListening) {
        if (voiceRecognition) voiceRecognition.stop();
        return;
    }
    voiceRecognition = new SpeechRecognition();
    voiceRecognition.lang = 'zh-CN';
    voiceRecognition.continuous = false;
    voiceRecognition.interimResults = false;

    voiceRecognition.onstart = function() {
        isVoiceListening = true;
        voiceBtnEl = document.getElementById('voiceBtn') || document.getElementById('btnVoiceSearch');
        if (voiceBtnEl) voiceBtnEl.classList.add('listening');
        toast('🎤 请说话...', 'info');
    };
    voiceRecognition.onerror = function(ev) {
        isVoiceListening = false;
        if (voiceBtnEl) voiceBtnEl.classList.remove('listening');
        toast('语音识别失败: ' + (ev.error || '未知错误'), 'danger');
    };
    voiceRecognition.onend = function() {
        isVoiceListening = false;
        if (voiceBtnEl) voiceBtnEl.classList.remove('listening');
    };
    voiceRecognition.onresult = function(ev) {
        var transcript = ev.results[0][0].transcript.trim();
        var searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.value = transcript;
            if (!searchActive) toggleSearch();
            doSearch();
        }
    };
    voiceRecognition.start();
}

function toggleGeofence() {
    var panel = document.getElementById('geofencePanel');
    if (panel.style.display === 'block') { panel.style.display = 'none'; return; }
    if (TCG.sel.length === 0) { toast('请先选择亭子', 'warning'); return; }
    var p = TCG.sel[0];
    if (!p.longitude || !p.latitude) { toast('该亭子无坐标', 'warning'); return; }
    var msg = '📍 ' + p.chineseName + ' — 电子围栏已激活。';
    if (p.description) msg += '<br><small>' + p.description.substring(0, 80) + '</small>';
    panel.innerHTML = msg + '<br><small style="color:#888">坐标: ' + p.longitude.toFixed(6) + ', ' + p.latitude.toFixed(6) + ' | 范围: 100m</small>';
    panel.style.display = 'block';
    var circle = L.circle(leafCoord(p.longitude, p.latitude), { radius: 100, color: '#ff9800', fillColor: '#ff9800', fillOpacity: 0.15, weight: 2 }).addTo(TCG.lmap);
    TCG._geofenceCircle = circle;
}

function pointInPolygon(pt, polygon) {
    var x = pt[0], y = pt[1], inside = false;
    for (var i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
        var xi = polygon[i][0], yi = polygon[i][1];
        var xj = polygon[j][0], yj = polygon[j][1];
        if ((yi > y) !== (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) inside = !inside;
    }
    return inside;
}

async function loadTourismRoutes() {
    try {
        var resp = await fetch('/tourism-routes');
        var data = await resp.json();
        TCG.tourismRoutes = data.success ? data.data : [];
        renderRouteTable();
        renderRouteLines();
        renderRouteStats();
    } catch(e) { console.error('加载路线失败', e); }
}

function renderRouteTable() {
    var body = document.getElementById('routeTblBody');
    document.getElementById('routeTotal').textContent = TCG.tourismRoutes.length;
    if (!TCG.tourismRoutes.length) {
        body.innerHTML = '<tr><td colspan="7" style="text-align:center;color:#888;padding:20px">暂无路线，点击上方「+ 添加路线」添加</td></tr>';
        return;
    }
    var html = '';
    TCG.tourismRoutes.forEach(function(r){
        var hasGeom = r.geomWkt ? '<span style="color:#2ecc71">●</span>' : '<span style="color:#555">○</span>';
        html += '<tr style="border-bottom:1px solid rgba(255,255,255,.05)" onclick="selRouteRow('+r.id+')" class="'+(selRoute&&selRoute.id===r.id?'sel-row':'')+'">'
            + '<td style="padding:5px"><b>'+r.name+'</b><br><span style="color:#666;font-size:10px">'+(r.description||'')+'</span></td>'
            + '<td style="padding:5px;font-size:10px">'+(r.routeType||'-')+'</td>'
            + '<td style="padding:5px;font-size:10px">'+r.difficulty+'</td>'
            + '<td style="padding:5px">'+(r.distance?r.distance.toFixed(1):'-')+'</td>'
            + '<td style="padding:5px;font-size:10px">'+(r.duration?r.duration+'min':'-')+'</td>'
            + '<td style="padding:5px;text-align:center">'+hasGeom+'</td>'
            + '<td style="padding:5px">'
            + '<button class="btn btn-sm" style="font-size:9px;background:rgba(230,126,34,.2);color:#e67e22;padding:1px 6px;margin-right:2px" onclick="event.stopPropagation();loadRouteOnMap('+r.id+')">▶ 查看</button>'
            + '<button class="btn btn-sm" style="font-size:9px;background:rgba(231,76,60,.15);color:#e74c3c;padding:1px 6px" onclick="event.stopPropagation();delRoute('+r.id+')">删除</button>'
            + '</td></tr>';
    });
    body.innerHTML = html;
}

function selRouteRow(id) {
    selRoute = TCG.tourismRoutes.find(function(r){ return r.id === id; });
    document.getElementById('routeEditBtn').disabled = !selRoute;
    document.getElementById('routeDelBtn').disabled = !selRoute;
    renderRouteTable();
}

function filterRouteTable() { renderRouteTable(); }

function openRouteModal(data) {
    if (!routeModalObj) routeModalObj = new bootstrap.Modal(document.getElementById('routeModal'));
    var isEdit = data && data.id;
    document.getElementById('routeModalTitle').textContent = isEdit ? '编辑路线' : '添加路线';
    document.getElementById('rfName').value = data ? data.name : '';
    document.getElementById('rfDesc').value = data ? (data.description||'') : '';
    document.getElementById('rfType').value = data ? (data.routeType||'HIKING') : 'HIKING';
    document.getElementById('rfDiff').value = data ? (data.difficulty||'EASY') : 'EASY';
    document.getElementById('rfColor').value = data ? (data.color||'#e67e22') : '#e67e22';
    document.getElementById('rfDist').value = data ? (data.distance||'') : '';
    document.getElementById('rfDur').value = data ? (data.duration||'') : '';
    document.getElementById('rfStops').value = data ? (data.scenicStops||'') : '';
    document.getElementById('rfWkt').value = data ? (data.geomWkt||'') : '';
    routeEditTarget = isEdit ? data.id : null;
    var saveBtn = document.getElementById('saveRouteBtn');
    saveBtn.onclick = function(){ saveRoute(isEdit); };
    routeModalObj.show();
}

function editCurRoute() {
    if (selRoute) openRouteModal(selRoute);
}

async function saveRoute(isEdit) {
    var body = {
        name: document.getElementById('rfName').value.trim(),
        description: document.getElementById('rfDesc').value.trim(),
        routeType: document.getElementById('rfType').value,
        difficulty: document.getElementById('rfDiff').value,
        color: document.getElementById('rfColor').value,
        distance: parseFloat(document.getElementById('rfDist').value) || null,
        duration: parseInt(document.getElementById('rfDur').value) || null,
        scenicStops: document.getElementById('rfStops').value.trim() || null,
        geomWkt: document.getElementById('rfWkt').value.trim() || null
    };
    if (!body.name) { toast('请输入名称', 'warning'); return; }
    try {
        var url = isEdit ? '/tourism-routes/' + routeEditTarget : '/tourism-routes';
        var method = isEdit ? 'PUT' : 'POST';
        var resp = await fetch(url, {method:method, headers:{'Content-Type':'application/json'}, body: JSON.stringify(body)});
        var data = await resp.json();
        if (data.success) {
            routeModalObj.hide();
            toast(isEdit ? '路线已更新' : '路线已添加', 'success');
            loadTourismRoutes();
        } else toast('保存失败: ' + (data.message||''), 'danger');
    } catch(e) { toast('保存失败: ' + e.message, 'danger'); }
}

async function delRoute(id) {
    if (!confirm('确认删除该路线?')) return;
    try {
        var resp = await fetch('/tourism-routes/'+id, {method:'DELETE'});
        var data = await resp.json();
        if (data.success) { toast('路线已删除', 'success'); loadTourismRoutes(); }
        else toast('删除失败', 'danger');
    } catch(e) { toast('删除失败: '+e.message, 'danger'); }
}

async function loadRouteOnMap(id) {
    var r = TCG.tourismRoutes.find(function(x){ return x.id === id; });
    if (!r) { toast('路线不存在', 'warning'); return; }
    if (!r.geomWkt) { toast('路线无几何数据', 'warning'); return; }
    switchTab('map');
    toast('正在定位路线: ' + r.name, 'info');
    setTimeout(function(){ window.zoomToWkt(r.geomWkt); }, 300);
}

function renderRouteStats() {
    var el = document.getElementById('routeStatsOverview');
    var total = TCG.tourismRoutes.length;
    var totalDist = TCG.tourismRoutes.reduce(function(s, r){ return s + (r.distance||0); }, 0);
    el.innerHTML = '<div style="display:flex;gap:10px;flex-wrap:wrap;padding:8px 0">'
        + '<div class="stat-card"><div class="stat-num">'+total+'</div><div class="stat-label">路线</div></div>'
        + '<div class="stat-card"><div class="stat-num">'+totalDist.toFixed(1)+'</div><div class="stat-label">总里程(km)</div></div>'
        + '</div>';
}

function renderRouteLines() {
    TCG.routePolyLyr.clearLayers();
    TCG.tourismRoutes.forEach(function(r){
        if (!r.geomWkt) return;
        try {
            var coords = window.parseWktCoords(r.geomWkt);
            if (!coords || coords.length < 2) return;
            var ll = coords.map(function(c){ return [c[1], c[0]]; });
            var color = r.color || '#e67e22';
            var polyline = L.polyline(ll, {
                color: color, weight: 3, opacity: 0.8,
                dashArray: r.routeType === 'BOATING' ? '4,6' : null
            }).bindPopup('<b>' + r.name + '</b><br>' +
                (r.routeType||'') + ' · ' + (r.difficulty||'') +
                (r.distance ? ' · ' + r.distance.toFixed(1) + 'km' : '') +
                (r.duration ? ' · ' + r.duration + 'min' : ''));
            TCG.routePolyLyr.addLayer(polyline);
        } catch(e) { console.warn('路线解析失败:', r.name, e); }
    });
}

window.toggleTbDropdown = toggleTbDropdown;
window.togglePanel = togglePanel;
window.toggleSel = toggleSel;
window.updateSelDisplay = updateSelDisplay;
window.removeSel = removeSel;
window.showDetail = showDetail;
window.showBestPath = showBestPath;
window.clearPath = clearPath;
window.startNav = startNav;
window.stopNav = stopNav;
window.toggleSearch = toggleSearch;
window.closeSearch = closeSearch;
window.doSearch = doSearch;
window.gotoSearchResult = gotoSearchResult;
window.showPavilionDetail = showPavilionDetail;
window.selFromDetail = selFromDetail;
window.closeInfo = closeInfo;
window.toggleMeasure = toggleMeasure;
window.stopMeasure = stopMeasure;
window.clearMeasure = clearMeasure;
window.toggleAddOnMap = toggleAddOnMap;
window.switchTab = switchTab;
window.selectFromTable = selectFromTable;
window.filterTable = filterTable;
window.tblSort = tblSort;
window.tblClearSel = tblClearSel;
window.exportTableCSV = exportTableCSV;
window.showCollectorInfo = showCollectorInfo;
window.loadScenicAreas = loadScenicAreas;
window.showScenicDetail = showScenicDetail;
window.showScenicCollectorInfo = showScenicCollectorInfo;
window.selScenicRow = selScenicRow;
window.filterScenicTable = filterScenicTable;
window.exportScenicCSV = exportScenicCSV;
window.flyToScenic = flyToScenic;
window.loadAdminDivisions = loadAdminDivisions;
window.selAdminRow = selAdminRow;
window.toggleAdminTree = toggleAdminTree;
window.toggleTreeNode = toggleTreeNode;
window.showAdminCollectorInfo = showAdminCollectorInfo;
window.filterAdminTable = filterAdminTable;
window.exportAdminCSV = exportAdminCSV;
window.flyToAdmin = flyToAdmin;
window.openScenicModal = openScenicModal;
window.editCurScenic = editCurScenic;
window.saveScenic = saveScenic;
window.delCurScenic = delCurScenic;
window.editCurScenicBoundary = editCurScenicBoundary;
window.openAdminModal = openAdminModal;
window.editCurAdmin = editCurAdmin;
window.saveAdmin = saveAdmin;
window.delCurAdmin = delCurAdmin;
window.editCurAdminBoundary = editCurAdminBoundary;
window.onDataSelect = onDataSelect;
window.startVoiceSearch = startVoiceSearch;
window.toggleGeofence = toggleGeofence;
window.loadTourismRoutes = loadTourismRoutes;
window.selRouteRow = selRouteRow;
window.filterRouteTable = filterRouteTable;
window.openRouteModal = openRouteModal;
window.editCurRoute = editCurRoute;
window.delRoute = delRoute;
window.loadRouteOnMap = loadRouteOnMap;
window.levelLabel = levelLabel;
window.pointInPolygon = pointInPolygon;
})();
