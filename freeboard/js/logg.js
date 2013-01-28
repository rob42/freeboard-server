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

odoValue = 99998.2;
function resizeLog(amount){
	var wsize = $("#canvasLog").width();
	var hsize = $("#canvasLog").height();
	$("#canvasLog").width(wsize+(wsize*amount));
	$("#canvasLog").height(hsize+(hsize*amount));
	var wsmallSize =  $("#canvasWaypoint").width();
	var hsmallSize =  $("#canvasWaypoint").height();
	$("#canvasHeading").width(wsmallSize+(wsmallSize*amount));
	$("#canvasHeading").height(hsmallSize+(hsmallSize*amount));
	$("#canvasWaypoint").width(wsmallSize+(wsmallSize*amount));
	$("#canvasWaypoint").height(hsmallSize+(hsmallSize*amount));
	var wLat =  $("#canvasLat").width();
	var hLat =  $("#canvasLat").height();
	$("#canvasLat").width(wLat+(wLat*amount));
	$("#canvasLat").height(hLat+(hLat*amount));
	$("#canvasLon").width(wLat+(wLat*amount));
	$("#canvasLon").height(hLat+(hLat*amount));
	this.initLogg();
	
}
function Logg () {
	this.onmessage = function (mArray) {
		//var mArray=m.data.split(",");
		jQuery.each(mArray, function(i, data) {

			if (data && data.indexOf('LAT:') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					if(c>0){
						lcdLat.setValue(c.toFixed(5)+' N');
					}else{
						lcdLat.setValue(Math.abs(c.toFixed(5))+' S');
					}
				}
				
			}
			if (data && data.indexOf('LON:') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					if(c>0){
						lcdLon.setValue(c.toFixed(5)+' E');
					}else{
						lcdLon.setValue(c.toFixed(5)+' W');
					}
				}
	
			}
			if (data && data.indexOf('SOG:') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					lcdLog.setValue(c);
				}
	
			}
			if (data && data.indexOf('MGH:') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					lcdHeading.setValue(c);
				}
			}
			if (data && data.indexOf('YAW:') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					// -180 <> 180
					if (c >= 179) {
						lcdWaypoint.setValue(-(360 - c));
					} else {
						lcdWaypoint.setValue(c);
					}
				}
			}
		});
	}
	
}



function initLogg() {

	// Initialzing lcds
	// log
	lcdLat = new steelseries.DisplaySingle('canvasLat', {
		// gaugeType : steelseries.GaugeType.TYPE4,
		width : document.getElementById('canvasLat').width,
		height : document.getElementById('canvasLat').height,
		lcdDecimals : 5,
		lcdColor: steelseries.LcdColor.BEIGE,
		//unitString:"",
		unitStringVisible: false,
		valuesNumeric: false	

	});
	lcdLon = new steelseries.DisplaySingle('canvasLon', {
		// gaugeType : steelseries.GaugeType.TYPE4,
		width : document.getElementById('canvasLon').width,
		height : document.getElementById('canvasLon').height,
		lcdDecimals : 5,
		lcdColor: steelseries.LcdColor.BEIGE,
		//unitString:"",
		unitStringVisible: false,
		valuesNumeric: false

	});
	
	// log
	lcdLog = new steelseries.DisplayMulti('canvasLog', {
		// gaugeType : steelseries.GaugeType.TYPE4,
		width : document.getElementById('canvasLog').width,
		height : document.getElementById('canvasLog').height,
		lcdDecimals : 1,
		lcdColor: steelseries.LcdColor.BEIGE,
		headerString : "Knots",
		headerStringVisible : true,
		detailString : "Avg: ",
		detailStringVisible : true,
	// unitString:"Knts",
	// unitStringVisible: true

	});
	
	// heading
	lcdHeading = new steelseries.DisplayMulti('canvasHeading', {
		width : document.getElementById('canvasHeading').width,
		height : document.getElementById('canvasHeading').height,
		lcdDecimals : 0,
		lcdColor: steelseries.LcdColor.BEIGE,
		headerString : "Heading(M)",
		headerStringVisible : true,
		detailString : "Avg(M): ",
		detailStringVisible : true,
	});

	// waypoint
	lcdWaypoint = new steelseries.DisplayMulti('canvasWaypoint', {
		width : document.getElementById('canvasWaypoint').width,
		height : document.getElementById('canvasWaypoint').height,
		lcdDecimals : 0,
		lcdColor: steelseries.LcdColor.BEIGE,
		headerString : "To Waypoint",
		headerStringVisible : true,
		detailString : "ETA: ",
		detailStringVisible : true,
	});
	lcdWaypoint.setValue(0);


wsList.push(new Logg());
}