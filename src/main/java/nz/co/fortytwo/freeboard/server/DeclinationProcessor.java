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
import nz.co.fortytwo.freeboard.server.util.Magfield;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Processes messages, if it finds a Magnetic bearing, and has seen a LAT and LON, it calculates declination, 
 * and appends declination to the mag heading message. Since the calculation of declination is expensive we only do it once, 
 * and only redo it if the integer LAT or LON changes 
 *  
 * @author robert
 * 
 */
public class DeclinationProcessor implements Processor {

		private static Logger logger = Logger.getLogger(DeclinationProcessor.class);
		private double declination=0.0;
		private double lat = 0;
		private double lon = 0;
		private boolean calc = false;

	public void process(Exchange exchange) throws Exception {
		if (StringUtils.isEmpty(exchange.getIn().getBody(String.class)))
			return;
		// so we have a string
		try{
			String bodyStr = exchange.getIn().getBody(String.class).trim();
			String latStr = StringUtils.substringBetween(bodyStr,Constants.LAT+":",".");
			if(StringUtils.isNotEmpty(latStr)){
				double tmp= Double.valueOf(latStr).doubleValue();
				if(Math.round(lat)!=Math.round(tmp)){
					calc=true;
					lat=tmp;
				}
			}
			String lonStr = StringUtils.substringBetween(bodyStr,Constants.LON+":",".");
			if(StringUtils.isNotEmpty(lonStr)){
				double tmp= Double.valueOf(lonStr).doubleValue();
				if(Math.round(lon)!=Math.round(tmp)){
					calc=true;
					lon=tmp;
				}
			}
			if(calc){
				declination =-1 * Magfield.SGMagVar(Magfield.deg_to_rad(lat), -1* Magfield.deg_to_rad(lon), 0, Magfield.yymmdd_to_julian_days(13, 1, 1),7,new double[6]);
				declination=Magfield.rad_to_deg(declination)*-1;//declination is positive when true N is west of MagN, eg subtract the declination 
				declination=(double)(Math.round( declination * 10 ) )/10;
				logger.debug("Declination = "+declination);
				calc=false;
			}
			if(bodyStr.indexOf(Constants.MGH)>0){
				exchange.getOut().setBody( bodyStr+Constants.MGD+":"+declination+",");
			}
		}catch(Exception e){
			logger.error(e);
		}
	}

}
