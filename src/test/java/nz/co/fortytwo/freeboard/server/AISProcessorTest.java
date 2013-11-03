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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import junit.framework.Assert;
import nz.co.fortytwo.freeboard.server.ais.AisVesselInfo;
import nz.co.fortytwo.freeboard.server.util.Constants;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

//public class AISProcessorTest extends CamelTestSupport {
public class AISProcessorTest{

	private static Logger log = Logger.getLogger(AISProcessorTest.class);
	/*
	@EndpointInject(uri = "mock:result")
	protected MockEndpoint resultEndpoint;

	@Produce(uri = "direct:start")
	protected ProducerTemplate template;
*/
	@Test
	public void shouldIgnoreSingleMessage() {
		String msg = "$PGHP,1,2010,6,11,11,46,11,929,244,0,,1,72*21\r\n";
		msg += "\\1G2:0125,c:1354719387*0D";
		msg += "\\!AIVDM,2,1,4,A,539LiHP2;42`@pE<000<tq@V1<TpL4000000001?1SV@@73R0J0TQCAD,0*1E\r\n";
		msg += "\\2G2:0125*7B";
		msg += "\\!AIVDM,2,2,4,A,R0EQCP000000000,2*45";
		AISProcessor processor = new AISProcessor();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(Constants.AIS, msg);
		processor.handle(map);
		Assert.assertNotNull(map.get(Constants.AIS));

	}

	@Test
	public void shouldParseSingleMessage() {
		String msg = "!AIVDM,1,1,,B,15MwkRUOidG?GElEa<iQk1JV06Jd,0*6D";

		AISProcessor processor = new AISProcessor();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(Constants.AIS, msg);
		processor.handle(map);
		Assert.assertNotNull(map.get(Constants.AIS));
		Assert.assertTrue(map.get(Constants.AIS) instanceof AisVesselInfo);
		log.debug(msg);
	}

	@Test
	public void shouldIgnoreSeparatedMessage() {

		String msg1 = "$PGHP,1,2013,3,13,10,39,18,375,219,,2190047,1,4A*57\r\n";
		String msg2 = "\\g:1-2-0136,c:1363174860*24\\!BSVDM,2,1,4,B,53B>2V000000uHH4000@T4p4000000000000000S30C6340006h00000,0*4C\r\n";
		String msg3 = "\\g:2-2-0136*59\\!BSVDM,2,2,4,B,000000000000000,2*3A";
		AISProcessor processor = new AISProcessor();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(Constants.AIS, msg1);
		processor.handle(map);
		Assert.assertNull(map.get(Constants.AIS));

		map.put(Constants.AIS, msg2);
		processor.handle(map);
		Assert.assertNull(map.get(Constants.AIS));

		map.put(Constants.AIS, msg3);
		processor.handle(map);
		Assert.assertNotNull(map.get(Constants.AIS));
		// Assert.assertEquals(map.get("TEST"), 5);
	}

	@Test
	public void shouldParseTwoMessages() {

		String msg1 = "!AIVDM,1,1,,A,15MvJw5P0NG?Us6EaDVTTOvR06Jd,0*22";
		AISProcessor processor = new AISProcessor();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(Constants.AIS, msg1);
		processor.handle(map);
		Assert.assertNotNull(map.get(Constants.AIS));
		Assert.assertTrue(map.get(Constants.AIS) instanceof AisVesselInfo);

		map.clear();

		String msg = "!AIVDM,1,1,,B,15Mtu:0000o@05tE`?Ctn@6T06Jd,0*40";

		map.put(Constants.AIS, msg);
		processor.handle(map);
		Assert.assertNotNull(map.get(Constants.AIS));
		Assert.assertTrue(map.get(Constants.AIS) instanceof AisVesselInfo);
	}

	@Test
	public void shouldIgnoreTwoMessages() {

		String msg1 = "$PGHP,1,2013,3,13,10,39,18,375,219,,2190047,1,4A*57\r\n";
		String msg2 = "\\g:1-2-0136,c:1363174860*24\\!BSVDM,2,1,4,B,53B>2V000000uHH4000@T4p4000000000000000S30C6340006h00000,0*4C\r\n";
		String msg3 = "\\g:2-2-0136*59\\!BSVDM,2,2,4,B,000000000000000,2*3A";
		AISProcessor processor = new AISProcessor();
		HashMap<String, Object> map = new HashMap<String, Object>();
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
		Assert.assertNotNull(map.get(Constants.AIS));
		map.clear();

		String msg = "$PGHP,1,2010,6,11,11,46,11,929,244,0,,1,72*21\r\n";
		msg += "\\1G2:0125,c:1354719387*0D";
		msg += "\\!AIVDM,2,1,4,A,539LiHP2;42`@pE<000<tq@V1<TpL4000000001?1SV@@73R0J0TQCAD,0*1E\r\n";
		msg += "\\2G2:0125*7B";
		msg += "\\!AIVDM,2,2,4,A,R0EQCP000000000,2*45";

		map.put(Constants.AIS, msg);
		processor.handle(map);
		Assert.assertNotNull(map.get(Constants.AIS));
		// Assert.assertEquals(map.get("TEST"), 5);
	}
/*
	@Test
	@Ignore
	public void shouldProduceJson() throws InterruptedException, UnsupportedEncodingException {
		resultEndpoint.expectedMessageCount(1);
		String msg = "!AIVDM,1,1,,A,15Mw0LPP3hG?PPTEag8UW?vj0@?A,0*47";
		AISProcessor processor = new AISProcessor();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(Constants.AIS, msg);
		processor.handle(map);
		// RouteBuilder builder = createRouteBuilder();
		template.sendBody(map);
		resultEndpoint.assertIsSatisfied();
		byte[] bytes = (byte[]) resultEndpoint.getReceivedExchanges().get(0).getIn().getBody();
		String json = new String(bytes, "UTF-8");

		log.debug(json);
		assertTrue(json.indexOf("{\"AIS\":{\"position\":{") == 0);

	}

	@Test
	@Ignore
	public void shouldProduceJson2() throws InterruptedException, UnsupportedEncodingException {
		resultEndpoint.expectedMessageCount(1);

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(Constants.AIS, 123);
		// processor.handle(map);
		// RouteBuilder builder = createRouteBuilder();
		template.sendBody(map);
		resultEndpoint.assertIsSatisfied();
		byte[] bytes = (byte[]) resultEndpoint.getReceivedExchanges().get(0).getIn().getBody();
		String json = new String(bytes, "UTF-8");

		log.debug(json);
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			public void configure() {
				from("direct:start").marshal().json(JsonLibrary.Jackson).to("mock:result");
			}
		};
	}*/
}
