package nz.co.fortytwo.freeboard.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.apache.camel.main.Main;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;

public class ServerMain {

	public static  final String DEMO = "freeboard.demo";
	public static  final String FREEBOARD_URL = "freeboard.web.url";
	public static  final String FREEBOARD_RESOURCE = "freeboard.web.dir";
	public static  final String MAPCACHE_RESOURCE = "freeboard.mapcache.dir";
	public static  final String MAPCACHE = "freeboard.mapcache.url";
	public static  final String HTTP_PORT = "freeboard.http.port";
	public static  final String WEBSOCKET_PORT = "freeboard.websocket.port";
	public static  final String SERIAL_PORT = "freeboard.serial.port";
	public static  final String CFG_DIR = "freeboard.cfg.dir";
	public static  final String CFG_FILE = "freeboard.cfg.file";
	public static final String SERIAL_URL = "freeboard.serial.demo.file";
	
	private static Server server;

	private static Logger logger = Logger.getLogger(ServerMain.class);
	
	private Properties config=null;
	
	public ServerMain(String configDir) throws Exception {
		
		config=getConfig(configDir);
		
		logger.info("Freeboard starting....");

		// create a new Camel Main so we can easily start Camel
		Main main = new Main();

		// enable hangup support which mean we detect when the JVM terminates, and stop Camel graceful
		main.enableHangupSupport();

		NavDataWebSocketRoute route = new NavDataWebSocketRoute(config);
		
		// web socket on port 9090
		logger.info("  Websocket port:"+config.getProperty(WEBSOCKET_PORT));
		route.setPort(Integer.valueOf(config.getProperty(WEBSOCKET_PORT)));
		
		logger.info("  Serial url:"+config.getProperty(SERIAL_URL));
		route.setSerialUrl(config.getProperty(SERIAL_URL));
		
		logger.info("  Serial port:"+config.getProperty(SERIAL_PORT));
		route.setSerialPort(config.getProperty(SERIAL_PORT));
		
		// add our routes to Camel
		main.addRouteBuilder(route);

		Connector connector = new SelectChannelConnector();
		logger.info("  Webserver http port:"+config.getProperty(HTTP_PORT));
		connector.setPort(Integer.valueOf(config.getProperty(HTTP_PORT)));
		
		server = new Server();
		server.addConnector(connector);

		// serve mapcache
		ServletContextHandler mapContext = new ServletContextHandler();
		logger.info("  Mapcache url:"+config.getProperty(MAPCACHE));
		mapContext.setContextPath(config.getProperty(MAPCACHE));
		logger.info("  Mapcache resource:"+config.getProperty(MAPCACHE_RESOURCE));
		mapContext.setResourceBase(config.getProperty(MAPCACHE_RESOURCE));
		mapContext.addServlet(DefaultServlet.class, "/*");

		HandlerList handlers = new HandlerList();
		handlers.addHandler(mapContext);

		// serve freeboard
		WebAppContext wac = new WebAppContext();
		logger.info("  Freeboard resource:"+config.getProperty(FREEBOARD_RESOURCE));
		wac.setWar(config.getProperty(FREEBOARD_RESOURCE));
		wac.setDefaultsDescriptor(config.getProperty(FREEBOARD_RESOURCE)+"WEB-INF/webdefault.xml");
		
		wac.setDescriptor(config.getProperty(FREEBOARD_RESOURCE)+"WEB-INF/web.xml");
		logger.info("  Freeboard url:"+config.getProperty(FREEBOARD_URL));
		wac.setContextPath(config.getProperty(FREEBOARD_URL));
		wac.setServer(server);
		wac.setParentLoaderPriority(true);

		handlers.addHandler(wac);
		
		server.setHandler(handlers);
		server.start();
		
		// and run, which keeps blocking until we terminate the JVM (or stop CamelContext)
		main.run();
		
		//so now shutdown serial reader and server
		
		route.stopSerial();
		server.stop();
		System.exit(0);
	}

	public static Properties getConfig(String dir) throws FileNotFoundException, IOException{
		Properties props = new Properties();
		setDefaults(props);
		if(StringUtils.isNotBlank(dir)){
			props.setProperty(CFG_DIR, dir);
		}
		File cfg = new File(props.getProperty(CFG_DIR)+props.getProperty(CFG_FILE));
		
		if(cfg.exists()){
			props.load(new FileReader(cfg));
		}
		return props;
	}
	private static void setDefaults(Properties props) {
		//populate sensible defaults here
		props.setProperty(FREEBOARD_URL,"/freeboard");
		props.setProperty(FREEBOARD_RESOURCE,"freeboard/");
		props.setProperty(MAPCACHE_RESOURCE,"./mapcache");
		props.setProperty(MAPCACHE,"/mapcache");
		props.setProperty(HTTP_PORT,"8080");
		props.setProperty(WEBSOCKET_PORT,"9090");
		props.setProperty(SERIAL_PORT,"/dev/ttyUSB0");
		props.setProperty(CFG_DIR,"./conf/");
		props.setProperty(CFG_FILE,"freeboard.cfg");
		props.setProperty(DEMO,"false");
		props.setProperty(SERIAL_URL,"./src/test/resources/motu.log&scanStream=true&scanStreamDelay=500");
	}

	public static void main(String[] args) throws Exception {
		//we look for and use a freeboard.cfg in the launch/cfg dir and use that to override defaults
		//the only arg is conf dir
		String conf = null;
		if(args!=null && args.length>0 && StringUtils.isNotBlank(args[0])){
			conf=args[0];
			if(!conf.endsWith("/")){
				conf=conf+"/";
			}
		}
		new ServerMain(conf);
		
	}

}
