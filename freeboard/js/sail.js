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
var selectorNdx = 0;
var tripElapsedTime;
var tripDistance;
var tripAverageSpeed;


//colours
var gaugeLcdColor = steelseries.LcdColor.BEIGE;
//TILTED_BLACK
var gaugePointerType = steelseries.PointerType.TYPE4;
var gaugeBackgroundColor = steelseries.BackgroundColor.CARBON;
var gaugeFrameDesign = steelseries.FrameDesign.BLACK_METAL;
var vpSize;
var vpHeight;
var vpWidth;
var sogUnit;
var sowUnit;
var distUnit;
var depthUnit;
var MILLISPERHR;


function Sail() {

    this.onmessage = function(navObj) {

        //avoid commands
        if (!navObj) return true;

        //depth
        if (navObj.DBT) {
            unitTemp = zk.Widget.$('$cfgDepthUnit').getValue();
            if (unitTemp !== depthUnit) {
                headerString = "Depth " + unitTemp;
                depthUnit = unitTemp;
                console.log("DBT DisplaySingle");
                lcdSailDepth = new steelseries.DisplaySingle('sailDepth', {
                    height: vpHeight * .25,
                    width: vpWidth * .30,
                    lcdDecimals: 1,
                    lcdColor: steelseries.LcdColor.BEIGE,
                    headerString: headerString,
                    headerStringVisible: true,
                    unitString: depthUnit,
                    unitStringVisible: false
                });
            }
            lcdSailDepth.setValue(navObj.DBT);
            //            console.log(navObj.DBT + "\n" + depthArray);
            depthArray.shift();
            //            console.log(depthArray);
            depthArray.push(navObj.DBT);
            anAlarm = zk.Widget.$('$alarmDepth').getValue();
            sPoints = zk.Widget.$('$sparkPts').getValue();
            if (navObj.DBT < anAlarm) {
                lcdSailDepth.setLcdColor(steelseries.LcdColor.RED);
            } else {
                lcdSailDepth.setLcdColor(steelseries.LcdColor.BEIGE);
            }
            $('.dynamicsparkline').sparkline(depthArray, options);
        }

        //SOG
        if (navObj.SOG) {
            //            console.log("navObj.SOG = " + navObj.SOG.toString());
            unitTemp = zk.Widget.$('$cfgSOGUnit').getValue();
            if (unitTemp !== sogUnit) {
                //                console.log("unitTemp, sogUnit = " + unitTemp + " " + sogUnit);
                headerString = "SOG " + unitTemp;
                sogUnit = unitTemp;
                console.log("SOG DisplaySingle");
                lcdSOG = new steelseries.DisplaySingle('sailSOG', {
                    height: vpHeight * .25,
                    width: vpWidth * .30,
                    lcdDecimals: 1,
                    lcdColor: steelseries.LcdColor.BEIGE,
                    headerString: headerString,
                    headerStringVisible: true,
                });
            }
            //            console.log("SOG= " + navObj.SOG);
            lcdSOG.setValue(navObj.SOG);
        }

        //SOW
        if (navObj.SOW) {
            unitTemp = zk.Widget.$('$cfgSOWUnit').getValue();
            if (unitTemp !== sowUnit) {
                headerString = "SOW " + unitTemp;
                sowUnit = unitTemp;
                console.log("SOW DisplaySingle");
                lcdSOW = new steelseries.DisplaySingle('sailLog', {
                    height: vpHeight * .25,
                    width: vpWidth * .30,
                    lcdDecimals: 1,
                    lcdColor: steelseries.LcdColor.BEIGE,
                    headerString: headerString,
                    headerStringVisible: true,
                });
            }
            lcdSOW.setValue(navObj.SOW);
        }
        
        if (navObj.TET){
            if (selectorNdx == 1){
                tripElapsedTime = navObj.TET;
                lcdSummary.setValue(tripElapsedTime/MILLISPERHR);
            }
        }
        
        if (navObj.DST){
            if (selectorNdx == 0){
                tripDistance = navObj.DST;
                lcdSummary.setValue(tripDistance);
            }
        }
        if (navObj.TAS){
            if (selectorNdx == 2){
                tripAverageSpeed = navObj.TAS;
                lcdSummary.setValue(tripAverageSpeed);
            }
        }
    };
}



//setup gauge scales here

//colours
var gaugeLcdColor = steelseries.LcdColor.BEIGE;
//TILTED_BLACK
var gaugePointerType = steelseries.PointerType.TYPE4;
var gaugeBackgroundColor = steelseries.BackgroundColor.CARBON;
var gaugeFrameDesign = steelseries.FrameDesign.BLACK_METAL;

function initSail() {
    // Define some sections for gauges
    // Initialzing gauges
    //get the smallest distance
    // The SailToolbar is located at the top of the window and is 50 px high
    vpSize = Math.min(window.innerHeight - 50, window.innerWidth);
    vpHeight = window.innerHeight - 50;
    vpWidth = window.innerWidth;
    MILLISPERHR=3600000.;

    // Depth 
    //if we cant do canvas, skip out here!
    if (!window.CanvasRenderingContext2D) return;
    // 
    // Initialzing displays
    var tackAngle = 45;
    var areasCloseHaul = [
    steelseries.Section((0 - tackAngle), 0, 'rgba(0, 0, 220, 0.3)'), steelseries.Section(0, tackAngle, 'rgba(0, 0, 220, 0.3)')];
    var areasCloseHaulTrue = [
    steelseries.Section((360 - tackAngle), 0, 'rgba(0, 0, 220, 0.3)'), steelseries.Section(0, tackAngle, 'rgba(0, 0, 220, 0.3)')];

    // Initialzing gauges
    // wind dir apparent
    vpSize = Math.min(window.innerWidth * .30, (window.innerHeight - 50) * .75);
    radialWindDirApp = new steelseries.WindDirection('sailWindDir', {
        // size : document.getElementById('canvasWindDirApp').width,
        titleString: "WIND          APP",
        size: vpSize,
        lcdVisible: true,
        lcdColor: steelseries.LcdColor.BEIGE,
        pointSymbolsVisible: false,
        degreeScaleHalf: true,
        section: areasCloseHaul,
        area: areasCloseHaul,
        pointerTypeLatest: steelseries.PointerType.TYPE2,
        pointerTypeAverage: steelseries.PointerType.TYPE1,
        backgroundColor: steelseries.BackgroundColor.BROWN,
    });


    // depth
    // init Sparkline array
    depthArraySize = zk.Widget.$('$sparkPts').getValue();
    while (depthArraySize--)
    depthArray.push(6);

    depthUnit = zk.Widget.$('$depthUnit').getValue();
    headerString = "Depth " + depthUnit;
    lcdSailDepth = new steelseries.DisplaySingle('sailDepth', {
        height: vpHeight * .25,
        width: vpWidth * .30,
        lcdDecimals: 1,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: headerString,
        headerStringVisible: true,
        unitString: depthUnit,
        unitStringVisible: false
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
        chartRangeMin: '3',
        normalRangeMin: '3',
        normalRangeMax: anAlarm,
        drawNormalOnTop: 'true',
        normalRangeColor: 'rgba(255, 0, 0, .20)'
    };

    //   $("#selector").height(vpHeight*.10);

    // log
    sowUnit = zk.Widget.$('$cfgSOWUnit').getValue();
    headerString = "SOW " + sowUnit;
    lcdSOW = new steelseries.DisplaySingle('sailLog', {
        height: vpHeight * .25,
        width: vpWidth * .30,
        lcdDecimals: 1,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: headerString,
        headerStringVisible: true,
    });

    // wind app
    lcdWindApp = new steelseries.DisplayMulti('sailWind', {
        // width : document.getElementById('canvasWindApp').width,
        // : document.getElementById('canvasWindApp').height,
        height: vpHeight * .25,
        width: vpWidth * .30,
        lcdDecimals: 1,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: "Knots(A)",
        headerStringVisible: true,
        detailString: "Avg: ",
        detailStringVisible: true,
    });

    // GPS SOG
    sogUnit = zk.Widget.$('$cfgSOGUnit').getValue();
    headerString = "SOG " + sogUnit;
    lcdSOG = new steelseries.DisplaySingle('sailSOG', {
        height: vpHeight * .25,
        width: vpWidth * .30,
        lcdDecimals: 1,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: headerString,
        headerStringVisible: true,
    });

    distanceUnit();
    console.log("init sogUnit = " + sogUnit);
    console.log("init distUnit = " + distUnit);

    lcdSummary = new steelseries.DisplaySingle('tripSummary', {
        height: vpHeight * .25 / 2,
        width: vpWidth * .30 / 2,
        lcdDecimals: 1,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: "Distance " + distUnit,
        headerStringVisible: true,
    });

    jq('$Selector').height(vpHeight * .25 / 2);
    jq('$Selector').width(vpWidth * .30 / 2);

    // make a web socket
    addSocketListener(new Sail());
}

function selectorButton() {
    //            0: // distance traveled
    //            1: // elapsed time
    //            2: //average speed
    selectorNdx++;
    if (selectorNdx >= 3) {
        selectorNdx = 0;
    }
    distanceUnit();
    switch (selectorNdx) {
    case 0:
        lcdSummary = new steelseries.DisplaySingle('tripSummary', {
            height: vpHeight * .25 / 2,
            width: vpWidth * .30 / 2,
            lcdDecimals: 1,
            lcdColor: steelseries.LcdColor.BEIGE,
            headerString: "Distance "+distUnit,
            headerStringVisible: true,
        });
        lcdSummary.setValue(tripDistance);
        break;
    case 1:
        headerString = "Elapsed Time hr";
        lcdSummary = new steelseries.DisplaySingle('tripSummary', {
            height: vpHeight * .25 / 2,
            width: vpWidth * .30 / 2,
            lcdDecimals: 1,
//            valuesNumeric: false,
            lcdColor: steelseries.LcdColor.BEIGE,
            headerString: headerString,
            headerStringVisible: true,
        });
        lcdSummary.setValue(tripElapsedTime/MILLISPERHR);
        break;
    case 2:
        headerString = "Ave. Spd. " + sogUnit;
        lcdSummary = new steelseries.DisplaySingle('tripSummary', {
            height: vpHeight * .25 / 2,
            width: vpWidth * .30 / 2,
            lcdDecimals: 1,
            lcdColor: steelseries.LcdColor.BEIGE,
            headerString: headerString,
            headerStringVisible: true,
        });
        lcdSummary.setValue(tripAverageSpeed);
        break;
    }
}

function distanceUnit() {
    switch (sogUnit) {
    case "Kt":
        {
            distUnit = "n.m.";
            break;
        }
    case "km/hr":
        {
            distUnit = "km";
            break;
        }
    case "mi/hr":
        {
            distUnit = "mi";
            break;
        }
    }
}

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
