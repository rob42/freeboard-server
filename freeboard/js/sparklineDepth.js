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

var depthArraySize;
var depthArray = [];
var anAlarm;
var options;

function resizeDepth(amount) {
    if (amount == null) {
        amount = zk.Widget.$('$depthScale').getValue();
    } else {
        amount = 1 + (1 * amount);
    }
    if (amount == 0.0)
        return;

    var wsize = $("#canvasDepth").width();
    var hsize = $("#canvasDepth").height();

    $("#canvasDepth").width(wsize * amount);
    $("#canvasDepth").height(hsize * amount);
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
function Depth() {

	this.onmessage = function (navObj) {

        //avoid commands
        if (!navObj)
            return true;
        //depth
        if (navObj.DBT) {
            unitTemp = zk.Widget.$('$cfgDepthUnit').getValue();
            if (unitTemp !== depthUnit) {
                headerString = "Depth " + unitTemp;
                depthUnit = unitTemp;
                console.log("DBT DisplaySingle");
                lcdDepth = new steelseries.DisplaySingle('canvasDepth', {
                    height: 200,
                    width: 200,
                    lcdDecimals: 1,
                    lcdColor: steelseries.LcdColor.BEIGE,
                    headerString: headerString,
                    headerStringVisible: true,
                    unitString: depthUnit,
                    unitStringVisible: false
                });
            }
            lcdDepth.setValue(navObj.DBT);
            //            console.log(navObj.DBT + "\n" + depthArray);
            depthArray.shift();
            //            console.log(depthArray);
            anAlarm = zk.Widget.$('$alarmDepth').getValue();
            sPoints = zk.Widget.$('$sparkPts').getValue();
            sparkMin = zk.Widget.$('$sparkMin').getValue();

            // clip the sparkline values so that the scaling does not get screwed up by values < sparkMin
            if (navObj.DBT < sparkMin) {
                depthArray.push(sparkMin);
            } else {
                depthArray.push(navObj.DBT);
            }
            if (navObj.DBT < anAlarm) {
                lcdDepth.setLcdColor(steelseries.LcdColor.RED);
            } else {
                lcdDepth.setLcdColor(steelseries.LcdColor.BEIGE);
            }
            $('.dynamicsparkline').sparkline(depthArray, options);
						}
    };

}



function initDepth() {
    //if we cant do canvas, skip out here!
    if (!window.CanvasRenderingContext2D)
        return;
    // Initialzing lcds
    // depth

    depthArraySize = zk.Widget.$('$sparkPts').getValue();
    while (depthArraySize--)
    depthArray.push(6);

    depthUnit = zk.Widget.$('$depthUnit').getValue();
    headerString = "Depth " + depthUnit;
    lcdDepth = new steelseries.DisplaySingle('canvasDepth', {
//        height: vpHeight * .25,
//        width: vpWidth * .30,
        lcdDecimals: 1,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: headerString,
        headerStringVisible: true,
        unitString: depthUnit,
        unitStringVisible: false
    });

    // Set width of spring div
    $("#depthSpring").width(200);
    $("#depthSpring").height(200);
//    wid = Math.round(vpWidth * 0.3) + "px";
//    ht = Math.round(vpHeight * .30) + "px";

    anAlarm = zk.Widget.$('$alarmDepth').getValue();
    options = {
        width: '200px',
        height: '200px',
        maxSpotColor: '',
        minSpotColor: '',
        fillColor: '#cdf',
//        fillColor: '',
        chartRangeMin: zk.Widget.$('$sparkMin').getValue(),
        normalRangeMin: zk.Widget.$('$sparkMin').getValue(),
        normalRangeMax: anAlarm,
        drawNormalOnTop: 'true',
        normalRangeColor: 'rgba(255, 0, 0, .20)'
    };
    addSocketListener(new Depth());
}

function distanceUnit(unit) {
    var convert;
    switch (unit) {
    case "Kt":
        {
            distUnit = "n.m.";
            convert = 1.0;
            break;
        }
    case "km/hr":
        {
            distUnit = "km";
            convert = 1.852;
            break;
        }
    case "mi/hr":
        {
            distUnit = "mi";
            convert = 1.1450779448;
            break;
        }
    }
    return convert;
}

    $(function() {

        /* Sparklines can also take their values from the first argument
        passed to the sparkline() function */
        $('.dynamicsparkline').sparkline(depthArray, options);

    });


// This doesn't work - why???
//function distanceUnit(spdOvrGnd) {
//    var du;
//    switch (spdOvrGnd) {
//    case "kt":
//        {
//            du = "n.m.";
//            break;
//        }
//    case "km/hr":
//        {
//            du = "km";
//            break;
//        }
//    case "mi/hr":
//        {
//            du = "mi";
//            break;
//        }
//    }
//    return du;
//}
