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

import gnu.io.NoSuchPortException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.VHWSentence;
import net.sf.marineapi.nmea.util.CompassPoint;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.websocket.WebsocketComponent;

/**
 * Main camel route definition to handle Arduino to web processing
 * 
 * @author robert
 *
 */
public class NavDataWebSocketRoute extends RouteBuilder {
	private int port = 9090;
	private String serialUrl;
	private InputFilterProcessor inputFilterProcessor = new InputFilterProcessor();
	private NMEAProcessor nmeaProcessor= new NMEAProcessor();
	private IMUProcessor imuProcessor= new IMUProcessor();
	private String serialPorts;
	private List<SerialPortReader> serialPortList = new ArrayList<SerialPortReader>();
	private Properties config;
	private WindProcessor windProcessor = new WindProcessor();
	private CommandProcessor commandProcessor = new CommandProcessor();

	public NavDataWebSocketRoute(Properties config) {
		this.config=config;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void configure() throws Exception {
		// setup Camel web-socket component on the port we have defined
		
		WebsocketComponent wc = getContext().getComponent("websocket", WebsocketComponent.class);

		wc.setPort(port);
		// we can serve static resources from the classpath: or file: system
		wc.setStaticResources("classpath:.");
		
		//init commandProcessor
		commandProcessor.setProducer(wc.getCamelContext().createProducerTemplate());
		
		//init NMEAProcessor
		setNMEAListeners((NMEAProcessor) nmeaProcessor);
		
		if(Boolean.valueOf(config.getProperty(ServerMain.DEMO))){
			
			from("stream:file?fileName=" + serialUrl).
			to("seda:input?multipleConsumers=true");
		
		}else{
			// start a serial port reader
			//we could have several serial ports
			String[] ports = serialPorts.split(",");
			for (String port: ports){
				ProducerTemplate producer = wc.getCamelContext().createProducerTemplate();
				ConsumerTemplate consumer = wc.getCamelContext().createConsumerTemplate();
				SerialPortReader serial = new SerialPortReader();
				serial.setProducer(producer);
				serial.setConsumer(consumer);
				try{
					serial.connect(port);
					log.info("Comm port "+serial+" found and connected");
					serialPortList.add(serial);
				}catch(NoSuchPortException nsp){
					log.info("Comm port "+serial+" not found, or nothing connected");
				}catch(Exception e){
					log.error("Port "+serial+" failed",e);
				}
			}
		}
		//send to listeners
		from("seda:input?multipleConsumers=true")
			.process(inputFilterProcessor)
			.process(nmeaProcessor)
			.process(imuProcessor)
			.process(windProcessor )
			.process(commandProcessor )
			.to("log:nz.co.fortytwo.navdata?level=INFO")
			// and push to all web socket subscribers 
			.to("websocket:navData?sendToAll=true")
			.onException(Exception.class)
		    .handled(true).maximumRedeliveries(0)
		    .to("log:nz.co.fortytwo.navdata?level=ERROR");
		
		// log commands
		from("seda:output?multipleConsumers=true")
			.to("log:nz.co.fortytwo.command?level=INFO")
			.onException(Exception.class)
		    .handled(true).maximumRedeliveries(0)
		    .to("log:nz.co.fortytwo.navdata?level=ERROR");
	}

	public String getSerialUrl() {
		return serialUrl;
	}

	public void setSerialUrl(String serialUrl) {
		this.serialUrl = serialUrl;
	}
	
	/**
	 * Adds NMES sentence listeners to process NMEA to simple output
	 * @param processor
	 */
	private void setNMEAListeners(NMEAProcessor processor){
		
		processor.addSentenceListener(new SentenceListener() {
			
			public void sentenceRead(SentenceEvent evt) {
				Exchange exchange= (Exchange)evt.getSource();
				StringBuffer body = new StringBuffer();
				if(evt.getSentence() instanceof PositionSentence){
					PositionSentence sen = (PositionSentence) evt.getSentence();
					if(sen.getPosition().getLatHemisphere()==CompassPoint.SOUTH){
						body.append(Constants.LAT+":-"+sen.getPosition().getLatitude()+",");
					}else{
						body.append(Constants.LAT+":"+sen.getPosition().getLatitude()+",");
					}
					if(sen.getPosition().getLonHemisphere()==CompassPoint.WEST){
						body.append(Constants.LON+":-"+sen.getPosition().getLongitude()+",");
					}else{
						body.append(Constants.LON+":"+sen.getPosition().getLongitude()+",");
					}
				}
				if(evt.getSentence() instanceof HeadingSentence){
					HeadingSentence sen = (HeadingSentence) evt.getSentence();
					if(sen.isTrue()){
						body.append(Constants.COG+":"+sen.getHeading()+",");
					}else{
						body.append(Constants.MGH+":"+sen.getHeading()+",");
					}
				}
				if(evt.getSentence() instanceof RMCSentence){
					//;
					RMCSentence sen = (RMCSentence) evt.getSentence();
						body.append(Constants.SOG+":"+sen.getSpeed()+",");
					}
				if(evt.getSentence() instanceof VHWSentence){
					//;
					VHWSentence sen = (VHWSentence) evt.getSentence();
						body.append(Constants.SOG+":"+sen.getSpeedKnots()+",");
						body.append(Constants.MGH+":"+sen.getMagneticHeading()+",");
						body.append(Constants.COG+":"+sen.getHeading()+",");
					}
				
				
				//MWV wind
				if(evt.getSentence() instanceof MWVSentence){
					MWVSentence sen = (MWVSentence) evt.getSentence();
					if(sen.isTrue()){
						body.append(Constants.WDT+":"+sen.getAngle()+","
							+Constants.WST+":"+sen.getSpeed()+"," 
							+Constants.WSU+":"+sen.getSpeedUnit()+",");
					}else{
						body.append(Constants.WDA+":"+sen.getAngle()+","
							+Constants.WSA+":"+sen.getSpeed()+"," 
							+Constants.WSU+":"+sen.getSpeedUnit()+",");
					}
				}
				exchange.getOut().setBody(body);
			}
			
			public void readingStopped() {}
			public void readingStarted() {}
			public void readingPaused() {}
		});
	}

	public String getSerialPorts() {
		return serialPorts;
	}

	public void setSerialPorts(String serialPorts) {
		this.serialPorts = serialPorts;
	}
	
	/**
	 * When the serial port is used to read from the arduino this must be called to shut
	 * down the readers, which are in their own threads.
	 */
	public void stopSerial(){
		for(SerialPortReader serial: serialPortList){
			if(serial != null){
				serial.setRunning(false);
			}
		}
		
	}
	
}
