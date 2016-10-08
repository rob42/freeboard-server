/*
 * Copyright 2016 Philip J Freeman <elektron@halo.nu>
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

/*
 * vectorArray is an object definition to track changing vectors of
 * wind speed and direction. All directional angles should be given
 * in degrees (0 =< d < 360.)
 *
 * Usage Example:
 *
 * var windHistory = new vectorArray();
 *
 * while (...) {
 *   windHistory.addVector( [windSpeed, directionDegrees] )
 * }
 *
 * vectorAverage = windHistory.getVectorAverage
 * console.log('windSpeed:'+vectorAverage[0]+' directionDegrees:'+vectorAverage[1]);
 *
 */

function vectorArray() {
    this.maxElements = 15;
    this.vectorArray = [];
    this.pos = 0;

    this.addVector = function(vector) {
        this.vectorArray[this.pos] = vector;
        this.pos++;
        if (this.pos >= this.maxElements)
            this.pos = 0;
    };

    this.getVectorLast = function() {
        if (this.pos == 0) {
	    if (this.vectorArray.length == 0) {
		return [null,null];
	    } else {
	        return this.vectorArray[this.vectorArray.length-1];
	    }
	} else {
	    return this.vectorArray[this.pos-1];
	}
    };

    this.getVectorAverage = function() {
        var count = 0;
        var totX = 0;
        var totY = 0;
        for (var i = 0; i < this.vectorArray.length; i++) {
            if (this.vectorArray[i] != null) {
                count++;
                var spd = this.vectorArray[i][0];
                var dir = this.vectorArray[i][1];
                var dirRad = dir*(Math.PI / 180);
                totX += spd*Math.cos(dirRad)
                totY += spd*Math.sin(dirRad)
            }
        }
        if (count == 0) {
            return [null, null];
        }
        avgX = totX/count;
        avgY = totY/count;

        var avgSpd = Math.sqrt(Math.pow(avgX,2)+Math.pow(avgY,2));
        var avgDir = (Math.atan2(avgY, avgX)/(Math.PI / 180));
        if (avgDir < 0) {
            avgDir +=360;
        }

        //console.log( 'vectorAvg('+vectors+') = '+[avgSpd, avgDir] );
        return [avgSpd, avgDir];
    };
}
