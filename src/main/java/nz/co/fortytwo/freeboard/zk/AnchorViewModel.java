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

public class AnchorViewModel extends SelectorComposer<Window>{

	private static Logger logger = Logger.getLogger(AnchorViewModel.class);
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@WireVariable
    private Session sess;
	
	@Wire("button#anchorRadiusUp1")
	private Button anchorRadiusUp1; 
	@Wire("button#anchorRadiusDown1")
	private Button anchorRadiusDown1; 
	@Wire("button#anchorRadiusUp10")
	private Button anchorRadiusUp10; 
	@Wire("button#anchorRadiusDown10")
	private Button anchorRadiusDown10; 
	
	@Wire("button#anchorAlarmOnOff")
	private Button anchorAlarmOnOff; 
	
	private ProducerTemplate producer;
	
	private boolean anchorAlarmOn=false;
	
	public AnchorViewModel() {
		super();
		logger.debug("Constructing..");

		producer = CamelContextFactory.getInstance().createProducerTemplate();
		producer.setDefaultEndpointUri("seda://input?multipleConsumers=true");
		
	}

	@AfterCompose
	public void init() {
		logger.debug("Init..");
		
		setAnchorAlarmState();
	}
	
	@Listen("onClick = button#anchorRadiusUp1")
	public void anchorRadiusUp1Click(MouseEvent event) {
	    logger.debug(" anchorRadiusUp1 button event = "+event);
	    producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.ANCHOR_ALARM_ADJUST+":1,");
	}
	@Listen("onClick = button#anchorRadiusDown1")
	public void anchorRadiusDown1Click(MouseEvent event) {
	    logger.debug(" anchorRadiusDown1 button event = "+event);
	    producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.ANCHOR_ALARM_ADJUST+":-1,");
	}
	@Listen("onClick = button#anchorRadiusUp10")
	public void anchorRadiusUp10Click(MouseEvent event) {
	    logger.debug(" anchorRadiusUp10 button event = "+event);
	    producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.ANCHOR_ALARM_ADJUST+":10,");
	}
	@Listen("onClick = button#anchorRadiusDown10")
	public void anchorRadiusDown10Click(MouseEvent event) {
	    logger.debug(" anchorRadiusDown10 button event = "+event);
	    producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.ANCHOR_ALARM_ADJUST+":-10,");
	}
	@Listen("onClick = button#anchorAlarmOnOff")
	public void anchorAlarmOnOffClick(MouseEvent event) {
	    logger.debug(" anchorAlarmOnOff button event = "+event);
	    anchorAlarmOn=!anchorAlarmOn;
	   setAnchorAlarmState();
	}
	
	private void setAnchorAlarmState() {
		 if(anchorAlarmOn){
		    	anchorAlarmOnOff.setImage("./js/img/stop.png");
		    	producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.ANCHOR_ALARM_STATE+":1,");
		    }else{
		    	anchorAlarmOnOff.setImage("./js/img/tick.png");
		    	producer.sendBody(Constants.UID+":"+Constants.MEGA+","+Constants.ANCHOR_ALARM_STATE+":0,");
		    }
	}

	

		

}
