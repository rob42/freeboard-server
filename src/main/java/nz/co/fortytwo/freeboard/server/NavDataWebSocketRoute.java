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

import java.util.Properties;

import nz.co.fortytwo.freeboard.server.util.Constants;

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
	private OutputFilterProcessor outputFilterProcessor = new OutputFilterProcessor();
	private NMEAProcessor nmeaProcessor= new NMEAProcessor();
	//private IMUProcessor imuProcessor= new IMUProcessor();
	private SerialPortManager serialPortManager;
	
	private Properties config;
	private WindProcessor windProcessor = new WindProcessor();
	private CommandProcessor commandProcessor = new CommandProcessor();
	private DeclinationProcessor declinationProcessor = new DeclinationProcessor();
	private GPXProcessor gpxProcessor;

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
		
		//init processors who depend on this being started
		commandProcessor.init();
		gpxProcessor = new GPXProcessor();
		
		if(Boolean.valueOf(config.getProperty(Constants.DEMO))){
			
			from("stream:file?fileName=" + serialUrl).
			to("seda:input?multipleConsumers=true");
		
		}else{
			// start a serial port manager
			
			serialPortManager=new SerialPortManager();
			//serialPortManager.setWc(wc);
			new Thread(serialPortManager).start();
		}
		//dump nulls
		intercept().when(body().isNull()).stop();
		//intercept().when(((String)body(String.class)).trim().length()==0).stop();
		//send to listeners
		from("seda:input?multipleConsumers=true")
						.process(inputFilterProcessor)
						.process(nmeaProcessor)
						//.process(imuProcessor)
						.process(windProcessor )
						.process(commandProcessor )
						.process(declinationProcessor)
						.process(gpxProcessor)
						.process(outputFilterProcessor)
						.to("log:nz.co.fortytwo.freeboard.navdata?level=INFO")
						// and push to all web socket subscribers 
						.to("websocket:navData?sendToAll=true")
			.onException(Exception.class)
			.handled(true).maximumRedeliveries(0)
		    .to("log:nz.co.fortytwo.freeboard.navdata?level=ERROR");
		
		// log commands
		from("seda:output?multipleConsumers=true")
			//.process(outputFilterProcessor)
			.process(serialPortManager)
			.to("log:nz.co.fortytwo.freeboard.command?level=INFO")
			.onException(Exception.class)
		    .handled(true).maximumRedeliveries(0)
		    .to("log:nz.co.fortytwo.freeboard.command?level=ERROR");
	}

	public String getSerialUrl() {
		return serialUrl;
	}

	public void setSerialUrl(String serialUrl) {
		this.serialUrl = serialUrl;
	}
	
	/**
	 * When the serial port is used to read from the arduino this must be called to shut
	 * down the readers, which are in their own threads.
	 */
	public void stopSerial(){
		serialPortManager.stopSerial();
		
	}
	
}
