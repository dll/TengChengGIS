(function(){ 'use strict';
if (!window.TCG) window.TCG = {};

window.TCG.lmap = null;
window.TCG.viewer = null;
window.TCG.is3D = false;
window.TCG.pavs = [];
window.TCG.sel = [];
window.TCG.markers = {};
window.TCG.csMarkers = {};
window.TCG.pathLyr = null;
window.TCG.csPath = null;
window.TCG.tileLyr = null;
window.TCG.mapProv = 'gaode';
window.TCG.trafficLyr = null;
window.TCG.busLyr = null;
window.TCG.taxiLyr = null;
window.TCG.ebikeLyr = null;
window.TCG.bikeLyr = null;
window.TCG.walkLyr = null;
window.TCG.csRoutes = [];
window.TCG.pavModal = null;
window.TCG.impModal = null;
window.TCG.editTarget = null;
window.TCG.navActive = false;
window.TCG.navIdx = 0;
window.TCG.scenicAreas = [];
window.TCG.adminDivisions = [];
window.TCG.scenicPolyLyr = null;
window.TCG.adminPolyLyr = null;
window.TCG.adminTreeMode = false;
window.TCG.s3Layers = [];
window.TCG.s3NextId = 1;
window.TCG.addWmsModal = null;
window.TCG.addWfsModal = null;
window.TCG.wmsCapabilitiesCache = null;
window.TCG.s3LayerGroup = null;
window.TCG.drawActive = false;
window.TCG.drawMode = null;
window.TCG.drawPoints = [];
window.TCG.drawLayer = null;
window.TCG.drawCallback = null;
window.TCG.digitPendingType = null;
window.TCG.tourismRoutes = [];
window.TCG.routePolyLyr = null;
window.TCG.routeModalObj = null;

window.TCG.clusterLayer = null;
window.TCG.clusterEnabled = false;

window.TCG.TYPE_LAB = { HISTORICAL: '历史文化亭', MODERN: '现代景观亭', CULTURAL: '文化主题亭' };
window.TCG.TYPE_STYLE = {
    MODERN:  { color:'#3498db', cls:'m', cesium: 'BLUE' },
    CULTURAL:{ color:'#2ecc71', cls:'c', cesium: 'GREEN' },
    DEFAULT: { color:'#e74c3c', cls:'h', cesium: 'RED' }
};

})();
