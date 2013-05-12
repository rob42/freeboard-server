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

import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

/**
 * Churns through incoming nav data and filters out misc debug and unnecessary messages from the other devices
 * 
 * @author robert
 *
 */
public class InputFilterProcessor extends FreeboardProcessor implements Processor {
	private static Logger logger = Logger.getLogger(InputFilterProcessor.class);
	private Pattern p;
	
	public InputFilterProcessor(){
		p=Pattern.compile("^[#]?[A-Z]{3}:.*");
	}
	public void process(Exchange exchange) throws Exception {
		String msg = (String) exchange.getIn().getBody(String.class);
		if(msg !=null){
			msg=msg.trim();
			if(msg.indexOf("!!!VER:")==0){
				//from IMU or MEGA - good
				msg=msg.substring(msg.indexOf(",") + 1);
				exchange.getIn().setBody(stringToHashMap(msg));
				return;
			}
			if(p.matcher(msg).matches()){
				//#CMD: or VAL: - good
				exchange.getIn().setBody(stringToHashMap(msg));
				return;
			}
			if(msg.startsWith("$")){
				//NMEA - good
				//System.out.println(msg);
				exchange.getIn().setBody(stringToHashMap(msg));
				return;
			}
			//uh-oh log it, squash it
			exchange.getUnitOfWork().done(exchange);
			logger.info("Dropped invalid message:"+msg);
			exchange.getIn().setBody(null);
			//throw new CamelExchangeException("Invalid msg", exchange);
		}
		
	}

	
}
