//var  lcdWindTrue, radialWindDirTrue
var avgArray = [ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ];
var avgPos = 0;

function resizeWind(amount){
	var size = $("#canvasWindDirTrue").width();
	$("#canvasWindDirTrue").width(size+(size*amount));
	$("#canvasWindDirTrue").height(size+(size*amount));
	$("#canvasWindDirApp").width(size+(size*amount));
	$("#canvasWindDirApp").height(size+(size*amount));
	var wsmallSize =  $("#canvasWindTrue").width();
	var hsmallSize =  $("#canvasWindTrue").height();
	$("#canvasWindTrue").width(wsmallSize+(wsmallSize*amount));
	$("#canvasWindTrue").height(hsmallSize+(hsmallSize*amount));
	$("#canvasWindApp").width(wsmallSize+(wsmallSize*amount));
	$("#canvasWindApp").height(hsmallSize+(hsmallSize*amount));
	this.initWind();
	
}
function Wind2 () {
	this.onmessage = function (m) {
		var mArray=m.data.split(",");
		jQuery.each(mArray, function(i, data) {
			if (data && data.indexOf('WSA') >= 0) {
				var c = data.substring(data.indexOf('WSA') + 4);
				lcdWindApp.setValue(parseFloat(c));
				lcdWindTrue.setValue(parseFloat(c));
			}
			if (data && data.indexOf('WDA') >= 0) {
				var c = data.substring(data.indexOf('WDA') + 4);
				// -180 <> 180
				if (parseFloat(c) >= 179) {
					radialWindDirApp.setValueAnimatedLatest(-(360 - c));
				} else {
					radialWindDirApp.setValueAnimatedLatest(c);
				}
				
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
		});
	}
}

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

	// wind app
	// wind
	lcdWindApp = new steelseries.DisplayMulti('canvasWindApp', {
		width : document.getElementById('canvasWindApp').width,
		height : document.getElementById('canvasWindApp').height,
		lcdDecimals : 1,
		lcdColor: steelseries.LcdColor.BEIGE,
		unitString : "Knots(A)",
		unitStringVisible : true,
		detailString : "Avg: ",
		detailStringVisible : true,
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
		backgroundColor: steelseries.BackgroundColor.CARBON,
	});
	
	// wind true
	lcdWindTrue = new steelseries.DisplayMulti('canvasWindTrue', {
		width : document.getElementById('canvasWindTrue').width,
		height : document.getElementById('canvasWindTrue').height,
		lcdDecimals : 1,
		lcdColor: steelseries.LcdColor.BEIGE,
		unitString : "Knots(T)",
		unitStringVisible : true,
		detailString : "Avg: ",
		detailStringVisible : true,
	});

	// wind dir

	radialWindDirTrue = new steelseries.WindDirection('canvasWindDirTrue', {
		size : document.getElementById('canvasWindDirTrue').width,
		titleString : "WIND     TRUE",
		roseVisible : false,
		lcdVisible : true,
		lcdColor: steelseries.LcdColor.BEIGE,
		pointSymbolsVisible : false,
		// pointSymbols: ["N", "", "", "", "", "", "", ""]
		lcdTitleStrings : [ "Latest", "Average" ],
		pointerTypeLatest : steelseries.PointerType.TYPE8,
		pointerTypeAverage : steelseries.PointerType.TYPE1,
		backgroundColor: steelseries.BackgroundColor.CARBON,
	});
	
	wsList.push(new Wind2());
	
}
