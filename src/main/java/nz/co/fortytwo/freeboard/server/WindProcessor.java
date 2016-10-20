/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 * 
 * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
 * 
 * FreeBoard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FreeBoard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FreeBoard. If not, see <http://www.gnu.org/licenses/>.
 */
package nz.co.fortytwo.freeboard.server;

import java.util.HashMap;

import nz.co.fortytwo.freeboard.server.util.Constants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

/**
 * Calculates the true wind from apparent wind and vessel speed/heading
 * 
 * @author robert
 * 
 */
public class WindProcessor extends FreeboardProcessor implements Processor, FreeboardHandler {

	private static Logger logger = Logger.getLogger(WindProcessor.class);

	double apparentWindSpeed = 0.0;
	// 0-360 from bow clockwise
	double apparentDirection = 0;
	double vesselSpeed = 0;
	// 0-360 from bow clockwise
	double trueDirection = 0.0;
	double trueWindSpeed = 0.0;

	public void process(Exchange exchange) throws Exception {
		if (exchange.getIn().getBody() == null)
			return;
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = exchange.getIn().getBody(HashMap.class);
		map = handle(map);
		exchange.getIn().setBody(map);
	}

	@Override
	public HashMap<String, Object> handle(HashMap<String, Object> map) {
		try {

			boolean valid = false;
			// int heading=0;

			// we need HDG, and LOG
			if (map.containsKey(Constants.SPEED_OVER_GND)) {
				vesselSpeed = (Double) map.get(Constants.SPEED_OVER_GND);
				valid = true;
			}
			if (map.containsKey(Constants.WIND_SPEED_APPARENT)) {
				apparentWindSpeed = (Double) map.get(Constants.WIND_SPEED_APPARENT);
				valid = true;
			}
			if (map.containsKey(Constants.WIND_DIR_APPARENT)) {
				apparentDirection = (Double) map.get(Constants.WIND_DIR_APPARENT);
				valid = true;
			}
			// if(s.startsWith(Constants.COG)){
			// heading= Integer.valueOf(s.substring(4));
			// }

			if (valid) {
				// now calc and add to body
				calcTrueWindDirection(apparentWindSpeed, apparentDirection, vesselSpeed);

				if (!Double.isNaN(trueDirection)) {
					map.put(Constants.WIND_DIR_TRUE, round(trueDirection, 2));
				}
				if (!Double.isNaN(trueWindSpeed)) {
					map.put(Constants.WIND_SPEED_TRUE, round(trueWindSpeed, 2));
				}

			}

		} catch (Exception e) {
			logger.error(e);
		}
		return map;
	}

	/**
	 * Calculates the true wind direction from apparent wind on vessel
	 * Result is relative to bow
	 * 
	 * @param apparentWnd
	 * @param apparentDir CCW = + (0 --> 180) CW = - (0 --> -180)
	 * @param vesselSpd
	 * @return trueDirection 0 to 360 deg to the bow CW = + (0 --> 360)
	 */
	void calcTrueWindDirection(double apparentWnd, double apparentDir, double vesselSpd) {
		
                // Switch to standard mathematical sign onvention CCW = +
                double theta = -apparentDir;
                if (theta < 0.0d){
                    theta = theta+360.;
                }
                theta = theta % 360.;
		double appX = apparentWnd*Math.cos(Math.toRadians(theta));
                double appY = apparentWnd*Math.sin(Math.toRadians(theta));
                double trueX = appX - vesselSpd;
                if (trueX == 0.0){
                    trueDirection = 0;
                } else {
                    trueDirection = Math.toDegrees(Math.atan(appY/trueX));
                }
                
                // atan gives result between -Pi/2 and Pi/2
                // we need to assign the proper quadrant.
                
                if (appY >= 0.0d){
                    if (trueX >= 0.0d){
                        // 1st Quad 
                    } else {
                        // 2nd Quad
                        trueDirection = trueDirection + 180.;
                    }
                } else {
                    if (trueX >= 0.0d){
                        // 4th Quad;
                        trueDirection = trueDirection + 360.;
                    } else {
                        // 3rd Quad
                        trueDirection = trueDirection + 180.;
                    }
                }
                
                // convert back to boat convention
                trueDirection = -trueDirection;
                if (trueDirection < 0.0d){
                    trueDirection = trueDirection + 360.d;
                }
                
                trueWindSpeed = Math.sqrt(trueX*trueX + appY*appY);
                              
        }

	public double getTrueDirection() {
		return trueDirection;
	}

	public double getTrueWindSpeed() {
                return trueWindSpeed;
	}

}
