//var mapBounds = new OpenLayers.Bounds( -180.0, -80.9971907157, 179.997075819, 81.0);

function addLayers(map) {
	
	var WORLD = L.tileLayer('http://{s}.'+window.location.host+':8080/mapcache/WORLD/{z}/{x}/{y}.png', {
		subdomains: 'abcd',
	    attribution: 'Blue Marble',
	    minZoom: 0,
	    maxZoom: 7,
	    tms: true,
	    
	}).addTo(map);
	
	var NZ14600 = L.tileLayer('http://{s}.'+window.location.host+':8080/mapcache/NZ14600/{z}/{x}/{y}.png', {
		subdomains: 'abcd',
	    attribution: 'NZ14600 New Zeland including Norfolk & Campbell',
	    minZoom: 0,
	    maxZoom: 5,
	    tms: true
	}).addTo(map);
	
	
	var NZ46 = L.tileLayer("http://{s}.'+window.location.host+':8080/mapcache/NZ46/{z}/{x}/{y}.png", { 
		subdomains: 'abcd',
		minZoom: 7,
		maxZoom: 12,
		 tms: true
		});//.addTo(map);

	var NZ615 = L.tileLayer("http://{s}.'+window.location.host+':8080/mapcache/NZ615/{z}/{x}/{y}.png", {
		subdomains: 'abcd',
		minZoom : 7,
		maxZoom : 13,
		 tms: true
	});//.addTo(map);

	var NZ61 = L.tileLayer("http://{s}.'+window.location.host+':8080/mapcache/NZ61/{z}/{x}/{y}.png", {
		subdomains: 'abcd',
		minZoom : 7,
		maxZoom : 12,
		tms: true
	}).addTo(map);
	var NZ614 = L.tileLayer("http://{s}.'+window.location.host+':8080/mapcache/NZ614/{z}/{x}/{y}.png", { 
		subdomains: 'abcd',
		minZoom : 8,
				maxZoom : 13,
				tms: true
			});//.addTo(map);
	
	var NZ6142_1 = L.tileLayer("http://{s}.'+window.location.host+':8080/mapcache/NZ6142_1/{z}/{x}/{y}.png", { 
		subdomains: 'abcd',
		minZoom : 10,
				maxZoom : 15,
				tms: true
			});//.addTo(map);
	var NZ6142_2 = L.tileLayer("http://{s}.'+window.location.host+':8080/mapcache/NZ6142_2/{z}/{x}/{y}.png", { 
		subdomains: 'abcd',
		minZoom : 12,
				maxZoom : 17,
				tms: true
			});//.addTo(map);
	
	var NZ6144 = L.tileLayer("http://{s}.'+window.location.host+':8080/mapcache/NZ6144/{z}/{x}/{y}.png", { 
		subdomains: 'abcd',
		minZoom : 9,
				maxZoom : 15,
				tms: true
			});//.addTo(map);
	var NZ614_1 = L.tileLayer("http://{s}.'+window.location.host+':8080/mapcache/NZ614_1/{z}/{x}/{y}.png", { 
		subdomains: 'abcd',
		minZoom : 12,
				maxZoom : 18,
				tms: true
			});//.addTo(map);
	
	baseLayers = {
		    "World": WORLD,
	};
	overlays = {
		    //"World": WORLD,
		    "NZ14600 New Zealand including Norfolk & Campbell": NZ14600,
		    "NZ46":NZ46,
		    "NZ615": NZ615,
		    "NZ61": NZ61,
		    "NZ614": NZ614,
		    "NZ6142_1":NZ6142_1,
		    "NZ6142_2":NZ6142_2,
		    "NZ6144":NZ6144,
		    "NZ614_1":NZ614_1,
		};
	layers = L.control.layers(baseLayers, overlays).addTo(map);
};
