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
//accel
var x_min=0.0;
var y_min=0.0;
var z_min=0.0;
var x_max=0.0;
var y_max=0.0;
var z_max=0.0;

//gyro
var x_gyromin=0.0;
var y_gyromin=0.0;
var z_gyromin=0.0;
var x_gyromax=0.0;
var y_gyromax=0.0;
var z_gyromax=0.0;

//mag
var x_magmin=0.0;
var y_magmin=0.0;
var z_magmin=0.0;
var x_magmax=0.0;
var y_magmax=0.0;
var z_magmax=0.0;

function Cal () {
	this.onmessage = function (navObj) {
		var mArray=m.data.split(",");
		if (!navObj)
			return true;
			//accelerator
		if (navObj.AN3) {
	
				var c = navObj.AN3;
				if(x_min>c)x_min=c;
				if(x_max<c)x_max=c;
				//lcdLat.setValue(parseFloat(c));
				var x_minWidget = zk.Widget.$("$x_min");
				x_minWidget.setValue(x_min);
				var x_maxWidget = zk.Widget.$("$x_max");
				x_maxWidget.setValue(x_max);
				var x_calWidget = zk.Widget.$("$x_cal");
				x_calWidget.setValue(-(x_min+x_max)/2);
			}
		if (navObj.AN4) {
				var c = navObj.AN4;
				if(y_min>c)y_min=c;
				if(y_max<c)y_max=c;
				//lcdLat.setValue(parseFloat(c));
				var y_minWidget = zk.Widget.$("$y_min");
				y_minWidget.setValue(y_min);
				var y_maxWidget = zk.Widget.$("$y_max");
				y_maxWidget.setValue(y_max);
				var y_calWidget = zk.Widget.$("$y_cal");
				y_calWidget.setValue(-(y_min+y_max)/2);
			}
		if (navObj.AN5) {
			var c = navObj.AN5;
				if(z_min>c)z_min=c;
				if(z_max<c)z_max=c;
				//lcdLat.setValue(parseFloat(c));
				var z_minWidget = zk.Widget.$("$z_min");
				z_minWidget.setValue(z_min);
				var z_maxWidget = zk.Widget.$("$z_max");
				z_maxWidget.setValue(z_max);
				var z_calWidget = zk.Widget.$("$z_cal");
				z_calWidget.setValue(-(z_min+z_max)/2);
			}
			//gyro
		if (navObj.AN0) {
			var c = navObj.AN0;
				if(x_gyromin>c)x_gyromin=c;
				if(x_gyromax<c)x_gyromax=c;
				//lcdLat.setValue(parseFloat(c));
				var x_gyrominWidget = zk.Widget.$("$x_gyromin");
				x_gyrominWidget.setValue(x_gyromin);
				var x_gyromaxWidget = zk.Widget.$("$x_gyromax");
				x_gyromaxWidget.setValue(x_gyromax);
				var x_gyrocalWidget = zk.Widget.$("$x_gyrocal");
				x_gyrocalWidget.setValue(-(x_gyromin+x_gyromax)/2);
			}
		if (navObj.AN1) {
			var c = navObj.AN1;
				if(y_gyromin>c)y_gyromin=c;
				if(y_gyromax<c)y_gyromax=c;
				//lcdLat.setValue(parseFloat(c));
				var y_gyrominWidget = zk.Widget.$("$y_gyromin");
				y_gyrominWidget.setValue(y_gyromin);
				var y_gyromaxWidget = zk.Widget.$("$y_gyromax");
				y_gyromaxWidget.setValue(y_gyromax);
				var y_gyrocalWidget = zk.Widget.$("$y_gyrocal");
				y_gyrocalWidget.setValue(-(y_gyromin+y_gyromax)/2);
			}
		if (navObj.AN2) {
			var c = navObj.AN2;
				if(z_gyromin>c)z_gyromin=c;
				if(z_gyromax<c)z_gyromax=c;
				//lcdLat.setValue(parseFloat(c));
				var z_gyrominWidget = zk.Widget.$("$z_gyromin");
				z_gyrominWidget.setValue(z_gyromin);
				var z_gyromaxWidget = zk.Widget.$("$z_gyromax");
				z_gyromaxWidget.setValue(z_gyromax);
				var z_gyrocalWidget = zk.Widget.$("$z_gyrocal");
				z_gyrocalWidget.setValue(-(z_gyromin+z_gyromax)/2);
			}
			
			//mag
		if (navObj.MGX) {
			var c = navObj.MGX;
				if(x_magmin>c)x_magmin=c;
				if(x_magmax<c)x_magmax=c;
				//lcdLat.setValue(parseFloat(c));
				var x_magminWidget = zk.Widget.$("$x_magmin");
				x_magminWidget.setValue(x_magmin);
				var x_magmaxWidget = zk.Widget.$("$x_magmax");
				x_magmaxWidget.setValue(x_magmax);
				var x_magcalWidget = zk.Widget.$("$x_magcal");
				x_magcalWidget.setValue(-(x_magmin+x_magmax)/2);
			}
		if (navObj.MGY) {
			var c = navObj.MGY;
				if(y_magmin>c)y_magmin=c;
				if(y_magmax<c)y_magmax=c;
				//lcdLat.setValue(parseFloat(c));
				var y_magminWidget = zk.Widget.$("$y_magmin");
				y_magminWidget.setValue(y_magmin);
				var y_magmaxWidget = zk.Widget.$("$y_magmax");
				y_magmaxWidget.setValue(y_magmax);
				var y_magcalWidget = zk.Widget.$("$y_magcal");
				y_magcalWidget.setValue(-(y_magmin+y_magmax)/2);
			}
		if (navObj.MGZ) {
			var c = navObj.MGZ;
				if(z_magmin>c)z_magmin=c;
				if(z_magmax<c)z_magmax=c;
				//lcdLat.setValue(parseFloat(c));
				var z_magminWidget = zk.Widget.$("$z_magmin");
				z_magminWidget.setValue(z_magmin);
				var z_magmaxWidget = zk.Widget.$("$z_magmax");
				z_magmaxWidget.setValue(z_magmax);
				var z_magcalWidget = zk.Widget.$("$z_magcal");
				z_magcalWidget.setValue(-(z_magmin+z_magmax)/2);
			}
	
	};
	
}



function initCal() {

	addSocketListener(new Cal());
}