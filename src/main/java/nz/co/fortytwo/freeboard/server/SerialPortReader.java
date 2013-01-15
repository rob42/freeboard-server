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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

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
	private File portFile;
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
	 * NOTE: This uses nrjavaserial-3.7.7.jar built from the src, with a few minor mods to deal with timeouts etc
	 * see http://code.google.com/p/nrjavaserial/
	 * Since the last maven version is 3.7.5, you will need to build it, which will add it to the local maven respoitory!!
	 * 
	 * @param portName
	 * @throws Exception
	 */
	void connect(String portName) throws Exception {
		this.portName = portName;
		this.portFile = new File(portName);
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
								
								String type = line.substring(pos, pos1);
								logger.debug(portName + ":  device name:" + type);
								deviceType = type.trim();
								mapped = true;
							}

							producer.sendBody(line);
						}
						// delay for 100 msecs, we dont want to burn up CPU for nothing
						try {
							Thread.currentThread().sleep(100);
						} catch (InterruptedException ie) {
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

		BufferedOutputStream out;

		public SerialWriter() throws Exception {
			out = new BufferedOutputStream(serialPort.getOutputStream());
		}

		public void run() {
			try {
				try {
					while (running) {

						String message = consumer.receiveBodyNoWait("seda:output?multipleConsumers=true", String.class);
						//logger.debug(portName + ":Serial output read:" + message);
						if (message != null) {
							// check its valid for this device
							if (deviceType == null || message.contains(Constants.UID + ":" + deviceType)) {
								logger.debug(portName + ":Serial written:" + message);
								out.write((message + "\n").getBytes());
								out.flush();
							}
						}
						// delay for 100 msecs, we dont want to burn up CPU for nothing
						try {
							Thread.currentThread().sleep(100);
						} catch (InterruptedException ie) {
						}
					}
				} catch (IOException e) {
					running = false;
					logger.error(portName, e);
				}

//				try {
//					consumer.stop();
//				} catch (Exception e) {
//					logger.error(portName, e);
//				}
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
		if (!portFile.exists()) {
			serialPort.disconnect();
			running = false;
		}
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
