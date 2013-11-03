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
var autopilotOn=false;
var followCompass=true;
function Autopilot() {
	this.onmessage = function(navObj) {
				
				//get autopilot state, avoid commands
				if (!navObj)
					return true;
				if (navObj.APX) {
					if(navObj.APX==1){
						autopilotOn=true;
					}else{
						autopilotOn=false;
					}
				}
				if (navObj.APS) {
					if ('W' == navObj.APS) {
						followCompass=false;
					}else{
						followCompass=true;
					}
				}
				// show either compass or apparent wind depending on ap source
				if (!autopilotOn && followCompass && navObj.MGH) {
						autopilotTarget.setValue(navObj.MGH);
				}
				if (!autopilotOn && !followCompass &&  navObj.WDA) {
					var c = navObj.WDA;
						// -180 <> 180
						if (c >= 179) {
							autopilotTarget.setValue(-(360 - c));
						} else {
							autopilotTarget.setValue(c);
						}
				}
				//autopilot on, show target
				if (autopilotOn && navObj.APT) {
						autopilotTarget.setValue(navObj.APT);
				}
			
	};

}



function initAutopilot() {
	//if we cant do canvas, skip out here!
	if(!window.CanvasRenderingContext2D)return;
	// heading
	autopilotTarget = new steelseries.DisplaySingle('canvasTarget', {
		//width : document.getElementById('canvasTarget').width,
		//height : document.getElementById('canvasTarget').height,
		lcdDecimals : 0,
		lcdColor : steelseries.LcdColor.BEIGE,
		headerString : "Target",
		headerStringVisible : true,
		detailString : "XTE",
		detailStringVisible : true,
	});
	// autopilotTarget.setAltValue("OFF");
	addSocketListener(new Autopilot());
}
