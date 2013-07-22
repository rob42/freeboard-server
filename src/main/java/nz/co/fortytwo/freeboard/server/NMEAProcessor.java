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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.parser.XDRValue;
import net.sf.marineapi.nmea.sentence.BVESentence;
import net.sf.marineapi.nmea.sentence.DepthSentence;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.VHWSentence;
import net.sf.marineapi.nmea.sentence.XDRSentence;
import net.sf.marineapi.nmea.util.CompassPoint;
import nz.co.fortytwo.freeboard.server.util.Constants;
import nz.co.fortytwo.freeboard.server.util.Util;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Processes NMEA sentences in the body of a message, firing events to interested listeners
 * 
 * @author robert
 * 
 */
public class NMEAProcessor extends FreeboardProcessor implements Processor, FreeboardHandler {

	private static Logger logger = Logger.getLogger(NMEAProcessor.class);
	private static final String DISPATCH_ALL = "DISPATCH_ALL";

	// map of sentence listeners
	private ConcurrentMap<String, List<SentenceListener>> listeners = new ConcurrentHashMap<String, List<SentenceListener>>();

	public NMEAProcessor() {
		setNmeaListeners();
	}

	public void process(Exchange exchange) throws Exception {
		if (exchange.getIn().getBody() == null) {
			return;
		}
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = exchange.getIn().getBody(HashMap.class);
		map = handle(map);
		exchange.getIn().setBody(map);
	}

	// @Override
	public HashMap<String, Object> handle(HashMap<String, Object> map) {
		// so we have a string
		String bodyStr = (String) map.get(Constants.NMEA);
		if (StringUtils.isNotBlank(bodyStr)) {
			try {
				logger.debug("Processing NMEA:"+bodyStr);
				// dont need the NMEA now
				map.remove(Constants.NMEA);
				Sentence sentence = SentenceFactory.getInstance().createParser(bodyStr);
				fireSentenceEvent(map, sentence);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return map;
	}

	/**
	 * Adds a {@link SentenceListener} that wants to receive all sentences read
	 * by the reader.
	 * 
	 * @param listener
	 *            {@link SentenceListener} to be registered.
	 * @see net.sf.marineapi.nmea.event.SentenceListener
	 */
	public void addSentenceListener(SentenceListener listener) {
		registerListener(DISPATCH_ALL, listener);
	}

	/**
	 * Adds a {@link SentenceListener} that is interested in receiving only
	 * sentences of certain type.
	 * 
	 * @param sl
	 *            SentenceListener to add
	 * @param type
	 *            Sentence type for which the listener is registered.
	 * @see net.sf.marineapi.nmea.event.SentenceListener
	 */
	public void addSentenceListener(SentenceListener sl, SentenceId type) {
		registerListener(type.toString(), sl);
	}

	/**
	 * Adds a {@link SentenceListener} that is interested in receiving only
	 * sentences of certain type.
	 * 
	 * @param sl
	 *            SentenceListener to add
	 * @param type
	 *            Sentence type for which the listener is registered.
	 * @see net.sf.marineapi.nmea.event.SentenceListener
	 */
	public void addSentenceListener(SentenceListener sl, String type) {
		registerListener(type, sl);
	}

	/**
	 * Remove a listener from reader. When removed, listener will not receive
	 * any events from the reader.
	 * 
	 * @param sl
	 *            {@link SentenceListener} to be removed.
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
	 * 
	 * @param map
	 * 
	 * @param sentence
	 *            sentence string.
	 */
	private void fireSentenceEvent(HashMap<String, Object> map, Sentence sentence) {
		if (!sentence.isValid()){
			logger.warn("NMEA Sentence is invalid:"+sentence.toSentence());
			return;
		}
		//TODO: Why am I creating all these lists?
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
				SentenceEvent se = new SentenceEvent(map, sentence);
				sl.sentenceRead(se);
			} catch (Exception e) {
				// ignore listener failures
				e.printStackTrace();
			}
		}

	}

	/**
	 * Registers a SentenceListener to hash map with given key.
	 * 
	 * @param type
	 *            Sentence type to register for
	 * @param sl
	 *            SentenceListener to register
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

	/**
	 * Adds NMEA sentence listeners to process NMEA to simple output
	 * 
	 * @param processor
	 */
	private void setNmeaListeners() {

		addSentenceListener(new SentenceListener() {

			private boolean startLat = true;
			private boolean startLon = true;
			double previousLat = 0;
			double previousLon = 0;
			double previousSpeed = 0;
			static final double ALPHA = 1 - 1.0 / 6;

			public void sentenceRead(SentenceEvent evt) {
				// Exchange exchange = (Exchange) evt.getSource();
				// StringBuilder body = new StringBuilder();
				@SuppressWarnings("unchecked")
				HashMap<String, Object> map = (HashMap<String, Object>) evt.getSource();
				if (evt.getSentence() instanceof PositionSentence) {
					PositionSentence sen = (PositionSentence) evt.getSentence();

					if (startLat) {
						previousLat = sen.getPosition().getLatitude();
						startLat = false;
					}
					previousLat = Util.movingAverage(ALPHA, previousLat, sen.getPosition().getLatitude());
					if (sen.getPosition().getLatHemisphere() == CompassPoint.SOUTH) {
						map.put(Constants.LAT, 0 - previousLat);
					} else {
						map.put(Constants.LAT, previousLat);
					}
					if (startLon) {
						previousLon = sen.getPosition().getLongitude();
						startLon = false;
					}
					previousLon = Util.movingAverage(ALPHA, previousLon, sen.getPosition().getLongitude());
					if (sen.getPosition().getLonHemisphere() == CompassPoint.WEST) {
						map.put(Constants.LON, 0 - previousLon);
					} else {
						map.put(Constants.LON, previousLon);
					}
				}

				if (evt.getSentence() instanceof HeadingSentence) {
					HeadingSentence sen = (HeadingSentence) evt.getSentence();
					if (sen.isTrue()) {
						map.put(Constants.COURSE_OVER_GND, sen.getHeading());
					} else {
						map.put(Constants.MAG_HEADING, sen.getHeading());
					}
				}
				if (evt.getSentence() instanceof RMCSentence) {
					RMCSentence sen = (RMCSentence) evt.getSentence();
					Util.checkTime(sen);

					previousSpeed = Util.movingAverage(ALPHA, previousSpeed, sen.getSpeed());
					map.put(Constants.SPEED_OVER_GND, previousSpeed);
				}
				if (evt.getSentence() instanceof VHWSentence) {
					// ;
					VHWSentence sen = (VHWSentence) evt.getSentence();
					previousSpeed = Util.movingAverage(ALPHA, previousSpeed, sen.getSpeedKnots());
					map.put(Constants.SPEED_OVER_GND, previousSpeed);

					map.put(Constants.MAG_HEADING, sen.getMagneticHeading());
					map.put(Constants.COURSE_OVER_GND, sen.getHeading());

				}

				// MWV wind
				// Mega sends $IIMVW with 0-360d clockwise from bow, (relative to bow)
				// Mega value is int+'.0'
				if (evt.getSentence() instanceof MWVSentence) {
					MWVSentence sen = (MWVSentence) evt.getSentence();
					// relative to true north
					// if (sen.isTrue()) {
					// map.put( Constants.WDT,sen.getAngle());
					// map.put( Constants.WST,sen.getSpeed());
					// map.put( Constants.WSU,sen.getSpeedUnit());
					//
					// } else {
					// relative to bow
					double angle = sen.getAngle();
					map.put(Constants.WIND_DIR_APPARENT, angle);
					map.put(Constants.WIND_SPEED_APPARENT, sen.getSpeed());
					map.put(Constants.WIND_SPEED_UNITS, sen.getSpeedUnit());
					// }
				}
				// Cruzpro BVE sentence
				if (evt.getSentence() instanceof BVESentence) {
					BVESentence sen = (BVESentence) evt.getSentence();
					if (sen.isFuelGuage()) {
						map.put(Constants.FUEL_REMAINING, sen.getFuelRemaining());
						map.put(Constants.FUEL_USE_RATE, sen.getFuelUseRateUnitsPerHour());
						map.put(Constants.FUEL_USED, sen.getFuelUsedOnTrip());
					}
					if (sen.isEngineRpm()) {
						map.put(Constants.ENGINE_RPM, sen.getEngineRpm());
						map.put(Constants.ENGINE_HOURS, sen.getEngineHours());
						map.put(Constants.ENGINE_MINUTES, sen.getEngineMinutes());

					}
					if (sen.isTempGuage()) {
						map.put(Constants.ENGINE_TEMP, sen.getEngineTemp());
						map.put(Constants.ENGINE_VOLTS, sen.getVoltage());
						// map.put(Constants.ENGINE_TEMP_HIGH_ALARM, sen.getHighTempAlarmValue());
						// map.put(Constants.ENGINE_TEMP_LOW_ALARM, sen.getLowTempAlarmValue());

					}
					if (sen.isPressureGuage()) {
						map.put(Constants.ENGINE_OIL_PRESSURE, sen.getPressure());
						// map.put(Constants.ENGINE_PRESSURE_HIGH_ALARM, sen.getHighPressureAlarmValue());
						// map.put(Constants.ENGINE_PRESSURE_LOW_ALARM, sen.getLowPressureAlarmValue());

					}

				}
				if (evt.getSentence() instanceof DepthSentence) {
					DepthSentence sen = (DepthSentence) evt.getSentence();
					//in meters
					map.put(Constants.DEPTH_BELOW_TRANSDUCER, sen.getDepth());
				}
				// Cruzpro YXXDR sentence
				//from Cruzpro - The fields in the YXXDR sentence are always from the same "critical" functions, in order:
					//RPM
					//Battery #1 Volts
					//Depth
					//Oil Pressure
					//Engine Temperature
				//freeboard.nmea.YXXDR.MaxVu110=RPM,EVV,DBT,EPP,ETT
				if (evt.getSentence() instanceof XDRSentence) {
					XDRSentence sen = (XDRSentence) evt.getSentence();
					
					if(StringUtils.isNotBlank(sen.getDevice())){
						try {
							String key = Util.getConfig(null).getProperty(Constants.NMEA_XDR+sen.getTalkerId()+Constants.XDR+sen.getDevice());
							String[] keys = key.split(",");
							List<XDRValue> values = sen.getValues();
							if(values.size()==keys.length){
								//iterate through the values assigning to Freeboard keys
								for(int x=0;x<keys.length;x++){
									if(StringUtils.isNotBlank(keys[x])){
										map.put(keys[x], values.get(x).getValue());
									}
								}
							}
						
						} catch (Exception e) {
							logger.debug(e.getMessage(),e);
						}
					}
				}
				
			}

			public void readingStopped() {
			}

			public void readingStarted() {
			}

			public void readingPaused() {
			}
		});
	}

}
