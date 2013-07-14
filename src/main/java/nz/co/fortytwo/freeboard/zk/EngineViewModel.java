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

import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Window;

public class EngineViewModel extends SelectorComposer<Window>{

	private static Logger logger = Logger.getLogger(EngineViewModel.class);
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@WireVariable
    private Session sess;
	
	@Wire ("#logg")
	private Panel logg;
	@Wire("#wind")
	private Panel wind;
	private double size = 400;
	
	
	public EngineViewModel() {
		super();
		logger.debug("Constructing..");
	}

	//@AfterCompose
	public void init() {
		logger.debug("Init..");
		//all hidden and in left corner
		if(sess.hasAttribute("size")){
			size=(Double) sess.getAttribute("sess");
			logger.debug("Size recovered from sess.."+size);
		}else{
			sess.setAttribute("sess", size);
		}
		logger.debug("Size = "+size);
		//logg.setFloatable(true);
		//wind.setFloatable(true);
		//chartplotter.setFloatable(true);
	}
	
	@Listen("onClick = button#apPort")
	public void apPort(MouseEvent event) {
	    logger.debug(" event = "+event);
	    
	}
	
	
	
	public double getSize() {
		if(sess.hasAttribute("size")){
			size=(Double) sess.getAttribute("sess");
			logger.debug("Size recovered from sess.."+size);
		}
		return size;
	}
	
	
	public void setSize(double size) {
		this.size = size;
		sess.setAttribute("sess", size);
	}

}
