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
                lcdSailDepth.setLcdColor(steelseries.LcdColor.RED);
            } else {
                lcdSailDepth.setLcdColor(steelseries.LcdColor.BEIGE);
            }
            $('.dynamicsparkline').sparkline(depthArray, options);
            lcdDepth.setValue(navObj.DBT);
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

    lcdDepth = new steelseries.DisplaySingle('canvasDepth', {
        // gaugeType : steelseries.GaugeType.TYPE4,
        //width : document.getElementById('canvasLog').width,
        //height : document.getElementById('canvasLog').height,
        lcdDecimals: 1,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: "Depth",
        headerStringVisible: true,
//		detailString : "Avg: ",
//		detailStringVisible : true,
        unitString: depthUnit,
        unitStringVisible: true

    });

    // Set width of spring div
    $("#spring").width(vpWidth * 0.3);
    $("#spring").height(vpHeight * .20);
    wid = Math.round(vpWidth * 0.3) + "px";
    ht = Math.round(vpHeight * .30) + "px";

    anAlarm = zk.Widget.$('$alarmDepth').getValue();
    options = {
        width: wid,
        height: ht,
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

