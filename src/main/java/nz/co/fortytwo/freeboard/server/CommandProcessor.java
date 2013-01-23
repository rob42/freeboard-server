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
import org.apache.camel.ProducerTemplate;

/**
 * Churns through incoming nav data and creates output messages for the other devices which need it
 * 
 * @author robert
 *
 */
public class CommandProcessor extends FreeboardProcessor implements Processor {

	private ProducerTemplate producer;
	
	public void process(Exchange exchange) throws Exception {
		String msg = (String) exchange.getIn().getBody(String.class);
		String[] data = msg.split(",");
		StringBuilder outMsg = new StringBuilder();
		for(String m:data){
			//send to MEGA for anchor alarms, and autopilot
			//be careful to avoid misc arduino error/debug messages
			if(m.startsWith(Constants.WST+":")){outMsg.append(m); outMsg.append(",");}
			if(m.startsWith(Constants.WSA+":")){outMsg.append(m); outMsg.append(",");}
			if(m.startsWith(Constants.WDT+":")){outMsg.append(m); outMsg.append(",");}
			if(m.startsWith(Constants.WDA+":")){outMsg.append(m); outMsg.append(",");}
			if(m.startsWith(Constants.WSU+":")){outMsg.append(m); outMsg.append(",");}
			if(m.startsWith(Constants.LAT+":")){outMsg.append(m); outMsg.append(",");}
			if(m.startsWith(Constants.LON+":")){outMsg.append(m); outMsg.append(",");}
			if(m.startsWith(Constants.COG+":")){outMsg.append(m); outMsg.append(",");}
			if(m.startsWith(Constants.MGH+":")){outMsg.append(m); outMsg.append(",");}
			if(m.startsWith(Constants.SOG+":")){outMsg.append(m); outMsg.append(",");}
			if(m.startsWith(Constants.YAW+":")){outMsg.append(m); outMsg.append(",");}
			if(m.startsWith(Constants.PCH+":")){outMsg.append(m); outMsg.append(",");}
			if (m.startsWith(Constants.RLL + ":")) {
				outMsg.append(m);
				outMsg.append(",");
			}
		}
		//now send to output queue
		if(outMsg.length()>0){
			outMsg.insert(0, Constants.UID+":"+Constants.MEGA+",");
			producer.sendBody("seda:output?multipleConsumers=true", outMsg.toString());
		}
	}

	public void setProducer(ProducerTemplate producer) {
		this.producer = producer;
	}

}
