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
//this needed before leaflet code is loaded so we do it here for now
L_PREFER_CANVAS = true;

var _ws;
var _comet;
var wsList = [];
var popped = false;

function addSocketListener(l){
	//is it already there, if so remove it
	$.each(wsList, function(i){
	    if(wsList[i] &&  wsList[i].constructor === l.constructor) wsList.splice(i,1);
	});
	//add new
	wsList.push(l);
}

function initSocket(){
	if ("WebSocket" in window){
		console.log("Starting websockets");
		initWebSocket();
	}else{
		console.log("Starting comet");
		initComet();
	}
}

function initComet(){
	if(_comet== null){
		_comet = new FreeboardComet();
	}
}

function initWebSocket(){
	//make a web socket
	if(this._ws == null) {
	
			var location = "ws://"+window.location.hostname+":9090/navData";
			//alert(location);
			
			this._ws = new WebSocket(location);
			this._ws.binaryType = "arraybuffer";
			this._ws.onopen = function() {
			};
			this._ws.onmessage = function(m) {
				//for debug
				//console.log(JSON.stringify(m.data));
				var mObj = $.parseJSON(m.data);
				
				//TODO: Note memory leak in native websockets code  - https://code.google.com/p/chromium/issues/detail?id=146304
				
				//var mArray=m.data.trim().split(",");
				jQuery.each(wsList, function(i, obj) {
				      obj.onmessage(mObj);
				  });
				//mArray=null;
				m=null;
			};
			this._ws.onclose = function() {
				this._ws = null;
			};
			this._ws.onerror = function(error) {
				popped = true;
				alert('Cannot connect to Freeboard server');
				popped=false;
			};
 
	}
}

function reloadSocket(){
	if(this._ws != null)this._ws.close();
	//if(this._comet != null)this._comet.leave();
	this._ws = null;
	//this._comet = null;
	if(!popped){
		initSocket();
		console.log("Reloaded..");
	}
}

setInterval(function(){reloadSocket();},60000);

