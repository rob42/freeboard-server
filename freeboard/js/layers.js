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


	var _1552_1 = L.tileLayer("http://{server}:8080/mapcache/11552_1/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: '11552_1 NEUSE RIVER AND UPPER PART OF BAY RIVER ',
		minZoom: 10,
		maxZoom: 15,
		tms: true
		}).addTo(map);


	var _1552_2 = L.tileLayer("http://{server}:8080/mapcache/11552_2/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: '11552_2 CONTINUATION OF TRENT RIVER HAYWARD CREE',
		minZoom: 12,
		maxZoom: 15,
		tms: true
		}).addTo(map);


	var _1552_3 = L.tileLayer("http://{server}:8080/mapcache/11552_3/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: '11552_3 CONTINUATION OF TRENT RIVER POLLOCKSVILL',
		minZoom: 12,
		maxZoom: 15,
		tms: true
		}).addTo(map);


	var _1552_4 = L.tileLayer("http://{server}:8080/mapcache/11552_4/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: '11552_4 CONTINUATION OF NEUSE RIVER NU/518 RA/41',
		minZoom: 15,
		maxZoom: 18,
		tms: true
		}).addTo(map);

	baseLayers = {
		"World": WORLD,
	};
	overlays = {
		"Natural Earth": WORLD,
		"Natural Earth 1": WORLD1,
		"11552_1 NEUSE RIVER AND UPPER PART OF BAY RIVER ": _1552_1,
		"11552_2 CONTINUATION OF TRENT RIVER HAYWARD CREE": _1552_2,
		"11552_3 CONTINUATION OF TRENT RIVER POLLOCKSVILL": _1552_3,
		"11552_4 CONTINUATION OF NEUSE RIVER NU/518 RA/41": _1552_4,
	};
	layers = L.control.layers(baseLayers, overlays).addTo(map);
	};
