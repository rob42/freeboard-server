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

import java.util.HashMap;

import nz.co.fortytwo.freeboard.server.util.Constants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;

/**
 * Churns through incoming nav data and creates output messages for the other devices which need it
 * 
 * @author robert
 *
 */
public class CommandProcessor extends FreeboardProcessor implements Processor {

	private ProducerTemplate producer;
	
	public CommandProcessor(){
		
	}
	
	public void process(Exchange exchange) throws Exception {
		if (exchange.getIn().getBody()==null)
			return;
		
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = exchange.getIn().getBody(HashMap.class);
		HashMap<String, Object> outMap = new HashMap<String, Object>();
		
			//send to MEGA for anchor alarms, and autopilot
			//be careful to avoid misc arduino error/debug messages
			if(map.containsKey(Constants.WST)){outMap.put(Constants.WST, map.get(Constants.WST));}
			if(map.containsKey(Constants.WSA)){outMap.put(Constants.WSA, map.get(Constants.WSA));}
			if(map.containsKey(Constants.WDT)){outMap.put(Constants.WDT, map.get(Constants.WDT));}
			if(map.containsKey(Constants.WDA)){outMap.put(Constants.WDA, map.get(Constants.WDA));}
			if(map.containsKey(Constants.WSU)){outMap.put(Constants.WSU, map.get(Constants.WSU));}
			if(map.containsKey(Constants.LAT)){outMap.put(Constants.LAT, map.get(Constants.LAT));}
			if(map.containsKey(Constants.LON)){outMap.put(Constants.LON, map.get(Constants.LON));}
			if(map.containsKey(Constants.COG)){outMap.put(Constants.COG, map.get(Constants.COG));}
			if(map.containsKey(Constants.MGH)){outMap.put(Constants.MGH, map.get(Constants.MGH));}
			if(map.containsKey(Constants.SOG)){outMap.put(Constants.SOG, map.get(Constants.SOG));}
			if(map.containsKey(Constants.YAW)){outMap.put(Constants.YAW, map.get(Constants.YAW));}
			if(map.containsKey(Constants.PCH)){outMap.put(Constants.PCH, map.get(Constants.PCH));}
			if(map.containsKey(Constants.RLL)){outMap.put(Constants.RLL, map.get(Constants.RLL));}
			if(map.containsKey(Constants.AUTOPILOT_ADJUST)){outMap.put(Constants.AUTOPILOT_ADJUST, map.get(Constants.AUTOPILOT_ADJUST));}
			//if(map.containsKey(Constants.AUTOPILOT_GOAL)){outMap.put(Constants.AUTOPILOT_GOAL, map.get(Constants.AUTOPILOT_GOAL));}
			if(map.containsKey(Constants.AUTOPILOT_SOURCE)){outMap.put(Constants.AUTOPILOT_SOURCE, map.get(Constants.AUTOPILOT_SOURCE));}
			//if(map.containsKey(Constants.AUTOPILOT_TARGET)){outMap.put(Constants.AUTOPILOT_TARGET, map.get(Constants.AUTOPILOT_TARGET));}
			if(map.containsKey(Constants.AUTOPILOT_STATE)){outMap.put(Constants.AUTOPILOT_STATE, map.get(Constants.AUTOPILOT_STATE));}
		
		//now send to output queue
		if(!outMap.isEmpty()){
			outMap.put( Constants.UID,Constants.MEGA);
			producer.sendBody(hashMapToString(outMap));
		}
	}

	public void init() {
		this.producer = CamelContextFactory.getInstance().createProducerTemplate();
		producer.setDefaultEndpointUri("seda:output?multipleConsumers=true");
	}


}
