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

import gnu.io.NoSuchPortException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * A manager to monitor the USB tty ports. It dynamically adds/removes
 * ports as the USB devices are added/removed
 * 
 * @author robert
 * 
 */
public class SerialPortManager implements Runnable, Processor {

	private static Logger logger = Logger.getLogger(SerialPortManager.class);

	private List<SerialPortReader> serialPortList = new CopyOnWriteArrayList<SerialPortReader>();

	private boolean running = true;

	public void run() {
		// not running, start now.
		ProducerTemplate producer = CamelContextFactory.getInstance().createProducerTemplate();
		producer.setDefaultEndpointUri("seda:input");
		
		while (running) {
			// remove any stopped readers
			List<SerialPortReader> tmpPortList = new ArrayList<SerialPortReader>();
			for (SerialPortReader reader : serialPortList) {
				if (!reader.isRunning()) {
					logger.debug("Comm port " + reader.getPortName() + " finished and marked for removal");
					tmpPortList.add(reader);
				}
				logger.debug("Comm port " + reader.getPortName() + " currently running");
			}
			serialPortList.removeAll(tmpPortList);
			
			//not reliable, cant properly deal with hot-plug
			//Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();
			

			String[] ports = {"/dev/ttyUSB0","/dev/ttyUSB1","/dev/ttyUSB2"};
			for (String port:ports) {
				boolean portOk = false;
				File portFile = new File(port);
				try {
					if (!portFile.exists()){
						logger.debug("Comm port "+port+" doesnt exist");
						continue;
					}
					for (SerialPortReader reader : serialPortList) {
						if (StringUtils.equals(port, reader.getPortName())) {
							// its already up and running
							portOk = true;
						}
					}
					// if its running, ignore
					if (portOk){
						logger.debug("Comm port " + port + " found already connected");
						continue;
					}

					
					SerialPortReader serial = new SerialPortReader();
					serial.setProducer(producer);
					
					logger.debug("Comm port " + port + " found and connecting...");
					serial.connect(port);
					logger.info("Comm port " + port + " found and connected");
					serialPortList.add(serial);
				} catch (NullPointerException np) {
					logger.error("Comm port " + port + " was null, probably not found, or nothing connected");
				} catch (NoSuchPortException nsp) {
					logger.error("Comm port " + port + " not found, or nothing connected");
				} catch (Exception e) {
					logger.error("Port " + port + " failed", e);
				}
			}
			// delay for 30 secs, we dont want to burn up CPU for nothing
			try {
				Thread.currentThread().sleep(10 * 1000);
			} catch (InterruptedException ie) {
			}
		}
	}

	/**
	 * When the serial port is used to read from the arduino this must be called to shut
	 * down the readers, which are in their own threads.
	 */
	public void stopSerial() {

		for (SerialPortReader serial : serialPortList) {
			if (serial != null) {
				serial.setRunning(false);
			}
		}
		running = false;

	}

	public void process(Exchange exchange) throws Exception {
		for (SerialPortReader serial : serialPortList) {
			if (serial != null) {
				serial.process(exchange);
			}
		}
		
	}



}
