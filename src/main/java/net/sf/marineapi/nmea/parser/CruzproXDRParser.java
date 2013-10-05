/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 * 
 * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
 *
 *  FreeBoard is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.

 *  FreeBoard is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with FreeBoard.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * XDRParser.java
 * Copyright (C) 2010 Kimmo Tuukkanen
 * 
 * This file is part of Java Marine API.
 * <http://ktuukkan.github.io/marine-api/>
 * 
 * Java Marine API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Java Marine API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Java Marine API. If not, see <http://www.gnu.org/licenses/>.
 * 
 *  === XDR - Cross Track Error - Dead Reckoning ===
 *
 * ------------------------------------------------------------------------------
 *         1 2   3 4            n
 *         | |   | |            |
 *  $--XDR,a,x.x,a,c--c, ..... *hh<CR><LF>
 * ------------------------------------------------------------------------------
 *
 * Field Number: 
 *
 * 1. Transducer Type
 * 2. Measurement Data
 * 3. Units of measurement
 * 4. Name of transducer
 *
 * There may be any number of quadruplets like this, each describing a
 * sensor.  The last field will be a checksum as usual.
 */
package net.sf.marineapi.nmea.parser;

import java.util.ArrayList;
import java.util.List;

import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.sentence.XDRSentence;
import net.sf.marineapi.nmea.util.Measurement;

/**
 * Cruzpro non-standard XDR sentence parser.
 * The scheme for resolving the values to freeboard keys is done via the freeboard.cfg properties.
 * Under the spec each value has a name, and the names are concatenated to a string as 'getDevice()'. This string is 
 * looked up as property freeboard.nmea.YXXDR.cat_string=key1,key2,key3,keyn
 * <p>
 * The number of keys should match the values in the XDR. A blank key (key1,,key3) is skipped.
 * In the case of Cruzpro (3 fields per value) the device string is 'MaxVu110' 
 * 
 * @author Robert Huitema
 */
public class CruzproXDRParser extends SentenceParser implements XDRSentence {

	private static final String MAX_VU110 = "MaxVu110";
	private List<Measurement> measurements = new ArrayList<Measurement>();
	String device = null;
	/**
	 * Constructor.
	 * 
	 * @param nmea
	 *            XDR sentence string
	 */
	public CruzproXDRParser(String nmea) {
		
		super(nmea, "XDR");
		int n = getFieldCount();
		if(n<4)return;
		//Cruzpro uses 3 per group and name at the end, std NMEA uses 4
		if(nmea.indexOf(MAX_VU110)<0){
			StringBuilder bldr = new StringBuilder();
			for(int x=0; x<n;x=x+4 ){
				Measurement val = new Measurement();
				if(hasValue(x))val.setType(getStringValue(x));
				if(hasValue(x+1))val.setValue(getDoubleValue(x+1));
				if(hasValue(x+2))val.setUnits(getStringValue(x+2));
				if(hasValue(x+3))val.setName(getStringValue(x+3));
				measurements.add(val);
				if(hasValue(x+3))bldr.append(getStringValue(x+3));
			}
			setDevice(bldr.toString());
		}else{
			String name = getStringValue(n-1);
			setDevice(name);
			for(int x=0; x<n-1;x=x+3 ){
				Measurement val = new Measurement();
				if(hasValue(x))val.setType(getStringValue(x));
				if(hasValue(x+1))val.setValue(getDoubleValue(x+1));
				if(hasValue(x+2))val.setUnits(getStringValue(x+2));
				val.setName(name);
				measurements.add(val);
			}
		}
	}

	/**
	 * Creates XDR parser with empty sentence.
	 * 
	 * @param talker
	 *            TalkerId to set
	 */
	public CruzproXDRParser(TalkerId talker) {
		super(talker, "XDR", 1);
	}

	
	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public List<Measurement> getMeasurements() {
		return measurements;
	}

	public void setMeasurement(Measurement m) {
		measurements.add(m);
		
	}

	/* (non-Javadoc)
	 * @see net.sf.marineapi.nmea.sentence.XDRSentence#setMeasurements(java.util.List)
	 */
	public void setMeasurements(List<Measurement> measurements) {
		this.measurements=measurements;
		
	}

	public void addMeasurement(Measurement... m) {
		for(Measurement mt : m){
			measurements.add(mt);
		}
		
	}


}

