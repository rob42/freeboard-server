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

var headWayPtWidth = 150;
var headWayPtHeight = 100;
var logWidth = 350;
var logHeight = 240;
var latLonWidth = 245;
var latLonHeight = 55;

function resizeLog(amount) {
    if (amount == null) {
        amount = localStorage.getItem("logg.scale");
    } else {
        amount = 1*localStorage.getItem("logg.scale") + (1 * amount);
    }
    if (amount == 0.0)
        return;
    localStorage.setItem("logg.scale", amount);

    $("#canvasLog").width(logWidth * amount);
    $("#canvasLog").height(logHeight * amount);
    $("#canvasHeading").width(headWayPtWidth * amount);
    $("#canvasHeading").height(headWayPtHeight * amount);
    $("#canvasWaypoint").width(headWayPtWidth * amount);
    $("#canvasWaypoint").height(headWayPtHeight * amount);
    $("#canvasLat").width(latLonWidth * amount);
    $("#canvasLat").height(latLonHeight * amount);
    $("#canvasLon").width(latLonWidth * amount);
    $("#canvasLon").height(latLonHeight * amount);

    this.initLogg();
}

function loggOnMove(event) {
    var e = event;
    localStorage.setItem("logg.top", event.top + "");
    localStorage.setItem("logg.left", event.left + "");
    return;
}

function loggOnLoad(){
    zk.Widget.$(jq('$logg')[0]).setLeft(localStorage.getItem("logg.left"));
    zk.Widget.$(jq('$logg')[0]).setTop(localStorage.getItem("logg.top"));
}

function Logg() {
    this.onmessage = function (navObj) {


        //avoid commands
        if (!navObj)
            return true;

        if (navObj.LAT) {
            var c = navObj.LAT;
            if ($.isNumeric(c)) {
                if (c > 0) {
                    lcdLat.setValue(c.toFixed(5) + ' N');
                } else {
                    lcdLat.setValue(Math.abs(c.toFixed(5)) + ' S');
                }
            }
            c = null;
        }
        if (navObj.LON) {
            var c = navObj.LON;
            if ($.isNumeric(c)) {
                if (c > 0) {
                    lcdLon.setValue(c.toFixed(5) + ' E');
                } else {
                    lcdLon.setValue(c.toFixed(5) + ' W');
                }
            }
            c = null;
        }
        if (navObj.SOG) {
            lcdLog.setValue(navObj.SOG);

        }
        if (navObj.MGH) {
            lcdHeading.setValue(navObj.MGH);

        }
        if (navObj.COG) {
            if (declination) {
                lcdHeading.setValue(navObj.COG + declination);
            } else {
                lcdHeading.setValue(navObj.COG);
            }

        }
        if (navObj.YAW) {
            var c = navObj.YAW;
            if ($.isNumeric(c)) {
                // -180 <> 180
                if (c >= 179) {
                    lcdWaypoint.setValue(-(360 - c));
                } else {
                    lcdWaypoint.setValue(c);
                }
            }
            c = null;
        }

    };

}



function initLogg() {
    //if we cant do canvas, skip out here!
    if (!window.CanvasRenderingContext2D)
        return;

    // Setting declination to 0 to avoid  "Uncaught reference" or "undefined" for declination
    declination = 0.;

    vpHeight = window.innerHeight - 50;
    vpWidth = window.innerWidth;

    if (typeof (Storage) == "undefined") {
        // Sorry! No Web Storage support..
        alert("Sorry! No Web Storage support. Please use a different browser.");
        return;
    }
    if (localStorage.getItem("logg.scale") == null) {
        localStorage.setItem("logg.scale", "1.0");
        localStorage.setItem("logg.top",  "0px");
        localStorage.setItem("logg.left", Math.floor(vpWidth/2)+"px");
    }
    amount = localStorage.getItem("logg.scale")

    //// Initialzing lcds
    // log
    lcdLat = new steelseries.DisplaySingle('canvasLat', {
        // gaugeType : steelseries.GaugeType.TYPE4,
        width: latLonWidth*amount,
        height: latLonHeight*amount,
        lcdDecimals: 5,
        lcdColor: steelseries.LcdColor.BEIGE,
        //unitString:"",
        unitStringVisible: false,
        valuesNumeric: false

    });
    lcdLon = new steelseries.DisplaySingle('canvasLon', {
        // gaugeType : steelseries.GaugeType.TYPE4,
        width: latLonWidth*amount,
        height: latLonHeight*amount,
        lcdDecimals: 5,
        lcdColor: steelseries.LcdColor.BEIGE,
        //unitString:"",
        unitStringVisible: false,
        valuesNumeric: false

    });

    // log
    lcdLog = new steelseries.DisplayMulti('canvasLog', {
        // gaugeType : steelseries.GaugeType.TYPE4,
        width: logWidth*amount,
        height: logHeight*amount,
        lcdDecimals: 1,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: "Knots",
        headerStringVisible: true,
        detailString: "Avg: ",
        detailStringVisible: true,
        // unitString:"Knts",
        // unitStringVisible: true

    });

    // heading
    lcdHeading = new steelseries.DisplayMulti('canvasHeading', {
        width: headWayPtWidth*amount,
        height: headWayPtHeight*amount,
        lcdDecimals: 0,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: "Heading(M)",
        headerStringVisible: true,
        detailString: "Avg(M): ",
        detailStringVisible: true,
    });

    // waypoint
    lcdWaypoint = new steelseries.DisplayMulti('canvasWaypoint', {
        width: headWayPtWidth*amount,
        height: headWayPtHeight*amount,
        lcdDecimals: 0,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: "To Waypoint",
        headerStringVisible: true,
        detailString: "ETA: ",
        detailStringVisible: true,
    });
    lcdWaypoint.setValue(0);

    addSocketListener(new Logg());
}
