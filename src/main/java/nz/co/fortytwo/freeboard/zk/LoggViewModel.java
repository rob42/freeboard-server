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

import nz.co.fortytwo.freeboard.server.util.Constants;
import nz.co.fortytwo.freeboard.server.util.Util;

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

public class LoggViewModel extends SelectorComposer<Window>{

	private static Logger logger = Logger.getLogger(LoggViewModel.class);

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@WireVariable
    private Session sess;

	@Wire ("#logg")
	Window logg;

	@Wire ("#logScale")
	Label logScale;

	@Wire ("#headWptSiz")
	Label headWptSiz;

	@Wire ("#logSiz")
	Label logSiz;

	@Wire ("#latLonSiz")
	Label latLonSiz;

	@Wire ("button#logShrink")
	Button logShrink;

	@Wire ("button#logGrow")
	Button logGrow;

	private double scale=0.7;

	public LoggViewModel() {
		super();
		if(logger.isDebugEnabled())logger.debug("Constructing..");

	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		if(logger.isDebugEnabled())logger.debug("Init..");
			if(Util.getConfig(null).containsKey(Constants.LOGG_X)){
				logg.setLeft(Util.getConfig(null).getProperty(Constants.LOGG_X));
				logg.setTop(Util.getConfig(null).getProperty(Constants.LOGG_Y));
				if(logger.isDebugEnabled())logger.debug("  logg location set to "+logg.getLeft()+", "+logg.getTop());
			}else{
				logg.setPosition("left,center");
				if(logger.isDebugEnabled())logger.debug("  logg location set to "+logg.getPosition());
			}
			if(Util.getConfig(null).containsKey(Constants.LOGG_SCALE)){
				scale = Double.valueOf(Util.getConfig(null).getProperty(Constants.LOGG_SCALE));
			} else {
				scale = 1.0;
			}
			logScale.setValue(String.valueOf(scale));
			//resize

	}

	@Listen("onMove =  #logg")
	public void onMoveWindow(Event event) {
		if(logger.isDebugEnabled())logger.debug(" move event = "+((Window)event.getTarget()).getLeft()+", "+((Window)event.getTarget()).getTop());
		    try {
		    	Util.getConfig(null).setProperty(Constants.LOGG_X, ((Window)event.getTarget()).getLeft());
		    	Util.getConfig(null).setProperty(Constants.LOGG_Y, ((Window)event.getTarget()).getTop());
				Util.saveConfig();
			} catch (FileNotFoundException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}

	}

	@Listen("onClick =  button#logShrink")
	public void logShrinkClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" shrink event = "+event);
		try {
			scale = Util.updateScale(Constants.LOGG_SCALE, 0.8, scale);
		} catch (Exception e) {
			logger.error(e);
		}
		logScale.setValue(String.valueOf(scale));
	}

	@Listen("onClick =   button#logGrow")
	public void logGrowClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" grow event = "+event);
		try{
			scale = Util.updateScale(Constants.LOGG_SCALE, 1.2, scale);
		} catch (Exception e) {
			logger.error(e);
		}
		logScale.setValue(String.valueOf(scale));
	}
}
