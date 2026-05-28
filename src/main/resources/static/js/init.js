(function(){ 'use strict';
if (!window.TCG) window.TCG = {};

var TCG = window.TCG;

function boot() {
    TCG.pavModal = new bootstrap.Modal(document.getElementById('pavModal'));
    TCG.impModal = new bootstrap.Modal(document.getElementById('impModal'));
    window.scenicModal = new bootstrap.Modal(document.getElementById('scenicModal'));
    window.adminModal = new bootstrap.Modal(document.getElementById('adminModal'));
    window.initMaps();
    setTimeout(window.initDefaultS3Layers, 500);
}

document.addEventListener('click', function(e){
    if (!e.target.closest('.tb-dropdown'))
        document.querySelectorAll('.tb-drop-content.show').forEach(function(el){ el.classList.remove('show'); });
});

document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('mapProvSel').addEventListener('change', function(){ window.switchMap(this.value); });
    document.getElementById('searchInput').addEventListener('keydown', function(e){ if (e.key==='Enter') window.doSearch(); });
    document.getElementById('savePavBtn').addEventListener('click', window.savePavilion);
    document.getElementById('saveScenicBtn').addEventListener('click', window.saveScenic);
    document.getElementById('saveAdminBtn').addEventListener('click', window.saveAdmin);
    document.getElementById('startImportBtn').addEventListener('click', window.doImport);

    document.getElementById('tspStartBtn').addEventListener('click', async function(){
        var mode = document.querySelector('input[name=tspMode]:checked').value;
        var objective = document.querySelector('input[name=tspObj]:checked').value;
        var scope = document.getElementById('tspScope').value;
        var ids;
        if (scope === 'selected') {
            if (TCG.sel.length < 2) { window.toast('请至少选择2个亭子', 'warning'); return; }
            ids = TCG.sel.map(function(p){return p.id});
        } else {
            ids = TCG.pavs.map(function(p){return p.id});
            if (ids.length < 2) { window.toast('亭子数量不足', 'warning'); return; }
        }
        TCG.tspSetupModal = TCG.tspSetupModal || new bootstrap.Modal(document.getElementById('tspSetupModal'));
        TCG.tspSetupModal.hide();
        await TCG.runTspPlanning(ids, mode, objective);
    });

    document.getElementById('tspcPlay').addEventListener('click', function(){
        if (!TCG.tspPlanData) return;
        if (TCG.tspProgress >= 1.0) { TCG.tspProgress = 0; TCG.tspProgressStart = 0; TCG.updateTspUi(0,'#ff9800'); }
        if (TCG.tspPaused) TCG.startTspPlay(); else TCG.pauseTspPlay();
    });
    document.getElementById('tspcReset').addEventListener('click', function(){
        if (!TCG.tspPlanData) return;
        TCG.pauseTspPlay();
        TCG.tspProgress = 0; TCG.tspProgressStart = 0; TCG.tspLastIdx = 0;
        TCG.updateTspUi(0, '#ff9800');
    });
    document.getElementById('tspcSpeed').addEventListener('change', function(){
        TCG.tspSpeed = parseFloat(this.value);
        if (!TCG.tspPaused) {
            TCG.tspProgressStart = TCG.tspProgress;
            TCG.tspAnimStart = Date.now();
        }
    });
    document.getElementById('tspcBar').addEventListener('click', function(e){
        if (!TCG.tspPlanData) return;
        var rect = this.getBoundingClientRect();
        var p = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
        TCG.pauseTspPlay();
        TCG.tspProgress = p; TCG.tspProgressStart = p; TCG.tspLastIdx = 0;
        TCG.updateTspUi(p, '#ff9800');
    });
    document.getElementById('tspcClose').addEventListener('click', window.stopTspAnimation);
    document.getElementById('tspcSave').addEventListener('click', async function(){
        if (!TCG.tspPlanData) return;
        var name = prompt('方案名称', '方案-' + new Date().toLocaleString('zh-CN'));
        if (!name) return;
        try {
            var resp = await fetch('/route-plans', {
                method:'POST', headers:{'Content-Type':'application/json'},
                body: JSON.stringify({
                    planName: name, mode: TCG.tspPlanData.mode, objective: TCG.tspPlanData.objective,
                    plan: TCG.tspPlanData
                })
            });
            var data = await resp.json();
            if (data.success) {
                TCG.tspPlanData._savedId = data.data && data.data.id;
                window.toast('✅ 方案已保存！点击顶栏「📂 已存方案」或左侧「智能功能→已存方案」查看和管理', 'success');
                setTimeout(function(){
                    if (TCG.tspPlanData && TCG.tspPlanData._savedId) {
                        window.toast('正在自动生成动画GIF...', 'info');
                        window.exportTspGif();
                    }
                }, 500);
            } else window.toast('保存失败: ' + (data.message||''), 'danger');
        } catch(e) { window.toast('保存失败: '+e.message, 'danger'); }
    });
    document.getElementById('tspcGif').addEventListener('click', window.exportTspGif);

    boot();
});

window.delCurRoute = function(id) {
    if (!id && window.selRoute) id = window.selRoute.id;
    window.delRoute(id);
};

window.toggleLang = function() {
    var newLang = window.TCG.getLang() === 'zh' ? 'en' : 'zh';
    window.TCG.setLang(newLang);
    var btn = document.getElementById('langToggle');
    if (btn) btn.textContent = newLang === 'zh' ? '🌐 EN' : '🌐 中';
};

// 监听语言切换更新页面文本
window.TCG.onLangChange(function(){
    var btn = document.getElementById('langToggle');
    var lang = window.TCG.getLang();
    if (btn) btn.textContent = lang === 'zh' ? '🌐 EN' : '🌐 中';
});

// 初始化语言按钮状态
(function initLang(){
    var btn = document.getElementById('langToggle');
    var lang = window.TCG.getLang();
    if (btn) btn.textContent = lang === 'zh' ? '🌐 EN' : '🌐 中';
})();
})();
