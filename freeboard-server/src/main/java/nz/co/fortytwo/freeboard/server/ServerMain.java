package nz.co.fortytwo.freeboard.server;

import org.apache.camel.main.Main;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;


public class ServerMain {

	private static Server server;

	private static String SERIAL_URL = "/home/robert/camel.txt&scanStream=true&scanStreamDelay=500";

	private ServerMain() {
        // to pass checkstyle we have a private constructor
    }

    public static void main(String[] args) throws Exception {
    	if(args!=null && args.length>0 && StringUtils.isNotBlank(args[0])){
    		SERIAL_URL=args[0];
    	}
        System.out.println("\n\n\n\n");
        System.out.println("===============================================");
        System.out.println("Open your web browser on http://localhost:9090");
        System.out.println("Press ctrl+c to stop this example");
        System.out.println("===============================================");
        System.out.println("\n\n\n\n");

        // create a new Camel Main so we can easily start Camel
        Main main = new Main();

        // enable hangup support which mean we detect when the JVM terminates, and stop Camel graceful
        main.enableHangupSupport();

        NavDataWebSocketRoute route = new NavDataWebSocketRoute();

        // web socket on port 9090
        route.setPort(9090);
        route.setSerialUrl(SERIAL_URL);
        // add our routes to Camel
        main.addRouteBuilder(route);
        
        //add the meshcms
        server = new Server();

        Connector connector = new SelectChannelConnector();
        connector.setPort(8080);

        server.addConnector(connector);
        

        WebAppContext wac = new WebAppContext();
     
        wac.setWar("meshcms/.");
        wac.setDefaultsDescriptor("meshcms/WEB-INF/webdefault.xml");

        wac.setDescriptor("meshcms/WEB-INF/web.xml");
        wac.setContextPath("/meshcms");
        wac.setServer(server);
        //wac.setWar("target/meshcms.war");
        wac.setParentLoaderPriority(true);
        server.setHandler(wac);
        server.start();
        // and run, which keeps blocking until we terminate the JVM (or stop CamelContext)
        main.run();
    }

}
