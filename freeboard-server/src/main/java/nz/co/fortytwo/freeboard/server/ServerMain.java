package nz.co.fortytwo.freeboard.server;

import java.io.File;
import java.io.FileReader;
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
	public static  final String MESHCMS_URL = "freeboard.meshcms.url";
	public static  final String MESHCMS_RESOURCE = "freeboard.meshcms.dir";
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
	
	private Properties config=new Properties();
	
	public ServerMain(String configDir) throws Exception {
		setDefaults(config);
		if(StringUtils.isNotBlank(configDir)){
			config.setProperty(CFG_DIR, configDir);
		}
		File cfg = new File(config.getProperty(CFG_DIR)+config.getProperty(CFG_FILE));
		
		if(cfg.exists()){
			config.load(new FileReader(cfg));
		}
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

		// serve meshcms
		WebAppContext wac = new WebAppContext();
		logger.info("  Meshcms resource:"+config.getProperty(MESHCMS_RESOURCE));
		wac.setWar(config.getProperty(MESHCMS_RESOURCE));
		wac.setDefaultsDescriptor(config.getProperty(MESHCMS_RESOURCE)+"WEB-INF/webdefault.xml");

		wac.setDescriptor(config.getProperty(MESHCMS_RESOURCE)+"WEB-INF/web.xml");
		logger.info("  Meshcms url:"+config.getProperty(MESHCMS_URL));
		wac.setContextPath(config.getProperty(MESHCMS_URL));
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
	}

	private void setDefaults(Properties config2) {
		//populate sensible defaults here
		config.setProperty(MESHCMS_URL,"/meshcms");
		config.setProperty(MESHCMS_RESOURCE,"meshcms/");
		config.setProperty(MAPCACHE_RESOURCE,"./mapcache");
		config.setProperty(MAPCACHE,"/mapcache");
		config.setProperty(HTTP_PORT,"8080");
		config.setProperty(WEBSOCKET_PORT,"9090");
		config.setProperty(SERIAL_PORT,"/dev/ttyUSB0");
		config.setProperty(CFG_DIR,"./conf/");
		config.setProperty(CFG_FILE,"freeboard.cfg");
		config.setProperty(DEMO,"false");
		config.setProperty(SERIAL_URL,"./src/test/resources/motu.log&scanStream=true&scanStreamDelay=500");

		
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
