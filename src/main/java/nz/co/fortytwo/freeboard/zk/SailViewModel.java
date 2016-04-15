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

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import nz.co.fortytwo.freeboard.server.util.Constants;
import nz.co.fortytwo.freeboard.server.util.Util;
import nz.co.fortytwo.freeboard.server.NMEAProcessor;

import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Window;
import org.zkoss.zul.Toolbarbutton;


public class SailViewModel extends SelectorComposer<Window> {

    private static Logger logger = Logger.getLogger(EngineViewModel.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Properties config;

    @WireVariable
    private Session sess;

    @Wire("#logg")
    private Panel logg;
    @Wire("#wind")
    private Panel wind;
    @Wire("#depth")
    private Panel depth;
    private double size = 400;

    @Wire("toolbarbutton#resetLog")
    private Toolbarbutton tbBtn;
	

    public SailViewModel() {
        super();
        logger.debug("Constructing..");
    }

    //@AfterCompose
    public void init() {
        logger.debug("Init..");
        //all hidden and in left corner
        if (sess.hasAttribute("size")) {
            size = (Double) sess.getAttribute("sess");
            logger.debug("Size recovered from sess.." + size);
        } else {
            sess.setAttribute("sess", size);
        }
        logger.debug("Size = " + size);
        //logg.setFloatable(true);
        //wind.setFloatable(true);
        //chartplotter.setFloatable(true);
    }


    @Listen("onClick= #resetLog")
    public void resetLog(MouseEvent event) {
        System.out.println("Got resetLog click");
        NMEAProcessor.resetTripLog = true;
    }

    public double getSize() {
        if (sess.hasAttribute("size")) {
            size = (Double) sess.getAttribute("sess");
            logger.debug("Size recovered from sess.." + size);
        }
        return size;
    }

    public void setSize(double size) {
        this.size = size;
        sess.setAttribute("sess", size);
    }

}
