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


	var NZ14600 = L.tileLayer("http://{s}.{server}:8080/mapcache/NZ14600/{z}/{x}/{y}.png", {
		server: host,
		subdomains: 'abcd',
		attribution: 'NZ14600 New Zeland including Norfolk & Campbell ',
		minZoom: 0,
		maxZoom: 5,
		tms: true
		}).addTo(map);


	var NZ61 = L.tileLayer("http://{s}.{server}:8080/mapcache/NZ61/{z}/{x}/{y}.png", {
		server: host,
		subdomains: 'abcd',
		attribution: 'NZ61 Karamea River to Stephens Island',
		minZoom: 7,
		maxZoom: 12,
		tms: true
		}).addTo(map);


	var NZ614 = L.tileLayer("http://{s}.{server}:8080/mapcache/NZ614/{z}/{x}/{y}.png", {
		server: host,
		subdomains: 'abcd',
		attribution: 'NZ614 Tasman Bay',
		minZoom: 8,
		maxZoom: 13,
		tms: true
		}).addTo(map);


	var NZ6144 = L.tileLayer("http://{s}.{server}:8080/mapcache/NZ6144/{z}/{x}/{y}.png", {
		server: host,
		subdomains: 'abcd',
		attribution: 'NZ6144 Abel Tasman',
		minZoom: 9,
		maxZoom: 15,
		tms: true
		}).addTo(map);


	var NZ6142_1 = L.tileLayer("http://{s}.{server}:8080/mapcache/NZ6142_1/{z}/{x}/{y}.png", {
		server: host,
		subdomains: 'abcd',
		attribution: 'NZ6142_1 Nelson Harbour & Entrance',
		minZoom: 10,
		maxZoom: 15,
		tms: true
		}).addTo(map);


	var NZ6142_2 = L.tileLayer("http://{s}.{server}:8080/mapcache/NZ6142_2/{z}/{x}/{y}.png", {
		server: host,
		subdomains: 'abcd',
		attribution: 'NZ6142_2 Nelson Harbour & Entrance - Port Nelson',
		minZoom: 12,
		maxZoom: 17,
		tms: true
		}).addTo(map);


	var US50_1 = L.tileLayer("http://{s}.{server}:8080/mapcache/US50_1/{z}/{x}/{y}.png", {
		server: host,
		subdomains: 'abcd',
		attribution: 'US50_1 NORTH PACIFIC OCEAN   EASTERN PART NU/24',
		minZoom: 1,
		maxZoom: 7,
		tms: true
		}).addTo(map);


	var UK_6560_1 = L.tileLayer("http://{s}.{server}:8080/mapcache/UK_6560_1/{z}/{x}/{y}.png", {
		server: host,
		subdomains: 'abcd',
		attribution: 'UK_6560_1 The Solent - Partie Est Spithead (1:3160',
		minZoom: 9,
		maxZoom: 14,
		tms: true
		}).addTo(map);


	var UK_6560_2 = L.tileLayer("http://{s}.{server}:8080/mapcache/UK_6560_2/{z}/{x}/{y}.png", {
		server: host,
		subdomains: 'abcd',
		attribution: 'UK_6560_2 Southampton Water (1:31600)',
		minZoom: 11,
		maxZoom: 14,
		tms: true
		}).addTo(map);

	baseLayers = {
		"World": WORLD,
	};
	overlays = {
		"Natural Earth": WORLD,
		"Natural Earth 1": WORLD1,
		"NZ14600 New Zeland including Norfolk & Campbell ": NZ14600,
		"NZ61 Karamea River to Stephens Island": NZ61,
		"NZ614 Tasman Bay": NZ614,
		"NZ6144 Abel Tasman": NZ6144,
		"NZ6142_1 Nelson Harbour & Entrance": NZ6142_1,
		"NZ6142_2 Nelson Harbour & Entrance - Port Nelson": NZ6142_2,
		"US50_1 NORTH PACIFIC OCEAN   EASTERN PART NU/24": US50_1,
		"UK_6560_1 The Solent - Partie Est Spithead (1:3160": UK_6560_1,
		"UK_6560_2 Southampton Water (1:31600)": UK_6560_2,
	};
	layers = L.control.layers(baseLayers, overlays).addTo(map);
	};
