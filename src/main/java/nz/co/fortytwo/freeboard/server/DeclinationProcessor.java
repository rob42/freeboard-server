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
import nz.co.fortytwo.freeboard.server.util.Magfield;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

/**
 * Processes messages, if it finds a Magnetic bearing, and has seen a LAT and LON, it calculates declination,
 * and appends declination to the mag heading message. Since the calculation of declination is expensive we only do it once,
 * and only redo it if the integer LAT or LON changes
 * 
 * @author robert
 * 
 */
public class DeclinationProcessor extends FreeboardProcessor implements Processor, FreeboardHandler {

	private static Logger logger = Logger.getLogger(DeclinationProcessor.class);
	private double declination = 0.0;
	private double lat = 0;
	private double lon = 0;
	private boolean calc = false;

	public void process(Exchange exchange) throws Exception {
		if (exchange.getIn().getBody() == null)
			return;

		// so we have a string
		try {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> map = exchange.getIn().getBody(HashMap.class);
			map = handle(map);
			exchange.getIn().setBody(map);

		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public HashMap<String, Object> handle(HashMap<String, Object> map) {
		if (map.containsKey(Constants.LAT)) {
			if (hasChanged((Double) map.get(Constants.LAT), lat)) {
				calc = true;
				lat = (Double) map.get(Constants.LAT);
			}
		}

		if (map.containsKey(Constants.LON)) {
			if (hasChanged((Double) map.get(Constants.LON), lon)) {
				calc = true;
				lon = (Double) map.get(Constants.LON);
			}
		}

		if (calc) {
			declination = -1
					* Magfield.SGMagVar(Magfield.deg_to_rad(lat), -1 * Magfield.deg_to_rad(lon), 0, Magfield.yymmdd_to_julian_days(13, 1, 1), 7, new double[6]);
			declination = Magfield.rad_to_deg(declination) * -1;// declination is positive when true N is west of MagN, eg subtract the declination
			declination = round(declination, 1);
			logger.debug("Declination = " + declination);
			calc = false;
		}
		if (map.containsKey(Constants.MGH)) {
			map.put(Constants.DEC, declination);

		}
		return map;
	}

	/**
	 * Compares the lat or lon by rounding to int, with the last value we had
	 * True if they differ, false otherwise
	 * Used to stop us constantly recalculating the declination
	 * @param now
	 * @param then
	 * @return
	 */
	private boolean hasChanged(double now, double then) {
		if(Math.round(now)!=Math.round(then))return true;
		return false;
	}
}
