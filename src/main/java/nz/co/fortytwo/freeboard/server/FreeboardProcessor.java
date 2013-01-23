/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
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

package nz.co.fortytwo.freeboard.server;


/**
 * Holder for some useful methods for processors
 * @author robert
 *
 */
public class FreeboardProcessor {
	
	/**
	 * Appends key/value pair as KEY:value, 
	 * @param builder
	 * @param key
	 * @param value
	 */
	public void appendValue(StringBuilder builder, String key, Object value){
		builder.append(key);
		builder.append(":");
		builder.append(value);
		builder.append(",");
	}

	/**
	 * Round to specified decimals
	 * @param val
	 * @param places
	 * @return
	 */
	public double round(double val, int places){
		double scale = Math.pow(10, places);
		long iVal = Math.round (val*scale);
		return iVal/scale;
	}
}
