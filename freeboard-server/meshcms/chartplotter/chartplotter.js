//var map;
// var mapBounds = new OpenLayers.Bounds( 173.014971899, -41.1579399972,
// 173.061684157, -41.1303857203);
// var mapMinZoom = 12;
var mapBounds = new OpenLayers.Bounds(171.672658537, -41.3148906329, 174.54758838, -40.5453691267);
var mapMinZoom = 0;
var mapMaxZoom = 18;
var chartProjection = new OpenLayers.Projection("EPSG:900913");
var screenProjection = new OpenLayers.Projection("EPSG:4326");
var shipMarker = new OpenLayers.Layer.Vector('Ship', {
    styleMap: new OpenLayers.StyleMap({
        externalGraphic: './img/marker.png',
        graphicWidth: 20, graphicHeight: 24, graphicYOffset: -24,
        title: '${tooltip}'
    })
});
//var lat=mapBounds.getCenterLonLat().lat;
//var lon=mapBounds.getCenterLonLat().lon;
// avoid pink tiles
OpenLayers.IMAGE_RELOAD_ATTEMPTS = 3;
OpenLayers.Util.onImageLoadErrorColor = "transparent";

function init() {

	var options = {
		controls : [],
		projection : chartProjection,
		displayProjection : screenProjection,
		units : "m",
		maxResolution : 156543.0339,
		maxExtent : new OpenLayers.Bounds(-20037508, -20037508, 20037508,
				20037508.34)
	};
	map = new OpenLayers.Map('map', options);

	// create TMS Overlay layer

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

	var NZ61 = new OpenLayers.Layer.TMS("NZ61 ", "../../mapcache/NZ61/", {
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

	var switcherControl = new OpenLayers.Control.LayerSwitcher();
	map.addControl(switcherControl);
	switcherControl.maximizeControl();

	map.zoomToExtent(mapBounds.transform(map.displayProjection,
					map.projection));

	map.addControls([ new OpenLayers.Control.Navigation(),
			new OpenLayers.Control.Attribution(),
			new OpenLayers.Control.PanZoomBar(),
			new OpenLayers.Control.MousePosition()]);

	// measurement
	// style the sketch fancy
	var sketchSymbolizers = {
		"Point" : {
			pointRadius : 4,
			graphicName : "square",
			fillColor : "white",
			fillOpacity : 1,
			strokeWidth : 1,
			strokeOpacity : 1,
			strokeColor : "#333333"
		},
		"Line" : {
			strokeWidth : 3,
			strokeOpacity : 1,
			strokeColor : "#666666",
			strokeDashstyle : "dash"
		},
		"Polygon" : {
			strokeWidth : 2,
			strokeOpacity : 1,
			strokeColor : "#666666",
			fillColor : "white",
			fillOpacity : 0.3
		}
	};
	var style = new OpenLayers.Style();
	style.addRules([ new OpenLayers.Rule({
		symbolizer : sketchSymbolizers
	}) ]);
	var styleMap = new OpenLayers.StyleMap({
		"default" : style
	});

	// allow testing of specific renderers via "?renderer=Canvas", etc
	var renderer = OpenLayers.Util.getParameters(window.location.href).renderer;
	renderer = (renderer) ? [ renderer ]
			: OpenLayers.Layer.Vector.prototype.renderers;

	measureControls = {
		line : new OpenLayers.Control.Measure(OpenLayers.Handler.Path, {
			persist : true,
			handlerOptions : {
				layerOptions : {
					renderers : renderer,
					styleMap : styleMap
				}
			}
		}),

	};

	var control;
	for ( var key in measureControls) {
		control = measureControls[key];
		control.setImmediate(true);
		control.geodesic = true;
		control.events.on({
			"measure" : handleMeasurements,
			"measurepartial" : handleMeasurements
		});
		map.addControl(control);
	}

	document.getElementById('noneToggle').checked = true;

}




function osm_getTileURL(bounds) {
	var res = this.map.getResolution();
	var x = Math.round((bounds.left - this.maxExtent.left)
			/ (res * this.tileSize.w));
	var y = Math.round((this.maxExtent.top - bounds.top)
			/ (res * this.tileSize.h));
	var z = this.map.getZoom();
	var limit = Math.pow(2, z);

	if (y < 0 || y >= limit) {
		return "./blank.gif";
	} else {
		x = ((x % limit) + limit) % limit;
		return this.url + z + "/" + x + "/" + y + "." + this.type;
	}
}

function overlay_getTileURL(bounds) {
	var res = this.map.getResolution();
	var x = Math.round((bounds.left - this.maxExtent.left)
			/ (res * this.tileSize.w));
	var y = Math.round((bounds.bottom - this.tileOrigin.lat)
			/ (res * this.tileSize.h));
	var z = this.map.getZoom();
	if (this.map.baseLayer.name == 'Virtual Earth Roads'
			|| this.map.baseLayer.name == 'Virtual Earth Aerial'
			|| this.map.baseLayer.name == 'Virtual Earth Hybrid') {
		z = z + 1;
	}
	if (mapBounds.intersectsBounds(bounds) && z >= mapMinZoom
			&& z <= mapMaxZoom) {
		// console.log( this.url + z + "/" + x + "/" + y + "." + this.type);
		return this.url + z + "/" + x + "/" + y + "." + this.type;
	} else {
		return "./blank.gif";
	}

}

function getWindowHeight() {
	if (self.innerHeight)
		return self.innerHeight;
	if (document.documentElement && document.documentElement.clientHeight)
		return document.documentElement.clientHeight;
	if (document.body)
		return document.body.clientHeight;
	return 0;
}

function getWindowWidth() {
	return 0;
}

function resize() {
	var map = document.getElementById("map");
	map.style.height = (getWindowHeight() - 80) + "px";
	map.style.width = (getWindowWidth() - 20) + "px";
	if (map.updateSize) {
		map.updateSize();
	}
	;
}

onresize = function() {
	resize();
};

// measurement
function handleMeasurements(event) {
	var geometry = event.geometry;
	var units = event.units;
	var order = event.order;
	var measure = event.measure;
	var element = document.getElementById('output');
	var out = "";
	if (order == 1) {
		out += "measure: " + measure.toFixed(3) + " " + units;
	} else {
		out += "measure: " + measure.toFixed(3) + " " + units + "<sup>2</"
				+ "sup>";
	}
	element.innerHTML = out;
}

function toggleControl(element) {
	for (key in measureControls) {
		var control = measureControls[key];
		if (element.value == key && element.checked) {
			control.activate();
		} else {
			control.deactivate();
		}
	}
}



function setPosition(lat, lon){
	// The location of our marker and popup. We usually think in geographic
    // coordinates ('EPSG:4326'), but the map is projected ('EPSG:3857').
    var shipLocation = new OpenLayers.Geometry.Point(lon, lat) //new OpenLayers.Geometry.Point(lonLat);
        .transform(screenProjection,
        		chartProjection);
    // We add the marker with a tooltip text to the overlay
    shipMarker.removeAllFeatures();
    shipMarker.addFeatures([
        new OpenLayers.Feature.Vector(shipLocation, {tooltip: 'Motu'+lat+', '+lon})
    ]);

}

function posInit(){
	//make a web socket
	
	var location = "ws://"+window.location.hostname+":9090/navData";
	//alert(location);
	var lat = 0;
	var lon = 0;
	this._ws = new WebSocket(location);
	this._ws.onopen = function() {
	};
	this._ws.onmessage = function(m) {
		
		if (m.data && m.data.indexOf('LAT') >= 0) {
			var c = m.data.substring(m.data.indexOf('LAT') + 4);
			lat=c;
			//alert(lat);
			setPosition(lat,lon);
		}
		if (m.data && m.data.indexOf('LON') >= 0) {
			var c = m.data.substring(m.data.indexOf('LON') + 4);
			lon=c;
			setPosition(lat,lon);
		}
		
	};
	this._ws.onclose = function() {
		this._ws = null;
	};
}