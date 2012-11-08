var radialLog, radialWindTrue, radialWindDirTrue
var avgArray = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ];
var avgPos = 0;
function initWind() {

	// Define some sections for wind
	var sections = [ steelseries.Section(0, 20, 'rgba(0, 0, 220, 0.3)'),
			steelseries.Section(20, 35, 'rgba(0, 220, 0, 0.3)'),
			steelseries.Section(35, 75, 'rgba(220,0, 0, 0.3)') ],

	areasCloseHaul = [ steelseries.Section(-45, 0, 'rgba(0, 0, 220, 0.3)'),
			steelseries.Section(0, 45, 'rgba(0, 0, 220, 0.3)') ],
	// Define one area
	areas = [ steelseries.Section(20, 25, 'rgba(220, 0, 0, 0.3)') ],

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
	// radialLog = new steelseries.DisplaySingle('canvasLog', {
	// //gaugeType : steelseries.GaugeType.TYPE4,
	// width : document.getElementById('canvasLog').width,
	// height : document.getElementById('canvasLog').height,
	// lcdDecimals : 1,
	// });

	// wind app
	// wind
	radialWindApp = new steelseries.Radial('canvasWindApp', {
		gaugeType : steelseries.GaugeType.TYPE4,
		size : document.getElementById('canvasWindApp').width,
		minValue : 0,
		maxValue : 60,
		threshold : 35,
		section : sections,
		// area: areas,
		titleString : "WIND APPARENT",
		unitString : "knots",
		lcdVisible : true
	});

	// wind dir
	radialWindDirApp = new steelseries.WindDirection('canvasWindDirApp', {
		size : document.getElementById('canvasWindDirApp').width,
		titleString : "WIND     APP.",
		lcdVisible : false,
		pointSymbolsVisible : false,
		degreeScaleHalf : true,
		section : areasCloseHaul,
		area : areasCloseHaul,
	});
	// radialWindClose.setValue(0);

	// wind true
	radialWindTrue = new steelseries.Radial('canvasWindTrue', {
		gaugeType : steelseries.GaugeType.TYPE4,
		size : document.getElementById('canvasWindTrue').width,
		maxValue : 60,
		threshold : 35,
		section : sections,
		titleString : "WIND TRUE",
		unitString : "knots",
		lcdVisible : true
	});

	// wind dir

	radialWindDirTrue = new steelseries.WindDirection('canvasWindDirTrue', {
		size : document.getElementById('canvasWindDirTrue').width,
		titleString : "WIND     TRUE",
		roseVisible : false,
		lcdVisible : true,
		pointSymbolsVisible : false,
		// pointSymbols: ["N", "", "", "", "", "", "", ""]
		lcdTitleStrings : [ "Latest", "Average" ],
		pointerTypeLatest : steelseries.PointerType.TYPE8,
		pointerTypeAverage : steelseries.PointerType.TYPE1,
	});
	// make a web socket
	var location = "ws://" + window.location.hostname + ":9090/navData"
	this._ws = new WebSocket(location);
	this._ws.onopen = function() {
	};
	this._ws.onmessage = function(m) {
		if (m.data && m.data.indexOf('LOG') >= 0) {
			var c = m.data.substring(m.data.indexOf('LOG') + 4);
			radialLog.setValue(parseFloat(c));
		}
		if (m.data && m.data.indexOf('WSA') >= 0) {
			var c = m.data.substring(m.data.indexOf('WSA') + 4);
			radialWindApp.setValueAnimated(c);
			radialWindTrue.setValueAnimated(c);
		}
		if (m.data && m.data.indexOf('WDA') >= 0) {
			var c = m.data.substring(m.data.indexOf('WDA') + 4);
			// -180 <> 180
			if (parseFloat(c) >= 179) {
				radialWindDirApp.setValueAnimated(-(360 - c));
			} else {
				radialWindDirApp.setValueAnimated(c);
			}
			// radialWindDirApp.setValueAnimated(c);
			// radialWindClose.setValueAnimated(c);
			radialWindDirTrue.setValueAnimatedLatest(c);
			// make average
			avgArray[avgPos] = parseFloat(c);
			avgPos = avgPos + 1;
			if (avgPos >= avgArray.length)
				avgPos = 0;
			var v = 0;
			for ( var i = 0; i < avgArray.length; i++) {
				v = v + avgArray[i];
			}
			radialWindDirTrue.setValueAnimatedAverage(v / avgArray.length);
		}
	};
	this._ws.onclose = function() {
		this._ws = null;
	};
}
