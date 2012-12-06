package nz.co.fortytwo.freeboard.server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.util.CompassPoint;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;

/**
 * Processes IMU sentences in the body of a message, firing events to interested listeners
 * 
 * <ul>
 * <li>!!!VER:1.9,RLL:-0.52,PCH:0.06,YAW:80.24,IMUH:253,MGX:44,MGY:-254,MGZ:-257,MGH:80.11,LAT:-412937350,LON:1732472000,ALT:14,COG:116,SOG:0,FIX:1,SAT:5,TOW:
 * 22504700***
 * <li>
 * <li>!!!VER:1.9,RLL:0.49,PCH:-0.53,YAW:80.24,IMUH:253,MGX:45,MGY:-253,MGZ:-257,MGH:80.08,TOW:22504700***</LI>
 * <li></li>
 * <li>Roll: Measured in degrees with positive and increasing as the right wing drops</li>
 * <li>Pitch: Measured in degrees with positive and increasing as the nose rises</li>
 * <li>Yaw: Measured in degrees with positive and increasing as the nose goes right</li>
 * <li>IMUH: outputs imu_health?</li>
 * <li>Latitude: Measured in decimal degrees times 10^7</li>
 * <li>Longitude: Measured in decimal degrees times 10^7</li>
 * <li>Altitude: Measured in meters above sea level times 10^1</li>
 * <li>Course over ground:</li>
 * <li>Speed over ground:</li>
 * <li>GPS Fix: A binary indicator of a valid gps fix</li>
 * <li>Satellite Count: The number of GPS satellites used to calculate this position</li>
 * <li>Time of week: Time of week is related to GPS time formats. If this is important to your system, I suggest you read external resources on this confusing
 * topic.</li>
 * </ul>
 * 
 * @author robert
 * 
 */
public class IMUProcessor implements Processor {

	private static final String DISPATCH_ALL = "DISPATCH_ALL";

	// map of sentence listeners
	private ConcurrentMap<String, List<SentenceListener>> listeners = new ConcurrentHashMap<String, List<SentenceListener>>();

	public void process(Exchange exchange) throws Exception {
		if (StringUtils.isEmpty(exchange.getIn().getBody(String.class)))
			return;
		// so we have a string
		String bodyStr = exchange.getIn().getBody(String.class).trim();
		if (bodyStr.startsWith("!!!VER:")) {
			try {
				// trim start/end, add comma
				bodyStr=bodyStr.substring(bodyStr.indexOf(",") + 1, bodyStr.lastIndexOf("***"))+",";
						//LAT:-412937350,LON:1732472000
				//String [] bodyArray=bodyStr.split(",");
				//for(String s:bodyArray){
					
				//}
				exchange.getOut().setBody(bodyStr);

			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}

	

}
