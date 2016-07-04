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


function resizeDoubleLog(amount){
	if(amount==null){
		amount = zk.Widget.$('$doubleLogScale').getValue();
				}else{
		amount=1+(1*amount);
	}
	if(amount==0.0)return;
	var wsize = $("#canvasDoubleLog").width();
	var hsize = $("#canvasDoubleLog").height();
	$("#canvasDoubleLog").width(wsize*amount);
	$("#canvasDoubleLog").height(hsize*amount);
	this.initDoubleLog();
}

function DoubleLog () {
	this.onmessage = function (navObj) {
			//avoid commands
			if(!navObj)return true;

			if (navObj.SOG) {
					lcdLog.setValue(navObj.SOG);
			}
	};

}



function initDoubleLog() {
	//if we cant do canvas, skip out here!
	if(!window.CanvasRenderingContext2D)return;
   // Initialzing lcds

	// log
	lcdLog = new steelseries.DisplayMulti('canvasDoubleLog', {
		// gaugeType : steelseries.GaugeType.TYPE4,
		//width : document.getElementById('canvasLog').width,
		//height : document.getElementById('canvasLog').height,
		lcdDecimals : 1,
		lcdColor: steelseries.LcdColor.BEIGE,
		headerString : "Knots",
		headerStringVisible : true,
		detailString : "Avg: ",
		detailStringVisible : true,
	// unitString:"Knts",
	// unitStringVisible: true

	});

	addSocketListener(new DoubleLog());
}
