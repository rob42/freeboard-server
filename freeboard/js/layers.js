function addLayers(map) {
	var host = window.location.hostname;

	var WORLD = L.tileLayer("http://{server}:8080/mapcache/WORLD/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: 'Natural Earth',
		minZoom: 0,
		maxZoom: 4,
		tms: true
		}).addTo(map);


	var WORLD1 = L.tileLayer("http://{server}:8080/mapcache/WORLD1/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: 'Natural Earth 1',
		minZoom: 3,
		maxZoom: 6,
		tms: true
		}).addTo(map);


	var NZ14600 = L.tileLayer("http://{server}:8080/mapcache/NZ14600/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: 'NZ14600 New Zeland including Norfolk & Campbell ',
		minZoom: 0,
		maxZoom: 5,
		tms: true
		}).addTo(map);


	var NZ614 = L.tileLayer("http://{server}:8080/mapcache/NZ614/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: 'NZ614 Tasman Bay',
		minZoom: 8,
		maxZoom: 13,
		tms: true
		}).addTo(map);

	baseLayers = {
		"World": WORLD,
	};
	overlays = {
		"Natural Earth": WORLD,
		"Natural Earth 1": WORLD1,
		"NZ14600 New Zeland including Norfolk & Campbell ": NZ14600,
		"NZ614 Tasman Bay": NZ614,
	};
	layers = L.control.layers(baseLayers, overlays).addTo(map);
	};
