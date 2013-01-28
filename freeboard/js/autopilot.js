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


function Autopilot () {
	this.onmessage = function (mArray) {
		//var mArray=m.data.split(",");
		jQuery.each(mArray, function(i, data) {
//			
			if (data && data.indexOf('MGH:') >= 0) {
				var c = parseFloat(data.substring(4));
				if($.isNumeric(c)){
					autopilotTarget.setValue(c);
				}
			}
			
		});
	}
	
}

function toggleAutopilot(){
	autopilotOn=!autopilotOn;
	
}

function initAutopilot() {

	// heading
	autopilotTarget = new steelseries.DisplaySingle('canvasTarget', {
		width : document.getElementById('canvasTarget').width,
		height : document.getElementById('canvasTarget').height,
		lcdDecimals : 0,
		lcdColor: steelseries.LcdColor.BEIGE,
		headerString : "Target",
		headerStringVisible : true,
		detailString : "XTE",
		detailStringVisible : true,
	});
	//autopilotTarget.setAltValue("OFF");
wsList.push(new Autopilot());
}