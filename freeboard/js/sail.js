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
//var  radialWindTrue, radialWindDirTrue


/*
 * // Fuel gauge constants
	public static final String FUEL_REMAINING = "FFV";
	public static final String FUEL_USED = "FFU";
	public static final String FUEL_USE_RATE = "FFR";

	public static final String ENGINE_RPM = "RPM";
	public static final String ENGINE_HOURS = "EHH";
	public static final String ENGINE_MINUTES = "EMM";
	public static final String ENGINE_TEMP = "ETT";
	public static final String ENGINE_VOLTS = "EVV";
	public static final String ENGINE_OIL_PRESSURE = "EPP";
	public static final String DEPTH_BELOW_TRANSDUCER = "DBT";
 */
var avgArrayA = [ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
		0.0, 0.0, 0.0 ];
var avgPosA = 0;
var avgArrayT = [ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
		0.0, 0.0, 0.0 ];
var avgPosT = 0;

function Sail () {
	this.onmessage = function (navObj) {

			//avoid commands
			if (!navObj)
				return true;

			//depth
			if (navObj.DBT) {
					linearDepth.setValue(navObj.DBT);
			}
			//speed
			if (navObj.SOG) {
					radialBoatSpeed.setValue(navObj.SOG);
			}
	};

}

//setup gauge scales here

//colours
var gaugeLcdColor = steelseries.LcdColor.BEIGE;
//TILTED_BLACK
var gaugePointerType = steelseries.PointerType.TYPE4;
var gaugeBackgroundColor = steelseries.BackgroundColor.CARBON;
var gaugeFrameDesign = steelseries.FrameDesign.BLACK_METAL;

function initSail() {
	// Define some sections for gauges
	// Initialzing gauges
	//get the smallest distance
	var vpSize=Math.min(window.innerHeight,window.innerWidth);

	// Depth 
	//if we cant do canvas, skip out here!
	if(!window.CanvasRenderingContext2D)return;
   // 
	// Initialzing displays
   var tackAngle = 45;
	var areasCloseHaul = [
			steelseries.Section((0 - tackAngle), 0, 'rgba(0, 0, 220, 0.3)'),
			steelseries.Section(0, tackAngle, 'rgba(0, 0, 220, 0.3)') ];
	var areasCloseHaulTrue = [
			steelseries.Section((360 - tackAngle), 0, 'rgba(0, 0, 220, 0.3)'),
			steelseries.Section(0, tackAngle, 'rgba(0, 0, 220, 0.3)') ];

	// Initialzing gauges


	// wind dir apparent

	radialWindDirApp = new steelseries.WindDirection('sailWindDir', {
		// size : document.getElementById('canvasWindDirApp').width,
		titleString : "WIND          APP",
		size: vpSize*.5,
		lcdVisible : true,
		lcdColor : steelseries.LcdColor.BEIGE,
		pointSymbolsVisible : false,
		degreeScaleHalf : true,
		section : areasCloseHaul,
		area : areasCloseHaul,
		pointerTypeLatest : steelseries.PointerType.TYPE2,
		pointerTypeAverage : steelseries.PointerType.TYPE1,
		backgroundColor : steelseries.BackgroundColor.BROWN,
	});


   // depth
   
   depthUnit = zk.Widget.$('$depthUnit').getValue();
   var depthString = "Depth ("+depthUnit+" )";

	lcdDepth = new steelseries.DisplaySingle('sailDepth', {
		// gaugeType : steelseries.GaugeType.TYPE4,
		//width : document.getElementById('canvasLog').width,
		//height : document.getElementById('canvasLog').height,
		//width : vpSize*.4,
      height : vpSize*.35,
      lcdDecimals : 1,
		lcdColor: steelseries.LcdColor.BEIGE,
		headerString : depthString,
		headerStringVisible : true,
//		detailString : "Avg: ",
//		detailStringVisible : true,
		unitString : depthUnit,
		unitStringVisible: false
	});

	// log
	lcdLog = new steelseries.DisplayMulti('sailLog', {
		// gaugeType : steelseries.GaugeType.TYPE4,
		//width : document.getElementById('canvasLog').width,
		//height : document.getElementById('canvasLog').height,
		//width : vpSize*0.5,
      height : vpSize*.35,
		lcdDecimals : 1,
		lcdColor: steelseries.LcdColor.BEIGE,
		headerString : " Knots  ",
		headerStringVisible : true,
		detailString : "Avg: ",
		detailStringVisible : true
	// unitString:"Knts",
	// unitStringVisible: true

	});

   // spacer
   //$("#spacer").width(vpSize*0.5);
	//$("#spacer").height(vpSize*0.4);
/*
   spacer = new steelseries.DisplayMulti('spacer', {
		// width : document.getElementById('canvasWindApp').width,
		// : document.getElementById('canvasWindApp').height,
		//height : vpSize*0.2,
      lcdDecimals : 1,
		lcdColor : steelseries.LcdColor.BEIGE,
		//unitString : "Knots(A)",
		unitStringVisible : false,
		//detailString : "Avg: ",
		detailStringVisible : false,
	});
*/
   
   // wind app

	lcdWindApp = new steelseries.DisplayMulti('sailWind', {
		// width : document.getElementById('canvasWindApp').width,
		// : document.getElementById('canvasWindApp').height,
		height : vpSize*0.2,
      lcdDecimals : 1,
		lcdColor : steelseries.LcdColor.BEIGE,
		headerString : "Knots(A)",
		headerStringVisible : true,
		detailString : "Avg: ",
		detailStringVisible : true,
	});

   // heading
	lcdHeading = new steelseries.DisplayMulti('heading', {
		//width : document.getElementById('canvasHeading').width,
		//height : document.getElementById('canvasHeading').height,
		//width : 0.5,
		height : vpSize*0.2,
      lcdDecimals : 1,
		lcdColor: steelseries.LcdColor.BEIGE,
		headerString : "Heading(M)",
		headerStringVisible : true,
		detailString : "Avg(M): ",
		detailStringVisible : true
	});

   // make a web socket

	addSocketListener(new Sail());
}
