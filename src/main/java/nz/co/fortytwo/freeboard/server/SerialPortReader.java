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

import gnu.io.NRSerialPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.seda.SedaEndpoint;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.io.EndPoint;

/**
 * Wrapper to read serial port via rxtx, then fire messages into the camel route
 * via the seda queue.
 * 
 * @author robert
 * 
 */
public class SerialPortReader {

	private static Logger logger = Logger.getLogger(SerialPortReader.class);
	private String portName;
	private ProducerTemplate producer;
	private ConsumerTemplate consumer;

	private boolean running = true;
	private boolean mapped = false;
	private String deviceType = null;
	// private SerialPort serialPort = null;
	private NRSerialPort serialPort = null;

	public SerialPortReader() {
		super();
	}

	/**
	 * Opens a connection to the serial port, and starts two threads, one to read, one to write.
	 * A background thread looks for new/lost USB devices and (re)attaches them
	 * 
	 * NOTE: This uses nrjavaserial-3.8.4.jar from http://code.google.com/p/nrjavaserial/downloads/ not rxtx.jar.
	 * Since the last maven version is 3.7.5, you will need to manually add it to the local maven respoitory!!
	 * 
	 * @param portName
	 * @throws Exception
	 */
	void connect(String portName) throws Exception {
		this.portName = portName;

		serialPort = new NRSerialPort(portName, 38400);

		serialPort.connect();
		(new Thread(new SerialReader())).start();
		(new Thread(new SerialWriter())).start();
	}

	/** */
	public class SerialReader implements Runnable {

		BufferedReader in;

		public SerialReader() throws Exception {

			this.in = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

		}

		public void run() {
			try {
				try {
					while (running) {
						if (in.ready()) {
							String line = in.readLine();
							if (!mapped && line.indexOf(Constants.UID) >= 0) {
								// add to map
								logger.debug(portName + ":Serial Recieved:" + line);
								int pos = line.indexOf(Constants.UID) + 4;
								int pos1 = line.indexOf(",", pos);
								if (pos1 < 0)
									pos1 = line.length();
								logger.debug(portName + ":  pos:" + pos + " pos1:" + pos1);
								String type = line.substring(pos, pos1);
								deviceType = type.trim();
								mapped = true;
							}

							producer.sendBody(line);
						}
					}
				} catch (IOException e) {
					running = false;
					logger.error(portName, e);
				}
				// try {
				// producer.stop();
				// } catch (Exception e) {
				// logger.error(portName, e);
				// }
			} finally {
				if (serialPort.isConnected())
					serialPort.disconnect();
			}
		}

	}

	/** */
	public class SerialWriter implements Runnable {

		OutputStream out;

		public SerialWriter() throws Exception {
			out = serialPort.getOutputStream();
		}

		public void run() {
			try {
				try {
					while (running) {
						String message = consumer.receiveBody("seda:output", 1000, String.class);
						if (message != null) {

							// check its valid for this device
							if (deviceType == null || message.contains(Constants.UID + ":" + deviceType)) {
								logger.debug(portName + ":Serial written:" + message);
								this.out.write((message + "\n").getBytes());
								this.out.flush();
							}
						}
					}
				} catch (IOException e) {
					running = false;
					logger.error(portName, e);
				}

				try {
					consumer.stop();
				} catch (Exception e) {
					logger.error(portName, e);
				}
			} finally {
				if (serialPort.isConnected())
					serialPort.disconnect();
			}
		}
	}

	/**
	 * Set the camel producer, which fire the messages into camel
	 * 
	 * @param producer
	 */
	public void setProducer(ProducerTemplate producer) {
		this.producer = producer;

	}

	/**
	 * True if the serial port read/write threads are running
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Set to false to stop the serial port read/write threads.
	 * You must connect() to restart.
	 * 
	 * @param running
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	public void setConsumer(ConsumerTemplate consumer) {
		this.consumer = consumer;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

}
