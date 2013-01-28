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

import static org.junit.Assert.assertEquals;

import org.apache.camel.util.ExchangeHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WindProcessorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTrueWindDir() {
		WindProcessor wp = new WindProcessor();
		// test 0 wind, 0deg, 0spd
		wp.calcTrueWindDirection(0, 0, 0);
		assertEquals(0.0,wp.getTrueDirection(), 1.0);
		assertEquals(0.0, wp.getTrueWindSpeed(), 0.1);

		// test 10 wind, 90deg, 0spd
		wp.calcTrueWindDirection(10, 90, 0);
		assertEquals(90.0, wp.getTrueDirection(), 1.0);
		assertEquals(10.0, wp.getTrueWindSpeed(), 0.1);
		
		// test 10 wind, 900deg, 10spd = 135deg 14.14
		wp.calcTrueWindDirection(10, 90, 10);
		assertEquals(135.0, wp.getTrueDirection(), 1.0);
		assertEquals(14.14, wp.getTrueWindSpeed(), 0.1);
		
		// test 10 wind, 270deg, 10spd = 360-135, 14.14
		wp.calcTrueWindDirection(10, 270, 10);
		assertEquals(225.0, wp.getTrueDirection(), 1.0);
		assertEquals(14.14, wp.getTrueWindSpeed(), 0.1);
		
		// test .3 wind, 80deg, 0.5spd = 146, 0.9
		wp.calcTrueWindDirection(.3, 80, .5);
		assertEquals(146.0, wp.getTrueDirection(), 1.0);
		assertEquals(0.5, wp.getTrueWindSpeed(), 0.1);
		
		// test 10 wind, -90deg, 6.5spd = 146, 0.9
		wp.calcTrueWindDirection(10, 270, 6.5);
		assertEquals(360-123.0, wp.getTrueDirection(), 1.0);
		assertEquals(11.9, wp.getTrueWindSpeed(), 0.1);

	}
	

}
