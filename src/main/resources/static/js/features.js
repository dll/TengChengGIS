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

function switchPavTab(tab) {
    ['basic','survey','collector'].forEach(function(t){
        document.getElementById('pavTab'+t.charAt(0).toUpperCase()+t.slice(1)).style.display=(t===tab?'':'none');
        document.getElementById('tabPav'+t.charAt(0).toUpperCase()+t.slice(1)).classList.toggle('active',t===tab);
    });
    if (tab==='collector' && TCG.editTarget) loadCollectors(TCG.editTarget.id);
}

function addPavilion() {
    TCG.editTarget=null; document.getElementById('pavModalTitle').textContent='添加亭子';
    document.getElementById('pavForm').reset();
    switchPavTab('basic');
    document.getElementById('collList').innerHTML = '';
    TCG.pavModal.show();
}

function editCurPavilion() {
    if (TCG.sel.length!==1) { toast('请选择一个亭子', 'warning'); return; }
    TCG.editTarget=TCG.sel[0]; document.getElementById('pavModalTitle').textContent='编辑亭子';
    document.getElementById('fName').value=TCG.editTarget.chineseName||'';
    document.getElementById('fEName').value=TCG.editTarget.englishName||TCG.editTarget.name||'';
    document.getElementById('fType').value=tLabel(TCG.editTarget.pavilionType||TCG.editTarget.type);
    document.getElementById('fLng').value=TCG.editTarget.longitude||'';
    document.getElementById('fLat').value=TCG.editTarget.latitude||'';
    document.getElementById('fYear').value=TCG.editTarget.builtYear||'';
    document.getElementById('fRenovYear').value=TCG.editTarget.lastRenovationYear||'';
    document.getElementById('fDesc').value=TCG.editTarget.description||'';
    document.getElementById('fHistory').value=TCG.editTarget.historicalSignificance||'';
    document.getElementById('fStyle').value=TCG.editTarget.architecturalStyle||'';
    document.getElementById('fPeriod').value=TCG.editTarget.constructionPeriod||'';
    document.getElementById('fRating').value=TCG.editTarget.visitorRating||'';
    document.getElementById('fPrice').value=TCG.editTarget.ticketPrice||'';
    document.getElementById('fArea').value=TCG.editTarget.areaSize||'';
    document.getElementById('fOpen').value=TCG.editTarget.isOpenToPublic?'true':'false';
    document.getElementById('fStructure').value=TCG.editTarget.structure||'';
    document.getElementById('fTopStyle').value=TCG.editTarget.topStyle||'';
    document.getElementById('fStreet').value=TCG.editTarget.street||'';
    document.getElementById('fLocDesc').value=TCG.editTarget.locationDesc||'';
    document.getElementById('fNotes').value=TCG.editTarget.notes||'';
    switchPavTab('basic');
    TCG.pavModal.show();
}

async function loadCollectors(pavilionId) {
    try {
        var resp = await fetch('/thousand-pavilions/' + pavilionId + '/collectors');
        var data = await resp.json();
        var colls = data.data || [];
        var h = '';
        colls.forEach(function(c, i){
            h += '<div style="background:rgba(255,255,255,.04);border-radius:4px;padding:6px 8px;margin:4px 0;font-size:11px">';
            h += '<b>#' + (i+1) + ' ' + escHtml(c.collectorName || '未知') + '</b>';
            h += '<table style="width:100%;font-size:10px;margin-top:2px;color:#bbb">';
            h += '<tr><td style="color:#888;width:60px">工具</td><td>' + escHtml(c.collectionTool || '-') + '</td></tr>';
            h += '<tr><td style="color:#888">来源</td><td>' + escHtml(c.dataSource || '-') + '</td></tr>';
            h += '<tr><td style="color:#888">时间</td><td>' + (c.collectionTime ? new Date(c.collectionTime).toLocaleString('zh-CN') : '-') + '</td></tr>';
            if (c.accuracy != null) h += '<tr><td style="color:#888">精度</td><td>' + c.accuracy + 'm</td></tr>';
            if (c.notes) h += '<tr><td style="color:#888">备注</td><td>' + escHtml(c.notes) + '</td></tr>';
            h += '</table></div>';
        });
        if (colls.length === 0) h += '<p style="color:#888;font-size:11px;padding:8px">暂无采集记录</p>';
        document.getElementById('collList').innerHTML = h;
    } catch(e) {
        toast('加载采集记录失败: ' + e.message, 'danger');
    }
}

async function addCollector() {
    if (!TCG.editTarget) { toast('请先保存亭子基本信息', 'warning'); return; }
    var collName = document.getElementById('fCollName').value.trim();
    if (!collName) { toast('请输入采集者姓名', 'warning'); return; }
    var data = {
        collectorName: collName,
        collectionTool: document.getElementById('fCollTool').value.trim(),
        dataSource: document.getElementById('fCollSource').value.trim(),
        accuracy: document.getElementById('fCollAcc').value ? parseFloat(document.getElementById('fCollAcc').value) : null,
        notes: document.getElementById('fCollNotes').value.trim()
    };
    try {
        var resp = await fetch('/thousand-pavilions/' + TCG.editTarget.id + '/collectors', {
            method:'POST',
            headers:{'Content-Type':'application/json'},
            body:JSON.stringify(data)
        });
        var result = await resp.json();
        if (result.success) {
            toast('采集记录添加成功', 'success');
            document.getElementById('fCollName').value='';
            document.getElementById('fCollTool').value='';
            document.getElementById('fCollSource').value='';
            document.getElementById('fCollAcc').value='';
            document.getElementById('fCollNotes').value='';
            loadCollectors(TCG.editTarget.id);
        } else {
            toast('添加失败: '+(result.message||''), 'danger');
        }
    } catch(e) { toast('添加失败: '+e.message, 'danger'); }
}

function delCurPavilion() {
    if (TCG.sel.length!==1) { toast('请选择一个亭子', 'warning'); return; }
    var p = TCG.sel[0];
    if (!confirm('确定删除"'+p.chineseName+'"？')) return;
    fetch('/thousand-pavilions/'+p.id, {method:'DELETE'}).then(function(r){
        if (r.ok) { toast('删除成功', 'success'); TCG.sel=[]; window.updateSelDisplay(); window.loadPavilions(); }
        else r.json().then(function(e){ toast('删除失败: '+(e.message||'未知'), 'danger'); });
    });
}

async function savePavilion() {
    var data = {
        chineseName: document.getElementById('fName').value,
        name: document.getElementById('fEName').value,
        pavilionType: document.getElementById('fType').value,
        builtYear: document.getElementById('fYear').value ? parseInt(document.getElementById('fYear').value) : null,
        lastRenovationYear: document.getElementById('fRenovYear').value ? parseInt(document.getElementById('fRenovYear').value) : null,
        longitude: parseFloat(document.getElementById('fLng').value),
        latitude: parseFloat(document.getElementById('fLat').value),
        description: document.getElementById('fDesc').value,
        historicalSignificance: document.getElementById('fHistory').value,
        architecturalStyle: document.getElementById('fStyle').value,
        constructionPeriod: document.getElementById('fPeriod').value,
        visitorRating: document.getElementById('fRating').value ? parseFloat(document.getElementById('fRating').value) : null,
        ticketPrice: document.getElementById('fPrice').value ? parseFloat(document.getElementById('fPrice').value) : null,
        areaSize: document.getElementById('fArea').value ? parseFloat(document.getElementById('fArea').value) : null,
        isOpenToPublic: document.getElementById('fOpen').value==='true',
        structure: document.getElementById('fStructure').value,
        topStyle: document.getElementById('fTopStyle').value,
        street: document.getElementById('fStreet').value,
        locationDesc: document.getElementById('fLocDesc').value,
        notes: document.getElementById('fNotes').value
    };
    if (!data.chineseName||!data.pavilionType||isNaN(data.longitude)||isNaN(data.latitude)) { toast('请填写必填字段', 'warning'); return; }
    var url = '/thousand-pavilions', method='POST';
    if (TCG.editTarget) { url+='/'+TCG.editTarget.id; method='PUT'; }
    try {
        var resp = await fetch(url, {method:method, headers:{'Content-Type':'application/json'}, body:JSON.stringify(data)});
        if (resp.ok) { toast(TCG.editTarget?'更新成功':'添加成功', 'success'); TCG.pavModal.hide(); window.loadPavilions(); }
        else { var e = await resp.json(); toast('操作失败: '+(e.message||'未知'), 'danger'); }
    } catch(e) { toast('操作失败: '+e.message, 'danger'); }
}

async function traverseAll() {
    if (TCG.pavs.length < 2) { toast('亭子数量不足', 'warning'); return; }
    if (!confirm('遍历全部 ' + TCG.pavs.length + ' 个亭子使用TSP求解+OSRM真实路网。这可能耗时数十秒。是否继续?')) return;
    var ids = TCG.pavs.map(function(p){return p.id});
    await runTspPlanning(ids, 'WALKING', 'distance');
    if (tspPlanData) startTspPlay();
}

async function tourismSvc() {
    try {
        var resp = await fetch('/thousand-pavilions/tourism-services'), s = await resp.json();
        document.getElementById('pavDetail').innerHTML='<div><b>旅游服务</b>'+'<p style="font-size:11px;margin:4px 0">开放: '+s.openingHours+'<br>门票: '+s.ticketInfo+'<br>设施: '+(s.facilities||[]).join('、')+'<br>无障碍: '+s.accessibility+'<br>紧急: '+s.emergency+'</p></div>';
    } catch(e) { toast('获取失败: '+e.message, 'danger'); }
}

async function smartTour() {
    var dur = prompt('预计游览时间 (分钟):', '240'); if (!dur) return;
    try {
        var resp = await fetch('/thousand-pavilions/smart-tour?duration='+dur), plan = await resp.json();
        if (plan.route&&plan.route.length>0) {
            var h = '<div><b>智能规划</b><p style="font-size:11px">共'+plan.totalPavilions+'个亭子, '+plan.totalDistance+'km, 约'+plan.estimatedDuration+'分钟</p>';
            plan.route.forEach(function(s,i){
                h+='<div style="border-left:2px solid #4a86e8;padding-left:8px;margin:4px 0;font-size:11px"><b>'+s.order+'. '+s.name+'</b><br>'+tLabel(s.type)+(i>0?' | +'+(s.distanceFromPrevious||0)+'km':'')+'</div>';
            });
            h+='</div>'; document.getElementById('pavDetail').innerHTML = h;
            var coords = plan.route.filter(function(s){return s.latitude&&s.longitude}).map(function(s){return[s.latitude,s.longitude]});
            if (TCG.pathLyr) TCG.lmap.removeLayer(TCG.pathLyr);
            if (coords.length>1) { TCG.pathLyr=L.polyline(coords,{color:'#27ae60',weight:4,dashArray:'10,10'}).addTo(TCG.lmap); TCG.lmap.fitBounds(TCG.pathLyr.getBounds()); }
            toast('智能规划完成', 'success');
        }
    } catch(e) { toast('规划失败: '+e.message, 'danger'); }
}

async function showWeather() {
    try {
        var resp = await fetch('/thousand-pavilions/weather'), w = await resp.json();
        document.getElementById('pavDetail').innerHTML='<div style="background:linear-gradient(135deg,#74b9ff,#0984e3);color:#fff;padding:12px;border-radius:4px"><b>天气预报</b><div style="font-size:32px;margin:8px 0">'+w.temperature+'°C</div>'+w.condition+'<br><small>湿度:'+w.humidity+'% | '+w.windSpeed+'</small><p style="margin-top:8px;font-size:11px">'+w.recommendation+'</p></div>';
    } catch(e) { toast('获取天气失败: '+e.message, 'danger'); }
}

async function nearbyFac() {
    if (TCG.sel.length===0) { toast('请先选择一个亭子', 'warning'); return; }
    var p = TCG.sel[0], rad = prompt('搜索半径 (公里):', '1'); if (!rad) return;
    try {
        var resp = await fetch('/thousand-pavilions/nearby-facilities/'+p.id+'?radius='+rad), f = await resp.json();
        if (f.error) { toast(f.error, 'danger'); return; }
        var h = '<div><b>附近设施</b><p style="font-size:10px;color:#888">半径: '+f.searchRadius+'km</p>';
        if (f.facilities) f.facilities.forEach(function(fc){
            h+='<div style="background:rgba(255,255,255,.04);padding:6px;border-radius:3px;margin:3px 0"><b style="font-size:11px">'+fc.icon+' '+fc.name+'</b><p style="font-size:10px;margin:2px 0">'+fc.description+'</p><small style="color:#888">距离约'+calcDist(p.longitude,p.latitude,fc.longitude,fc.latitude).toFixed(2)+'km</small></div>';
        });
        h+='</div>'; document.getElementById('pavDetail').innerHTML = h;
    } catch(e) { toast('获取附近设施失败: '+e.message, 'danger'); }
}

async function shareRoute() {
    if (TCG.sel.length<2) { toast('请至少选择2个亭子', 'warning'); return; }
    var name = prompt('路线名称:', '我的亭城之旅'); if (!name) return;
    try {
        var resp = await fetch('/thousand-pavilions/share-route', {
            method:'POST', headers:{'Content-Type':'application/json'},
            body:JSON.stringify({pavilionIds:TCG.sel.map(function(p){return p.id}),routeName:name})
        }), info = await resp.json();
        if (info.shareUrl) {
            document.getElementById('pavDetail').innerHTML='<div><b>分享路线</b><p style="font-size:11px">'+info.routeName+'<br>共'+info.totalPavilions+'个亭子, '+info.totalDistance+'km, 约'+info.estimatedDuration+'分钟</p><code style="word-break:break-all;font-size:10px">'+window.location.origin+info.shareUrl+'</code></div>';
            toast('分享链接已生成', 'success');
        }
    } catch(e) { toast('分享失败: '+e.message, 'danger'); }
}

function showImportDialog() {
    document.getElementById('impFile').value = '';
    document.getElementById('impProgress').style.display = 'none';
    document.getElementById('impResult').style.display = 'none';
    TCG.impModal.show();
}

async function doImport() {
    var fi = document.getElementById('impFile');
    if (!fi.files || !fi.files[0]) { toast('请选择文件', 'warning'); return; }
    var fd = new FormData(); fd.append('file', fi.files[0]);
    document.getElementById('impProgress').style.display = 'block';
    document.getElementById('impResult').style.display = 'none';
    document.getElementById('startImportBtn').disabled = true;
    try {
        var resp = await fetch('/thousand-pavilions/import', { method:'POST', body:fd });
        var data = await resp.json();
        document.getElementById('impProgress').style.display = 'none';
        document.getElementById('startImportBtn').disabled = false;
        var rd = document.getElementById('impResult'); rd.style.display = 'block';
        if (data.success) {
            rd.innerHTML = '<div style="color:#2ecc71;">'+data.message+'</div>';
            if (data.data && data.data.errors && data.data.errors.length>0)
                rd.innerHTML += '<div style="color:#f39c12;margin-top:4px;max-height:120px;overflow-y:auto;">'+data.data.errors.slice(0,20).join('<br>')+'</div>';
            toast('导入完成', 'success');
            setTimeout(window.loadPavilions, 500);
        } else {
            rd.innerHTML = '<div style="color:#e74c3c;">'+data.message+'</div>';
            toast('导入失败', 'danger');
        }
    } catch(e) {
        document.getElementById('impProgress').style.display = 'none';
        document.getElementById('startImportBtn').disabled = false;
        toast('导入失败: '+e.message, 'danger');
    }
}

function dlExport(fmt) {
    window.open('/thousand-pavilions/export/'+fmt, '_blank');
    toast('导出中...', 'info');
}

function dlTemplate(fmt) {
    window.open('/thousand-pavilions/export/'+fmt+'-template', '_blank');
    toast('模板下载中...', 'info');
}

var tspAnimMarker = null, tspAnimPath = null, tspAnimTimer = null;
var tspSnapLayers = [], tspPlanData = null, tspProgress = 0, tspPaused = true;
var tspSpeed = 1, tspAnimMs = 0, tspAnimStart = 0, tspProgressStart = 0;
var tspSetupModal = null, plansModal = null;
var tspLastIdx = 0, tspUiEls = null;
var TSP_MODE_COLORS = {BUS:'#3498db', TAXI:'#f39c12', E_BIKE:'#2ecc71', BICYCLE:'#e67e22', WALKING:'#e74c3c'};
var TSP_SNAP_THRESHOLD_M = 5;
var TSP_ANIM_MS_PER_KM = 1500, TSP_ANIM_MS_MIN = 8000, TSP_ANIM_MS_MAX = 90000;

function openTspSetup() {
    var scope = document.getElementById('tspScope');
    var info = document.getElementById('tspScopeInfo');
    var refresh = function(){
        if (scope.value === 'selected') info.textContent = '当前已选: ' + TCG.sel.length + ' 个亭子' + (TCG.sel.length<2?' (至少需要2个)':'');
        else info.textContent = '将规划全部 ' + TCG.pavs.length + ' 个亭子(可能耗时较长)';
    };
    scope.onchange = refresh; refresh();
    document.querySelectorAll('#tspModeGrid label, #tspObjGrid label').forEach(function(l){
        l.classList.toggle('checked', l.querySelector('input').checked);
        l.onclick = function(){
            var grp = l.parentElement.querySelectorAll('label');
            grp.forEach(function(x){ x.classList.remove('checked'); x.querySelector('input').checked = false; });
            l.classList.add('checked'); l.querySelector('input').checked = true;
        };
    });
    if (!tspSetupModal) tspSetupModal = new bootstrap.Modal(document.getElementById('tspSetupModal'));
    tspSetupModal.show();
}

async function runTspPlanning(ids, mode, objective) {
    toast('TSP路线规划中... ('+ids.length+' 个亭子)', 'info');
    try {
        var resp = await fetch('/transport-routes/tsp-plan', {
            method:'POST', headers:{'Content-Type':'application/json'},
            body: JSON.stringify({pavilionIds: ids, mode: mode, objective: objective})
        });
        var data = await resp.json();
        if (!data.success || !data.segments || data.segments.length===0) {
            toast('TSP路线规划失败: '+(data.message||'无数据'), 'danger'); return;
        }
        stopTspAnimation(); window.clearPath();
        setupTspAnimation(data);
    } catch(e) { toast('TSP规划失败: '+e.message, 'danger'); }
}

function setupTspAnimation(data) {
    tspPlanData = data;
    var c = TSP_MODE_COLORS[data.mode] || '#95a5a6';

    var allLeaf = data.allCoordinates.map(function(pt){return leafCoord(pt[0],pt[1]);});
    tspAnimPath = L.polyline(allLeaf, {color:c, weight:4, opacity:.85, smoothFactor:.5}).addTo(TCG.lmap);
    TCG.lmap.fitBounds(tspAnimPath.getBounds(), {padding:[30,30]});

    tspSnapLayers.forEach(function(l){ if(TCG.lmap.hasLayer(l)) TCG.lmap.removeLayer(l); });
    tspSnapLayers = [];
    data.segments.forEach(function(seg){
        addSnapLine(seg.fromLng, seg.fromLat, seg.snapFrom, seg.fromName);
        addSnapLine(seg.toLng, seg.toLat, seg.snapTo, seg.toName);
    });

    var animIcon = L.divIcon({
        className:'tsp-anim-marker',
        html:'<div style="width:18px;height:18px;background:'+c+';border:3px solid #fff;border-radius:50%;box-shadow:0 0 14px '+c+';animation:tspPulse .8s infinite;"></div>',
        iconSize:[18,18], iconAnchor:[9,9]
    });
    tspAnimMarker = L.marker(allLeaf[0], {icon:animIcon}).addTo(TCG.lmap);

    var flat=[], cum=0;
    data.allCoordinates.forEach(function(pt,i){
        if (i>0) cum += calcDist(data.allCoordinates[i-1][1], data.allCoordinates[i-1][0], pt[1], pt[0]);
        flat.push({wgs:pt, leaf:allLeaf[i], cumDist:cum});
    });
    data._flat = flat;
    tspAnimMs = Math.max(TSP_ANIM_MS_MIN, Math.min(data.totalDistance * TSP_ANIM_MS_PER_KM, TSP_ANIM_MS_MAX));

    tspProgress = 0; tspPaused = true; tspLastIdx = 0;
    tspUiEls = {
        fill: document.getElementById('tspcFill'),
        pct: document.getElementById('tspcPct'),
        counter: document.getElementById('tspcCounter'),
        info: document.getElementById('tspcInfo'),
        play: document.getElementById('tspcPlay')
    };
    document.getElementById('tspCtrl').classList.add('show');
    tspUiEls.play.textContent = '▶ 播放';
    updateTspUi(0, c);
}

function addSnapLine(pavLng, pavLat, snap, label) {
    if (!snap || !snap.offsetM || snap.offsetM <= TSP_SNAP_THRESHOLD_M) return;
    var pavLeaf = leafCoord(pavLng, pavLat);
    var snapLeaf = leafCoord(snap.lng, snap.lat);
    var dl = L.polyline([pavLeaf, snapLeaf], {color:'#f39c12', weight:2, dashArray:'6,5', opacity:.75})
        .bindTooltip(label + ' → 路线 (' + snap.offsetM.toFixed(0) + 'm)', {direction:'top'})
        .addTo(TCG.lmap);
    tspSnapLayers.push(dl);
}

function startTspPlay() {
    if (!tspPlanData) return;
    tspPaused = false;
    tspAnimStart = Date.now();
    tspProgressStart = tspProgress;
    tspUiEls.play.textContent = '⏸ 暂停';
    tspStep();
}

function pauseTspPlay() {
    tspPaused = true;
    if (tspUiEls) tspUiEls.play.textContent = '▶ 播放';
    if (tspAnimTimer) { clearTimeout(tspAnimTimer); tspAnimTimer = null; }
}

function tspStep() {
    if (!tspPlanData || tspPaused) return;
    var elapsed = (Date.now() - tspAnimStart) * tspSpeed;
    tspProgress = Math.min(tspProgressStart + elapsed/tspAnimMs, 1.0);
    var c = TSP_MODE_COLORS[tspPlanData.mode] || '#95a5a6';
    updateTspUi(tspProgress, c);
    if (tspProgress < 1.0) {
        tspAnimTimer = setTimeout(tspStep, 50);
    } else {
        tspPaused = true;
        tspUiEls.play.textContent = '✓ 完成';
        toast('TSP完成: '+tspPlanData.totalDistance.toFixed(1)+'km, '+
            Math.round(tspPlanData.totalDuration/60)+'min, ¥'+
            (tspPlanData.totalCost||0).toFixed(2), 'success');
    }
}

function updateTspUi(prog, color) {
    if (!tspPlanData) return;
    var flat = tspPlanData._flat;
    var tgt = prog * tspPlanData.totalDistance;
    if (flat[tspLastIdx] && flat[tspLastIdx].cumDist > tgt) tspLastIdx = 0;
    while (tspLastIdx < flat.length-1 && flat[tspLastIdx+1].cumDist < tgt) tspLastIdx++;
    if (tspAnimMarker) tspAnimMarker.setLatLng(flat[tspLastIdx].leaf);
    tspUiEls.fill.style.width = (prog*100)+'%';
    tspUiEls.pct.textContent = Math.round(prog*100)+'%';
    tspUiEls.counter.textContent =
        (prog*tspPlanData.totalDistance).toFixed(2) + ' / ' + tspPlanData.totalDistance.toFixed(2) + ' km';
    var totalMin = Math.round(tspPlanData.totalDuration / 60);
    tspUiEls.info.innerHTML =
        '<b style="color:'+color+'">TSP['+tspPlanData.mode+'/'+tspPlanData.objective+']</b> '+
        '路程<b>'+tspPlanData.totalDistance.toFixed(1)+'km</b> 时间<b>'+totalMin+'min</b>'+
        (tspPlanData.totalCost ? ' 费用<b>¥'+tspPlanData.totalCost.toFixed(0)+'</b>' : '') +
        ' (经过 '+(tspPlanData.visitOrderIds.length-1)+' 亭子)';
}

function stopTspAnimation() {
    tspPaused = true;
    if (tspAnimTimer) { clearTimeout(tspAnimTimer); tspAnimTimer = null; }
    if (tspAnimMarker) { TCG.lmap.removeLayer(tspAnimMarker); tspAnimMarker = null; }
    if (tspAnimPath) { TCG.lmap.removeLayer(tspAnimPath); tspAnimPath = null; }
    tspSnapLayers.forEach(function(l){ if(TCG.lmap.hasLayer(l)) TCG.lmap.removeLayer(l); });
    tspSnapLayers = [];
    document.getElementById('tspCtrl').classList.remove('show');
    tspPlanData = null; tspUiEls = null; tspLastIdx = 0;
}

async function exportTspGif() {
    if (!tspPlanData) { toast('没有TSP方案可导出', 'warning'); return; }
    var data = tspPlanData;
    var coords = data.allCoordinates;
    if (!coords || coords.length < 2) { toast('路径数据不足，无法导出', 'warning'); return; }

    pauseTspPlay();

    var canvasW = 800, canvasH = 600;
    var totalFrames = Math.min(Math.max(Math.ceil(data.totalDistance * 1.5), 30), 150);
    var delayMs = Math.max(30, Math.floor(3000 / totalFrames));

    var minLat = Infinity, maxLat = -Infinity, minLng = Infinity, maxLng = -Infinity;
    coords.forEach(function(c){
        if (c[1] < minLat) minLat = c[1];
        if (c[1] > maxLat) maxLat = c[1];
        if (c[0] < minLng) minLng = c[0];
        if (c[0] > maxLng) maxLng = c[0];
    });
    var dLat = (maxLat - minLat) || 0.001, dLng = (maxLng - minLng) || 0.001;
    var pad = 0.08;
    minLat -= dLat * pad; maxLat += dLat * pad;
    minLng -= dLng * pad; maxLng += dLng * pad;
    var scaleX = canvasW / (maxLng - minLng);
    var scaleY = canvasH / (maxLat - minLat);

    var pavMap = {};
    (data.segments || []).forEach(function(seg){
        pavMap[seg.fromName] = {lng: seg.fromLng, lat: seg.fromLat};
        pavMap[seg.toName] = {lng: seg.toLng, lat: seg.toLat};
    });

    var modeColor = TSP_MODE_COLORS[data.mode] || '#ff9800';

    function toCanvas(lng, lat) {
        return [(lng - minLng) * scaleX, (maxLat - lat) * scaleY];
    }

    var cumDist = [0];
    for (var i = 1; i < coords.length; i++) {
        var d = calcDist(coords[i-1][1], coords[i-1][0], coords[i][1], coords[i][0]);
        cumDist.push(cumDist[i-1] + d);
    }
    var total = cumDist[cumDist.length - 1] || 1;

    function getPosition(progress) {
        var target = progress * total;
        for (var i = 1; i < cumDist.length; i++) {
            if (cumDist[i] >= target || i === cumDist.length - 1) {
                var segLen = cumDist[i] - cumDist[i-1] || 1;
                var frac = (target - cumDist[i-1]) / segLen;
                return {
                    lng: coords[i-1][0] + (coords[i][0] - coords[i-1][0]) * Math.min(frac, 1),
                    lat: coords[i-1][1] + (coords[i][1] - coords[i-1][1]) * Math.min(frac, 1),
                    idx: i
                };
            }
        }
        var last = coords[coords.length - 1];
        return {lng: last[0], lat: last[1], idx: coords.length - 1};
    }

    toast('正在生成GIF... (' + totalFrames + ' 帧)', 'info');

    try {
        if (typeof GIF === 'undefined') {
            toast('GIF库未加载，请检查网络连接', 'danger');
            return;
        }
        var gif = new GIF({
            workers: 2,
            quality: 10,
            width: canvasW,
            height: canvasH,
            workerURL: 'gif.worker.js'
        });

        var canvas = document.createElement('canvas');
        canvas.width = canvasW;
        canvas.height = canvasH;
        var ctx = canvas.getContext('2d');

        for (var f = 0; f <= totalFrames; f++) {
            var prog = f / totalFrames;

            ctx.fillStyle = '#1a1a2e';
            ctx.fillRect(0, 0, canvasW, canvasH);

            ctx.strokeStyle = 'rgba(255,255,255,0.04)';
            ctx.lineWidth = 1;
            for (var g = 0; g < 10; g++) {
                ctx.beginPath();
                ctx.moveTo(g * canvasW / 10, 0);
                ctx.lineTo(g * canvasW / 10, canvasH);
                ctx.stroke();
                ctx.beginPath();
                ctx.moveTo(0, g * canvasH / 10);
                ctx.lineTo(canvasW, g * canvasH / 10);
                ctx.stroke();
            }

            ctx.beginPath();
            ctx.strokeStyle = 'rgba(255,255,255,0.12)';
            ctx.lineWidth = 2;
            coords.forEach(function(c, i){
                var p = toCanvas(c[0], c[1]);
                if (i === 0) ctx.moveTo(p[0], p[1]);
                else ctx.lineTo(p[0], p[1]);
            });
            ctx.stroke();

            var tgtDist = prog * total;
            ctx.beginPath();
            ctx.strokeStyle = modeColor;
            ctx.lineWidth = 3.5;
            ctx.shadowColor = modeColor;
            ctx.shadowBlur = 4;
            var drawn = false;
            for (var i = 0; i < cumDist.length; i++) {
                if (cumDist[i] > tgtDist) {
                    var frac = (tgtDist - cumDist[i-1]) / (cumDist[i] - cumDist[i-1] || 1);
                    var px = coords[i-1][0] + (coords[i][0] - coords[i-1][0]) * frac;
                    var py = coords[i-1][1] + (coords[i][1] - coords[i-1][1]) * frac;
                    var pp = toCanvas(px, py);
                    var prevP = toCanvas(coords[i-1][0], coords[i-1][1]);
                    ctx.moveTo(prevP[0], prevP[1]);
                    ctx.lineTo(pp[0], pp[1]);
                    drawn = true;
                    break;
                }
                if (i > 0) {
                    var p = toCanvas(coords[i-1][0], coords[i-1][1]);
                    if (i === 1) ctx.moveTo(p[0], p[1]);
                    else ctx.lineTo(p[0], p[1]);
                }
            }
            if (!drawn && coords.length > 0) {
                var lastP = toCanvas(coords[coords.length-1][0], coords[coords.length-1][1]);
                ctx.lineTo(lastP[0], lastP[1]);
            }
            ctx.stroke();
            ctx.shadowBlur = 0;

            ctx.font = 'bold 12px "Microsoft YaHei", sans-serif';
            ctx.textAlign = 'center';
            Object.keys(pavMap).forEach(function(name){
                var pv = pavMap[name];
                var pos = toCanvas(pv.lng, pv.lat);
                ctx.fillStyle = modeColor;
                ctx.beginPath();
                ctx.arc(pos[0], pos[1], 5, 0, Math.PI * 2);
                ctx.fill();
                ctx.fillStyle = '#fff';
                ctx.fillText(name, pos[0], pos[1] - 10);
            });

            var pos = getPosition(prog);
            var dot = toCanvas(pos.lng, pos.lat);
            var grad = ctx.createRadialGradient(dot[0], dot[1], 0, dot[0], dot[1], 16);
            grad.addColorStop(0, 'rgba(255,152,0,0.9)');
            grad.addColorStop(1, 'rgba(255,152,0,0)');
            ctx.fillStyle = grad;
            ctx.beginPath();
            ctx.arc(dot[0], dot[1], 16, 0, Math.PI * 2);
            ctx.fill();
            ctx.fillStyle = '#fff';
            ctx.beginPath();
            ctx.arc(dot[0], dot[1], 4, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = 'rgba(0,0,0,0.5)';
            ctx.fillRect(0, 0, canvasW, 36);
            ctx.fillStyle = '#ff9800';
            ctx.font = 'bold 13px "Microsoft YaHei", sans-serif';
            ctx.textAlign = 'left';
            ctx.fillText('TSP [' + data.mode + '/' + data.objective + ']  ' +
                data.totalDistance.toFixed(1) + 'km  ' +
                Math.round(data.totalDuration / 60) + 'min' +
                (data.totalCost ? '  ¥' + data.totalCost.toFixed(0) : ''), 12, 24);
            ctx.fillStyle = '#aaa';
            ctx.font = '11px "Microsoft YaHei", sans-serif';
            ctx.textAlign = 'right';
            ctx.fillText(Math.round(prog * 100) + '%', canvasW - 12, 24);

            gif.addFrame(ctx, {copy: true, delay: delayMs});

            if (f % 20 === 0) await new Promise(r => setTimeout(r, 0));
        }

        toast('GIF渲染中...', 'info');
        gif.on('finished', async function(blob){
            var url = URL.createObjectURL(blob);
            var a = document.createElement('a');
            a.href = url;
            a.download = 'TSP动画-' + data.mode + '-' + data.objective + '.gif';
            a.click();
            URL.revokeObjectURL(url);
            toast('GIF已下载！文件保存在浏览器「下载」文件夹，大小 ' + (blob.size/1024).toFixed(1) + 'KB', 'success');

            if (tspPlanData && tspPlanData._savedId) {
                try {
                    var formData = new FormData();
                    formData.append('file', blob, 'plan_' + tspPlanData._savedId + '.gif');
                    await fetch('/route-plans/' + tspPlanData._savedId + '/gif', {
                        method: 'POST', body: formData
                    });
                    toast('GIF已保存到方案中！可在「已存方案」中下载', 'success');
                } catch(e) {
                    console.warn('GIF上传失败:', e);
                }
            }
        });
        gif.render();
    } catch(e) {
        toast('GIF生成失败: ' + e.message, 'danger');
        console.error(e);
    }
}

function openSavedPlans() {
    if (!plansModal) plansModal = new bootstrap.Modal(document.getElementById('plansModal'));
    plansModal.show();
    loadSavedPlans();
}

async function loadSavedPlans() {
    var list = document.getElementById('plansList');
    list.innerHTML = '加载中...';
    try {
        var resp = await fetch('/route-plans');
        var data = await resp.json();
        if (!data.success || data.count===0) { list.innerHTML = '<p style="color:#888">暂无保存的方案</p>'; return; }
        var html = '<table style="width:100%;font-size:11px"><thead><tr style="border-bottom:1px solid rgba(255,255,255,.1)">'
            + '<th style="padding:6px">名称</th><th>模式</th><th>目标</th><th>亭子</th><th>距离</th><th>费用</th><th>时间</th><th>GIF</th><th></th></tr></thead><tbody>';
        data.data.forEach(function(p){
            var gifBtn = p.hasGif
                ? '<a href="'+p.gifUrl+'" download style="font-size:10px;color:#2ecc71;text-decoration:none" title="下载GIF">🎬 下载</a>'
                : '<button class="btn btn-sm" style="font-size:10px;background:rgba(46,204,113,.15);color:#2ecc71;padding:2px 6px" onclick="loadPlan('+p.id+');setTimeout(exportTspGif,500)">🎬 生成</button>';
            html += '<tr style="border-bottom:1px solid rgba(255,255,255,.05)">'
                + '<td style="padding:5px"><b>'+p.planName+'</b><br><span style="color:#666;font-size:10px">'+(p.createdAt||'')+'</span></td>'
                + '<td>'+(p.transportMode||'-')+'</td>'
                + '<td>'+(p.objective||'-')+'</td>'
                + '<td>'+(p.pavilionCount||0)+'</td>'
                + '<td>'+(p.totalDistance!=null?p.totalDistance.toFixed(2)+'km':'-')+'</td>'
                + '<td>'+(p.totalCost!=null?'¥'+p.totalCost.toFixed(0):'-')+'</td>'
                + '<td>'+(p.totalDuration!=null?Math.round(p.totalDuration/60)+'min':'-')+'</td>'
                + '<td style="text-align:center">'+gifBtn+'</td>'
                + '<td>'
                + '<button class="btn btn-sm" style="font-size:10px;background:rgba(255,152,0,.2);color:#ff9800;margin-right:4px" onclick="loadPlan('+p.id+')">▶ 加载</button>'
                + '<button class="btn btn-sm" style="font-size:10px;background:rgba(231,76,60,.2);color:#e74c3c" onclick="delPlan('+p.id+')">删除</button>'
                + '</td></tr>';
        });
        html += '</tbody></table>';
        list.innerHTML = html;
    } catch(e) { list.innerHTML = '<p style="color:#e74c3c">加载失败: '+e.message+'</p>'; }
}

async function loadPlan(id) {
    try {
        var resp = await fetch('/route-plans/'+id);
        var data = await resp.json();
        if (!data.success || !data.plan) { toast('加载方案失败', 'danger'); return; }
        plansModal.hide();
        stopTspAnimation();
        data.plan._savedId = id;
        setupTspAnimation(data.plan);
        toast('方案已加载,点击播放查看动画', 'success');
    } catch(e) { toast('加载失败: '+e.message, 'danger'); }
}

async function delPlan(id) {
    if (!confirm('确认删除该方案?')) return;
    try {
        var resp = await fetch('/route-plans/'+id, {method:'DELETE'});
        var data = await resp.json();
        if (data.success) { toast('方案已删除', 'success'); loadSavedPlans(); }
        else toast('删除失败', 'danger');
    } catch(e) { toast('删除失败: '+e.message, 'danger'); }
}

var cameraStream = null;
var cameraModalObj = null;
var capturedPhotoData = null;
var isRecording = false;
var mediaRecorder = null;
var recordedChunks = [];
var capturedVideoBlob = null;

function openCamera() {
    capturedPhotoData = null;
    capturedVideoBlob = null;
    document.getElementById('capturedPhoto').style.display = 'none';
    document.getElementById('capturedPhoto').src = '';
    document.getElementById('cameraPreview').style.display = 'block';
    document.getElementById('savePhotoBtn').disabled = true;
    document.getElementById('sharePhotoBtn').disabled = true;
    document.getElementById('retakeBtn').style.display = 'none';
    document.getElementById('cameraStatus').textContent = '正在启动摄像头...';

    if (!cameraModalObj) {
        cameraModalObj = new bootstrap.Modal(document.getElementById('cameraModal'));
    }
    cameraModalObj.show();

    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' }, audio: false })
            .then(function(stream) {
                cameraStream = stream;
                document.getElementById('cameraPreview').srcObject = stream;
                document.getElementById('cameraStatus').textContent = '点击「拍照」按钮拍摄照片';
            })
            .catch(function(err) {
                document.getElementById('cameraStatus').textContent = '摄像头启动失败: ' + err.message;
                toast('无法访问摄像头: ' + err.message, 'danger');
            });
    } else {
        document.getElementById('cameraStatus').textContent = '您的浏览器不支持摄像头访问';
    }
}

function stopCamera() {
    if (cameraStream) {
        cameraStream.getTracks().forEach(function(t){ t.stop(); });
        cameraStream = null;
    }
    document.getElementById('cameraPreview').srcObject = null;
    if (mediaRecorder && mediaRecorder.state !== 'inactive') {
        mediaRecorder.stop();
    }
    isRecording = false;
    document.getElementById('recordBtn').textContent = '🎥 开始录像';
}

function capturePhoto() {
    var video = document.getElementById('cameraPreview');
    var canvas = document.createElement('canvas');
    canvas.width = video.videoWidth || 1280;
    canvas.height = video.videoHeight || 720;
    var ctx = canvas.getContext('2d');
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
    capturedPhotoData = canvas.toDataURL('image/png');
    document.getElementById('capturedPhoto').src = capturedPhotoData;
    document.getElementById('capturedPhoto').style.display = 'block';
    document.getElementById('cameraPreview').style.display = 'none';
    document.getElementById('savePhotoBtn').disabled = false;
    document.getElementById('sharePhotoBtn').disabled = false;
    document.getElementById('retakeBtn').style.display = '';
    document.getElementById('cameraStatus').textContent = '照片已拍摄，可保存或分享';
}

function retakePhoto() {
    capturedPhotoData = null;
    document.getElementById('capturedPhoto').style.display = 'none';
    document.getElementById('capturedPhoto').src = '';
    document.getElementById('cameraPreview').style.display = 'block';
    document.getElementById('savePhotoBtn').disabled = true;
    document.getElementById('sharePhotoBtn').disabled = true;
    document.getElementById('retakeBtn').style.display = 'none';
    document.getElementById('cameraStatus').textContent = '准备拍摄...';
}

function saveCapturedPhoto() {
    if (capturedPhotoData) {
        var link = document.createElement('a');
        link.download = 'tingcheng_photo_' + new Date().toISOString().slice(0,19).replace(/[:-]/g,'') + '.png';
        link.href = capturedPhotoData;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        toast('照片已保存', 'success');
    }
}

function sharePhoto() {
    if (capturedPhotoData && navigator.share) {
        capturedPhotoDataToBlob(capturedPhotoData).then(function(blob){
            var file = new File([blob], 'tingcheng_photo.png', { type: 'image/png' });
            navigator.share({ title: '滁州亭城 - 照片', files: [file] }).catch(function(e){});
        });
    } else if (capturedPhotoData) {
        toast('您的浏览器不支持Web Share API，请截图分享', 'info');
    }
}

function capturedPhotoDataToBlob(dataUrl) {
    return fetch(dataUrl).then(function(r){ return r.blob(); });
}

function toggleRecording() {
    var btn = document.getElementById('recordBtn');
    if (!isRecording) {
        if (!cameraStream) { toast('请先打开摄像头', 'warning'); return; }
        recordedChunks = [];
        try {
            mediaRecorder = new MediaRecorder(cameraStream, { mimeType: 'video/webm' });
        } catch(e) {
            mediaRecorder = new MediaRecorder(cameraStream);
        }
        mediaRecorder.ondataavailable = function(e) {
            if (e.data.size > 0) recordedChunks.push(e.data);
        };
        mediaRecorder.onstop = function() {
            capturedVideoBlob = new Blob(recordedChunks, { type: 'video/webm' });
            document.getElementById('cameraStatus').textContent = '录像完成，已录制 ' + (recordedChunks.length > 0 ? Math.round(capturedVideoBlob.size/1024) + 'KB' : '0KB');
            btn.textContent = '🎥 开始录像';
            isRecording = false;
        };
        mediaRecorder.start();
        isRecording = true;
        btn.textContent = '⏹️ 停止录像';
        document.getElementById('cameraStatus').textContent = '正在录像...';
    } else {
        if (mediaRecorder && mediaRecorder.state !== 'inactive') {
            mediaRecorder.stop();
        }
    }
}

var travelMapModalObj = null;

function openTravelMap() {
    document.getElementById('travelDateStr').textContent = new Date().toLocaleString('zh-CN');
    document.getElementById('travelMapPlaceholder').style.display = 'block';

    if (!travelMapModalObj) {
        travelMapModalObj = new bootstrap.Modal(document.getElementById('travelMapModal'));
    }
    travelMapModalObj.show();

    setTimeout(function(){
        captureMapScreenshot();
    }, 800);
}

function captureMapScreenshot() {
    var mapContainer = document.getElementById('leafletContainer') || document.getElementById('mapwrap');
    var canvas = document.getElementById('travelMapCanvas');
    canvas.style.display = 'block';

    var bounds = TCG.lmap.getBounds();
    var center = TCG.lmap.getCenter();
    var zoom = TCG.lmap.getZoom();
    var w = 800, h = 500;
    canvas.width = w;
    canvas.height = h;
    var ctx = canvas.getContext('2d');

    ctx.fillStyle = '#f0f0f0';
    ctx.fillRect(0, 0, w, h);

    ctx.save();
    ctx.translate(w/2, h/2);
    var scale = Math.pow(2, zoom - 13) * 400;
    ctx.scale(1, -1);
    ctx.font = '12px sans-serif';
    ctx.fillStyle = '#333';
    ctx.textAlign = 'center';
    ctx.fillText('📍 滁州亭城 GIS', 0, -h/2 + 30);

    var centerLng = center.lng, centerLat = center.lat;
    var pxPerDeg = scale;

    var needGcjForCanvas = needGcj();
    function canvasCoord(lng, lat) {
        if (needGcjForCanvas) { var g = gcj02(lng, lat); return [g[0], g[1]]; }
        return [lng, lat];
    }
    if (TCG.pavs && TCG.pavs.length) {
        var shown = TCG.pavs.slice(0, 50);
        shown.forEach(function(p, i){
            var cc = canvasCoord(p.longitude, p.latitude);
            var dx = (cc[0] - centerLng) * pxPerDeg;
            var dy = (cc[1] - centerLat) * pxPerDeg;
            if (Math.abs(dx) < w/2 && Math.abs(dy) < h/2) {
                ctx.beginPath();
                ctx.arc(dx, dy, 3, 0, 2*Math.PI);
                ctx.fillStyle = '#e74c3c';
                ctx.fill();
            }
        });
    }
    if (TCG.scenicAreas && TCG.scenicAreas.length) {
        TCG.scenicAreas.forEach(function(s){
            if (s.geomWkt) {
                ctx.strokeStyle = '#27ae60';
                ctx.lineWidth = 2;
                ctx.setLineDash([]);
                var rings = window.parseWktPolygonSimple(s.geomWkt);
                if (rings && rings.length) {
                    rings.forEach(function(ring){
                        if (ring.length < 3) return;
                        ctx.beginPath();
                        ring.forEach(function(pt, i){
                            var cc = canvasCoord(pt[0], pt[1]);
                            var dx = (cc[0] - centerLng) * pxPerDeg;
                            var dy = (cc[1] - centerLat) * pxPerDeg;
                            if (i === 0) ctx.moveTo(dx, dy);
                            else ctx.lineTo(dx, dy);
                        });
                        ctx.closePath();
                        ctx.fillStyle = 'rgba(39,174,96,0.1)';
                        ctx.fill();
                        ctx.stroke();
                    });
                }
            }
        });
    }
    if (TCG.tourismRoutes && TCG.tourismRoutes.length) {
        TCG.tourismRoutes.forEach(function(r){
            if (r.geomWkt) {
                ctx.strokeStyle = r.color || '#e67e22';
                ctx.lineWidth = 3;
                var pts = window.parseWktLineSimple(r.geomWkt);
                if (pts && pts.length > 1) {
                    ctx.beginPath();
                    pts.forEach(function(pt, i){
                        var cc = canvasCoord(pt[0], pt[1]);
                        var dx = (cc[0] - centerLng) * pxPerDeg;
                        var dy = (cc[1] - centerLat) * pxPerDeg;
                        if (i === 0) ctx.moveTo(dx, dy);
                        else ctx.lineTo(dx, dy);
                    });
                    ctx.stroke();
                }
            }
        });
    }

    ctx.setLineDash([4, 4]);
    ctx.strokeStyle = 'rgba(0,0,0,0.2)';
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(-15, 0); ctx.lineTo(15, 0);
    ctx.moveTo(0, -15); ctx.lineTo(0, 15);
    ctx.stroke();
    ctx.setLineDash([]);

    ctx.restore();

    ctx.fillStyle = 'rgba(0,0,0,0.5)';
    ctx.fillRect(0, h - 28, w, 28);
    ctx.fillStyle = '#fff';
    ctx.font = '11px sans-serif';
    ctx.textAlign = 'left';
    ctx.fillText('地图中心: ' + center.lat.toFixed(4) + ', ' + center.lng.toFixed(4) + '  缩放: ' + zoom, 10, h - 10);

    document.getElementById('travelMapPlaceholder').style.display = 'none';
}

function saveTravelMap() {
    var title = document.getElementById('travelMapTitleInput').value.trim() || '滁州亭城·旅行纪念';
    document.getElementById('travelMapTitle').textContent = title;
    var canvas = document.getElementById('travelMapCanvas');
    setTimeout(function(){
        typeof html2canvas !== 'undefined' ? html2canvas(document.getElementById('travelMapPreview')).then(function(c){
            var link = document.createElement('a');
            link.download = 'tingcheng_travel_map_' + new Date().toISOString().slice(0,10) + '.png';
            link.href = c.toDataURL('image/png');
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            toast('纪念地图已保存', 'success');
        }) : (function(){
            var link = document.createElement('a');
            link.download = 'tingcheng_travel_map_' + new Date().toISOString().slice(0,10) + '.png';
            link.href = canvas.toDataURL('image/png');
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            toast('纪念地图已保存（基础版）', 'success');
        })();
    }, 300);
}

function printTravelMap() {
    var title = document.getElementById('travelMapTitleInput').value.trim() || '滁州亭城·旅行纪念';
    document.getElementById('travelMapTitle').textContent = title;
    setTimeout(function(){
        var printWin = window.open('', '_blank', 'width=900,height=700');
        printWin.document.write('<html><head><title>滁州亭城旅行纪念地图</title>');
        printWin.document.write('<style>body{margin:0;padding:20px;text-align:center}img{max-width:100%;border-radius:4px;box-shadow:0 2px 10px rgba(0,0,0,.2)}</style></head><body>');
        printWin.document.write('<img src="' + document.getElementById('travelMapCanvas').toDataURL() + '" onload="window.print();window.close()">');
        printWin.document.write('</body></html>');
        printWin.document.close();
    }, 300);
}

function shareTravelMap() {
    var canvas = document.getElementById('travelMapCanvas');
    if (navigator.share) {
        canvas.toBlob(function(blob){
            var file = new File([blob], 'tingcheng_travel_map.png', { type: 'image/png' });
            navigator.share({
                title: '滁州亭城旅行纪念地图',
                text: '我在滁州亭城GIS制作的旅行纪念地图',
                files: [file]
            }).catch(function(e){});
        });
    } else {
        canvas.toBlob(function(blob){
            navigator.clipboard.write([
                new ClipboardItem({ 'image/png': blob })
            ]).then(function(){
                toast('地图已复制到剪贴板，可粘贴分享', 'success');
            }).catch(function(){
                saveTravelMap();
                toast('已保存图片，可手动分享', 'info');
            });
        });
    }
}

var travelLogModalObj = null;
var travelLogEditId = null;

function openTravelLog() {
    if (!travelLogModalObj) {
        travelLogModalObj = new bootstrap.Modal(document.getElementById('travelLogModal'));
    }
    travelLogModalObj.show();
    loadTravelLogs();
}

function loadTravelLogs() {
    var list = document.getElementById('travelLogList');
    list.innerHTML = '<p style="color:#888;font-size:11px;text-align:center;padding:10px">加载中...</p>';
    fetch('/travel-logs')
        .then(function(r){ return r.json(); })
        .then(function(res){
            if (!res.success || !res.data || res.data.length === 0) {
                list.innerHTML = '<p style="color:#888;font-size:11px;text-align:center;padding:20px">暂无游记，点击「写游记」开始记录</p>';
                return;
            }
            var h = '';
            res.data.forEach(function(l){
                h += '<div style="background:rgba(255,255,255,.04);border-radius:6px;padding:10px 12px;margin:6px 0;font-size:11px">';
                h += '<div style="display:flex;justify-content:space-between;align-items:center">';
                h += '<b style="color:#8ab4f8;font-size:12px">' + escHtml(l.title) + '</b>';
                h += '<div>';
                if (l.rating) h += '<span style="color:#f39c12">' + '★'.repeat(l.rating) + '</span> ';
                h += '<span style="color:#888;font-size:10px">' + (l.createdAt ? new Date(l.createdAt).toLocaleDateString('zh-CN') : '') + '</span>';
                h += '</div></div>';
                if (l.location) h += '<div style="color:#aaa;font-size:10px;margin:2px 0">📍 ' + escHtml(l.location) + '</div>';
                if (l.author) h += '<div style="color:#888;font-size:10px">👤 ' + escHtml(l.author) + '</div>';
                if (l.content) h += '<div style="color:#bbb;margin-top:4px;line-height:1.4">' + escHtml(l.content.substring(0, 200)) + (l.content.length > 200 ? '...' : '') + '</div>';
                if (l.photoUrl) h += '<img src="' + l.photoUrl + '" style="max-width:120px;max-height:80px;border-radius:4px;margin-top:4px;cursor:pointer" onclick="window.open(\'' + l.photoUrl + '\')">';
                h += '<div style="margin-top:4px;display:flex;gap:4px">';
                h += '<button style="background:rgba(255,255,255,.08);color:#ccc;border:1px solid rgba(255,255,255,.15);border-radius:3px;padding:2px 8px;cursor:pointer;font-size:10px" onclick="showTravelLogForm(' + l.id + ')">编辑</button>';
                h += '<button style="background:rgba(231,76,60,.15);color:#e74c3c;border:1px solid rgba(231,76,60,.2);border-radius:3px;padding:2px 8px;cursor:pointer;font-size:10px" onclick="deleteTravelLog(' + l.id + ')">删除</button>';
                h += '</div></div>';
            });
            list.innerHTML = h;
        })
        .catch(function(e){
            list.innerHTML = '<p style="color:#e74c3c;font-size:11px;text-align:center;padding:20px">加载失败: ' + e.message + '</p>';
        });
}

function showTravelLogForm(id) {
    travelLogEditId = id;
    if (id) {
        fetch('/travel-logs/' + id)
            .then(function(r){ return r.json(); })
            .then(function(res){
                if (res.success) showFormWithData(res.data);
            });
    } else {
        showFormWithData(null);
    }
}

function showFormWithData(data) {
    var list = document.getElementById('travelLogList');
    var isEdit = !!data;
    list.innerHTML = '<div style="padding:4px">';
    list.innerHTML += '<div class="mb-2"><label style="font-size:11px;color:#aaa">标题 *</label><input type="text" id="tlTitle" class="form-control form-control-sm" value="' + (data ? escHtml(data.title) : '') + '"></div>';
    list.innerHTML += '<div class="mb-2"><label style="font-size:11px;color:#aaa">内容</label><textarea id="tlContent" class="form-control form-control-sm" rows="4">' + (data ? escHtml(data.content || '') : '') + '</textarea></div>';
    list.innerHTML += '<div class="row"><div class="col"><label style="font-size:11px;color:#aaa">位置</label><input type="text" id="tlLocation" class="form-control form-control-sm" value="' + (data ? escHtml(data.location || '') : '') + '"></div>';
    list.innerHTML += '<div class="col"><label style="font-size:11px;color:#aaa">评分 (1-5)</label><select id="tlRating" class="form-select form-select-sm"><option value="">无</option>';
    for (var i = 1; i <= 5; i++) {
        list.innerHTML += '<option value="' + i + '"' + (data && data.rating === i ? ' selected' : '') + '>' + i + '星</option>';
    }
    list.innerHTML += '</select></div></div>';
    list.innerHTML += '<div class="mb-2"><label style="font-size:11px;color:#aaa">作者</label><input type="text" id="tlAuthor" class="form-control form-control-sm" value="' + (data ? escHtml(data.author || '') : '') + '"></div>';
    list.innerHTML += '<div class="mb-2"><label style="font-size:11px;color:#aaa">关联路线ID (可选)</label><input type="number" id="tlRouteId" class="form-control form-control-sm" value="' + (data && data.routeId ? data.routeId : '') + '"></div>';
    list.innerHTML += '<div class="mb-2"><label style="font-size:11px;color:#aaa">关联景区ID (可选)</label><input type="number" id="tlScenicId" class="form-control form-control-sm" value="' + (data && data.scenicId ? data.scenicId : '') + '"></div>';
    list.innerHTML += '<div class="mb-2"><label style="font-size:11px;color:#aaa">照片上传</label>';
    list.innerHTML += '<div style="display:flex;gap:4px;align-items:center"><input type="file" id="tlPhotoInput" accept="image/*" style="font-size:11px;background:rgba(255,255,255,.08);color:#ccc;border:1px solid rgba(255,255,255,.15);border-radius:3px;padding:3px">';
    list.innerHTML += '<button class="btn btn-sm" style="background:rgba(52,152,219,.2);color:#3498db" onclick="uploadTravelPhoto()">📤 上传</button></div>';
    list.innerHTML += '<div id="tlPhotoStatus" style="font-size:10px;color:#888;margin-top:2px"></div>';
    if (data && data.photoUrl) {
        list.innerHTML += '<div style="margin-top:4px"><img src="' + data.photoUrl + '" style="max-width:120px;max-height:80px;border-radius:4px"> <span style="font-size:10px;color:#888">当前照片</span></div>';
        list.innerHTML += '<input type="hidden" id="tlPhotoUrl" value="' + escHtml(data.photoUrl) + '">';
    } else {
        list.innerHTML += '<input type="hidden" id="tlPhotoUrl" value="">';
    }
    list.innerHTML += '</div>';
    list.innerHTML += '<div style="display:flex;gap:6px;margin-top:8px">';
    list.innerHTML += '<button class="btn btn-sm btn-primary" onclick="saveTravelLog()">💾 保存</button>';
    list.innerHTML += '<button class="btn btn-sm btn-secondary" onclick="loadTravelLogs()">取消</button>';
    list.innerHTML += '</div></div>';
}

function uploadTravelPhoto() {
    var input = document.getElementById('tlPhotoInput');
    if (!input.files || !input.files.length) { toast('请选择图片文件', 'warning'); return; }
    var formData = new FormData();
    formData.append('file', input.files[0]);
    document.getElementById('tlPhotoStatus').textContent = '上传中...';
    fetch('/api/upload/photo', { method: 'POST', body: formData })
        .then(function(r){ return r.json(); })
        .then(function(res){
            if (res.success) {
                document.getElementById('tlPhotoUrl').value = res.url;
                document.getElementById('tlPhotoStatus').innerHTML = '✅ 上传成功: <a href="'+res.url+'" target="_blank" style="color:#8ab4f8">查看图片</a>';
            } else {
                document.getElementById('tlPhotoStatus').textContent = '上传失败: ' + (res.message||'');
            }
        })
        .catch(function(e){
            document.getElementById('tlPhotoStatus').textContent = '上传失败: ' + e.message;
        });
}

function saveTravelLog() {
    var title = document.getElementById('tlTitle').value.trim();
    if (!title) { toast('请输入标题', 'warning'); return; }
    var body = {
        title: title,
        content: document.getElementById('tlContent').value.trim() || null,
        location: document.getElementById('tlLocation').value.trim() || null,
        rating: document.getElementById('tlRating').value ? parseInt(document.getElementById('tlRating').value) : null,
        author: document.getElementById('tlAuthor').value.trim() || null,
        photoUrl: document.getElementById('tlPhotoUrl').value || null,
        routeId: document.getElementById('tlRouteId').value ? parseInt(document.getElementById('tlRouteId').value) : null,
        scenicId: document.getElementById('tlScenicId').value ? parseInt(document.getElementById('tlScenicId').value) : null
    };
    var url = '/travel-logs', method = 'POST';
    if (travelLogEditId) { url += '/' + travelLogEditId; method = 'PUT'; }
    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    }).then(function(r){ return r.json(); })
    .then(function(res){
        if (res.success) {
            toast(travelLogEditId ? '游记已更新' : '游记已保存', 'success');
            loadTravelLogs();
        } else {
            toast('保存失败: ' + (res.message || '未知错误'), 'danger');
        }
    }).catch(function(e){
        toast('保存失败: ' + e.message, 'danger');
    });
}

function deleteTravelLog(id) {
    if (!confirm('确定删除此游记？')) return;
    fetch('/travel-logs/' + id, { method: 'DELETE' })
        .then(function(r){ return r.json(); })
        .then(function(res){
            if (res.success) { toast('游记已删除', 'success'); loadTravelLogs(); }
            else { toast('删除失败: ' + (res.message || '未知错误'), 'danger'); }
        }).catch(function(e){
            toast('删除失败: ' + e.message, 'danger');
        });
}

window.switchPavTab = switchPavTab;
window.addPavilion = addPavilion;
window.editCurPavilion = editCurPavilion;
window.delCurPavilion = delCurPavilion;
window.savePavilion = savePavilion;
window.addCollector = addCollector;
window.traverseAll = traverseAll;
window.tourismSvc = tourismSvc;
window.smartTour = smartTour;
window.showWeather = showWeather;
window.nearbyFac = nearbyFac;
window.shareRoute = shareRoute;
window.showImportDialog = showImportDialog;
window.doImport = doImport;
window.dlExport = dlExport;
window.dlTemplate = dlTemplate;
window.openTspSetup = openTspSetup;
window.stopTspAnimation = stopTspAnimation;
window.exportTspGif = exportTspGif;
window.openSavedPlans = openSavedPlans;
window.loadPlan = loadPlan;
window.delPlan = delPlan;
window.openCamera = openCamera;
window.stopCamera = stopCamera;
window.capturePhoto = capturePhoto;
window.retakePhoto = retakePhoto;
window.saveCapturedPhoto = saveCapturedPhoto;
window.sharePhoto = sharePhoto;
window.toggleRecording = toggleRecording;
window.openTravelMap = openTravelMap;
window.saveTravelMap = saveTravelMap;
window.printTravelMap = printTravelMap;
window.shareTravelMap = shareTravelMap;
window.openTravelLog = openTravelLog;
window.loadTravelLogs = loadTravelLogs;
window.showTravelLogForm = showTravelLogForm;
window.saveTravelLog = saveTravelLog;
window.deleteTravelLog = deleteTravelLog;
window.uploadTravelPhoto = uploadTravelPhoto;
})();
