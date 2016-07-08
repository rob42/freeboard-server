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

function resizeDoubleLog(amount) {
    var wsize = $("#canvasDLogSOW").width();
    var hsize = $("#canvasDLogSOG").height();
    console.log("resize entry: amount, wsize, hzixe = "+amount+" "+wsize+" "+hsize);
    if (amount == null) {
        amount = zk.Widget.$('$doubleLogScale').getValue();
    } else {
        amount = 1 + (1 * amount);
    }
    if (amount == 0.0)
        return;
//    var wsize = $("#canvasDLogSOW").width();
//    var hsize = $("#canvasDLogSOG").height();
    $("#canvasDLogSOW").width(wsize * amount);
    $("#canvasDLogSOW").height(hsize * amount);
    $("#canvasDLogSOG").width(wsize * amount);
    $("#canvasDLogSOG").height(hsize * amount);
    console.log("resize before init: amount, wsize, hzixe = "+amount+" "+wsize+" "+hsize);
    this.initDoubleLog();
    console.log("resize after init: amount, wsize, hzixe = "+amount+" "+wsize+" "+hsize);
}

// see http://stackoverflow.com/questions/21731152/resize-zk-modal-programmatically
//


this.dlOnMove = function (event) {
	var e = event;
//	var test = zk.Widget.$(jq('$doubleLog'));
//	var l = zk.Widget.$(jq('$doubleLog')[0])._left;
//	var t = zk.Widget.$(jq('$doubleLog')[0])._top;
	console.log("doubleLog moved (left, top) = "+event.left+ " "+event.top);
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



function initDoubleLog() {
    //if we cant do canvas, skip out here!
    if (!window.CanvasRenderingContext2D)
        return;
    // Initialzing lcds
    vpSize = Math.min(window.innerHeight - 50, window.innerWidth);
    vpHeight = window.innerHeight - 50;
    vpWidth = window.innerWidth;
    console.log("vpHeight = "+ vpHeight + " vpWidth = "+vpWidth);

    wsize = $("#canvasDLogSOW").width();
    hsize = $("#canvasDLogSOG").height();
    amount = zk.Widget.$('$doubleLogScale').getValue();
    console.log("init before: amount, wsize, hsixe = "+amount+" "+wsize+" "+hsize);

    // logs
    sogUnit = zk.Widget.$('$cfgSOGUnit').getValue();
    headerString = "SOG " + sogUnit;
    lcdSOG = new steelseries.DisplaySingle('canvasDLogSOG', {
//        height: vpHeight * .25,
//        width: vpWidth * .30,
        lcdDecimals: 1,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: headerString,
        headerStringVisible: true,
    });

    sowUnit = zk.Widget.$('$cfgSOWUnit').getValue();
    headerString = "SOW " + sowUnit;
    lcdSOW = new steelseries.DisplaySingle('canvasDLogSOW', {
//        height: vpHeight * .25,
//        width: vpWidth * .30,
        lcdDecimals: 1,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: headerString,
        headerStringVisible: true,
    });
    
	 addSocketListener(new DoubleLog());
}
