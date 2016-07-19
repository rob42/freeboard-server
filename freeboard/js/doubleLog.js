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

var vpSize;
var vpHeight;
var vpWidth;
var sogUnit;
var sowUnit;
var width = 375;
var height = 250;
var initDone = false;

function resizeDoubleLog(amount) {
//    var wsize = $("#canvasDLogSOW").width();
//    var hsize = $("#canvasDLogSOW").height();
    var wsize = localStorage.getItem("doubleLog.width");
    var hsize = localStorage.getItem("doubleLog.height");
    console.log("resize entry: amount, wsize, hzixe = "+amount+" "+wsize+" "+hsize);
    if (amount == null) {
        amount = localStorage.getItem("doubleLog.scale");
    } else {
        amount = 1*localStorage.getItem("doubleLog.scale") + (1 * amount);
    }
    if (amount == 0.0)
        return;
    localStorage.setItem("doubleLog.scale", amount);
    $("#canvasDLogSOW").width(wsize * amount);
    $("#canvasDLogSOW").height(hsize * amount);
    $("#canvasDLogSOG").width(wsize * amount);
    $("#canvasDLogSOG").height(hsize * amount);
    this.initDoubleLog();
}

// see http://stackoverflow.com/questions/21731152/resize-zk-modal-programmatically
//


this.dlOnMove = function (event) {
	var e = event;
	console.log("doubleLog moved (left, top) = "+event.left+ " "+event.top);
   if (initDone){
       localStorage.setItem("doubleLog.top",  event.top+"");
       localStorage.setItem("doubleLog.left", event.left+"");
   }
	return;
}

function DoubleLog() {

	this.onmessage = function (navObj) {
        //avoid commands
        if (!navObj)
            return true;

        //SOG
        if (navObj.SOG) {
            //            console.log("navObj.SOG = " + navObj.SOG.toString());
            unitTemp = zk.Widget.$('$cfgSOGUnit').getValue();
            if (unitTemp !== sogUnit) {
                headerString = "SOG " + unitTemp;
                sogUnit = unitTemp;
                console.log("SOG DisplaySingle");
                lcdSOG = new steelseries.DisplaySingle('sailSOG', {
//                    height: vpHeight * .25,
//                    width: vpWidth * .30,
                    lcdDecimals: 1,
                    lcdColor: steelseries.LcdColor.BEIGE,
                    headerString: headerString,
                    headerStringVisible: true,
                });
            }
            console.log("SOG= " + navObj.SOG);
            temp = distanceUnit(sogUnit)*navObj.SOG;
            lcdSOG.setValue(temp);
            console.log("AdjSOG= " + temp);
        }

        //SOW
        if (navObj.SOW) {
            unitTemp = zk.Widget.$('$cfgSOWUnit').getValue();
            if (unitTemp !== sowUnit) {
                headerString = "SOW " + unitTemp;
                sowUnit = unitTemp;
                console.log("SOW DisplaySingle");
                lcdSOW = new steelseries.DisplaySingle('sailLog', {
//                    height: vpHeight * .25,
//                    width: vpWidth * .30,
                    lcdDecimals: 1,
                    lcdColor: steelseries.LcdColor.BEIGE,
                    headerString: headerString,
                    headerStringVisible: true,
                });
            }
            lcdSOW.setValue(distanceUnit(sowUnit)*navObj.SOW);
        }
    };

}

function onLoad(){
    zk.Widget.$(jq('$doubleLog')[0])._left = localStorage.getItem("doubleLog.left");
    zk.Widget.$(jq('$doubleLog')[0]).setTop(localStorage.getItem("doubleLog.top"));
	 varL = zk.Widget.$(jq('$doubleLog')[0])._left;
	 varT = zk.Widget.$(jq('$doubleLog')[0])._top;
	 initDoubleLog();
}

function initDoubleLog() {
    //if we cant do canvas, skip out here!
    if (!window.CanvasRenderingContext2D)
        return;
    // Initialzing lcds
    if (typeof(Storage) == "undefined") {
    // Sorry! No Web Storage support..
    alert("Sorry! No Web Storage support. Please use a different browser.");
    return;
    }
    if (localStorage.getItem("doubleLog.scale") == null){
        localStorage.setItem("doubleLog.scale", "1.0");
        localStorage.setItem("doubleLog.width", width);
        localStorage.setItem("doubleLog.height", height);
        localStorage.setItem("doubleLog.top",  zk.Widget.$(jq('$doubleLog')[0])._top);
        localStorage.setItem("doubleLog.left", zk.Widget.$(jq('$doubleLog')[0])._left);
    }
    zk.Widget.$(jq('$doubleLog')[0]).setLeft(localStorage.getItem("doubleLog.left"));
    zk.Widget.$(jq('$doubleLog')[0]).setTop(localStorage.getItem("doubleLog.top"));
	 varL = zk.Widget.$(jq('$doubleLog')[0])._left;
	 varT = zk.Widget.$(jq('$doubleLog')[0])._top;

	 var test = zk.Widget.$(jq('$doubleLog')[0]).id;
	 amount = localStorage.getItem("doubleLog.scale");
    width = localStorage.getItem("doubleLog.width")*amount;
    height = localStorage.getItem("doubleLog.height")*amount;
//    console.log("init: amount, width, height , left, top= "+amount+" "+width+" "+height+ " "+ zk.Widget.$(jq('$doubleLog')[0])._left + " "+zk.Widget.$(jq('$doubleLog')[0])._top);

    // logs
    sogUnit = zk.Widget.$('$cfgSOGUnit').getValue();
    headerString = "SOG " + sogUnit;
    lcdSOG = new steelseries.DisplaySingle('canvasDLogSOG', {
        height: height,
        width: width,
        lcdDecimals: 1,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: headerString,
        headerStringVisible: true,
    });

    sowUnit = zk.Widget.$('$cfgSOWUnit').getValue();
    headerString = "SOW " + sowUnit;
    lcdSOW = new steelseries.DisplaySingle('canvasDLogSOW', {
        height: height,
        width: width,
        lcdDecimals: 1,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: headerString,
        headerStringVisible: true,
    });

	 addSocketListener(new DoubleLog());
         initDone = true;
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
