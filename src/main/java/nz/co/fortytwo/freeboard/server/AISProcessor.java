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

import nz.co.fortytwo.freeboard.server.ais.AisVesselInfo;
import nz.co.fortytwo.freeboard.server.util.Constants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessage18;
import dk.dma.ais.message.AisPositionMessage;
import dk.dma.ais.message.AisStaticCommon;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.packet.AisPacketParser;
import dk.dma.ais.sentence.Abk;
import dk.dma.ais.sentence.SentenceException;

/**
 * Churns through incoming nav data and looking for AIVDM messages
 * Translates the VDMs into AisMessages and sends the AisPositionMessages on to the browser.
 * Mostly we need 1,2,3,5, 18,19
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
			if(logger.isDebugEnabled())logger.debug("Processing AIS:"+bodyStr);
			if (StringUtils.isNotBlank(bodyStr)) {
				try {
					
					// dont need the AIS VDM now
					map.remove(Constants.AIS);
					AisPacket packet = handleLine(bodyStr);
					if(packet!=null && packet.isValidMessage()){
						//process message here
						AisMessage message = packet.getAisMessage();
						if(logger.isDebugEnabled())logger.debug("AisMessage:"+message.getClass()+":"+message.toString());
						//1,2,3
						if(message instanceof AisPositionMessage){
							AisVesselInfo vInfo=new AisVesselInfo((AisPositionMessage) message);
							map.put("AIS", vInfo);
						}
						//5,19,24
						if(message instanceof AisStaticCommon){
							AisVesselInfo vInfo=new AisVesselInfo((AisStaticCommon) message);
							map.put("AIS", vInfo);
						}
						if(message instanceof AisMessage18){
							AisVesselInfo vInfo=new AisVesselInfo((AisMessage18) message);
							map.put("AIS", vInfo);
						}
					}
					
					//fireSentenceEvent(map, sentence);
				} catch (Exception e) {
					if(logger.isDebugEnabled())logger.debug(e.getMessage(),e);
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
    	if(logger.isDebugEnabled())logger.debug("AIS Received : " + messageString);
        // Check for ABK
        if (Abk.isAbk(messageString)) {
        	if(logger.isDebugEnabled())logger.debug("AIS Received ABK: " + messageString);
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
