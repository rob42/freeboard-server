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

import java.io.IOException;
import java.util.HashMap;

import nz.co.fortytwo.freeboard.server.util.Constants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import dk.dma.ais.message.AisMessage;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.packet.AisPacketParser;
import dk.dma.ais.sentence.Abk;
import dk.dma.ais.sentence.SentenceException;

/**
 * Churns through incoming nav data and creates a GPX file
 * Uses code from http://gpxparser.alternativevision.ro to do GPX parsing
 * Various strategies limit the amount of data collected
 * 
 * Writes to USB drive /tracks if one is found
 * Writes to freeboard/tracks if no drive is found
 * 
 * @author robert
 * 
 */
public class AISProcessor extends FreeboardProcessor implements Processor, FreeboardHandler {


	private static Logger logger = Logger.getLogger(AISProcessor.class);

    /** Reader to parse lines and deliver complete AIS packets. */
    private AisPacketParser packetParser = new AisPacketParser();
	
	
	public AISProcessor() {
		
	}

	public void process(Exchange exchange) {
		if (exchange.getIn().getBody() == null)
			return;

		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = exchange.getIn().getBody(HashMap.class);

		handle(map);

	}

	

	//@Override
	public HashMap<String, Object> handle(HashMap<String, Object> map) {
		
			String bodyStr = (String) map.get(Constants.AIS);
			logger.debug("Processing AIS:"+bodyStr);
			if (StringUtils.isNotBlank(bodyStr)) {
				try {
					
					// dont need the AIS now
					map.remove(Constants.AIS);
					AisPacket packet = handleLine(bodyStr);
					if(packet!=null && packet.isValidMessage()){
						//process message here
						AisMessage message = packet.getAisMessage();
						logger.debug("AisMessage:"+message.toString());
						map.put("TEST", message.getMsgId());
						map.put("MSG", message);
					}
					
					//fireSentenceEvent(map, sentence);
				} catch (Exception e) {
					logger.debug(e.getMessage(),e);
					logger.error(e.getMessage()+" : "+bodyStr);
				}
			}
			return map;
			//https://github.com/dma-ais/AisLib
			
			/*
			 *  HD-SF. Free raw AIS data feed for non-commercial use.
			 *   hd-sf.com:9009 
			 */
		
	}

	   /**
     * Handle a received line
     * 
     * @param line
     * @return
     */
    private AisPacket handleLine(String messageString) throws IOException {
    	logger.debug("AIS Received : " + messageString);
        // Check for ABK
        if (Abk.isAbk(messageString)) {
            	logger.debug("AIS Received ABK: " + messageString);
            return null;
        }

        try {
        	AisPacket packet = null;
            String[] lines = messageString.split("\\r?\\n");
           
            for (String line : lines) {
                packet = packetParser.readLine(line);
            }
            return packet;
        } catch (SentenceException se) {
        	logger.info("AIS Sentence error: " + se.getMessage() + " line: " + messageString);
            throw new IOException(se);
            
        }
    }
    
    
   
}
