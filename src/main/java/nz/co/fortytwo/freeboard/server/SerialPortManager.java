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

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.websocket.WebsocketComponent;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * A manager to monitor the USB tty ports. It dynamically adds/removes
 * ports as the USB devices are added/removed
 * 
 * @author robert
 * 
 */
public class SerialPortManager implements Runnable {

	private static Logger logger = Logger.getLogger(SerialPortManager.class);

	private WebsocketComponent wc;
	private List<SerialPortReader> serialPortList = new ArrayList<SerialPortReader>();

	private boolean running = true;

	public void run() {
		while (running) {
			// remove any stopped readers
			List<SerialPortReader> tmpPortList = new ArrayList<SerialPortReader>();
			for (SerialPortReader reader : serialPortList) {
				if (!reader.isRunning()) {
					tmpPortList.add(reader);
				}
			}
			serialPortList.removeAll(tmpPortList);

			Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();

			// String[] ports = serialPorts.split(",");
			while (ports.hasMoreElements()) {
				boolean portOk = false;
				String port = null;
				try {
					port = ports.nextElement().getName();
					if (port == null)
						continue;
					for (SerialPortReader reader : serialPortList) {
						if (StringUtils.equals(port, reader.getPortName())) {
							// its already up and running
							portOk = true;
						}
					}
					// if its running, ignore
					if (portOk)
						continue;

					// not running, start now.
					ProducerTemplate producer = wc.getCamelContext().createProducerTemplate();
					producer.setDefaultEndpointUri("seda://input?multipleConsumers=true");
					ConsumerTemplate consumer = wc.getCamelContext().createConsumerTemplate();
					SerialPortReader serial = new SerialPortReader();
					serial.setProducer(producer);
					serial.setConsumer(consumer);

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

	public WebsocketComponent getWc() {
		return wc;
	}

	public void setWc(WebsocketComponent wc) {
		this.wc = wc;
	}

}
