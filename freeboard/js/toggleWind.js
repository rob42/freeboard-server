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
// Arrays and pointers for sparkline
var windSparkArraySize;
var windSparkTrue = [];
var windSparkApp = [];
var windOptions;
// wind vector averaging objects
var toggleWindVectorArrayApparent = new vectorArray();
var toggleWindVectorArrayTrue = new vectorArray();

var width = 400;
var displayTrue = 0;
var smallHeightFract = 0.3;
// Define some sections for wind
var areasCloseHaul = [
    steelseries.Section((0 - tackAngle), 0, 'rgba(0, 0, 220, 0.3)'),
    steelseries.Section(0, tackAngle, 'rgba(0, 0, 220, 0.3)')];
var areasCloseHaulTrue = [
    steelseries.Section((360 - tackAngle), 0, 'rgba(0, 0, 220, 0.3)'),
    steelseries.Section(0, tackAngle, 'rgba(0, 0, 220, 0.3)')];


function resizeToggleWind(amount) {
    if (amount == null) {
        amount = localStorage.getItem("toggleWind.scale");
    } else {
        amount = 1 * localStorage.getItem("toggleWind.scale") + (1 * amount);
    }
    if (amount == 0.0)
        return;
    localStorage.setItem("toggleWind.scale", amount);

    $("#canvasToggleWindDir").width(width * amount);
    $("#canvasToggleWindDir").height(width * amount);

    var wsmallSize = width;
    var hsmallSize = width / 3.5;
    $("#canvasToggleWind").width(wsmallSize * amount);
    $("#canvasToggleWind").height(hsmallSize * amount);
    $("wSpring").width(wsmallSize * amount);
    $("wSpring").height(wsmallSize * amount * smallHeightFract);
    wid = Math.round(wsmallSize * amount) + "px";
    ht = Math.round(wsmallSize * amount * smallHeightFract) + "px";
    if (displayTrue){
        windOptions = {
            width: wid,
            height: ht,
            maxSpotColor: '',
            minSpotColor: '',
            fillColor: 'red',
            chartRangeMin: 0
        };
    } else {
        windOptions = {
            width: wid,
            height: ht,
            maxSpotColor: '',
            minSpotColor: '',
            fillColor: 'blue',
            chartRangeMin: 0
        };
    }

}

function toggleWindOnMove(event) {
    var e = event;
    localStorage.setItem("toggleWind.top", event.top + "");
    localStorage.setItem("toggleWind.left", event.left + "");
    return;
}

function toggleWindOnLoad() {
    zk.Widget.$(jq('$toggleWind')[0]).setLeft(localStorage.getItem("toggleWind.left"));
    zk.Widget.$(jq('$toggleWind')[0]).setTop(localStorage.getItem("toggleWind.top"));
}

function ToggleWind() {
    this.onmessage = function (navObj) {
        // avoid commands
        if (!navObj)
            return true;

        if (navObj.WSA && navObj.WDA) {
	    toggleWindVectorArrayApparent.addVector([navObj.WSA, navObj.WDA]);
	    avgVector = toggleWindVectorArrayApparent.getVectorAverage();

            windSparkApp.shift();
            windSparkApp.push(navObj.WSA);
            if (!displayTrue) {
                lcdToggleWind.setValue(navObj.WSA);
                lcdToggleWind.setAltValue(avgVector[0]);
                $('.windSparkline').sparkline(windSparkApp, windOptions);
                if (navObj.WDA > 180) {
                    radialToggleWindDir.setValueAnimatedLatest(-(360 - navObj.WDA));
                } else {
                    radialToggleWindDir.setValueAnimatedLatest(navObj.WDA);
                }
                if (avgVector[1] > 180) {
                    radialToggleWindDir
                            .setValueAnimatedAverage(-(360 - avgVector[1]));
                } else {
                    radialToggleWindDir.setValueAnimatedAverage(avgVector[1]);
                }
            }
        }
        if (navObj.WST && navObj.WDT) {
	    toggleWindVectorArrayTrue.addVector([navObj.WST, navObj.WDT]);
	    avgVector = toggleWindVectorArrayTrue.getVectorAverage();

            windSparkTrue.shift();
            windSparkTrue.push(navObj.WST);
            if (displayTrue) {
                lcdToggleWind.setValue(navObj.WST);
                lcdToggleWind.setAltValue(avgVector[0]);
                $('.windSparkline').sparkline(windSparkTrue, windOptions);
		if (navObj.WDT > 0.0 && navObj.WDT < 360.0)
		    radialToggleWindDir.setValueAnimatedLatest(navObj.WDT)
		else
		    radialToggleWindDir.setValueAnimatedLatest(0.0);
                if (avgVector[1] > 0.0)
                    radialToggleWindDir.setValueAnimatedAverage(avgVector[1]);
                else
                    radialToggleWindDir.setValueAnimatedAverage(0.0);
            }
        }
    };
}

var tackAngle = 45;

function toggle() {
    var s = zk.Widget.$('$twImageToggle');
    if (s._image.includes("./js/img/ToggleApp.png")) {
        s.setImage("./js/img/ToggleTrue.png");
        displayTrue = 1;
	var lastVector = toggleWindVectorArrayTrue.getVectorLast();
	var avgVector = toggleWindVectorArrayTrue.getVectorAverage();
        lcdToggleWind = new steelseries.DisplayMulti('canvasToggleWind', {
            width: smallWidth,
            height: smallHeight,
            lcdDecimals: 1,
            lcdColor: steelseries.LcdColor.BEIGE,
            unitString: "Knots(T)",
            value: lastVector[0],
            altValue: avgVector[0],
            linkAltValue: false,
            unitStringVisible: true,
            detailString: "Avg: ",
            detailStringVisible: true,
        });


        // wind dir

        radialToggleWindDir = new steelseries.WindDirection('canvasToggleWindDir', {
            size: size,
            titleString: "WIND           TRUE",
            roseVisible: false,
            lcdVisible: true,
            lcdColor: steelseries.LcdColor.BEIGE,
            section: areasCloseHaul,
            area: areasCloseHaul,
            pointSymbolsVisible: false,
            // pointSymbols: ["N", "", "", "", "", "", "", ""]
            lcdTitleStrings: ["Latest", "Average"],
            pointerTypeLatest: steelseries.PointerType.TYPE2,
            pointerTypeAverage: steelseries.PointerType.TYPE1,
            backgroundColor: steelseries.BackgroundColor.BROWN,
        });
        windOptions = {
            width: wid,
            height: ht,
            maxSpotColor: '',
            minSpotColor: '',
            fillColor: 'red',
            chartRangeMin: 0
        };
        $('.windSparkline').sparkline(windSparkTrue, windOptions);
	radialToggleWindDir.setValueLatest(lastVector[1]);
	radialToggleWindDir.setValueAverage(avgVector[1]);


    } else {
        s.setImage("./js/img/ToggleApp.png");
        displayTrue = 0;
	var lastVector = toggleWindVectorArrayApparent.getVectorLast();
	var avgVector = toggleWindVectorArrayApparent.getVectorAverage();
        lcdToggleWind = new steelseries.DisplayMulti('canvasToggleWind', {
            width: smallWidth,
            height: smallHeight,
            lcdDecimals: 1,
            lcdColor: steelseries.LcdColor.BEIGE,
            linkAltValue: false,
            value: lastVector[0],
            altValue: avgVector[0],
            unitString: "Knots(A)",
            unitStringVisible: true,
            detailString: "Avg: ",
            detailStringVisible: true,
        });

        // wind dir
        radialToggleWindDir = new steelseries.WindDirection('canvasToggleWindDir', {
            size: size,
            titleString: "WIND          APP",
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
        windOptions = {
            width: wid,
            height: ht,
            maxSpotColor: '',
            minSpotColor: '',
            fillColor: "blue",
            chartRangeMin: 0
        };
        $('.windSparkline').sparkline(windSparkApp, windOptions);
	radialToggleWindDir.setValueLatest(lastVector[1]);
	radialToggleWindDir.setValueAverage(avgVector[1]);
    }
//    initToggleWind();
}


function initToggleWind() {
    var pos;
    var vel;
    // if we cant do canvas, skip out here!
    if (!window.CanvasRenderingContext2D)
        return;

    // Initialzing lcds
    vpHeight = window.innerHeight - 50;
    vpWidth = window.innerWidth;

    if (typeof (Storage) == "undefined") {
        // Sorry! No Web Storage support..
        alert("Sorry! No Web Storage support. Please use a different browser.");
        return;
    }
    windSparkArraySize = zk.Widget.$('$sparkPts').getValue();
    while (windSparkArraySize--) {
        windSparkTrue.push(0);
        windSparkApp.push(0);
    }
    if (localStorage.getItem("toggleWind.scale") == null) {
        localStorage.setItem("toggleWind.scale", "1.0");
        localStorage.setItem("toggleWind.size", width);
        localStorage.setItem("toggleWind.top", "0px");
        localStorage.setItem("toggleWind.left", Math.floor(vpWidth / 2) + "px");
        localStorage.setItem("toggleWind.windSparkTrue", JSON.stringify(windSparkTrue));
        localStorage.setItem("toggleWind.windSparkApp", JSON.stringify(windSparkApp));
    }

    windSparkApp = JSON.parse(localStorage.getItem("toggleWind.windSparkApp"))
    windSparkTrue = JSON.parse(localStorage.getItem("toggleWind.windSparkTrue"))

    zk.Widget.$(jq('$toggleWind')[0]).setLeft(localStorage.getItem("toggleWind.left"));
    zk.Widget.$(jq('$toggleWind')[0]).setTop(localStorage.getItem("toggleWind.top"));

    amount = localStorage.getItem("toggleWind.scale");
    size = width * amount;


    // Initialzing gauges
    smallWidth = size;
    smallHeight = size / 3.5;
    $("#twButtons").width(smallWidth);
    $("#twBbuttons").height(smallHeight);
    $("wSpring").width(smallWidth);
    $("wSpring").height(smallWidth * smallHeightFract);
    wid = Math.round(smallWidth) + "px";
    ht = Math.round(smallHeight) + "px";

    // wind app
    // wind
//    radialToggleWindDir = null;
    if (!displayTrue) {
        lcdToggleWind = new steelseries.DisplayMulti('canvasToggleWind', {
            width: smallWidth,
            height: smallHeight,
            lcdDecimals: 1,
            lcdColor: steelseries.LcdColor.BEIGE,
            linkAltValue: false,
            value: 0,
            altValue: 0,
            unitString: "Knots(A)",
            unitStringVisible: true,
            detailString: "Avg: ",
            detailStringVisible: true,
        });

        // wind dir
        radialToggleWindDir = new steelseries.WindDirection('canvasToggleWindDir', {
            size: size,
            titleString: "WIND          APP",
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
        windOptions = {
            width: wid,
            height: ht,
            maxSpotColor: '',
            minSpotColor: '',
            fillColor: 'blue',
//        fillColor: '',
            chartRangeMin: 0
        };
    }

    // wind true
    if (displayTrue) {
        lcdToggleWind = new steelseries.DisplayMulti('canvasToggleWind', {
            width: smallWidth,
            height: smallHeight,
            lcdDecimals: 1,
            lcdColor: steelseries.LcdColor.BEIGE,
            unitString: "Knots(T)",
            value: 0,
            altValue: 0,
            linkAltValue: false,
            unitStringVisible: true,
            detailString: "Avg: ",
            detailStringVisible: true,
        });


        // wind dir

        radialToggleWindDir = new steelseries.WindDirection('canvasToggleWindDir', {
            size: size,
            titleString: "WIND           TRUE",
            roseVisible: false,
            lcdVisible: true,
            lcdColor: steelseries.LcdColor.BEIGE,
            section: areasCloseHaul,
            area: areasCloseHaul,
            pointSymbolsVisible: false,
            // pointSymbols: ["N", "", "", "", "", "", "", ""]
            lcdTitleStrings: ["Latest", "Average"],
            pointerTypeLatest: steelseries.PointerType.TYPE2,
            pointerTypeAverage: steelseries.PointerType.TYPE1,
            backgroundColor: steelseries.BackgroundColor.BROWN,
        });
        windOptions = {
            width: wid,
            height: ht,
            maxSpotColor: '',
            minSpotColor: '',
            fillColor: 'red',
//        fillColor: '',
            chartRangeMin: 0
        };
    }


    addSocketListener(new ToggleWind());

}
