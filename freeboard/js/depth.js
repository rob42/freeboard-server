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

var anAlarm;
var options;
var width;
var height;

function resizeDepth(amount) {
    var wsize = $("#canvasDepth").width();
    var hsize = $("#canvasDepth").height();
    if (amount == null) {
        amount = zk.Widget.$('$depthScale').getValue();
    } else {
        amount = 1 + (1 * amount);
    }
    if (amount == 0.0)
        return;


    $("#canvasDepth").width(wsize * amount);
    $("#canvasDepth").height(hsize * amount);
    initDepth();
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
//                    height: 200,
//                    width: 200,
                    lcdDecimals: 1,
                    lcdColor: steelseries.LcdColor.BEIGE,
                    headerString: headerString,
                    headerStringVisible: true,
                    unitString: depthUnit,
                    unitStringVisible: false
                });
            }
            lcdDepth.setValue(navObj.DBT);

            anAlarm = zk.Widget.$('$alarmDepth').getValue();
            if (navObj.DBT < anAlarm) {
                lcdDepth.setLcdColor(steelseries.LcdColor.RED);
            } else {
                lcdDepth.setLcdColor(steelseries.LcdColor.BEIGE);
            }
        }
    };

}



function initDepth() {
    //if we cant do canvas, skip out here!
    if (!window.CanvasRenderingContext2D)
        return;
    // Initialzing lcds
    // depth

    amount = zk.Widget.$('$depthScale').getValue();
    depthUnit = zk.Widget.$('$depthUnit').getValue();
    width = zk.Widget.$('$depthWidth').getValue()*amount;
    height = zk.Widget.$('$depthHeight').getValue()*amount;
    headerString = "Depth " + depthUnit;
    lcdDepth = new steelseries.DisplaySingle('canvasDepth', {
        height: height,
        width: width,
        lcdDecimals: 1,
        lcdColor: steelseries.LcdColor.BEIGE,
        headerString: headerString,
        headerStringVisible: true,
        unitString: depthUnit,
        unitStringVisible: false
    });


    anAlarm = zk.Widget.$('$alarmDepth').getValue();
    addSocketListener(new Depth());
}

