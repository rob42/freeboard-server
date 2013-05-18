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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;

/**
 * Processes IMU sentences in the body of a message, firing events to interested listeners
 * 
 * <ul>
 * <li>!!VER:1.9,RLL:-0.52,PCH:0.06,YAW:80.24,IMUH:253,MGX:44,MGY:-254,MGZ:-257,MGH:80.11,LAT:-412937350,LON:1732472000,ALT:14,COG:116,SOG:0,FIX:1,SAT:5,TOW:
 * 22504700***
 * <li>
 * <li>!!VER:1.9,RLL:0.49,PCH:-0.53,YAW:80.24,IMUH:253,MGX:45,MGY:-253,MGZ:-257,MGH:80.08,TOW:22504700***</LI>
 * <li></li>
 * <li>Roll: Measured in degrees with positive and increasing as the right wing drops</li>
 * <li>Pitch: Measured in degrees with positive and increasing as the nose rises</li>
 * <li>Yaw: Measured in degrees with positive and increasing as the nose goes right</li>
 * <li>IMUH: outputs imu_health?</li>
 * <li>Latitude: Measured in decimal degrees times 10^7</li>
 * <li>Longitude: Measured in decimal degrees times 10^7</li>
 * <li>Altitude: Measured in meters above sea level times 10^1</li>
 * <li>Course over ground:</li>
 * <li>Speed over ground:</li>
 * <li>GPS Fix: A binary indicator of a valid gps fix</li>
 * <li>Satellite Count: The number of GPS satellites used to calculate this position</li>
 * <li>Time of week: Time of week is related to GPS time formats. If this is important to your system, I suggest you read external resources on this confusing
 * topic.</li>
 * </ul>
 * 
 * @author robert
 * 
 */
public class IMUProcessor implements Processor {


	public void process(Exchange exchange) throws Exception {
		if (StringUtils.isEmpty(exchange.getIn().getBody(String.class)))
			return;
		// so we have a string
		String bodyStr = exchange.getIn().getBody(String.class).trim();
		if (bodyStr.startsWith("!!VER:") || bodyStr.startsWith("!!!VER:")) {
			try {
				// trim start/end, add comma
				bodyStr=bodyStr.substring(bodyStr.indexOf(",") + 1, bodyStr.lastIndexOf("***"))+",";
						//LAT:-412937350,LON:1732472000
				//String [] bodyArray=bodyStr.split(",");
				//for(String s:bodyArray){
					
				//}
				exchange.getIn().setBody(bodyStr);

			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}

	

}
