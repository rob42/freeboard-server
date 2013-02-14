/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 * 
 * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
 *
 *  FreeBoard is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  FreeBoard is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with FreeBoard.  If not, see <http://www.gnu.org/licenses/>.
 */
//var map;
var mapMinZoom = 0;
var mapMaxZoom = 18;
var lat=0.0;
var lon=0.0;
var heading=0.0;
var speed=0.0;
var declination=0.0;
var trackCount=0;
var moveCount=0;

var followBoat=false;

var chartProjection = new OpenLayers.Projection("EPSG:900913");
var screenProjection = new OpenLayers.Projection("EPSG:4326");
var shipMarker = new OpenLayers.Layer.Vector('Ship', {renderers: ['Canvas', 'SVG', 'VML'],
    styleMap: new OpenLayers.StyleMap({
        externalGraphic: './js/img/ship_red.png',
        graphicWidth: 10, graphicHeight: 24, graphicYOffset: -10,
        title: '${tooltip}',
        rotation: '${angle}',
    })
});
var shipTrack = new OpenLayers.Layer.Vector('Track', {renderers: ['Canvas', 'SVG', 'VML'],
    styleMap: new OpenLayers.StyleMap({
        strokeWidth : 2,
		strokeOpacity : 1,
		strokeColor : "#FF0066",
    })
});
var trackLine = null;


var hdgLayer = new OpenLayers.Layer.Vector('Heading', {renderers: ['Canvas', 'SVG', 'VML'],
	styleMap: new OpenLayers.StyleMap({
		strokeWidth : 2,
		strokeOpacity : 1,
		strokeColor : "#FF0066",
    })
});
var bearingLayer = new OpenLayers.Layer.Vector('Bearing', {renderers: ['Canvas', 'SVG', 'VML'],
	styleMap: new OpenLayers.StyleMap({
		strokeWidth : 1,
		strokeOpacity : 1,
		strokeColor : "#000000",
		strokeDashstyle : "dash",
    })
});

var gotoLayer = new OpenLayers.Layer.Vector('Go To Waypoint', {renderers: ['Canvas', 'SVG', 'VML'],
	styleMap: new OpenLayers.StyleMap({
		strokeWidth : 5,
		strokeOpacity : 0.4,
		strokeColor : "#FFFF00",
		//strokeDashstyle : "dash",
    })
});

//Add the Layer with the GPX Track
var tgpx = new OpenLayers.Layer.Vector("GPX track", {renderers: ['Canvas', 'SVG', 'VML'],
	strategies: [new OpenLayers.Strategy.Fixed()],
	protocol: new OpenLayers.Protocol.HTTP({
		url: "../../tracks/current.gpx",
		format: new OpenLayers.Format.GPX({extractTracks: true, extractWaypoints: false,  
			extractName: true, extractRoutes: false, extractAttributes: false})
	}),
	style: {pointRadius: 5, fillColor: "darkred", strokeColor: "red", strokeWidth: 2, strokeOpacity: 0.7},
	projection: new OpenLayers.Projection("EPSG:4326")
});

// Add the Layer with the GPX Waypoints
var wgpx = new OpenLayers.Layer.Vector("Waypoints", {renderers: ['Canvas', 'SVG', 'VML'],
	strategies: [new OpenLayers.Strategy.Fixed()],
	protocol: new OpenLayers.Protocol.HTTP({
		url: "../../tracks/waypoints.gpx",
		format: new OpenLayers.Format.GPX({extractTracks: false, extractWaypoints: true,  
			extractName: true, extractRoutes: false, extractAttributes: true})
	}),
	styleMap: new OpenLayers.StyleMap({
        externalGraphic: './js/img/marker-blue.png',
        graphicWidth: 15, graphicHeight: 20, graphicYOffset: -20, graphicXOffset: -8,
        title: '${name}'
    }),
	projection: new OpenLayers.Projection("EPSG:4326")
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
	
	//add GPX track
	map.addLayer(tgpx);
	
	//add waypoint layer
	map.addLayer(wgpx);
	
	//for waypoints and editing routes
	//http://openlayers.org/dev/examples/modify-feature.html
	//got layer
	map.addLayer(gotoLayer);
	//add the ship and bearings
	map.addLayer(bearingLayer);
	map.addLayer(hdgLayer);
	map.addLayer(shipTrack);
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
	
	//select controls
	function onPopupClose(evt) {
        selectControl.unselect(selectedFeature);
    }
	
	function onFeatureSelect(feature) {
		selectedFeature = feature;
		if(zk.Widget.$("$wptToggle").isChecked()){
				var position = feature.geometry.getBounds().getCenterLonLat();
		        var lonlat = map.getLonLatFromPixel(position);
		        var wptLocation = lonlat.transform(chartProjection, screenProjection );
		        console.log("wpt:"+lat+","+lon);
	        zAu.send(new zk.Event(zk.Widget.$("$this"), 'onWaypoint', new Array(wptLocation.lat,wptLocation.lon,lat,lon,feature.attributes.name)));
	        selectControl.unselect(selectedFeature);
		}else {
	        //selectedFeature = feature;
	        popup = new OpenLayers.Popup.FramedCloud("chicken", 
	                                 feature.geometry.getBounds().getCenterLonLat(),
	                                 null,
	                                 "<div style='font-size:.8em'>Waypoint: " + feature.attributes.name
	                                 +"<br>"+feature.attributes.desc
	                                 +"</div>",
	                                 null, true, onPopupClose);
	        feature.popup = popup;
	        map.addPopup(popup);
		}
    }
    function onFeatureUnselect(feature) {
        map.removePopup(feature.popup);
        feature.popup.destroy();
        feature.popup = null;
    } 
	
	var selectControl = new OpenLayers.Control.SelectFeature(wgpx,
             {onSelect: onFeatureSelect, onUnselect: onFeatureUnselect});
	
	map.addControl(selectControl);
	selectControl.activate();
	
	//add waypoints
	map.events.register("click", map, function(e) {
		if(zk.Widget.$("$wptToggle").isChecked()){
	        var position = this.events.getMousePosition(e);
	          
	        var lonlat = map.getLonLatFromPixel(position);
	        var wptLocation = lonlat.transform(chartProjection, screenProjection );
	    
	        zAu.send(new zk.Event(zk.Widget.$("$map"), 'onWaypoint', new Array(wptLocation.lat,wptLocation.lon,lat,lon)));
		}
    });
	
	//track map moving and zooming
	var followBoatCount =0;
	map.events.register("moveend", map, function() {
		 if(followBoat){
			 //increment count, we only save move event every 100 moves if we are following the boat
			 if(followBoatCount>100){
				 followBoatCount=0;
			 }else{
				 followBoatCount++;
				 //outa here
				 return;
			 }
		 }
		var chartLocation = map.getCenter().transform(chartProjection, screenProjection );
        //console.log("zoomEnd:"+chartLocation.getCenter().lat+","+chartLocation.lon+","+map.getZoom());
        zAu.send(new zk.Event(zk.Widget.$("$this"), 'onChartChange', new Array(chartLocation.lat,chartLocation.lon,map.getZoom())));
    });
	
	//track layers
	map.events.register('changelayer', null, function(evt){
       if(evt.property === "visibility") {
    	   //console.log(evt.layer.name + " layer visibility changed to " +	evt.layer.visibility );
    	   zAu.send(new zk.Event(zk.Widget.$("$this"), 'onLayerChange', new Array(evt.layer.name,evt.layer.visibility)));
       }
   });
	
	var switcherControl = new OpenLayers.Control.LayerSwitcher();
	map.addControl(switcherControl);
	//switcherControl.maximizeControl();

	//set layer visibility
	var vis = zk.Widget.$("$layerVisibility").getValue().split(';');
	jQuery.each(vis, function(i, data) {
		var lyr = data.split("=");
		if(lyr[0].length>0){
			var curLayer=map.getLayersByName(lyr[0]);
			//console.log("Check layer:"+lyr);
			//console.log("Found layer:"+curLayer[0].name);
			if(lyr[1]==='false'){
				curLayer[0].setVisibility(false);
			}else{
				curLayer[0].setVisibility(true);
			}
		}
	});
	//zoom to last pos and zoom
	map.zoomToExtent(mapBounds.transform(map.displayProjection,	map.projection));
	map.moveTo(new OpenLayers.LonLat(zk.Widget.$("$firstLon").getValue(),zk.Widget.$("$firstLat").getValue()).transform(screenProjection, chartProjection));
	map.zoomTo(zk.Widget.$("$firstZoom").getValue());
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
	var order = event.order;
	//convert to nautical miles
	var measure = event.measure * 0.539957;
	
	var eOutput = zk.Widget.$("$output");
	var out = "";
	if (order == 1) {
		out += measure.toFixed(3) + " Nm";
	} else {
		out += measure.toFixed(3) + " Nm" + "<sup>2</sup>";
	}
	eOutput.setValue(out);
}

/*
 * toggles  nav and measure mode
 */
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



var eLat = null;
var eLon = null;
function setPosition(llat, llon, brng, spd){
	
	
	if(llat>0){
		eLat.setValue(llat.toFixed(5)+' N');
	}else{
		eLat.setValue(Math.abs(llat.toFixed(5))+' S');
	}
	
	if(llon>0){
		eLon.setValue(llon.toFixed(5)+' E');
	}else{
		eLon.setValue(llon.toFixed(5)+' W');
	}
	 shipMarker.removeAllFeatures();
	 hdgLayer.removeAllFeatures();
	 bearingLayer.removeAllFeatures();
	// The location of our marker and popup. We usually think in geographic
    // coordinates ('EPSG:4326'), but the map is projected ('EPSG:3857').
    var shipLocation = new OpenLayers.Geometry.Point(llon, llat) //new OpenLayers.Geometry.Point(lonLat);
        .transform(screenProjection, chartProjection);
   
    shipMarker.addFeatures([
        new OpenLayers.Feature.Vector(shipLocation, {angle: brng, tooltip: 'Motu'})
    ]);
    
   
    // ref  http://www.movable-type.co.uk/scripts/latlong.html
    var start_point = new OpenLayers.LonLat(llon,llat);//.transform(screenProjection, chartProjection);
    //1852 meters in nautical mile
    
    var end_point = OpenLayers.Util.destinationVincenty(start_point,brng,spd*1852);
    var end_point2 = OpenLayers.Util.destinationVincenty(start_point,brng,185200);
    hdgLayer.addFeatures([
               new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString([
                   new OpenLayers.Geometry.Point(start_point.lon, start_point.lat).transform(screenProjection, chartProjection),
                   new OpenLayers.Geometry.Point(end_point.lon,end_point.lat).transform(screenProjection, chartProjection),
               ]))
         ]);
    bearingLayer.addFeatures([
                   new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString([
                       new OpenLayers.Geometry.Point(start_point.lon, start_point.lat).transform(screenProjection, chartProjection),
                       new OpenLayers.Geometry.Point(end_point2.lon,end_point2.lat).transform(screenProjection, chartProjection),
                   ]))
         ]);
}

/*
 * Set the goto destination and draw the line
 */
function setGotoDestination(toLat, toLon,fromLat,fromLon){
	gotoLayer.removeAllFeatures();
	if(toLat!=null && toLon!=null){
		var gotoStartPoint = new OpenLayers.LonLat(fromLon,fromLat);//.transform(screenProjection, chartProjection);
	    var gotoEndPoint = new OpenLayers.LonLat(toLon,toLat);
	    
	    gotoLayer.addFeatures([
	                  new OpenLayers.Feature.Vector(new OpenLayers.Geometry.LineString([
	                      new OpenLayers.Geometry.Point(gotoStartPoint.lon, gotoStartPoint.lat).transform(screenProjection, chartProjection),
	                      new OpenLayers.Geometry.Point(gotoEndPoint.lon,gotoEndPoint.lat).transform(screenProjection, chartProjection),
	                  ]))
	        ]);
	}
}
/*
 * Center the boat in the screen at zoom=10
 */
function centerBoat(){
	centerOnBoat=true;
	map.moveTo(new OpenLayers.LonLat(lon,lat).transform(screenProjection, chartProjection));
	map.zoomTo(10);
}

/*
 * toggle on/off follow boat mode
 */
function followBoatPosition(){
	followBoat=!followBoat;
}

/*
 * Move the chart so the boat is centered. Called when follow boat = true
 */
function moveToBoatPosition(llat, llon){
	 //every 10 moves
    if(followBoat && moveCount>10){
    	moveCount=0;
		map.moveTo(new OpenLayers.LonLat(llon,llat).transform(screenProjection, chartProjection));
	}else{
		moveCount++;
	}
}

/*
 * Add a point to the boat track, and draw to screen
 */
function setTrack(llat, llon){
	//add to tracks
    var trackPoint = new OpenLayers.Geometry.Point(llon, llat) //new OpenLayers.Geometry.Point(lonLat);
	.transform(screenProjection, chartProjection);

    if(trackLine==null){
    	trackLine = new OpenLayers.Geometry.LineString(new Array(trackPoint, trackPoint));
    	shipTrack.addFeatures([
    	                       new OpenLayers.Feature.Vector(trackLine, {})
    	                       ]);
    }else{
    	trackLine.addPoint(trackPoint);
    	shipTrack.redraw();
    	trackCount++;
    }
    //simplify every 100 points
    if(trackCount>100){
    	trackCount=0;
    	//console.log("TrackLine="+trackLine.getVertices().length);
    	//10 meters is 0.005 Nm, so we will use 20M for now
    	trackLine=trackLine.simplify(0.2);
    	shipTrack.removeAllFeatures();
    	shipTrack.addFeatures([
    	                       new OpenLayers.Feature.Vector(trackLine, {})
    	                       ]);
    	//console.log("TrackLine="+trackLine.getVertices().length);
    }
}

/*
 * Reload the GPXTrack layer, then clear the Track layer to start again
 * Called every 5 minutes or so.
 */
function refreshTrack(){
	tgpx.refresh();
	var trackPoints = shipTrack.features[0].geometry.getVertices();
	if(trackPoints.length>60){
		var tpPoint=trackPoints.slice[trackPoints.length-60,59];
		shipTrack.removeAllFeatures();
		trackLine = new OpenLayers.Geometry.LineString(tpPoint);
		shipTrack.addFeatures([
		                       new OpenLayers.Feature.Vector(trackLine, {})
		                       ]);
	}
}

/*
 * Refresh waypoints, called by server after adding/editing a waypoint
 */
//function refreshWaypoints(){
//	wgpx.refresh();
//}

function ChartPlotter () {
	this.onmessage = function (mArray) {
		
		var setPos=false;
		jQuery.each(mArray, function(i, data) {
			
			//avoid commands
			if(data && data.indexOf('#')>=0)return true;
			
			if (data && data.indexOf('LAT') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					lat=c;
					setPos=true;
				}
				c=null;
			}
			if (data && data.indexOf('LON') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					lon=c;
					setPos=true;
				}
				c=null;
			}
			if (data && data.indexOf('MGH') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					heading=c;
					setPos=true;
				}
				c=null;
			}
			if (data && data.indexOf('SOG') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					speed=c;
					setPos=true;
				}
				c=null;
			}
			if (data && data.indexOf('MGD') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					declination=c;
				}
				c=null;
			}
			data=null;
		});
		if(setPos){
			setPosition(lat,lon, heading+declination, speed);
			setTrack(lat,lon);
			moveToBoatPosition(lat,lon);
		}
	};
	
}
function posInit(){
	addSocketListener(new ChartPlotter());
	eLat = zk.Widget.$("$posLat");
	eLon = zk.Widget.$("$posLon");
	//reload track every 5 min so the local track doesnt get too long
	setInterval("refreshTrack()",300000);
}

