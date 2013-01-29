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

import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
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
	@Wire("button#apOnOff")
	private Button apOnOff; 
	
	private ProducerTemplate producer;
	//private ConsumerTemplate consumer;
	
	private boolean autopilotOn=false;
	private String APS = "C"; //C = compass, W = wind, compass by default
	
	public AutopilotViewModel() {
		super();
		logger.debug("Constructing..");

		producer = CamelContextFactory.getInstance().createProducerTemplate();
		producer.setDefaultEndpointUri("seda://input?multipleConsumers=true");
		
	}

	@AfterCompose
	public void init() {
		logger.debug("Init..");
		apCompassOnOff.setChecked(APS.equals(Constants.AUTOPILOT_COMPASS)?true:false);
		apWindOnOff.setChecked(APS.equals(Constants.AUTOPILOT_COMPASS)?true:false);
		setAutopilotState(autopilotOn);
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
	@Listen("onClick = button#apOnOff")
	public void apOnOffClick(MouseEvent event) {
	    logger.debug(" apOnOff button event = "+event);
	    autopilotOn=!autopilotOn;
	   setAutopilotState(autopilotOn);
	}
	private void setAutopilotState(boolean autopilotOn2) {
		 if(autopilotOn){
		    	apOnOff.setImage("./js/img/stop.png");
		    	producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.AUTOPILOT_STATE+":1,");
		    }else{
		    	apOnOff.setImage("./js/img/tick.png");
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

		

}
