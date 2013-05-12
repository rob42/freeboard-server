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

import java.util.Properties;

import nz.co.fortytwo.freeboard.server.util.Constants;

import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.websocket.WebsocketComponent;

/**
 * Main camel route definition to handle Arduino to web processing
 * 
 * 
 * <ul>
 * <li>Basically all input is added to seda:input
 * <li>From there nmea is copied to seda:nmeaOutput and output on port 5555
 * <li>Remained is converted to hashmap, processed, 
 * <ul>
 * 	<li>commands are sent to direct:command
 *  <li>generated NMEA is added to seda:nmeaOutput
 *  </ul> 
 * <li>Remaining key/values are output by multicast on websocket,cometd, and command
 * <li>Commands from direct:command are filtered to devices and sent
 * </ul>
 * 
 * 
 * @author robert
 * 
 */
public class NavDataWebSocketRoute extends RouteBuilder {
	private int port = 9090;
	private String serialUrl;
	private InputFilterProcessor inputFilterProcessor = new InputFilterProcessor();
	private OutputFilterProcessor outputFilterProcessor = new OutputFilterProcessor();
	private NMEAProcessor nmeaProcessor = new NMEAProcessor();
	// private IMUProcessor imuProcessor= new IMUProcessor();
	private SerialPortManager serialPortManager;

	private Properties config;
	private WindProcessor windProcessor = new WindProcessor();
	private CommandProcessor commandProcessor = new CommandProcessor();
	private DeclinationProcessor declinationProcessor = new DeclinationProcessor();
	private GPXProcessor gpxProcessor;
	private AddSourceProcessor addSrcProcessor = new AddSourceProcessor("freeboard");
	private CombinedProcessor combinedProcessor = new CombinedProcessor();
	private Predicate isNmea = null;
	private NmeaTcpServer nmeaTcpServer;

	public NavDataWebSocketRoute(Properties config) {
		this.config = config;

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
		// define nmea predicate
		isNmea = body(String.class).startsWith("$");
		nmeaTcpServer = new NmeaTcpServer();
		nmeaTcpServer.start();
		WebsocketComponent wc = getContext().getComponent("websocket", WebsocketComponent.class);

		wc.setPort(port);
		// we can serve static resources from the classpath: or file: system
		wc.setStaticResources("classpath:.");

		// init processors who depend on this being started
		initProcessors();

		if (Boolean.valueOf(config.getProperty(Constants.DEMO))) {

			from("stream:file?fileName=" + serialUrl).to("seda:input");

		} else {
			// start a serial port manager

			serialPortManager = new SerialPortManager();
			// serialPortManager.setWc(wc);
			new Thread(serialPortManager).start();
		}
		// dump nulls
		intercept().when(body().isNull()).stop();
		// intercept().when(((String)body(String.class)).trim().length()==0).stop();
		// deal with errors

		// distribute and log commands
		from("direct:command")
				.process(serialPortManager)
				// .to("log:nz.co.fortytwo.freeboard.command?level=INFO")
				.onException(Exception.class).handled(true).maximumRedeliveries(0)
					.to("log:nz.co.fortytwo.freeboard.command?level=ERROR&showException=true&showStackTrace=true")
					.end();
		
		// push NMEA out via TCPServer.
		from("seda:nmeaOutput")
			.process(nmeaTcpServer)
			.end();
		
		// out to websockets
		from("direct:websocket").to("websocket:navData?sendToAll=true")
			.onException(Exception.class).handled(true).maximumRedeliveries(0)
				.to("log:nz.co.fortytwo.freeboard.websocket?level=ERROR&showException=true&showStackTrace=true")
				.end();

		// out to cometd
		from("direct:cometd")
			 .process(addSrcProcessor)
			 .to("cometd://0.0.0.0:8082/freeboard/json?jsonCommented=false")
			 .onException(Exception.class)
			 .handled(true)
			 .maximumRedeliveries(0)
			 .to("log:nz.co.fortytwo.freeboard.json?level=ERROR&showException=true&showStackTrace=true")
			 .end();
		
		//main input to destination route
		// send input to listeners
		from("seda:input")
				// process all here
				.filter(isNmea).to("seda:nmeaOutput").end().process(inputFilterProcessor).process(combinedProcessor)
				.process(outputFilterProcessor)
				// .to("log:nz.co.fortytwo.freeboard.navdata?level=INFO")
				// and push to all subscribers. We use multicast/direct cos if we use SEDA then we get a queue growth if there are no consumers active.
				.multicast()
					.to("direct:websocket")
					.to("direct:cometd")
					.to("direct:command")
				.end()
				.onException(Exception.class).handled(true).maximumRedeliveries(0)
					.to("log:nz.co.fortytwo.freeboard.navdata?level=ERROR&showException=true&showStackTrace=true")
					.end();

	}

	private void initProcessors() {
		commandProcessor.init();
		gpxProcessor = new GPXProcessor();
		// add combined processors
		combinedProcessor.addHandler(nmeaProcessor);
		combinedProcessor.addHandler(windProcessor);
		combinedProcessor.addHandler(declinationProcessor);
		combinedProcessor.addHandler(commandProcessor);
		combinedProcessor.addHandler(gpxProcessor);

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
	public void stopSerial() {
		serialPortManager.stopSerial();
		nmeaTcpServer.stop();
	}

}
