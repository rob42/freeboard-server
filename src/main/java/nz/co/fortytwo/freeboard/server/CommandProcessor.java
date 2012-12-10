package nz.co.fortytwo.freeboard.server;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;

/**
 * Churns through incoming nav data and creates output messages for the other devices which need it
 * 
 * @author robert
 *
 */
public class CommandProcessor implements Processor {

	private ProducerTemplate producer;
	
	public void process(Exchange exchange) throws Exception {
		String msg = (String) exchange.getIn().getBody();
		String[] data = msg.split(",");
		StringBuffer outMsg = new StringBuffer();
		outMsg.append(Constants.UID+":"+Constants.MEGA+",");
		for(String m:data){
			//send to MEGA for anchor alarms, and autopilot
			if(m.startsWith(Constants.WST))outMsg.append(m+",");
			if(m.startsWith(Constants.WSA))outMsg.append(m+",");
			if(m.startsWith(Constants.WDT))outMsg.append(m+",");
			if(m.startsWith(Constants.WDA))outMsg.append(m+",");
			if(m.startsWith(Constants.WSU))outMsg.append(m+",");
			if(m.startsWith(Constants.LAT))outMsg.append(m+",");
			if(m.startsWith(Constants.LON))outMsg.append(m+",");
			if(m.startsWith(Constants.COG))outMsg.append(m+",");
			if(m.startsWith(Constants.MGH))outMsg.append(m+",");
			if(m.startsWith(Constants.SOG))outMsg.append(m+",");
			if(m.startsWith(Constants.YAW))outMsg.append(m+",");
		}
		//now send to output queue
        producer.asyncSendBody("seda:output", outMsg);
	}

	public void setProducer(ProducerTemplate producer) {
		this.producer = producer;
	}

}
