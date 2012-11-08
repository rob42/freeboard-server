var lcdLog, radialWindTrue, radialWindDirTrue
odoValue = 99998.2;

function initLogg() {

	// Define some sections for wind
	

	// Define value gradient for bargraph
	valGrad = new steelseries.gradientWrapper(0, 25,
			[ 0, 0.33, 0.66, 0.85, 1 ], [
					new steelseries.rgbaColor(0, 0, 200, 1),
					new steelseries.rgbaColor(0, 200, 0, 1),
					new steelseries.rgbaColor(200, 200, 0, 1),
					new steelseries.rgbaColor(200, 0, 0, 1),
					new steelseries.rgbaColor(200, 0, 0, 1) ]);

	// Initialzing gauges

	// log
	lcdLog = new steelseries.DisplayMulti('canvasLog', {
		// gaugeType : steelseries.GaugeType.TYPE4,
		width : document.getElementById('canvasLog').width,
		height : document.getElementById('canvasLog').height,
		lcdDecimals : 1,
		headerString : "Knots",
		headerStringVisible : true,
		detailString : "Avg: ",
		detailStringVisible : true,
	// unitString:"Knts",
	// unitStringVisible: true

	});
	
	// wind app
	// wind
	lcdHeading = new steelseries.DisplayMulti('canvasHeading', {
		width : document.getElementById('canvasHeading').width,
		height : document.getElementById('canvasHeading').height,
		lcdDecimals : 0,
		headerString : "Heading",
		headerStringVisible : true,
		detailString : "Avg: ",
		detailStringVisible : true,
	});

	// wind dir
	lcdWaypoint = new steelseries.DisplayMulti('canvasWaypoint', {
		width : document.getElementById('canvasWaypoint').width,
		height : document.getElementById('canvasWaypoint').height,
		lcdDecimals : 0,
		headerString : "To Waypoint",
		headerStringVisible : true,
		detailString : "ETA: ",
		detailStringVisible : true,
	});
	lcdWaypoint.setValue(0);

	// make a web socket

	var location = "ws://" + window.location.hostname + ":9090/navData";
	this._ws = new WebSocket(location);
	this._ws.onopen = function() {
	};
	this._ws.onmessage = function(m) {

		if (m.data && m.data.indexOf('LOG') >= 0) {
			var c = m.data.substring(m.data.indexOf('LOG') + 4);
			lcdLog.setValue(parseFloat(c));

		}
		if (m.data && m.data.indexOf('HDG') >= 0) {
			var c = m.data.substring(m.data.indexOf('HDG') + 4);
			lcdHeading.setValue(parseFloat(c));
		}
		if (m.data && m.data.indexOf('WPT') >= 0) {
			var c = m.data.substring(m.data.indexOf('WPT') + 4);
			// lcdWaypoint.setValue(parseFloat(c));
			// -180 <> 180
			if (parseFloat(c) >= 179) {
				lcdWaypoint.setValue(-(360 - parseFloat(c)));
			} else {
				lcdWaypoint.setValue(parseFloat(c));
			}
		}
	};
	this._ws.onclose = function() {
		this._ws = null;
	};
}