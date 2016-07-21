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
var width = 375;
var height = 240;

function resizeDepth(amount) {
    if (amount == null) {
        amount = localStorage.getItem("depth.scale");
    } else {
        amount = 1*localStorage.getItem("depth.scale") + (1 * amount);
    }
    if (amount == 0.0)
        return;
    console.log("resize entry: amount, wsize, hzixe = "+amount+" "+width*amount+" "+height*amount);
    localStorage.setItem("depth.scale", amount);

    $("#canvasDepth").width(width * amount);
    $("#canvasDepth").height(height * amount);
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

this.depthOnMove = function (event) {
	var e = event;
       localStorage.setItem("depth.top",  event.top+"");
       localStorage.setItem("depth.left", event.left+"");
	return;
}



function depthOnLoad(){
    zk.Widget.$(jq('$depth')[0])._left = localStorage.getItem("depth.left");
    zk.Widget.$(jq('$depth')[0]).setTop(localStorage.getItem("depth.top"));
}

function initDepth() {
    //if we cant do canvas, skip out here!
    if (!window.CanvasRenderingContext2D)
        return;
    // Initialzing lcds
    // depth
    vpHeight = window.innerHeight - 50;
    vpWidth = window.innerWidth;

    if (typeof(Storage) == "undefined") {
    // Sorry! No Web Storage support..
    alert("Sorry! No Web Storage support. Please use a different browser.");
    return;
    }
    if (localStorage.getItem("depth.scale") == null){
        localStorage.setItem("depth.scale", "1.0");
        localStorage.setItem("depth.top",  "0px");
        localStorage.setItem("depth.left", Math.floor(vpWidth/2)+"px");
    }
    zk.Widget.$(jq('$depth')[0]).setLeft(localStorage.getItem("depth.left"));
    zk.Widget.$(jq('$depth')[0]).setTop(localStorage.getItem("depth.top"));

    amount = localStorage.getItem("depth.scale");
    depthUnit = zk.Widget.$('$depthUnit').getValue();
    
    headerString = "Depth " + depthUnit;
    lcdDepth = new steelseries.DisplaySingle('canvasDepth', {
        height: height*amount,
        width: width*amount,
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

