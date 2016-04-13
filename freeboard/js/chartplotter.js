
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

//objects of global interest
var map;
var shipMarker;
var hdgLayer;
var bearingLayer;
var trackLayer;
var wgpxLayer;
var gotoLayer;
var baseLayers;
var overlays;
var layers;
var drawnItems;
var trackLine;
var aisGroup;
var aisList = new Array();

//vars of global interest
var followBoat = false;
var lat = 0.0;
var lon = 0.0;
var heading = 0.0;
var speed = 0.0;
var declination = 0.0;
var trackCount = 0;
var moveCount = 0;

var ONE_KNOT_LAT=(1000 / 40075017) * 360;

function initCharts() {
	//
	var firstLat = zk.Widget.$("$firstLat").getValue();
	var firstLon = zk.Widget.$("$firstLon").getValue();
	var firstZoom = zk.Widget.$("$firstZoom").getValue();
	console.log(firstLat+","+firstLon+","+firstZoom);
/*
	console.log("Set LatLon to Neusse River");
	firstLat = 35.077796;
	firstLon=-77.00753;
	firstZoom=11;
	console.log(firstLat+","+firstLon+","+firstZoom);
*/
	map = L.map('map', {
		//attributionControl: false,
	}).setView(new L.LatLng(firstLat,firstLon),firstZoom,true);

	addLayers(map);

	var zoomControl = new L.Control.Zoom({
		position : 'topright',
	});
	map.addControl(zoomControl);

	var measureControl = new L.Control.Measure({
		position : 'topright',
	});
	map.addControl(measureControl);

	// Initialize the FeatureGroup to store editable layers
	drawnItems = new L.FeatureGroup();
	map.addLayer(drawnItems);

	// Waypoints
	refreshWaypoints();

	//add local trackline
	trackLine = L.polyline([], {color: 'red',smoothFactor: 0.001}).addTo(map);

	// Track
	refreshTrack();


	// Initialize the draw control and pass it the FeatureGroup of editable
	// layers
	var drawControl = new L.Control.Draw({
		position : 'topright',
		draw : {
			polyline : {
				title : 'Create a route'
			},
			marker : {
				title : 'Add a waypoint'
			},
			polygon : false,
			rectangle : false,
			circle : false,
		},

		edit : {
			featureGroup : drawnItems,
			edit: { title: 'Move waypoints' },
			remove: false
		}
	});
	map.addControl(drawControl);

	L.control.mousePosition({position: 'bottomright'}).addTo(map);

	// ship
	var myIcon = L.icon({
		iconUrl : './js/img/ship_red.png',
		iconSize : [ 10, 24 ],
		iconAnchor : [ 5, 10 ],
	});
	shipMarker = L.marker([ 0.0, 0.0 ], {
		icon : myIcon
	}).addTo(map);
	layers.addOverlay(shipMarker, "Boat");

	hdgLayer = L.polyline([ new L.LatLng(0.0, 0.0), new L.LatLng(0.0, 0.0) ], {
		color : 'black',
		weight : 1,
		dashArray : '5,5',
	}).addTo(map);
	layers.addOverlay(hdgLayer, "Heading");

	bearingLayer = L.polyline(
			[ new L.LatLng(0.0, 0.0), new L.LatLng(0.0, 0.0) ], {
				color : 'green',
				weight : 3,
				opacity : 1,
				fillOpacity : 1,
			}).addTo(map);
	layers.addOverlay(bearingLayer, "Bearing");

	// add waypoints
	map.on('draw:created', function(e) {
		var type = e.layerType, layer = e.layer;

		if (type === 'marker') {
			zAu.send(new zk.Event(zk.Widget.$("$this"), 'onWaypointCreate',
					new Array(layer.getLatLng().lat, layer.getLatLng().lng,
							lat, lon)));
		}
		drawnItems.addLayer(layer);
	});

	//move waypoints
	map.on('draw:edited', function(e) {
		//var type = e.layerType, layers = e.layers;
		//console.log(layers);
		// loop through and send to backend
		var edits = new Array();
		jQuery.each(layers, function(i, val) {
			edits.push(val[0]);
			edits.push(val[1]);
			edits.push(val[2]);
			edits.push(val[3]);
		});
		zAu.send(new zk.Event(zk.Widget.$("$this"), 'onWaypointMove', edits));
	});
	//ais
	aisGroup = new L.LayerGroup();
	map.addLayer(aisGroup);

	map.on('zoomend', function(e) {
		zAu.send(new zk.Event(zk.Widget.$("$this"), 'onChartChange', new Array(map.getCenter().lat,map.getCenter().lng, map.getZoom())));
	});

	map.on('dragend', function(e) {
		zAu.send(new zk.Event(zk.Widget.$("$this"), 'onChartChange', new Array(map.getCenter().lat,map.getCenter().lng, map.getZoom())));
	});
	// track layers visibility
	map.on('layeradd', function(e){
		jQuery.each(layers._layers, function(i, n){
			if(e.layer==n.layer){
				zAu.send(new zk.Event(zk.Widget.$("$this"), 'onLayerChange', new Array(
						n.name, "true")));
			}
		});
	});
	map.on('layerremove', function(e){
		jQuery.each(layers._layers, function(i, n){
			if(e.layer==n.layer){
				zAu.send(new zk.Event(zk.Widget.$("$this"), 'onLayerChange', new Array(
						n.name, "false")));
			}
		});
	});

	setLayerVisibility();
}




// set layer visibility
function setLayerVisibility(){
	var vis = zk.Widget.$("$layerVisibility").getValue().split(';');
	jQuery.each(vis, function(i, data) {
		var lyr = data.split("=");
		//console.log(lyr);
		if (lyr[0].length > 0) {
			jQuery.each(layers._layers, function(i, n){
					if(n.name == lyr[0]){
						if (lyr[1] === 'false') {
							if(map.hasLayer(n.layer))map.removeLayer(n.layer);
						} else {
							if(!map.hasLayer(n.layer))map.addLayer(n.layer);
						}
					}
			});

		}
	});
}


/**
 * Set the current position, moving vessel, bearing and track, and associated stuff
 * @param llat
 * @param llon
 * @param brng - true north
 * @param spd
 */
function setPosition(llat, llon, brng, spd) {
	//console.log("Chartplotter:setPos:to lat:"+llat);
	if (llat > 0) {
		eLat.setValue(llat.toFixed(5) + ' N');
	} else {
		eLat.setValue(Math.abs(llat.toFixed(5)) + ' S');
	}

	if (llon > 0) {
		eLon.setValue(llon.toFixed(5) + ' E');
	} else {
		eLon.setValue(llon.toFixed(5) + ' W');
	}
	if(map.hasLayer(shipMarker)){
		//console.log("Chartplotter:moveBoat to "+llat+","+llon);
		shipMarker.setLatLng(new L.LatLng(llat, llon));
		shipMarker.setIconAngle(brng);
	}
	// ref http://www.movable-type.co.uk/scripts/latlong.html
	var start_point = new L.LatLng(llat, llon);
	// //1852 meters in nautical mile
	//
	var end_point = destVincenty(llat, llon, brng, spd * 1852);
	var end_point2 = destVincenty(llat, llon, brng, 185200);
	if(map.hasLayer(hdgLayer)){
		hdgLayer.setLatLngs([ start_point, end_point2 ]);
	}
	if(map.hasLayer(bearingLayer)){
		bearingLayer.setLatLngs([ start_point, end_point ]);
	}
	// add to tracks
	trackLine.addLatLng(new L.LatLng(llat, llon));
}

/**
 * Ask the server to perform a goto command
 * @param toLat
 * @param toLon
 */
function requestGotoDestination(toLat, toLon){
	zAu.send(new zk.Event(zk.Widget.$("$this"), 'onRequestGoto', [toLat,toLon,lat,lon]));
}
/**
 * Request editing a marker
 * @param toLat
 * @param toLon
 */
function requestMarkerEdit(toLat, toLon){
	zAu.send(new zk.Event(zk.Widget.$("$this"), 'onWaypointEdit', [toLat,toLon,lat,lon]));
}
/**
 * Request marker delete
 * @param toLat
 * @param toLon
 */
function requestMarkerDelete(toLat, toLon){
	zAu.send(new zk.Event(zk.Widget.$("$this"), 'onWaypointDelete', [toLat,toLon,lat,lon]));
}

 /*
 * Set the goto destination and draw the line
 */
function setGotoDestination(toLat, toLon, fromLat, fromLon) {
	if(gotoLayer){
		map.removeLayer(gotoLayer);
		gotoLayer=null;
	}
	if(toLat && toLon){
		var start_point = new L.LatLng(fromLat, fromLon);
		var end_point = new L.LatLng(toLat, toLon);
		gotoLayer = L.polyline(
				[ start_point, end_point ], {
					color : 'yellow',
					weight : 3,
					opacity : .5,
					fillOpacity : .5,
				}).addTo(map);
	}
}
/*
 * Center the boat in the screen
 */
function centerBoat() {
	centerOnBoat = true;
	map.panTo(new L.LatLng(lat, lon));
}

/*
 * toggle on/off follow boat mode
 */
function followBoatPosition() {
	followBoat = !followBoat;
}

/*
 * Move the chart so the boat is centered. Called when follow boat = true
 */
function moveToBoatPosition(llat, llon) {
	// every 10 moves
	if (followBoat && moveCount > 10) {
		moveCount = 0;
		map.panTo(new L.LatLng(llat, llon));
	} else {
		moveCount++;
	}
}



/*
 * Reload the GPXTrack layer, then clear the Track layer to start again Called
 * every 5 minutes or so.
 */
function refreshTrack() {
	if (trackLayer) {
		map.removeLayer(trackLayer);
		layers.removeLayer(trackLayer);
		//clear most of the points from trackLine
		if(trackLine.getLatLngs().length > 500){
			trackLine.spliceLatLngs(0,trackLine.getLatLngs().length-500);
		}
	}
	// GPX track
	// URL to your GPX file
	var trkUrl = "../tracks/current.gpx";
	trackLayer = new L.GPX(trkUrl, {
		async : true
	}).addTo(map);

	layers.addOverlay(trackLayer, "Track");
}
//
/*
 * Refresh waypoints, called by server after adding/editing a waypoint
 */
function refreshWaypoints() {
	// remove if we have one
	if (wgpxLayer) {
		map.removeLayer(wgpxLayer);
		layers.removeLayer(wgpxLayer);
		//also remove from drawnItems where they were added by createEvent
		drawnItems.eachLayer(function (layer) {
			//only waypoints
			if(layer instanceof L.Marker){
		    	drawnItems.removeLayer(layer);
			}
		});

	}
	// URL to your GPX file
	wgpxLayer = new L.GPX("../tracks/waypoints.gpx", {
		async : true
	}).addTo(map);

	layers.addOverlay(wgpxLayer, "Waypoints");
}

function refreshAis(ais){
	//{"AIS":{"position":{"latitude":37.839843333333334,"longitude":-122.44135666666666,"latitudeAsString":"37 50.391N","longitudeAsString":"122 26.481W"},"navStatus":0,"rot":128,"sog":240,"cog":1436,"trueHeading":511,"utcSec":25,"userId":366985330}}
	//aisGroup.clearLayers();
	var now = new Date().getTime();
	//10 min max
	now=now-600000;
	var found;
	aisGroup.eachLayer(function (layer) {
			if (layer.options.mmsi === ais.userId){
				//updatemarker.options.course = (ais.cog)/10;
				layer.options.status = ais.navStatus;
				layer.setIconAngle(marker.options.course - 90);
				found="true";
			}
			//cleanup after 10 min.
			if(ais.received < now){
				aisGroup.removeLayer(layer);
			}
		});
	if(found)return;
	//new one here
	var lat = ais.position.latitude;
	var lng = ais.position.longitude;
	var boatIcon = L.icon({
		iconUrl : './js/img/white_ship.png',
		iconSize : [ 24, 24 ],
		iconAnchor : [ 10, 10 ],
	});
	var marker = new L.Marker(new L.LatLng(lat,lng), { icon:boatIcon});
	marker.options.course = (ais.cog)/10;
	marker.options.status = ais.navStatus;
	marker.setIconAngle(marker.options.course - 90);
	marker.options.mmsi = ais.userId;
	marker.on('click', function(e) {
		var popup = new L.Popup({'minWidth': 350});
		popup.setLatLng(e.target._latlng);
		var name = "Unknown";
		if(ais.name)name=ais.name;
		var callsign = "Unknown";
		if(ais.callsign)callsign=ais.callsign;
		popup.setContent('MMSI: '+ais.userId+', Name: '+name+', Callsign: '+callsign+'<br/>SOG: '+ais.sog/10+', COG: '+ais.cog/10 +'<br/>True Heading: '+ais.trueHeading);
		map.openPopup(popup);
	});
	aisGroup.addLayer(marker);

}


//
function ChartPlotter() {
	this.onmessage = function(navObj) {
		//console.log("Chartplotter:"+mArray);
		var setPos = false;
		if (!navObj)
			return true;

		if (navObj.LAT) {
					lat = navObj.LAT;
					setPos = true;
			}
			if (navObj.LON) {
					lon = navObj.LON;
					setPos = true;
			}
			if (navObj.MGH) {
					heading = navObj.MGH - declination;
					if(heading<0){
						heading = heading +360;
					}
					setPos = true;
			}
			if (navObj.COG) {
				heading = navObj.COG;
				setPos = true;
			}
			if (navObj.SOG) {
					speed = navObj.SOG;
					setPos = true;
			}

			if (navObj.DEC) {
				declination = navObj.DEC;	
			}

			if (navObj.MGD) {
			declination = navObj.MGD;
			}
			if (navObj.WPC ) {
				// we refresh the waypoint layer
				refreshWaypoints();
			}
			if (navObj.WPG) {

				var coords = navObj.WPG;
				// console.log(coords);
				var coordsArray = coords.split('|');
				// console.log(coordsArray);
				// we refresh the goto layer
				if (coordsArray.length == 4) {
					// console.log("Setting goto =
					// "+coordsArray[0]+","+coordsArray[1]);
					setGotoDestination(parseFloat(coordsArray[0]),
							parseFloat(coordsArray[1]),
							parseFloat(coordsArray[2]),
							parseFloat(coordsArray[3]));
				} else {
					setGotoDestination(null, null, null, null);
				}
			}
			if (navObj.AIS ) {
				// we refresh the ais layer
				refreshAis(navObj.AIS);
			}

		if (setPos) {
			//console.log("Chartplotter:setPos");
			// avoid the 0,0 point
			if (lat < 0.001 && lat > -0.001 && lon < 0.001 && lon > -0.001)
				return;

			setPosition(lat, lon, heading, speed);
			// setTrack(lat,lon);
			moveToBoatPosition(lat, lon);
		}
	};

}
function posInit() {
	addSocketListener(new ChartPlotter());
	eLat = zk.Widget.$("$posLat");
	eLon = zk.Widget.$("$posLon");
	// //reload track every 5 min so the local track doesnt get too long
	setInterval("refreshTrack()",300000);
}


//++++++++++++++++++++++++++++++++++++++++++++++
//Code below added for bearing and heading calculation, copyright as stated
/*
 * ! JavaScript function to calculate the destination point given start point
 * latitude / longitude (numeric degrees), bearing (numeric degrees) and
 * distance (in m).
 *
 * Original scripts by Chris Veness Taken from
 * http://movable-type.co.uk/scripts/latlong-vincenty-direct.html and optimized /
 * cleaned up by Mathias Bynens <http://mathiasbynens.be/> Based on the Vincenty
 * direct formula by T. Vincenty, ¡ÈDirect and Inverse Solutions of Geodesics on
 * the Ellipsoid with application of nested equations¡É, Survey Review, vol XXII
 * no 176, 1975 <http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf>
 */
function toRad(n) {
	return n * Math.PI / 180;
};
function toDeg(n) {
	return n * 180 / Math.PI;
};
function destVincenty(lat1, lon1, brng, dist) {
	var a = 6378137, b = 6356752.3142, f = 1 / 298.257223563, // WGS-84
	// ellipsiod
	s = dist, alpha1 = toRad(brng), sinAlpha1 = Math.sin(alpha1), cosAlpha1 = Math
			.cos(alpha1), tanU1 = (1 - f) * Math.tan(toRad(lat1)), cosU1 = 1 / Math
			.sqrt((1 + tanU1 * tanU1)), sinU1 = tanU1 * cosU1, sigma1 = Math
			.atan2(tanU1, cosAlpha1), sinAlpha = cosU1 * sinAlpha1, cosSqAlpha = 1
			- sinAlpha * sinAlpha, uSq = cosSqAlpha * (a * a - b * b) / (b * b), A = 1
			+ uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq))), B = uSq
			/ 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq))), sigma = s
			/ (b * A), sigmaP = 2 * Math.PI;
	while (Math.abs(sigma - sigmaP) > 1e-12) {
		var cos2SigmaM = Math.cos(2 * sigma1 + sigma), sinSigma = Math
				.sin(sigma), cosSigma = Math.cos(sigma), deltaSigma = B
				* sinSigma
				* (cos2SigmaM + B
						/ 4
						* (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B
								/ 6 * cos2SigmaM
								* (-3 + 4 * sinSigma * sinSigma)
								* (-3 + 4 * cos2SigmaM * cos2SigmaM)));
		sigmaP = sigma;
		sigma = s / (b * A) + deltaSigma;
	}
	;

	var tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1, lat2 = Math
			.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1, (1 - f)
					* Math.sqrt(sinAlpha * sinAlpha + tmp * tmp)), lambda = Math
			.atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma
					* cosAlpha1), C = f / 16 * cosSqAlpha
			* (4 + f * (4 - 3 * cosSqAlpha)), La = lambda
			- (1 - C)
			* f
			* sinAlpha
			* (sigma + C
					* sinSigma
					* (cos2SigmaM + C * cosSigma
							* (-1 + 2 * cos2SigmaM * cos2SigmaM))), revAz = Math
			.atan2(sinAlpha, -tmp); // final bearing
	var llat = toDeg(lat2);
	var llon = lon1 + toDeg(La);
	return new L.LatLng(llat, llon);
};
