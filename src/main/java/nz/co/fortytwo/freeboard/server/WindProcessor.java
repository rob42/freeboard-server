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

import nz.co.fortytwo.freeboard.server.util.Constants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;

/**
 * Calculates the true wind from apparent wind and vessel speed/heading
 * @author robert
 *
 */
public class WindProcessor extends FreeboardProcessor implements Processor {
	double apparentWind=0;
	int apparentDirection=0;
	float vesselSpeed=0;
	
	public void process(Exchange exchange) throws Exception {
		if (StringUtils.isEmpty(exchange.getIn().getBody(String.class)))
			return;
		
		String bodyStr = exchange.getIn().getBody(String.class).trim();
		
			try {
				
				boolean valid=false;
				//int heading=0;
				String [] bodyArray=bodyStr.split(",");
				for(String s:bodyArray){
					// we need HDG, and LOG
					if(s.startsWith(Constants.SOG)){
						vesselSpeed= Float.valueOf(s.substring(4));
						valid=true;
					}
					if(s.startsWith(Constants.WSA)){
						apparentWind= Double.valueOf(s.substring(4));
						valid=true;
					}
					if(s.startsWith(Constants.WDA)){
						apparentDirection= Integer.valueOf(s.substring(4));
						valid=true;
					}
					//if(s.startsWith(Constants.COG)){
					//	heading= Integer.valueOf(s.substring(4));
					//}
				}
				if(valid){
					//now calc and add to body
					double trueWindSpeed = calcTrueWindSpeed(apparentWind, apparentDirection, vesselSpeed);
					double trueWindDir = calcTrueWindDirection(apparentWind,apparentDirection, vesselSpeed);
					StringBuilder builder = new StringBuilder(bodyStr);
					if(!Double.isNaN(trueWindDir)){
						appendValue(builder, Constants.WDT, round(trueWindDir,1));
					}
					if(!Double.isNaN(trueWindSpeed)){
						appendValue(builder, Constants.WST, round(trueWindSpeed,2));
					}
					exchange.getOut().setBody(builder.toString());
				}

			} catch (Exception e) {
				// e.printStackTrace();
			}
		

	}
	
	
	
	/**
	 * Calculates the true wind direction from apparent wind on vessel
	 * Result is relative to bow
	 * 
	 * @param apparentWnd
	 * @param apparentDir 0 to 360 deg to the bow
	 * @param vesselSpd
	 * @return trueDirection 0 to 360 deg to the bow
	 */
	double calcTrueWindDirection(double apparentWnd, int apparentDir, float vesselSpd){
		/*
			 Y = 90 - D
			a = AW * ( cos Y )
			bb = AW * ( sin Y )
			b = bb - BS
			True-Wind Speed = (( a * a ) + ( b * b )) 1/2
			True-Wind Angle = 90-arctangent ( b / a )
		*/
                apparentDir=apparentDir%360;
		boolean stbd = apparentDir<=180;
        if(!stbd){
           apparentDir=360-apparentDir; 
        }
		double y = 90-apparentDir;
		double a = apparentWnd * Math.cos(Math.toRadians(y));
		double b = (apparentWnd * Math.sin(Math.toRadians(y)))-vesselSpd;
		double td = 90-Math.toDegrees(Math.atan((b/a)));
		if(!stbd)return (360-td);
		return td;
				
	}
	
	/**
	 * Calculates the true wind speed from apparent wind speed on vessel
	 * 
	 * @param apparentWnd
	 * @param apparentDir 0 to 360 deg to the bow
	 * @param vesselSpd
	 * @return
	 */
        double calcTrueWindSpeed(double apparentWnd, int apparentDir, float vesselSpd){
                apparentDir=apparentDir%360;
                if(apparentDir>180){
                   apparentDir=360-apparentDir; 
                }
		double y = 90-apparentDir;
		double a = apparentWnd * Math.cos(Math.toRadians(y));
		double b = (apparentWnd * Math.sin(Math.toRadians(y)))-vesselSpd;
		return (Math.sqrt((a*a)+(b*b)));
	}



}
