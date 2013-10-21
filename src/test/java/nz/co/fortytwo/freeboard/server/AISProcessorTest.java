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

package nz.co.fortytwo.freeboard.server;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import nz.co.fortytwo.freeboard.server.util.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AISProcessorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldParseSingleMessage() {
		String msg = "$PGHP,1,2010,6,11,11,46,11,929,244,0,,1,72*21\r\n";
        msg += "\\1G2:0125,c:1354719387*0D";
        msg += "\\!AIVDM,2,1,4,A,539LiHP2;42`@pE<000<tq@V1<TpL4000000001?1SV@@73R0J0TQCAD,0*1E\r\n";
        msg += "\\2G2:0125*7B";
        msg += "\\!AIVDM,2,2,4,A,R0EQCP000000000,2*45";
		AISProcessor processor = new AISProcessor();
		HashMap<String, Object>map = new HashMap<String,Object>();
		map.put(Constants.AIS, msg);
		processor.handle(map);
		Assert.assertNull(map.get(Constants.AIS));
		Assert.assertEquals(map.get("TEST"), 5);
	}
	
	@Test
	public void shouldParseSeparatedMessage(){
		
		String msg1 = "$PGHP,1,2013,3,13,10,39,18,375,219,,2190047,1,4A*57\r\n";
		String msg2 = "\\g:1-2-0136,c:1363174860*24\\!BSVDM,2,1,4,B,53B>2V000000uHH4000@T4p4000000000000000S30C6340006h00000,0*4C\r\n";
		String msg3 = "\\g:2-2-0136*59\\!BSVDM,2,2,4,B,000000000000000,2*3A";
		AISProcessor processor = new AISProcessor();
		HashMap<String, Object>map = new HashMap<String,Object>();
		map.put(Constants.AIS, msg1);
		processor.handle(map);
		Assert.assertNull(map.get(Constants.AIS));
		Assert.assertNull(map.get("TEST"));
		
		map.put(Constants.AIS, msg2);
		processor.handle(map);
		Assert.assertNull(map.get(Constants.AIS));
		Assert.assertNull(map.get("TEST"));
		
		map.put(Constants.AIS, msg3);
		processor.handle(map);
		Assert.assertNull(map.get(Constants.AIS));
		Assert.assertEquals(map.get("TEST"), 5);
	}
	
	@Test
	public void shouldParseTwoMessages(){
		
		String msg1 = "$PGHP,1,2013,3,13,10,39,18,375,219,,2190047,1,4A*57\r\n";
		String msg2 = "\\g:1-2-0136,c:1363174860*24\\!BSVDM,2,1,4,B,53B>2V000000uHH4000@T4p4000000000000000S30C6340006h00000,0*4C\r\n";
		String msg3 = "\\g:2-2-0136*59\\!BSVDM,2,2,4,B,000000000000000,2*3A";
		AISProcessor processor = new AISProcessor();
		HashMap<String, Object>map = new HashMap<String,Object>();
		map.put(Constants.AIS, msg1);
		processor.handle(map);
		Assert.assertNull(map.get(Constants.AIS));
		Assert.assertNull(map.get("TEST"));
		
		map.put(Constants.AIS, msg2);
		processor.handle(map);
		Assert.assertNull(map.get(Constants.AIS));
		Assert.assertNull(map.get("TEST"));
		
		map.put(Constants.AIS, msg3);
		processor.handle(map);
		Assert.assertNull(map.get(Constants.AIS));
		Assert.assertEquals(map.get("TEST"), 5);
		map.clear();
		
		String msg = "$PGHP,1,2010,6,11,11,46,11,929,244,0,,1,72*21\r\n";
        msg += "\\1G2:0125,c:1354719387*0D";
        msg += "\\!AIVDM,2,1,4,A,539LiHP2;42`@pE<000<tq@V1<TpL4000000001?1SV@@73R0J0TQCAD,0*1E\r\n";
        msg += "\\2G2:0125*7B";
        msg += "\\!AIVDM,2,2,4,A,R0EQCP000000000,2*45";
		
		map.put(Constants.AIS, msg);
		processor.handle(map);
		Assert.assertNull(map.get(Constants.AIS));
		Assert.assertEquals(map.get("TEST"), 5);
	}

}
