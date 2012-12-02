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
	private Processor nmeaProcessor= new NMEAProcessor();
	private Processor imuProcessor= new IMUProcessor();
	private String serialPorts;
	private List<SerialPortReader> serialPortList = new ArrayList<SerialPortReader>();
	private Properties config;
	private Processor windProcessor = new WindProcessor();

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
			to("seda:input?multipleConsumers=true");
		
		}else{
			// start a serial port reader
			//we could have several serial ports
			String[] ports = serialPorts.split(",");
			for (String port: ports){
				ProducerTemplate producer = wc.getCamelContext().createProducerTemplate();
				SerialPortReader serial = new SerialPortReader();
				serial.setProducer(producer);
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
			.process(nmeaProcessor)
			.process(imuProcessor)
			.process(windProcessor )
			.to("log:nz.co.fortytwo?level=INFO")
			// and push to all web socket subscribers 
			.to("websocket:navData?sendToAll=true");
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
						body.append(Constants.LAT+"=-"+sen.getPosition().getLatitude()+",");
					}else{
						body.append(Constants.LAT+"="+sen.getPosition().getLatitude()+",");
					}
					if(sen.getPosition().getLonHemisphere()==CompassPoint.WEST){
						body.append(Constants.LON+"=-"+sen.getPosition().getLongitude()+",");
					}else{
						body.append(Constants.LON+"="+sen.getPosition().getLongitude()+",");
					}
				}
				if(evt.getSentence() instanceof HeadingSentence){
					HeadingSentence sen = (HeadingSentence) evt.getSentence();
					if(sen.isTrue()){
						body.append(Constants.COG+"="+sen.getHeading()+",");
					}else{
						body.append(Constants.MGH+"="+sen.getHeading()+",");
					}
				}
				if(evt.getSentence() instanceof RMCSentence){
					//;
					RMCSentence sen = (RMCSentence) evt.getSentence();
						body.append(Constants.SOG+"="+sen.getSpeed()+",");
					}
				if(evt.getSentence() instanceof VHWSentence){
					//;
					VHWSentence sen = (VHWSentence) evt.getSentence();
						body.append(Constants.SOG+"="+sen.getSpeedKnots()+",");
						body.append(Constants.MGH+"="+sen.getMagneticHeading()+",");
						body.append(Constants.COG+"="+sen.getHeading()+",");
					}
				
				
				//MWV wind
				if(evt.getSentence() instanceof MWVSentence){
					MWVSentence sen = (MWVSentence) evt.getSentence();
					if(sen.isTrue()){
						body.append(Constants.WDT+"="+sen.getAngle()+","
							+Constants.WST+"="+sen.getSpeed()+"," 
							+Constants.WSU+"="+sen.getSpeedUnit()+",");
					}else{
						body.append(Constants.WDA+"="+sen.getAngle()+","
							+Constants.WSA+"="+sen.getSpeed()+"," 
							+Constants.WSU+"="+sen.getSpeedUnit()+",");
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
