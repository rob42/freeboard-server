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


function resizeDepth(amount){
	if(amount==null){
		amount = zk.Widget.$('$depthScale').getValue();
	}else{
		amount=1+(1*amount);
	}
	if(amount==0.0)return;

var wsize = $("#canvasDepth").width();
	var hsize = $("#canvasDepth").height();

$("#canvasDepth").width(wsize*amount);
	$("#canvasDepth").height(hsize*amount);
/*
var wsmallSize =  $("#canvasWaypoint").width();
	var hsmallSize =  $("#canvasWaypoint").height();
	*/
/*	$("#canvasHeading").width(wsmallSize*amount);
	$("#canvasHeading").height(hsmallSize*amount);

	$("#canvasWaypoint").width(wsmallSize*amount);
	$("#canvasWaypoint").height(hsmallSize*amount);

var wLat =  $("#canvasLat").width();
	var hLat =  $("#canvasLat").height();
	$("#canvasLat").width(wLat*amount);
	$("#canvasLat").height(hLat*amount);
	$("#canvasLon").width(wLat*amount);
	$("#canvasLon").height(hLat*amount);
*/
	this.initDepth();

}
function Depth () {
	this.onmessage = function (navObj) {


			//avoid commands
			if(!navObj)return true;
			//depth
			if (navObj.DBT) {
					lcdDepth.setValue(navObj.DBT);
			}

	};

}



function initDepth() {
	//if we cant do canvas, skip out here!
	if(!window.CanvasRenderingContext2D)return;
	// Initialzing lcds
	// depth
   
   depthUnit = zk.Widget.$('$depthUnit').getValue();

	lcdDepth = new steelseries.DisplaySingle('canvasDepth', {
		// gaugeType : steelseries.GaugeType.TYPE4,
		//width : document.getElementById('canvasLog').width,
		//height : document.getElementById('canvasLog').height,
		lcdDecimals : 1,
		lcdColor: steelseries.LcdColor.BEIGE,
		headerString : "Depth",
		headerStringVisible : true,
//		detailString : "Avg: ",
//		detailStringVisible : true,
		unitString : depthUnit,
		unitStringVisible: true

	});

	addSocketListener(new Depth());
}

