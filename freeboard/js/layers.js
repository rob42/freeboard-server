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
var mapBounds = new OpenLayers.Bounds( 171.67265853743078, -41.37858426341804, 174.19399117138440, -40.10362112608023);
function addLayers(map) {

	var NZ46Bounds = new OpenLayers.Bounds( 173.86895223488963, -42.08918277388398, 175.46413452268570, -40.23603745695969);
	mapBounds.extend(NZ46Bounds );
	var NZ46 = new OpenLayers.Layer.TMS( "NZ46 Cook Strait", "../../mapcache/NZ46/",
		{ layername: '../../mapcache/NZ46/',
		type: 'png', 
		getURL: overlay_getTileURL, 
		alpha: true,
		isBaseLayer: true,
		visibility: false,
		//maxResolution : "auto",
		numZoomLevels: 18,
		minZoomLevel: 7,
		maxZoomLevel: 12,
		buffer : 0,
		});
	map.addLayer(NZ46);
	
	var NZ61Bounds = new OpenLayers.Bounds( 171.67265853743078, -41.37858426341804, 174.19399117138440, -40.10362112608023);
	mapBounds.extend(NZ61Bounds );
	var NZ61 = new OpenLayers.Layer.TMS("NZ61 Karamea River to Stephens Island", "../../mapcache/NZ61/", {
		layername : '../../mapcache/NZ61/',
		type : 'png',
		getURL : overlay_getTileURL,
		alpha : true,
		isBaseLayer : false,
		//maxResolution : "auto",
		numZoomLevels : 18,
		minZoomLevel : 7,
		maxZoomLevel : 12,
		buffer : 0,
	
	});
	map.addLayer(NZ61);
		
	var NZ615Bounds = new OpenLayers.Bounds( 173.70239532864488, -41.50082916444747, 174.54758837986532, -40.54536912665989);
	mapBounds.extend(NZ615Bounds );
	var NZ615 = new OpenLayers.Layer.TMS("NZ615 Marlborough Sounds", "../../mapcache/NZ615/", {
		layername : '../../mapcache/NZ615/',
		type : 'png',
		getURL : overlay_getTileURL,
		alpha : true,
		isBaseLayer : false,
		//maxResolution : "auto",
		numZoomLevels : 18,
		minZoomLevel : 8,
		maxZoomLevel : 13,
		buffer : 0,
		
	});
	map.addLayer(NZ615);
	
	var NZ614 = new OpenLayers.Layer.TMS("NZ614 Tasman Bay",
			"../../mapcache/NZ614/", { // url: '', serviceVersion: '.',
				layername : '../../mapcache/NZ614/',
				type : 'png',
				getURL : overlay_getTileURL,
				alpha : true,
				isBaseLayer : false,
				visibility: false,
				numZoomLevels : 18,
				//maxResolution: "auto",
				minZoomLevel : 8,
				maxZoomLevel : 13,
				buffer : 0,
			});
	map.addLayer(NZ614);
	
	var NZ6144 = new OpenLayers.Layer.TMS("NZ6144 Abel Tasman",
			"../../mapcache/NZ6144/", { // url: '', serviceVersion: '.',
				layername : '../../mapcache/NZ6144/',
				type : 'png',
				getURL : overlay_getTileURL,
				alpha : true,
				isBaseLayer : false,
				visibility: false,
				numZoomLevels: 18 ,
				//maxResolution : "auto",
				minZoomLevel : 9,
				maxZoomLevel : 15,
				buffer : 0,
			});
	map.addLayer(NZ6144);
	
	var NZ6142_1 = new OpenLayers.Layer.TMS("NZ6142_1 Nelson Harbour",
			"../../mapcache/NZ6142_1/", { // url: '', serviceVersion: '.',
				layername : '../../mapcache/NZ6142_1/',
				type : 'png',
				getURL : overlay_getTileURL,
				alpha : true,
				isBaseLayer : false,
				visibility: false,
				numZoomLevels: 18 ,
				//maxResolution : "auto",
				minZoomLevel : 10,
				maxZoomLevel : 15,
				buffer : 0,
			});
	map.addLayer(NZ6142_1);
	
	var NZ6142_2 = new OpenLayers.Layer.TMS("NZ6142_2 Port Nelson",
			"../../mapcache/NZ6142_2/", { // url: '', serviceVersion: '.',
				layername : '../../mapcache/NZ6142_2/',
				type : 'png',
				getURL : overlay_getTileURL,
				alpha : true,
				isBaseLayer : false,
				visibility: false,
				numZoomLevels: 18 ,
				//maxResolution : "auto",
				minZoomLevel : 12,
				maxZoomLevel : 17,
				buffer : 0,
			});
	map.addLayer(NZ6142_2);
	// create TMS Overlay layer
	var NZ614_1 = new OpenLayers.Layer.TMS("NZ614_1 Port Motueka",
			"../../mapcache/NZ614_1/", { // url: '', serviceVersion: '.',
				layername : '../../mapcache/NZ614_1/',
				type : 'png',
				getURL : overlay_getTileURL,
				alpha : true,
				isBaseLayer : false,
				visibility: false,
				numZoomLevels: 18 ,
				//maxResolution : "auto",
				minZoomLevel : 12,
				maxZoomLevel : 18,
				buffer : 0,
			});
	map.addLayer(NZ614_1);
	var NZ6151_1 = new OpenLayers.Layer.TMS("NZ6151_1 ",
			"../../mapcache/NZ6151_1/", {
				layername : '../../mapcache/NZ6151_1/',
				type : 'png',
				getURL : overlay_getTileURL,
				alpha : true,
				isBaseLayer : false,
				visibility: false,
				numZoomLevels : 18,
				//maxResolution : "auto",
				minZoomLevel : 10,
				maxZoomLevel : 15,
				buffer : 0,
			});
	map.addLayer(NZ6151_1);
	// if (OpenLayers.Util.alphaHack() == false) { tmsoverlay.setOpacity(0.7); }

}