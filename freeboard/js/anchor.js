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
var anchorAlarmOn=false;
var radius=true;
/*
 * public static final String ANCHOR_ALARM_STATE = "#AAX";
	public static final String ANCHOR_ALARM_RADIUS = "#AAR";
	public static final String ANCHOR_ALARM_ADJUST = "#AAJ";
	public static final String ANCHOR_ALARM_LAT = "#AAN";
	public static final String ANCHOR_ALARM_LON = "#AAE";
 */
function AnchorAlarm() {
	this.onmessage = function(navObj) {
			
				
				//get autopilot state, avoid commands
				if (!navObj)
					return true;
				if (navObj.AAX) {
					if(navObj.AAX==1){
						anchorAlarmOn=true;
					}else{
						anchorAlarmOn=false;
					}
				}
				if (navObj.AAR) {
						alcdRadius.setValue(navObj.AAR);
				}
				if (navObj.AAD) {
						alcdDistance.setValue(navObj.AAD);
				}
			
	};

}



function initAnchorAlarm() {
	//if we cant do canvas, skip out here!
	if(!window.CanvasRenderingContext2D)return;
	alcdRadius = new steelseries.DisplaySingle('acanvasRadius', {
		// gaugeType : steelseries.GaugeType.TYPE4,
		//width : document.getElementById('acanvasRadius').width,
		//height : document.getElementById('acanvasRadius').height,
		lcdDecimals : 0,
		lcdColor: steelseries.LcdColor.BEIGE,
		unitString:"Mtrs",
		unitStringVisible: true,
		valuesNumeric: true	

	});
	alcdDistance = new steelseries.DisplaySingle('acanvasDistance', {
		// gaugeType : steelseries.GaugeType.TYPE4,
		//width : document.getElementById('acanvasDistance').width,
		//height : document.getElementById('acanvasDistance').height,
		lcdDecimals : 0,
		lcdColor: steelseries.LcdColor.BEIGE,
		unitString:"Mtrs",
		unitStringVisible: true,
		valuesNumeric: true	

	});
	
	addSocketListener(new AnchorAlarm());
}
