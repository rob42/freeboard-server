package net.sf.marineapi.nmea.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import net.sf.marineapi.nmea.sentence.ROTSentence;

import org.junit.Before;
import org.junit.Test;

/**
 * ROTTest
 * 
 * @author Robert Huitema
 */
public class ROTTest {

	/** Example sentence */
	public static final String EXAMPLE = "$TIROT,4.1,A*3E";

	private ROTSentence rot;

	@Before
	public void setUp() {
		try {
			rot = new ROTParser(EXAMPLE);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link net.sf.marineapi.nmea.parser.ROTParser#getWaypointIds()}.
	 */
	@Test
	public void testGetRateOfTurn() {
		double rate = rot.getRateOfTurn();
		assertEquals(4.1, rate,0.01);
		
	}


}
