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

public class NMEAProcessor implements Processor {

	private static final String DISPATCH_ALL = "DISPATCH_ALL";

	// map of sentence listeners
    private ConcurrentMap<String, List<SentenceListener>> listeners = new ConcurrentHashMap<String, List<SentenceListener>>();
			
	public void process(Exchange exchange) throws Exception {
		if(StringUtils.isEmpty(exchange.getIn().getBody(String.class))) return;
		//so we have a string
		String bodyStr = exchange.getIn().getBody(String.class).trim();
		if(bodyStr.startsWith("$")){
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
