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
package nz.co.fortytwo.freeboard.zk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import nz.co.fortytwo.freeboard.server.CamelContextFactory;
import nz.co.fortytwo.freeboard.server.util.Constants;
import nz.co.fortytwo.freeboard.server.util.Util;

import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

public class WindViewModel extends SelectorComposer<Window>{

	private static Logger logger = Logger.getLogger(WindViewModel.class);
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Dial master size
	 */
	private double size = 400;
	
	@WireVariable
    private Session sess;
	
	@Wire ("#wind")
	Window wind;
	
	
	@Wire ("#windScale")
	Label windScale;

	@Wire ("button#windShrink")
	Button windShrink;
	
	@Wire ("button#windGrow")
	Button windGrow;

	private double scale=0.5;

	private ProducerTemplate producer;
	
	public WindViewModel() {
		super();
		logger.debug("Constructing..");
		producer = CamelContextFactory.getInstance().createProducerTemplate();
		producer.setDefaultEndpointUri("seda:input");
		
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		logger.debug("Init..");
			if(Util.getConfig(null).containsKey(Constants.WIND_X)){
				wind.setLeft(Util.getConfig(null).getProperty(Constants.WIND_X));
				wind.setTop(Util.getConfig(null).getProperty(Constants.WIND_Y));
				logger.debug("  wind location set to "+wind.getLeft()+", "+wind.getTop());
			}else{
				//top="10px" left="50px"
				wind.setTop("10px");
				wind.setLeft("50px");
				logger.debug("  wind location set to "+wind.getPosition());
			}
			if(Util.getConfig(null).containsKey(Constants.WIND_SCALE)){
				scale = Double.valueOf(Util.getConfig(null).getProperty(Constants.WIND_SCALE));
			}
			windScale.setValue(String.valueOf(scale));
			//adjust wind zero point here
			producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.WIND_ZERO_ADJUST+":"+Util.getConfig(null).getProperty(Constants.WIND_ZERO_OFFSET)+",");
	}
	
	@Listen("onMove = #wind")
	public void onMoveWindow(Event event) {
		    logger.debug(" move event = "+((Window)event.getTarget()).getLeft()+", "+((Window)event.getTarget()).getTop());
		    try {
		    	Util.getConfig(null).setProperty(Constants.WIND_X, ((Window)event.getTarget()).getLeft());
		    	Util.getConfig(null).setProperty(Constants.WIND_Y, ((Window)event.getTarget()).getTop());
				Util.saveConfig();
			} catch (FileNotFoundException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}
	    
	}

	@Listen("onClick = button#windShrink")
	public void windShrinkClick(MouseEvent event) {
		logger.debug(" shrink event = "+event);
		try {
			scale = Util.updateScale(Constants.WIND_SCALE, 0.8, scale);
		} catch (Exception e) {
			logger.error(e);
		} 
		windScale.setValue(String.valueOf(scale));
	}

	@Listen("onClick = button#windGrow")
	public void windGrowClick(MouseEvent event) {
		logger.debug(" grow event = "+event);
		try{
			scale = Util.updateScale(Constants.WIND_SCALE, 1.2, scale);
		} catch (Exception e) {
			logger.error(e);
		} 
		windScale.setValue(String.valueOf(scale));
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	

	

}
