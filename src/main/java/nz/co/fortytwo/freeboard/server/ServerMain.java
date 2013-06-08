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

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContext;

import nz.co.fortytwo.freeboard.server.util.Constants;
import nz.co.fortytwo.freeboard.server.util.Util;

import org.apache.camel.main.Main;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

public class ServerMain {

	private static Server server;

	private static Logger logger = Logger.getLogger(ServerMain.class);
	
	private Properties config=null;
	
	
		
	
	
	public ServerMain(String configDir) throws Exception {
		
		config=Util.getConfig(configDir);
		//make sure we have all the correct dirs and files now
		ensureInstall();
		
		logger.info("Freeboard starting....");

		//do we have a USB drive connected?
		logger.info("USB drive "+Util.getUSBFile());
		
		// create a new Camel Main so we can easily start Camel
		Main main = new Main();

		// enable hangup support which mean we detect when the JVM terminates, and stop Camel graceful
		main.enableHangupSupport();

		NavDataWebSocketRoute route = new NavDataWebSocketRoute(config);
		//must do this early!
		CamelContextFactory.setContext(route);
		// web socket on port 9090
		logger.info("  Websocket port:"+config.getProperty(Constants.WEBSOCKET_PORT));
		route.setPort(Integer.valueOf(config.getProperty(Constants.WEBSOCKET_PORT)));
		
		//are we running demo?
		logger.info("  Serial url:"+config.getProperty(Constants.SERIAL_URL));
		route.setSerialUrl(config.getProperty(Constants.SERIAL_URL));
		
		// add our routes to Camel
		main.addRouteBuilder(route);

		Connector connector = new SelectChannelConnector();
		logger.info("  Webserver http port:"+config.getProperty(Constants.HTTP_PORT));
		connector.setPort(Integer.valueOf(config.getProperty(Constants.HTTP_PORT)));

		//virtual hosts
		String virtualHosts = config.getProperty(Constants.VIRTUAL_URL);
		server = new Server();
		server.addConnector(connector);

		// serve mapcache
		ServletContextHandler mapContext = new ServletContextHandler();
		logger.info("  Mapcache url:"+config.getProperty(Constants.MAPCACHE));
		mapContext.setContextPath(config.getProperty(Constants.MAPCACHE));
		logger.info("  Mapcache resource:"+config.getProperty(Constants.MAPCACHE_RESOURCE));
		mapContext.setResourceBase(config.getProperty(Constants.MAPCACHE_RESOURCE));
		ServletHolder mapHolder= mapContext.addServlet(DefaultServlet.class, "/*");
		mapHolder.setInitParameter("cacheControl","max-age=3600,public" );
		
		//serve tracks
		ServletContextHandler trackContext = new ServletContextHandler();
		logger.info("  Tracks url:"+config.getProperty(Constants.TRACKS));
		trackContext.setContextPath(config.getProperty(Constants.TRACKS));
		logger.info("  Tracks resource:"+config.getProperty(Constants.TRACKS_RESOURCE));
		trackContext.setResourceBase(config.getProperty(Constants.TRACKS_RESOURCE));
		trackContext.addServlet(DefaultServlet.class, "/*");
		
		if(StringUtils.isNotBlank(virtualHosts)){
			mapContext.setVirtualHosts(virtualHosts.split(","));
			trackContext.setVirtualHosts(virtualHosts.split(","));
		}
		HandlerList handlers = new HandlerList();
		handlers.addHandler(mapContext);
		handlers.addHandler(trackContext);

		// serve freeboard
		WebAppContext wac = new WebAppContext();
		logger.info("  Freeboard resource:"+config.getProperty(Constants.FREEBOARD_RESOURCE));
		wac.setWar(config.getProperty(Constants.FREEBOARD_RESOURCE));
		wac.setDefaultsDescriptor(config.getProperty(Constants.FREEBOARD_RESOURCE)+"WEB-INF/webdefault.xml");
		
		wac.setDescriptor(config.getProperty(Constants.FREEBOARD_RESOURCE)+"WEB-INF/web.xml");
		logger.info("  Freeboard url:"+config.getProperty(Constants.FREEBOARD_URL));
		wac.setContextPath(config.getProperty(Constants.FREEBOARD_URL));
		wac.setServer(server);
		wac.setParentLoaderPriority(true);
		wac.setVirtualHosts(null);
		if(StringUtils.isNotBlank(virtualHosts)){
			wac.setVirtualHosts(virtualHosts.split(","));
		}
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

	private void ensureInstall() {

		File rootDir = new File(".");
		if(Util.cfg!=null){
			rootDir = Util.cfg.getParentFile();
		}
		//do we have a log dir?
		File logDir = new File(rootDir,"logs");
		if(!logDir.exists()){
			logDir.mkdirs();
		}
		//do we have a mapcache
		File mapDir = new File(rootDir,config.getProperty(Constants.MAPCACHE_RESOURCE));
		if(!mapDir.exists()){
			mapDir.mkdirs();
		}
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
