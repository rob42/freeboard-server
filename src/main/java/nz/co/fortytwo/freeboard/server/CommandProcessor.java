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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
public class CommandProcessor extends FreeboardProcessor implements Processor, FreeboardHandler {

	private ProducerTemplate producer;
	private List<String> msgs = new ArrayList<String>();

	public CommandProcessor() {

		msgs.add(Constants.DEC);
		msgs.add(Constants.WST);
		msgs.add(Constants.WSA);
		msgs.add(Constants.WDT);
		msgs.add(Constants.WDA);
		msgs.add(Constants.WSU);
		msgs.add(Constants.LAT);
		msgs.add(Constants.LON);
		msgs.add(Constants.COG);
		msgs.add(Constants.MGH);
		msgs.add(Constants.SOG);
		msgs.add(Constants.YAW);
		msgs.add(Constants.PCH);
		msgs.add(Constants.RLL);
		msgs.add(Constants.AUTOPILOT_ADJUST);
		// msgs.add(Constants.AUTOPILOT_GOAL);
		msgs.add(Constants.AUTOPILOT_SOURCE);
		// msgs.add(Constants.AUTOPILOT_TARGET);
		msgs.add(Constants.AUTOPILOT_STATE);
		// anchor alarm
		msgs.add(Constants.ANCHOR_ALARM_STATE);
		msgs.add(Constants.ANCHOR_ALARM_ADJUST);
		msgs.add(Constants.ANCHOR_ALARM_LAT);
		msgs.add(Constants.ANCHOR_ALARM_LON);
		msgs.add(Constants.ANCHOR_ALARM_RADIUS);
		// wind
		msgs.add(Constants.WIND_ZERO_ADJUST);
		msgs.add(Constants.WIND_ALARM_KNOTS);
		msgs.add(Constants.WIND_SPEED_ALARM_STATE);

	}

	public void process(Exchange exchange) throws Exception {
		if (exchange.getIn().getBody() == null)
			return;

		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = exchange.getIn().getBody(HashMap.class);
		handle(map);
	}

	public void init() {
		this.producer = CamelContextFactory.getInstance().createProducerTemplate();
		producer.setDefaultEndpointUri("direct:command");
	}

	@Override
	public HashMap<String, Object> handle(HashMap<String, Object> map) {
		HashMap<String, Object> outMap = new HashMap<String, Object>();

		// send to MEGA for anchor alarms, and autopilot
		// be careful to avoid misc arduino error/debug messages
		for (String tag : msgs) {
			if (map.containsKey(tag)) {
				outMap.put(tag, map.get(tag));
			}
		}

		// now send to output queue to MEGA
		if (!outMap.isEmpty()) {
			outMap.put(Constants.UID, Constants.MEGA);
			producer.sendBody(hashMapToString(outMap));
		}

		// we send DEC to IMU
		if (map.containsKey(Constants.DEC)) {
			producer.sendBody(Constants.UID + ":" + Constants.IMU + "," + Constants.DEC + ":" + map.get(Constants.DEC) + ",");
		}
		return map;
	}

}
