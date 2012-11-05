var mapBounds = new OpenLayers.Bounds(171.672658537, -41.3148906329, 174.54758838, -40.5453691267);
var mapMinZoom = 0;
var mapMaxZoom = 18;

function addLayers(map) {
// INSERT_HERE
	var NZ6151_1 = new OpenLayers.Layer.TMS("NZ6151_1 ",
			"../../mapcache/NZ6151_1/", {
				layername : '../../mapcache/NZ6151_1/',
				type : 'png',
				getURL : overlay_getTileURL,
				alpha : true,
				isBaseLayer : false,
				numZoomLevels : 18,
				minZoomLevel : 10,
				maxZoomLevel : 15,
				buffer : 1
			});

	var NZ615 = new OpenLayers.Layer.TMS("NZ615 ", "../../mapcache/NZ615/", {
		layername : '../../mapcache/NZ615/',
		type : 'png',
		getURL : overlay_getTileURL,
		alpha : true,
		isBaseLayer : false,
		numZoomLevels : 18,
		minZoomLevel : 8,
		maxZoomLevel : 13,
		buffer : 1
	});

	var NZ61 = new OpenLayers.Layer.TMS("NZ61 Karamea River to Stephens Island", "../../mapcache/NZ61/", {
		layername : '../../mapcache/NZ61/',
		type : 'png',
		getURL : overlay_getTileURL,
		alpha : true,
		isBaseLayer : true,
		numZoomLevels : 18,
		minZoomLevel : 7,
		maxZoomLevel : 12,
		buffer : 1
	});

	var NZ614 = new OpenLayers.Layer.TMS("NZ614 Tasman Bay",
			"../../mapcache/NZ614/", { // url: '', serviceVersion: '.',
				layername : '../../mapcache/NZ614/',
				type : 'png',
				getURL : overlay_getTileURL,
				alpha : true,
				isBaseLayer : false,
				numZoomLevels : 18,
				// maxResolution: "auto",
				minZoomLevel : 8,
				maxZoomLevel : 13,
				buffer : 1
			});
	var NZ6144 = new OpenLayers.Layer.TMS("NZ6144 Abel Tasman",
			"../../mapcache/NZ6144/", { // url: '', serviceVersion: '.',
				layername : '../../mapcache/NZ6144/',
				type : 'png',
				getURL : overlay_getTileURL,
				alpha : true,
				isBaseLayer : false,
				// numZoomLevels: 18 ,
				maxResolution : "auto",
				minZoomLevel : 9,
				maxZoomLevel : 15,
				buffer : 1
			});
	var NZ6142_1 = new OpenLayers.Layer.TMS("NZ6142_1 Nelson Harbour",
			"../../mapcache/NZ6142_1/", { // url: '', serviceVersion: '.',
				layername : '../../mapcache/NZ6142_1/',
				type : 'png',
				getURL : overlay_getTileURL,
				alpha : true,
				isBaseLayer : false,
				// numZoomLevels: 18 ,
				maxResolution : "auto",
				minZoomLevel : 10,
				maxZoomLevel : 15,
				buffer : 1
			});
	var NZ6142_2 = new OpenLayers.Layer.TMS("NZ6142_2 Port Nelson",
			"../../mapcache/NZ6142_2/", { // url: '', serviceVersion: '.',
				layername : '../../mapcache/NZ6142_2/',
				type : 'png',
				getURL : overlay_getTileURL,
				alpha : true,
				isBaseLayer : false,
				// numZoomLevels: 18 ,
				maxResolution : "auto",
				minZoomLevel : 12,
				maxZoomLevel : 17,
				buffer : 1
			});
	// create TMS Overlay layer
	var NZ614_1 = new OpenLayers.Layer.TMS("NZ614_1 Port Motueka",
			"../../mapcache/NZ614_1/", { // url: '', serviceVersion: '.',
				layername : '../../mapcache/NZ614_1/',
				type : 'png',
				getURL : overlay_getTileURL,
				alpha : true,
				isBaseLayer : false,
				// numZoomLevels: 18 ,
				maxResolution : "auto",
				minZoomLevel : 12,
				maxZoomLevel : 18,
				buffer : 1
			});
	// if (OpenLayers.Util.alphaHack() == false) { tmsoverlay.setOpacity(0.7); }
	
	map.addLayers([ NZ61,NZ614, NZ6142_1, NZ6142_2, shipMarker]);
	//map.addLayers([ NZ61, NZ614, NZ6142_1, NZ6142_2, NZ6144, NZ614_1, NZ615,NZ6151_1, vector ]);

}