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
	this.onmessage = function(mArray) {
			jQuery.each(mArray, function(i, data) {
				//get autopilot state, avoid commands
				if(data && data.indexOf('#')>=0){
					return true;
				}
				
				if (data && data.indexOf('APX:') >= 0) {
					var c =parseInt(data.substring(4));
					if(c==1){
						autopilotOn=true;
					}else{
						autopilotOn=false;
					}
					c=null;
				}
				if (data && data.indexOf('APS:') >= 0) {
					var c = data.substring(4);
					if ('W' == c) {
						followCompass=false;
					}else{
						followCompass=true;
					}
					c = null;
				}
				// show either compass or apparent wind depending on ap source
				if (!autopilotOn && followCompass && data && data.indexOf('MGH:') >= 0) {
					var c = parseFloat(data.substring(4));
					if ($.isNumeric(c)) {
						autopilotTarget.setValue(c);
					}
					c = null;
				}
				if (!autopilotOn && !followCompass &&  data && data.indexOf('WDA:') >= 0) {
					var c = parseFloat(data.substring(4));
					if ($.isNumeric(c)) {
						autopilotTarget.setValue(c);
					}
					c = null;
				}
				//autopilot on, show target
				if (autopilotOn && data && data.indexOf('APT:') >= 0) {
					var c = parseFloat(data.substring(4));
					if ($.isNumeric(c)) {
						autopilotTarget.setValue(c);
					}
					c = null;
				}
				data = null;
			});
	}

}



function initAutopilot() {

	// heading
	autopilotTarget = new steelseries.DisplaySingle('canvasTarget', {
		width : document.getElementById('canvasTarget').width,
		height : document.getElementById('canvasTarget').height,
		lcdDecimals : 0,
		lcdColor : steelseries.LcdColor.BEIGE,
		headerString : "Target",
		headerStringVisible : true,
		detailString : "XTE",
		detailStringVisible : true,
	});
	// autopilotTarget.setAltValue("OFF");
	wsList.push(new Autopilot());
}