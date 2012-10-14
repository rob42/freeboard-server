package nz.co.fortytwo.freeboard.server;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.websocket.WebsocketComponent;

public class NavDataWebSocketRoute extends RouteBuilder{
	  private int port = 9090;

	    public int getPort() {
	        return port;
	    }

	    public void setPort(int port) {
	        this.port = port;
	    }

	    @Override
	    public void configure() throws Exception {
	        // setup Camel web-socket component on the port we have defined
	        WebsocketComponent wc = getContext().getComponent("websocket", WebsocketComponent.class);
	       
	        wc.setPort(port);
	        // we can serve static resources from the classpath: or file: system
	        wc.setStaticResources("classpath:.");

	        // poll serial to get new data
	        //from("websocket:navData")
	        from("stream:file?fileName=/home/robert/camel.txt&scanStream=true&scanStreamDelay=500")
	        	.to("log:nz.co.fortytwo?level=DEBUG");
	            // and push tweets to all web socket subscribers on camel-tweet
	            //.to("websocket:navData?sendToAll=true");
	    }
}
