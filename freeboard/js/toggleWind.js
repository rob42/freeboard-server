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
var avgArrayA;
var avgPosA = 0;
var avgArrayT;
var avgPosT = 0;
//var aveArrayA;
//var avePosA;
//var aveArrayT;
//var avePostT;
var width = 400;
var displayTrue = 0;

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
//    $("#canvasWindDirApp").width(width * amount);
//    $("#canvasWindDirApp").height(width * amount);
    var wsmallSize = width;
    var hsmallSize = width / 3.5;
    $("#canvasToggleWind").width(wsmallSize * amount);
    $("#canvasToggleWind").height(hsmallSize * amount);
//    $("#canvasWindApp").width(wsmallSize * amount);
//    $("#canvasWindApp").height(hsmallSize * amount);
    this.initWind();
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

        if (navObj.WSA) {
            if (!displayTrue) {
                lcdToggleWind.setValue(navObj.WSA);
            }
        }
        if (navObj.WST) {
            if (displayTrue) {
                lcdToggleWind.setValue(navObj.WST);
            }
        }
        if (navObj.WDA) {
            var c = navObj.WDA;
            // -180 <> 180
            if (!displayTrue) {
                if (c > 180) {
                    radialToggleWindDir.setValueAnimatedLatest(-(360 - c));
                } else {
                    radialToggleWindDir.setValueAnimatedLatest(c);
                }
            }

            // make average
            avgArrayA = JSON.parse(localStorage.getItem("avgArrayA"));
            avgPosA = localStorage.getItem("avgPosA");
            avgArrayA[avgPosA] = c;
            avgPosA = avgPosA + 1;
            localStorage.setItem("toggleWind.avgArrayA", JSON.stringify(avgArrayA));
            if (avgPosA >= avgArrayA.length)
                avgPosA = 0;
            localStorage.setItem("toggleWind.avgPosA", avgPosA);
            var v = 0;
            for (var i = 0; i < avgArrayA.length; i++) {
                v = v + avgArrayA[i];
            }
            if (!displayTrue) {
                if (c > 180) {
                    radialToggleWindDirApp
                            .setValueAnimatedAverage(-(360 - (v / avgArrayA.length)));
                } else {
                    radialToggleWindDirApp.setValueAnimatedAverage(v / avgArrayA.length);
                }
            }

            c = null;
        }
        if (navObj.WDT) {
            var c = navObj.WDT;
            if (displayTrue) {
                if (c > 0.0 || c < 360.0)
                    radialToggleWindDir.setValueAnimatedLatest(c);
                else
                    radialToggleWindDir.setValueAnimatedLatest(0.0);
            }

            // make average
            avgArrayT = JSON.parse(localStorage.getItem("toggleWind.avgArrayT"));
            avgPosT = localStorage.getItem("toggleWind.avgPosT");
            gArrayT[avgPosT] = c;
            avgPosT = avgPosT + 1;
            if (avgPosT >= avgArrayT.length)
                avgPosT = 0;
            localStroage.setItem("toggleWind.avgPosT", avgPosT);
            var v = 0.0;
            for (var i = 0; i < avgArrayT.length; i++) {
                v = v + avgArrayT[i];
            }
            if (displayTrue) {
                if (v > 0.0)
                    radialToggleWindDir.setValueAnimatedAverage(v / avgArrayT.length);
                else
                    radialToggleWindDir.setValueAnimatedAverage(0.0);
            }
        }
        c = null;

        data = null;
    };
}

var tackAngle = 45;

function toggle() {
    var s = zk.Widget.$('$twImageToggle');
    if (s._image.includes("./js/img/ToggleApp.png")) {
        s.setImage("./js/img/ToggleTrue.png");
        displayTrue = 1;
    } else {
        s.setImage("./js/img/ToggleApp.png");
        displayTrue = 0;
    }
    initToggleWind();
}

function initToggleWind() {
    // if we cant do canvas, skip out here!
    if (!window.CanvasRenderingContext2D)
        return;

    // Define some sections for wind
    var areasCloseHaul = [
        steelseries.Section((0 - tackAngle), 0, 'rgba(0, 0, 220, 0.3)'),
        steelseries.Section(0, tackAngle, 'rgba(0, 0, 220, 0.3)')];
//    var areasCloseHaulTrue = [
//        steelseries.Section((360 - tackAngle), 0, 'rgba(0, 0, 220, 0.3)'),
//        steelseries.Section(0, tackAngle, 'rgba(0, 0, 220, 0.3)')];

    // Initialzing lcds
    vpHeight = window.innerHeight - 50;
    vpWidth = window.innerWidth;

    if (typeof (Storage) == "undefined") {
        // Sorry! No Web Storage support..
        alert("Sorry! No Web Storage support. Please use a different browser.");
        return;
    }
    if (localStorage.getItem("toggleWind.scale") == null) {
        localStorage.setItem("toggleWind.scale", "1.0");
        localStorage.setItem("toggleWind.size", width);
        localStorage.setItem("toggleWind.top", "0px");
        localStorage.setItem("toggleWind.left", Math.floor(vpWidth / 2) + "px");
        avgArrayA = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0];
        localStorage.setItem("toggleWind.avgArrayA", JSON.stringify(avgArrayA));
        localStorage.setItem("toggleWind.avgPosA", "0");
        var avgArrayT = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0];
        localStorage.setItem("toggleWind.avgPosT", "0");
        localStorage.setItem("toggleWind.avgArrayT", JSON.stringify(avgArrayT));
    }
    zk.Widget.$(jq('$toggleWind')[0]).setLeft(localStorage.getItem("toggleWind.left"));
    zk.Widget.$(jq('$toggleWind')[0]).setTop(localStorage.getItem("toggleWind.top"));

    amount = localStorage.getItem("toggleWind.scale");
    size = width * amount;

    // Initialzing gauges
    smallWidth = size;
    smallHeight = size / 3.5;

    // wind app
    // wind
    radialToggleWindDir = null;
    if (!displayTrue) {
        lcdToggleWind = new steelseries.DisplayMulti('canvasToggleWind', {
            width: smallWidth,
            height: smallHeight,
            lcdDecimals: 1,
            lcdColor: steelseries.LcdColor.BEIGE,
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
    }

    // wind true
    if (displayTrue) {
        lcdToggleWind = new steelseries.DisplayMulti('canvasToggleWind', {
            width: smallWidth,
            height: smallHeight,
            lcdDecimals: 1,
            lcdColor: steelseries.LcdColor.BEIGE,
            unitString: "Knots(T)",
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
    }
    $("#twButtons").width(smallWidth);
    $("#twBbuttons").height(smallHeight);

    addSocketListener(new ToggleWind());

}
