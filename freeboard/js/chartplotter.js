//var map;
var mapMinZoom = 7;
var mapMaxZoom = 18;

var chartProjection = new OpenLayers.Projection("EPSG:900913");
var screenProjection = new OpenLayers.Projection("EPSG:4326");
var shipMarker = new OpenLayers.Layer.Vector('Ship', {
    styleMap: new OpenLayers.StyleMap({
        externalGraphic: '../js/img/marker.png',
        graphicWidth: 20, graphicHeight: 24, graphicYOffset: -24,
        title: '${tooltip}'
    })
});

// avoid pink tiles
//OpenLayers.IMAGE_RELOAD_ATTEMPTS = 1;
OpenLayers.Util.onImageLoadErrorColor = "transparent";


function initCharts() {

	var options = {
		allOverlays: true,
		controls : [],
		projection : chartProjection,
		displayProjection : screenProjection,
		units : "nmi",
		maxResolution : 156543.0339,
		maxExtent : new OpenLayers.Bounds(-20037508, -20037508, 20037508,
				20037508.34),
	};
	map = new OpenLayers.Map('map', options);

	//add layers
	addLayers(map);
	map.addLayer(shipMarker);
	
	map.addControls([ 
			new OpenLayers.Control.TouchNavigation({
			    dragPanOptions: {
			        enableKinetic: true
			    }
			}),
	        new OpenLayers.Control.Navigation(),
			new OpenLayers.Control.Attribution(),
	        new OpenLayers.Control.Zoom(),
			new OpenLayers.Control.MousePosition(),
			]);

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
				displaySystemUnits: "nmi",
				layerOptions : {
					renderers : renderer,
					styleMap : styleMap,
					geodesic: true,
					units : "nmi",
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

	var switcherControl = new OpenLayers.Control.LayerSwitcher();
	map.addControl(switcherControl);
	//switcherControl.maximizeControl();

	map.zoomToExtent(mapBounds.transform(map.displayProjection,	map.projection));
	map.zoomTo(10);
	$("#noneToggle").checked = true;

	
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
		return "../js/img/blank.gif";
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
	if (mapBounds.intersectsBounds(bounds) && z >= mapMinZoom
			&& z <= mapMaxZoom) {
		return this.url + z + "/" + x + "/" + y + "." + this.type;
	} else {
		return "../js/img/blank.gif";
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
	map.style.height = getWindowHeight()  + "px";
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
	//convert to nautical miles
	var measure = event.measure * 0.539957;
	
	var element = zk.Widget.$("$output");
	var out = "";
	if (order == 1) {
		out += measure.toFixed(3) + " Nm";
	} else {
		out += measure.toFixed(3) + " Nm" + "<sup>2</sup>";
	}
	element.setValue(out);
}

function toggleControl(cmd) {
	for (key in measureControls) {
		var control = measureControls[key];
		if (cmd == key ) {
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

