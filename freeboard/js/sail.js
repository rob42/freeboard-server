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

   
function Sail() {
   this.onmessage = function(navObj) {

      //avoid commands
      if (!navObj)
         return true;

      //depth
      if (navObj.DBT) {
         lcdSailDepth.setValue(navObj.DBT);
             console.log(navObj.DBT+"\n"+depthArray);
             depthArray.shift();
             console.log(depthArray);
             depthArray.push(navObj.DBT);
             anAlarm = zk.Widget.$('$alarmDepth').getValue();
             sPoints = zk.Widget.$('$sparkPts').getValue();
            if (navObj.DBT < anAlarm){
                lcdSailDepth.setLcdColor(steelseries.LcdColor.RED);
            } else {
                lcdSailDepth.setLcdColor(steelseries.LcdColor.BEIGE);
            }

        $('.dynamicsparkline').sparkline(depthArray,options);

      }
      //speed
      if (navObj.SOG) {
         lcdSOG.setValue(navObj.SOG);
      }
      if (navObj.SOW) {
         lcdSOW.setValue(navObj.SOW);
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
   var vpSize = Math.min(window.innerHeight-50, window.innerWidth);
   var vpHeight = window.innerHeight-50;
   var vpWidth = window.innerWidth;

   // Depth 
   //if we cant do canvas, skip out here!
   if (!window.CanvasRenderingContext2D)
      return;
   // 
   // Initialzing displays
   var tackAngle = 45;
   var areasCloseHaul = [
      steelseries.Section((0 - tackAngle), 0, 'rgba(0, 0, 220, 0.3)'),
      steelseries.Section(0, tackAngle, 'rgba(0, 0, 220, 0.3)')];
   var areasCloseHaulTrue = [
      steelseries.Section((360 - tackAngle), 0, 'rgba(0, 0, 220, 0.3)'),
      steelseries.Section(0, tackAngle, 'rgba(0, 0, 220, 0.3)')];

   // Initialzing gauges

   // wind dir apparent

   vpSize = Math.min(window.innerWidth*.30, (window.innerHeight-50)*.75);
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

   depthUnit = zk.Widget.$('$depthUnit').getValue();
   var depthString = "Depth " + depthUnit ;
   
    // init Sparkline array
    depthArraySize = zk.Widget.$('$sparkPts').getValue();
    while(depthArraySize--) depthArray.push(6);

   lcdSailDepth = new steelseries.DisplaySingle('sailDepth', {
      height: vpHeight * .25,
      width: vpWidth* .30,
      lcdDecimals: 1,
      lcdColor: steelseries.LcdColor.BEIGE,
      headerString: depthString,
      headerStringVisible: true,
      unitString: depthUnit,
      unitStringVisible: false
   });
   
   // Set width of spring div
   $("#spring").width(vpWidth*0.3);
   $("#spring").height(vpHeight*.10);
   wid = Math.round(vpWidth*0.3)+"px";
   ht = Math.round(vpHeight*.10)+"px";

   options = {width: wid, height: ht, maxSpotColor:'', minSpotColor:''};

    // log
   lcdSOW = new steelseries.DisplaySingle('sailLog', {
      height: vpHeight * .25,
      width: vpWidth*.30,
      lcdDecimals: 1,
      lcdColor: steelseries.LcdColor.BEIGE,
      headerString: "SOW Knots  ",
      headerStringVisible: true,
   });

   // wind app

   lcdWindApp = new steelseries.DisplayMulti('sailWind', {
      // width : document.getElementById('canvasWindApp').width,
      // : document.getElementById('canvasWindApp').height,
      height: vpHeight * .25,
      width: vpWidth*.30,
      lcdDecimals: 1,
      lcdColor: steelseries.LcdColor.BEIGE,
      headerString: "Knots(A)",
      headerStringVisible: true,
      detailString: "Avg: ",
      detailStringVisible: true,
   });

   // GPS SOG
   lcdSOG = new steelseries.DisplaySingle('sailSOG', {
      height: vpHeight * .25,
      width: vpWidth*.30,
      lcdDecimals: 1,
      lcdColor: steelseries.LcdColor.BEIGE,
      headerString: "SOG Knots",
      headerStringVisible: true,
      detailString: "Ave: ",
      detailStringVisible: true
   });
   
// make a web socket

   addSocketListener(new Sail());
}