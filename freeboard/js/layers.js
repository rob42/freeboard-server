function addLayers(map) {
	var host = window.location.hostname;

	var WORLD = L.tileLayer("http://{server}:8080/mapcache/WORLD/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: 'Blue Marble',
		minZoom: 0,
		maxZoom: 7,
		tms: true
		}).addTo(map);


	var NZ14600 = L.tileLayer("http://{server}:8080/mapcache/NZ14600/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: 'NZ14600 New Zeland including Norfolk & Campbell ',
		minZoom: 0,
		maxZoom: 5,
		tms: true
		}).addTo(map);


	var NZ61 = L.tileLayer("http://{server}:8080/mapcache/NZ61/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: 'NZ61 Karamea River to Stephens Island',
		minZoom: 7,
		maxZoom: 12,
		tms: true
		}).addTo(map);


	var NZ614 = L.tileLayer("http://{server}:8080/mapcache/NZ614/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: 'NZ614 Tasman Bay',
		minZoom: 8,
		maxZoom: 13,
		tms: true
		}).addTo(map);


	var NZ6144 = L.tileLayer("http://{server}:8080/mapcache/NZ6144/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: 'NZ6144 Abel Tasman',
		minZoom: 9,
		maxZoom: 15,
		tms: true
		}).addTo(map);


	var NZ6142_1 = L.tileLayer("http://{server}:8080/mapcache/NZ6142_1/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: 'NZ6142_1 Nelson Harbour & Entrance',
		minZoom: 10,
		maxZoom: 15,
		tms: true
		}).addTo(map);


	var NZ6142_2 = L.tileLayer("http://{server}:8080/mapcache/NZ6142_2/{z}/{x}/{y}.png", {
		server: host,
		
		attribution: 'NZ6142_2 Nelson Harbour & Entrance - Port Nelson',
		minZoom: 12,
		maxZoom: 17,
		tms: true
		}).addTo(map);

	baseLayers = {
		"World": WORLD,
	};
	overlays = {
		"Blue Marble": WORLD,
		"NZ14600 New Zeland including Norfolk & Campbell ": NZ14600,
		"NZ61 Karamea River to Stephens Island": NZ61,
		"NZ614 Tasman Bay": NZ614,
		"NZ6144 Abel Tasman": NZ6144,
		"NZ6142_1 Nelson Harbour & Entrance": NZ6142_1,
		"NZ6142_2 Nelson Harbour & Entrance - Port Nelson": NZ6142_2,
	};
	layers = L.control.layers(baseLayers, overlays).addTo(map);
	};
