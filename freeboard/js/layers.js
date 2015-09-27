function addLayers(map) {
	var host = window.location.hostname;

	var WORLD = L.tileLayer("http://{s}.{server}:8080/mapcache/WORLD/{z}/{x}/{y}.png", {
		server: host,
		subdomains: 'abcd',
		attribution: 'Natural Earth',
		minZoom: 0,
		maxZoom: 4,
		tms: true
		}).addTo(map);


	var WORLD1 = L.tileLayer("http://{s}.{server}:8080/mapcache/WORLD1/{z}/{x}/{y}.png", {
		server: host,
		subdomains: 'abcd',
		attribution: 'Natural Earth 1',
		minZoom: 3,
		maxZoom: 6,
		tms: true
		}).addTo(map);


	var _1552_1 = L.tileLayer("http://{s}.{server}:8080/mapcache/11552_1/{z}/{x}/{y}.png", {
		server: host,
		subdomains: 'abcd',
		attribution: '11552_1 NEUSE RIVER AND UPPER PART OF BAY RIVER ',
		minZoom: 10,
		maxZoom: 14,
		tms: true
		}).addTo(map);

	baseLayers = {
		"World": WORLD,
	};
	overlays = {
		"Natural Earth": WORLD,
		"Natural Earth 1": WORLD1,
		"11552_1 NEUSE RIVER AND UPPER PART OF BAY RIVER ": _1552_1,
	};
	layers = L.control.layers(baseLayers, overlays).addTo(map);
	};
