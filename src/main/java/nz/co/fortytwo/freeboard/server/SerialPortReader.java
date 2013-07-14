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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import nz.co.fortytwo.freeboard.server.util.Constants;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import purejavacomm.CommPortIdentifier;
import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.SerialPortEventListener;

/**
 * Wrapper to read serial port via rxtx, then fire messages into the camel route
 * via the seda queue.
 * 
 * @author robert
 * 
 */
public class SerialPortReader implements Processor {

	private static Logger logger = Logger.getLogger(SerialPortReader.class);
	private String portName;
	private File portFile;
	private ProducerTemplate producer;

	private boolean running = true;
	private boolean mapped = false;
	private String deviceType = null;
	private SerialPort serialPort = null;

	private LinkedBlockingQueue<String> queue;
	private SerialReader serialReader;
	

	public SerialPortReader() {
		super();
		queue = new LinkedBlockingQueue<String>(100);
	}

	/**
	 * Opens a connection to the serial port, and starts two threads, one to read, one to write.
	 * A background thread looks for new/lost USB devices and (re)attaches them
	 * 
	 * 
	 * @param portName
	 * @throws Exception
	 */
	void connect(String portName, int baudRate) throws Exception {
		this.portName = portName;
		this.portFile = new File(portName);
		CommPortIdentifier portid = CommPortIdentifier.getPortIdentifier(portName);
		serialPort = (SerialPort) portid.open("FreeboardSerialReader", 100);
		//TODO: change baud rate to config based setup
		serialPort.setSerialPortParams(baudRate, 8, 1, 0);
		serialReader = new SerialReader();
		serialPort.enableReceiveTimeout(1000);
		serialPort.notifyOnDataAvailable(true);
		serialPort.addEventListener(serialReader);
		//(new Thread(new SerialReader())).start();
		(new Thread(new SerialWriter())).start();

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
					if (msg != null) {
						out.write((msg + "\n").getBytes());
						out.flush();
					}
				}
			} catch (IOException e) {
				running = false;
				logger.error(portName+":"+ e.getMessage());
				logger.debug(e.getMessage(),e);
			} catch (InterruptedException e) {
				// do nothing
			}
		}

	}

	/** */
	public class SerialReader implements SerialPortEventListener {

		//BufferedReader in;
		
		private Pattern uid;
		List<String> lines = new ArrayList<String>();
		StringBuffer line = new StringBuffer(60);
		
		private boolean complete;
		private InputStream in;
		byte[] buff = new byte[256]; 
		int x=0;
		
		public SerialReader() throws Exception {
			
			//this.in = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			this.in = new BufferedInputStream(serialPort.getInputStream());
			uid = Pattern.compile(Constants.UID + ":");
			logger.info("Setup serialReader on :"+portName);
		}

		
		//@Override
		public void serialEvent(SerialPortEvent event) {
			logger.trace("SerialEvent:"+event.getEventType());
			try{
				if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
					
						int r=0;
						
						while((r>-1)&& in.available()>0 ){
							try {
								r = in.read();
								buff[x]=(byte) r;
								x++;
								
								//10=LF, 13=CR, lines should end in CR/LF
								if(r==10  ||x==256){
									if(r==10)complete=true;
									line.append(new String(buff));
									buff=new byte[256];
									x=0;
								}
								
							} catch (IOException e) {
								logger.error(portName + ":"+e.getMessage());
								logger.debug(e.getMessage(),e);
								return;
							}
							//we have a line ending in CR/LF
							if (complete) {
								String lineStr = line.toString().trim();
								logger.debug(portName + ":Serial Received:" + lineStr);
								//its not empty!
								if(lineStr.length()>0){
									//map it if we havent already
									if (!mapped && uid.matcher(lineStr).matches()) {
										// add to map
										logger.debug(portName + ":Serial Received:" + lineStr);
										String type = StringUtils.substringBetween(lineStr, Constants.UID + ":", ",");
										if (type != null) {
											logger.debug(portName + ":  device name:" + type);
											deviceType = type.trim();
											mapped = true;
										}
									}
									producer.sendBody(lineStr);
								}
								complete=false;
								line=new StringBuffer(60);
							}
						}
				}
			}catch (Exception e) {
				running=false;
				logger.error(portName, e);
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

			try {
				serialPort.close();
				serialPort.removeEventListener();
			} catch (Exception e) {
				logger.error("Problem disconnecting port " + portName +", "+ e.getMessage());
				logger.debug(e.getMessage(),e);
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

	/*
	 * Handles the messages to be delivered to the device attached to this port.
	 * 
	 * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
	 */
	public void process(Exchange exchange) throws Exception {
		// send to device
		String message = exchange.getIn().getBody(String.class);
		logger.debug(portName + ":msg received for device:" + message);
		if (message != null) {
			// check its valid for this device
			if (running && deviceType == null || message.contains(Constants.UID + ":" + deviceType)) {
				logger.debug(portName + ":wrote out to device:" + message);
				// queue them and write in background
				queue.put(message);
			}
		}
	}

}
