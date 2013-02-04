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
var avgArrayA = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ];
var avgPosA = 0;
var avgArrayT = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ];
var avgPosT = 0;

function resizeWind(amount){
	var size = $("#canvasWindDirTrue").width();
	$("#canvasWindDirTrue").width(size+(size*amount));
	$("#canvasWindDirTrue").height(size+(size*amount));
	$("#canvasWindDirApp").width(size+(size*amount));
	$("#canvasWindDirApp").height(size+(size*amount));
	var smallSize =  $("#canvasWindTrue").width();
	$("#canvasWindTrue").width(smallSize+(smallSize*amount));
	$("#canvasWindTrue").height(smallSize+(smallSize*amount));
	$("#canvasWindApp").width(smallSize+(smallSize*amount));
	$("#canvasWindApp").height(smallSize+(smallSize*amount));
	this.initWind();
	
}

function Wind () {
	this.onmessage = function (mArray) {
		
		
		jQuery.each(mArray, function(i, data) {
			//avoid commands
			if(data && data.indexOf('#')>=0)return true;
			
			if (data && data.indexOf('WSA') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					radialWindApp.setValueAnimated(c);
				}
				c=null;
			}
			if (data && data.indexOf('WST') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					radialWindTrue.setValueAnimated(c);
				}
				c=null;
			}
			if (data && data.indexOf('WDA') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					// -180 <> 180
					if (c >= 179) {
						radialWindDirApp.setValueAnimatedLatest(-(360 - c));
					} else {
						radialWindDirApp.setValueAnimatedLatest(c);
					}
					// make average
					avgArrayA[avgPosA] = c;
					avgPosA = avgPosA + 1;
					if (avgPosA >= avgArrayA.length)
						avgPosA = 0;
					var v = 0;
					for ( var i = 0; i < avgArrayA.length; i++) {
						v = v + avgArrayA[i];
					}
					if (c >= 179) {
						radialWindDirApp.setValueAnimatedAverage(-(360 - (v / avgArrayA.length)));
					} else {
						radialWindDirApp.setValueAnimatedAverage(v / avgArrayA.length);
					}
				}
				c=null;
			}
			if (data && data.indexOf('WDT') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					radialWindDirTrue.setValueAnimatedLatest(c);
					// make average
					avgArrayT[avgPosT] = c;
					avgPosT = avgPosT + 1;
					if (avgPosT >= avgArrayT.length)
						avgPosT = 0;
					var v = 0;
					for ( var i = 0; i < avgArrayT.length; i++) {
						v = v + avgArrayT[i];
					}
					radialWindDirTrue.setValueAnimatedAverage(v / avgArrayT.length);
				}
				c=null;
			}
			data=null;
		});
	}
}

var tackAngle=45;

function initWind() {

	// Define some sections for wind
	var sections = [ steelseries.Section(0, 20, 'rgba(0, 0, 220, 0.3)'),
			steelseries.Section(20, 35, 'rgba(0, 220, 0, 0.3)'),
			steelseries.Section(35, 75, 'rgba(220,0, 0, 0.3)') ];

	var areasCloseHaul = [ steelseries.Section((0-tackAngle), 0, 'rgba(0, 0, 220, 0.3)'),
			steelseries.Section(0, tackAngle, 'rgba(0, 0, 220, 0.3)') ];
	var areasCloseHaulTrue = [ steelseries.Section((360-tackAngle), 0, 'rgba(0, 0, 220, 0.3)'),
	           			steelseries.Section(0, tackAngle, 'rgba(0, 0, 220, 0.3)') ];

	// Initialzing gauges

	// wind app
	// wind
	radialWindApp = new steelseries.Radial('canvasWindApp', {
		gaugeType : steelseries.GaugeType.TYPE4,
		size : document.getElementById('canvasWindApp').width,
		minValue : 0,
		maxValue : 60,
		threshold : 35,
		section : sections,
		// area: areas,
		titleString : "WIND APPARENT",
		unitString : "knots",
		lcdVisible : true,
		lcdColor: steelseries.LcdColor.BEIGE,
		pointerType : steelseries.PointerType.TYPE2,
		backgroundColor: steelseries.BackgroundColor.BROWN,
	});

	// wind dir
	radialWindDirApp = new steelseries.WindDirection('canvasWindDirApp', {
		size : document.getElementById('canvasWindDirApp').width,
		titleString : "WIND           APP",
		lcdVisible : true,
		lcdColor: steelseries.LcdColor.BEIGE,
		pointSymbolsVisible : false,
		degreeScaleHalf : true,
		section : areasCloseHaul,
		area : areasCloseHaul,
		pointerTypeLatest : steelseries.PointerType.TYPE2,
		pointerTypeAverage : steelseries.PointerType.TYPE1,
		backgroundColor: steelseries.BackgroundColor.BROWN,
	});
	
	// wind true
	radialWindTrue = new steelseries.Radial('canvasWindTrue', {
		gaugeType : steelseries.GaugeType.TYPE4,
		size : document.getElementById('canvasWindTrue').width,
		maxValue : 60,
		threshold : 35,
		section : sections,
		titleString : "WIND          TRUE",
		unitString : "knots",
		lcdVisible : true,
		lcdColor: steelseries.LcdColor.BEIGE,
		pointerType : steelseries.PointerType.TYPE2,
		backgroundColor: steelseries.BackgroundColor.BROWN,
	});

	// wind dir

	radialWindDirTrue = new steelseries.WindDirection('canvasWindDirTrue', {
		size : document.getElementById('canvasWindDirTrue').width,
		titleString : "WIND     TRUE",
		roseVisible : false,
		lcdVisible : true,
		lcdColor: steelseries.LcdColor.BEIGE,
		section : areasCloseHaulTrue,
		area : areasCloseHaulTrue,
		pointSymbolsVisible : false,
		// pointSymbols: ["N", "", "", "", "", "", "", ""]
		lcdTitleStrings : [ "Latest", "Average" ],
		pointerTypeLatest : steelseries.PointerType.TYPE2,
		pointerTypeAverage : steelseries.PointerType.TYPE1,
		backgroundColor: steelseries.BackgroundColor.BROWN,
	});
	// make a web socket
	
	addSocketListener(new Wind());
}
