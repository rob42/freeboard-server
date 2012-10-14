package nz.co.fortytwo.freeboard.server;

import org.apache.camel.main.Main;

public class ServerMain {

	//private static Server server;

	private ServerMain() {
        // to pass checkstyle we have a private constructor
    }

    public static void main(String[] args) throws Exception {
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

        // add our routes to Camel
        main.addRouteBuilder(route);
        
        //add the meshcms
        //server = new Server();

        //Connector connector = new SelectChannelConnector();
        //connector.setPort(8080);

        //server.addConnector(connector);

//        WebAppContext wac = new WebAppContext();
//        wac.setWar("target/meshcms/.");
//        wac.setDescriptor("WEB-INF/web.xml");
//        wac.setContextPath("/meshcms");
//        wac.setServer(server);
//        //wac.setWar("target/meshcms.war");
//        wac.setParentLoaderPriority(true);
//        server.setHandler(wac);
//        server.start();
        // and run, which keeps blocking until we terminate the JVM (or stop CamelContext)
        main.run();
    }

}
