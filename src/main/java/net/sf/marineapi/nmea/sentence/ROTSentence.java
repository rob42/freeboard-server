/* 
 * ROTSentence.java
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
package net.sf.marineapi.nmea.sentence;

import net.sf.marineapi.nmea.parser.DataNotAvailableException;

/**
 * Interface for ROT sentence type. Route data and list of waypoint IDs.
 * <p>
 * Example:<br>
 * <pre>

        1   2 3
        |   | |
 $--ROT,x.x,A*hh<CR><LF>

Field Number:

    Rate Of Turn, degrees per minute, "-" means bow turns to port

    Status, A means data is valid

    Checksum
</pre>
 * 
 * @author Robert Huitema
 * @version $Revision$
 */
public interface ROTSentence extends Sentence {

    public double getRateOfTurn() throws DataNotAvailableException;
    
    public void setRateOfTurn(double rate);

}
