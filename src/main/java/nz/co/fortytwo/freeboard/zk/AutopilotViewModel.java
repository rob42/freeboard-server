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

import nz.co.fortytwo.freeboard.server.CamelContextFactory;
import nz.co.fortytwo.freeboard.server.util.Constants;
import nz.co.fortytwo.freeboard.server.util.Util;

import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

public class AutopilotViewModel extends SelectorComposer<Window>{

	private static Logger logger = Logger.getLogger(AutopilotViewModel.class);
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@WireVariable
    private Session sess;
	
	@Wire ("#autopilotWindow")
	Window autopilotWindow;
	
	@Wire("button#apPort1")
	private Button apPort1; 
	
	@Wire("button#apStbd1")
	private Button apStbd1; 
	
	@Wire("button#apPort10")
	private Button apPort10; 
	
	@Wire("button#apStbd10")
	private Button apStbd10; 
	
	@Wire("toolbarbutton#apCompassOnOff")
	private Toolbarbutton apCompassOnOff; 
	
	@Wire("toolbarbutton#apWindOnOff")
	private Toolbarbutton apWindOnOff; 
	
	@Wire("toolbarbutton#apOnOff")
	private Toolbarbutton apOnOff; 
	
	private ProducerTemplate producer;
	//private ConsumerTemplate consumer;
	
	private boolean autopilotOn=false;
	private String APS = "C"; //C = compass, W = wind, compass by default
	
	public AutopilotViewModel() {
		super();
		logger.debug("Constructing..");

		producer = CamelContextFactory.getInstance().createProducerTemplate();
		producer.setDefaultEndpointUri("seda:input");
		
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		logger.debug("Init..");
			if(Util.getConfig(null).containsKey(Constants.AUTOPILOT_X)){
				autopilotWindow.setLeft(Util.getConfig(null).getProperty(Constants.AUTOPILOT_X));
				autopilotWindow.setTop(Util.getConfig(null).getProperty(Constants.AUTOPILOT_Y));
				logger.debug("  autopilot location set to "+autopilotWindow.getLeft()+", "+autopilotWindow.getTop());
			}else{
				autopilotWindow.setPosition("center,bottom");
				logger.debug("  autopilot location set to default "+autopilotWindow.getPosition());
			}
		apCompassOnOff.setChecked(APS.equals(Constants.AUTOPILOT_COMPASS)?true:false);
		apWindOnOff.setChecked(APS.equals(Constants.AUTOPILOT_COMPASS)?true:false);
		setAutopilotState();
	}
	
	@Listen("onClick = button#apPort1")
	public void apPort1Click(MouseEvent event) {
	    logger.debug(" apPort1 button event = "+event);
	    producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.AUTOPILOT_ADJUST+":-1,");
	}
	
	@Listen("onClick = button#apStbd1")
	public void apStbd1Click(MouseEvent event) {
	    logger.debug(" apStbd1 button event = "+event);
	    producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.AUTOPILOT_ADJUST+":1,");
	}
	
	@Listen("onClick = button#apPort10")
	public void apPort10Click(MouseEvent event) {
	    logger.debug(" apPort10 button event = "+event);
	    producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.AUTOPILOT_ADJUST+":-10,");
	}
	
	@Listen("onClick = button#apStbd10")
	public void apStbd10Click(MouseEvent event) {
	    logger.debug(" apStbd10 button event = "+event);
	    producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.AUTOPILOT_ADJUST+":10,");
	}
	
	@Listen("onCheck = toolbarbutton#apOnOff")
	public void apOnOffCheck(CheckEvent event) {
	    logger.debug(" apOnOff button event = "+event);
	    if(event.isChecked()){
	    	autopilotOn=true;
	    }else{
	    	autopilotOn=false;
	    }
	   setAutopilotState();
	}
	
	private void setAutopilotState() {
		 if(autopilotOn){
		    	apOnOff.setImage("./js/img/stop.png");
		    	apOnOff.setLabel("Stop");
		    	producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.AUTOPILOT_STATE+":1,");
		    }else{
		    	apOnOff.setImage("./js/img/tick.png");
		    	apOnOff.setLabel("Start");
		    	producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.AUTOPILOT_STATE+":0,");
		    }
	}

	@Listen("onCheck = toolbarbutton#apCompassOnOff")
	public void apCompassOnOffCheck(CheckEvent event) {
	    logger.debug(" apCompassOnOff button event = "+event);
	    toggleSource(event, true);
	}
	
	@Listen("onCheck = toolbarbutton#apWindOnOff")
	public void apWindOnOffCheck(CheckEvent event) {
	    logger.debug(" apWindOnOff button event = "+event);
	    toggleSource(event, false);
	}
	
	private void toggleSource(CheckEvent event, boolean compass) {
		//four possible states
		 if(compass && event.isChecked()){
			    APS=Constants.AUTOPILOT_COMPASS;
			    //compass checked so wind off
			    if(apWindOnOff.isChecked())apWindOnOff.setChecked(false);
		 }
		 if(compass && !event.isChecked()){
			    APS=Constants.AUTOPILOT_WIND;
			  //compass not checked so wind on
			    if(!apWindOnOff.isChecked())apWindOnOff.setChecked(true);
		 }
		 if(!compass && event.isChecked()){
			    APS=Constants.AUTOPILOT_WIND;
			  //wind checked so compass off
			    if(apCompassOnOff.isChecked())apCompassOnOff.setChecked(false);
		 }
		 if(!compass && !event.isChecked()){
			    APS=Constants.AUTOPILOT_COMPASS;
			  //wind not checked so compass on
			    if(!apCompassOnOff.isChecked())apCompassOnOff.setChecked(true);
		 }
		 //now propagate command
		 producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.AUTOPILOT_SOURCE+":"+APS+",");
		 
	}

	@Listen("onMove = #autopilotWindow")
	public void onMoveWindow(Event event) {
		    logger.debug(" move event = "+((Window)event.getTarget()).getLeft()+", "+((Window)event.getTarget()).getTop());
		    try {
		    	Util.getConfig(null).setProperty(Constants.AUTOPILOT_X, ((Window)event.getTarget()).getLeft());
		    	Util.getConfig(null).setProperty(Constants.AUTOPILOT_Y, ((Window)event.getTarget()).getTop());
				Util.saveConfig();
			} catch (Exception e) {
				logger.error(e);
			} 
	    
	}

}
