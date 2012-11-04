package nz.co.fortytwo.freeboard.server;

import java.util.Properties;

import gnu.io.NoSuchPortException;
import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.VHWSentence;
import net.sf.marineapi.nmea.util.CompassPoint;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.websocket.WebsocketComponent;
import org.apache.commons.lang3.StringUtils;

public class NavDataWebSocketRoute extends RouteBuilder {
	private int port = 9090;
	private String serialUrl;
	private Processor nmeaProcessor= new NMEAProcessor();
	private String serialPort;
	private SerialPortReader serial;
	private Properties config;

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

		setNMEAListeners((NMEAProcessor) nmeaProcessor);
		if(Boolean.valueOf(config.getProperty(ServerMain.DEMO))){
			
			from("stream:file?fileName=" + serialUrl).
			to("seda:input");
		
		}else{
			// start a serial port reader
			ProducerTemplate producer = wc.getCamelContext().createProducerTemplate();
			serial = new SerialPortReader();
			serial.setProducer(producer);
			try{
				serial.connect(serialPort);
			}catch(NoSuchPortException nsp){
				//TODO: notify to plug in the arduino!
				nsp.printStackTrace();
			}catch(Exception e){
				//TODO: what should we do here?
				e.printStackTrace();
			}
		}
		
		from("seda:input")
			.process(nmeaProcessor)
			.split(body(String.class).tokenize("\n"))
			.to("log:nz.co.fortytwo?level=DEBUG")
			// and push to all web socket subscribers 
			.to("websocket:navData?sendToAll=true");
	}

	public String getSerialUrl() {
		return serialUrl;
	}

	public void setSerialUrl(String serialUrl) {
		this.serialUrl = serialUrl;
	}
	
	private void setNMEAListeners(NMEAProcessor processor){
		
		processor.addSentenceListener(new SentenceListener() {
			
			public void sentenceRead(SentenceEvent evt) {
				Exchange exchange= (Exchange)evt.getSource();
				StringBuffer body = new StringBuffer();
				if(evt.getSentence() instanceof PositionSentence){
					PositionSentence sen = (PositionSentence) evt.getSentence();
					if(sen.getPosition().getLatHemisphere()==CompassPoint.SOUTH){
						body.append("LAT=-"+sen.getPosition().getLatitude()+"\n");
					}else{
						body.append("LAT="+sen.getPosition().getLatitude()+"\n");
					}
					if(sen.getPosition().getLonHemisphere()==CompassPoint.WEST){
						body.append("LON=-"+sen.getPosition().getLongitude()+"\n");
					}else{
						body.append("LON="+sen.getPosition().getLongitude()+"\n");
					}
				}
				if(evt.getSentence() instanceof HeadingSentence){
					HeadingSentence sen = (HeadingSentence) evt.getSentence();
					if(sen.isTrue()){
						body.append("HDT="+sen.getHeading()+"\n");
					}else{
						body.append("HDM="+sen.getHeading()+"\n");
					}
				}
				if(evt.getSentence() instanceof RMCSentence){
					//;
					RMCSentence sen = (RMCSentence) evt.getSentence();
						body.append("LOG="+sen.getSpeed()+"\n");
					}
				if(evt.getSentence() instanceof VHWSentence){
					//;
					VHWSentence sen = (VHWSentence) evt.getSentence();
						body.append("LOG="+sen.getSpeedKnots()+"\n");
						body.append("HDM="+sen.getMagneticHeading()+"\n");
						body.append("HDT="+sen.getHeading()+"\n");
					}
				
				
				//MWV wind
				if(evt.getSentence() instanceof MWVSentence){
					MWVSentence sen = (MWVSentence) evt.getSentence();
					if(sen.isTrue()){
						body.append("WDT="+sen.getAngle()+"\n"
							+"WST="+sen.getSpeed()+"\n" 
							+"WSU="+sen.getSpeedUnit()+"\n");
					}else{
						body.append("WDA="+sen.getAngle()+"\n"
							+"WSA="+sen.getSpeed()+"\n" 
							+"WSU="+sen.getSpeedUnit()+"\n");
					}
				}
				exchange.getOut().setBody(body);
			}
			
			public void readingStopped() {}
			public void readingStarted() {}
			public void readingPaused() {}
		});
	}

	public String getSerialPort() {
		return serialPort;
	}

	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}
	
	public void stopSerial(){
		if(serial != null){
			serial.setRunning(false);
		}
	}
	
}
