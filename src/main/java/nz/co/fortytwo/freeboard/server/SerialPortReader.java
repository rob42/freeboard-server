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
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import nz.co.fortytwo.freeboard.server.util.Constants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;

/**
 * Wrapper to read serial port via rxtx, then fire messages into the camel route
 * via the seda queue.
 * 
 * @author robert
 * 
 */
public class SerialPortReader implements Processor{

	private static Logger logger = Logger.getLogger(SerialPortReader.class);
	private String portName;
	private File portFile;
	private ProducerTemplate producer;

	private boolean running = true;
	private boolean mapped = false;
	private String deviceType = null;
	private NRSerialPort serialPort = null;
	//private BufferedOutputStream out;
	private LinkedBlockingQueue<String> queue;

	public SerialPortReader() {
		super();
		queue=new LinkedBlockingQueue<String>(10);
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
		//out=new BufferedOutputStream(serialPort.getOutputStream());
	}

	public class SerialWriter implements Runnable {

		BufferedOutputStream out;

		public SerialWriter() throws Exception {

			this.out = new BufferedOutputStream(serialPort.getOutputStream());

		}
		public void run() {
			
			try {
				while (running) {
					String msg = queue.poll(5, TimeUnit.SECONDS);
					if(msg!=null){
						out.write((msg + "\n").getBytes());
						out.flush();
					}
				}
			}catch(IOException e) {
				running = false;
				logger.error(portName, e);
			} catch (InterruptedException e) {
				//do nothing
			}
		}
	
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
						while (in.ready()) {
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

							if(line!=null)producer.sendBody(line);
						}
						// delay for a bit (msecs), we dont want to burn up CPU for nothing
						try {
							Thread.currentThread().sleep(250);
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
				try{
					if(serialPort.isConnected())serialPort.disconnect();
				}catch(Exception e){
					logger.error("Problem disconnecting port "+portName,e);
				}
				
			}
		}

	}

	/** */
	

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
			try{
				if(serialPort.isConnected())serialPort.disconnect();
			}catch(Exception e){
				logger.error("Problem disconnecting port "+portName,e);
			}
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


	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	/* Handles the messages to be delivered to the device attached to this port.
	 * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
	 */
	public void process(Exchange exchange) throws Exception {
		//send to device
		String message = exchange.getIn().getBody(String.class);
		logger.debug(portName + ":msg received for device:" + message);
		if (message != null) {
			// check its valid for this device
			if (running && deviceType == null || message.contains(Constants.UID + ":" + deviceType)) {
				logger.debug(portName + ":wrote out to device:" + message);
				//queue them and write in background
				queue.put(message);
			}
		}
	}

}
