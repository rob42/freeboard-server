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
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;

/**
 * Processes IMU sentences in the body of a message, firing events to interested listeners 
 * 
 * <ul>
 * <li>PRINT_EULER = !!!VER:1.8.1,RLL:1.12,PCH:-0.07,YAW:34.03,IMUH:253,LAT:499247361,LON:-1193955623, ALT:3525,COG:0,SOG:0,FIX:1,SAT:9,TOW:21230400***</li>
 * <li>PRINT_MAGNETOMETER = !!!VER:1.7,MGX:-141,MGY:172,MGZ:389,MGH:-2.26,LAT:-412936366,LON:1732470066,ALT:0,COG:234.00,SOG:0.00,FIX:1,EVZ:0.00,SAT:0,TOW:0***</li>
 * <li>Both =!!!VER:1.7,RLL:1.12,PCH:-0.07,YAW:34.03,IMUH:253, MGX:-143,MGY:170,MGZ:386,MGH:-2.27,TOW:0***</li>
 *<li></li>
 *  <li>Roll: Measured in degrees with positive and increasing as the right wing drops</li>
 *  <li>Pitch: Measured in degrees with positive and increasing as the nose rises</li>
 *   <li>Yaw: Measured in degrees with positive and increasing as the nose goes right</li>
 *   <li>IMUH: outputs imu_health?</li>
 *   <li>Latitude: Measured in decimal degrees times 10^7</li>
  *  <li>Longitude: Measured in decimal degrees times 10^7</li>
  *  <li>Altitude: Measured in meters above sea level times 10^1</li>
  *  <li>Course over ground:</li>
  *  <li>Speed over ground:</li>
  *  <li>GPS Fix: A binary indicator of a valid gps fix</li>
  *  <li>Satellite Count: The number of GPS satellites used to calculate this position</li>
  *  <li>Time of week: Time of week is related to GPS time formats. If this is important to your system, I suggest you read external resources on this confusing topic.</li>
  *  </ul> 
 * @author robert
 *
 */
public class IMUProcessor implements Processor {

	private static final String DISPATCH_ALL = "DISPATCH_ALL";

	// map of sentence listeners
    private ConcurrentMap<String, List<SentenceListener>> listeners = new ConcurrentHashMap<String, List<SentenceListener>>();
			
	public void process(Exchange exchange) throws Exception {
		if(StringUtils.isEmpty(exchange.getIn().getBody(String.class))) return;
		//so we have a string
		String bodyStr = exchange.getIn().getBody(String.class).trim();
		if(bodyStr.startsWith("!!!VER:")){
			try{
				Sentence sentence = SentenceFactory.getInstance().createParser(bodyStr);
				fireSentenceEvent(exchange,sentence);
			}catch(Exception e){
				//e.printStackTrace();
			}
		}
	}

	  /**
     * Adds a {@link SentenceListener} that wants to receive all sentences read
     * by the reader.
     * 
     * @param listener {@link SentenceListener} to be registered.
     * @see net.sf.marineapi.nmea.event.SentenceListener
     */
    public void addSentenceListener(SentenceListener listener) {
        registerListener(DISPATCH_ALL, listener);
    }

    /**
     * Adds a {@link SentenceListener} that is interested in receiving only
     * sentences of certain type.
     * 
     * @param sl SentenceListener to add
     * @param type Sentence type for which the listener is registered.
     * @see net.sf.marineapi.nmea.event.SentenceListener
     */
    public void addSentenceListener(SentenceListener sl, SentenceId type) {
        registerListener(type.toString(), sl);
    }

    /**
     * Adds a {@link SentenceListener} that is interested in receiving only
     * sentences of certain type.
     * 
     * @param sl SentenceListener to add
     * @param type Sentence type for which the listener is registered.
     * @see net.sf.marineapi.nmea.event.SentenceListener
     */
    public void addSentenceListener(SentenceListener sl, String type) {
        registerListener(type, sl);
    }

    /**
     * Remove a listener from reader. When removed, listener will not receive
     * any events from the reader.
     * 
     * @param sl {@link SentenceListener} to be removed.
     */
    public void removeSentenceListener(SentenceListener sl) {
        for (List<SentenceListener> list : listeners.values()) {
            if (list.contains(sl)) {
                list.remove(sl);
            }
        }
    }
    
    /**
     * Dispatch data to all listeners.
     * @param exchange 
     * 
     * @param sentence sentence string.
     */
    private void fireSentenceEvent(Exchange exchange, Sentence sentence) {
    	if(!sentence.isValid())return;
    	
        String type = sentence.getSentenceId();
        Set<SentenceListener> list = new HashSet<SentenceListener>();

        if (listeners.containsKey(type)) {
            list.addAll(listeners.get(type));
        }
        if (listeners.containsKey(DISPATCH_ALL)) {
            list.addAll(listeners.get(DISPATCH_ALL));
        }

        for (SentenceListener sl : list) {
            try {
                SentenceEvent se = new SentenceEvent(exchange, sentence);
                sl.sentenceRead(se);
            } catch (Exception e) {
                // ignore listener failures
            }
        }
        
    }

    /**
     * Registers a SentenceListener to hash map with given key.
     * 
     * @param type Sentence type to register for
     * @param sl SentenceListener to register
     */
    private void registerListener(String type, SentenceListener sl) {
        if (listeners.containsKey(type)) {
            listeners.get(type).add(sl);
        } else {
            List<SentenceListener> list = new Vector<SentenceListener>();
            list.add(sl);
            listeners.put(type, list);
        }
    }

}
