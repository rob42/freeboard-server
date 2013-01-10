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

/**
 * Calculates the true wind from apparent wind and vessel speed/heading
 * @author robert
 *
 */
public class WindProcessor implements Processor {
	double apparentWind=0;
	int apparentDirection=0;
	float vesselSpeed=0;
	
	public void process(Exchange exchange) throws Exception {
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
					if(!Double.isNaN(trueWindDir)){
						bodyStr=bodyStr+"WDT:"+round(trueWindDir,1)+",";
					}
					if(!Double.isNaN(trueWindSpeed)){
						bodyStr=bodyStr+"WST:"+round(trueWindSpeed,2)+",";
					}
					exchange.getOut().setBody(bodyStr);
				}

			} catch (Exception e) {
				// e.printStackTrace();
			}
		

	}
	
	double round(double val, int places){
		double scale = Math.pow(10, places);
		long iVal = Math.round (val*scale);
		return iVal/scale;
	}
	
	/**
	 * Calculates the true wind direction from apparent wind on vessel
	 * Result is relative to bow
	 * 
	 * @param apparentWind
	 * @param apparentDirection 0 to 360 deg to the bow
	 * @param vesselSpeed
	 * @return trueDirection 0 to 360 deg to the bow
	 */
	double calcTrueWindDirection(double apparentWind, int apparentDirection, float vesselSpeed){
		/*
			 Y = 90 - D
			a = AW * ( cos Y )
			bb = AW * ( sin Y )
			b = bb - BS
			True-Wind Speed = (( a * a ) + ( b * b )) 1/2
			True-Wind Angle = 90-arctangent ( b / a )
		*/
                apparentDirection=apparentDirection%360;
		boolean stbd = apparentDirection<=180;
        if(!stbd){
           apparentDirection=360-apparentDirection; 
        }
		double y = 90-apparentDirection;
		double a = apparentWind * Math.cos(Math.toRadians(y));
		double b = (apparentWind * Math.sin(Math.toRadians(y)))-vesselSpeed;
		double td = 90-Math.toDegrees(Math.atan((b/a)));
		if(!stbd)return (360-td);
		return td;
				
	}
	
	/**
	 * Calculates the true wind speed from apparent wind speed on vessel
	 * 
	 * @param apparentWind
	 * @param apparentDirection 0 to 360 deg to the bow
	 * @param vesselSpeed
	 * @return
	 */
        double calcTrueWindSpeed(double apparentWind, int apparentDirection, float vesselSpeed){
                apparentDirection=apparentDirection%360;
                if(apparentDirection>180){
                   apparentDirection=360-apparentDirection; 
                }
		double y = 90-apparentDirection;
		double a = apparentWind * Math.cos(Math.toRadians(y));
		double b = (apparentWind * Math.sin(Math.toRadians(y)))-vesselSpeed;
		return (Math.sqrt((a*a)+(b*b)));
	}



}
