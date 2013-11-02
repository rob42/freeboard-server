/* 
 * ROTParser.java
 * Copyright (C) 2010 Kimmo Tuukkanen, Robert Huitema
 * 
 * This file is part of Java Marine API.
 * <http://sourceforge.net/projects/marineapi/>
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
 */
package net.sf.marineapi.nmea.parser;

import net.sf.marineapi.nmea.sentence.ROTSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;

/**
 * ROT sentence parser.
 * 
 * @author Robert Huitema
 * @version $Revision$
 */
public class ROTParser extends SentenceParser implements ROTSentence {



    /**
     * Creates a new instance of ROT parser.
     * 
     * @param nmea RTE sentence string.
     */
    public ROTParser(String nmea) {
        super(nmea, "ROT");
    }
    
    public ROTParser(TalkerId talker) {
		super(talker, "ROT", 1);
	}

	
	public double getRateOfTurn() throws DataNotAvailableException {
		return getDoubleValue(0);
	}

	
	public void setRateOfTurn(double rate) {
		setDoubleValue(0, rate);
		
	}

   
}
