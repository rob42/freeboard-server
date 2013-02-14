/*
 * Copyright 2012,2013 Robert Huitema robert@42.co.nz
 * 
 * This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
 * 
 * FreeBoard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FreeBoard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with FreeBoard. If not, see <http://www.gnu.org/licenses/>.
 */
package nz.co.fortytwo.freeboard.zk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import nz.co.fortytwo.freeboard.server.CamelContextFactory;
import nz.co.fortytwo.freeboard.server.util.Constants;
import nz.co.fortytwo.freeboard.server.util.Util;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Waypoint;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class WaypointViewModel extends SelectorComposer<Window> {

	private static Logger logger = Logger.getLogger(WaypointViewModel.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@WireVariable
	private Session sess;

	@Wire("#wptWindow")
	private Window wptWindow;

	@Wire("button#gotoWpt")
	private Button gotoWpt;
	@Wire("button#gotoCancel")
	private Button gotoCancel;
	@Wire("button#ok")
	private Button ok;
	@Wire("button#cancel")
	private Button cancel;
	@Wire("button#delete")
	private Button delete;

	@Wire("#curLat")
	private Label curLat;
	@Wire("#curLon")
	private Label curLon;

	@Wire("#latBox")
	private Decimalbox latBox;
	@Wire("#lonBox")
	private Decimalbox lonBox;
	@Wire("#nameBox")
	private Textbox nameBox;
	@Wire("#descBox")
	private Textbox descBox;

	@Wire("#editMark")
	private Label editMark;

	private File gpxFile;

	private ProducerTemplate producer;
	private String gotoLat = null;
	private String gotoLon = null;
	private String fromLat = null;
	private String fromLon = null;

	public WaypointViewModel() {
		super();
		try {
			String gpxDirStr = Util.getConfig(null).getProperty(Constants.WAYPOINTS_RESOURCE);
			File gpxDir = new File(gpxDirStr);
			FileUtils.forceMkdir(gpxDir);
			gpxFile = new File(gpxDir, Util.getConfig(null).getProperty(Constants.WAYPOINT_CURRENT));
			producer = CamelContextFactory.getInstance().createProducerTemplate();
			producer.setDefaultEndpointUri("seda://input?multipleConsumers=true");
			gotoLat = Util.getConfig(null).getProperty(Constants.GOTO_LAT);
			gotoLon = Util.getConfig(null).getProperty(Constants.GOTO_LON);
			fromLat = Util.getConfig(null).getProperty(Constants.FROM_LAT);
			fromLon = Util.getConfig(null).getProperty(Constants.FROM_LON);
			logger.debug("Constructing..");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		logger.debug("Init..");
		if (gotoLat != null && gotoLon != null && fromLat != null && fromLon != null) {
			Clients.evalJavaScript("setGotoWpt(" + gotoLat + "," + gotoLon + "," + fromLat + "," + fromLon + ");");
		}

	}

	@Listen("onClick = button#ok")
	public void wptSaveClick(MouseEvent event) {
		logger.debug(" ok button event = " + event);
		// save it
		try {
			if (StringUtils.isBlank(nameBox.getValue())) {
				Messagebox.show("A unique name is required");
				return;
			}
			GPX gpx = null;
			if (gpxFile.exists()) {
				gpx = new GPXParser().parseGPX(gpxFile);
			} else {
				gpx = new GPX();
			}

			// names must be unique if we are not editing
			Waypoint wp = gpx.getWaypointByName(nameBox.getName());
			if (wp != null) {
				int reply = Messagebox.show("A waypoint by this name already exists. Overwrite?", "Question", Messagebox.OK | Messagebox.CANCEL,
						Messagebox.QUESTION);
				if (reply == Messagebox.CANCEL)
					return;
			}
			wp = gpx.getWaypointByLocation(latBox.getValue().doubleValue(), lonBox.getValue().doubleValue());
			if (wp != null) {
				int reply = Messagebox.show("A waypoint within 30 meters already exists. Overwrite?", "Question", Messagebox.OK | Messagebox.CANCEL,
						Messagebox.QUESTION);
				if (reply == Messagebox.CANCEL)
					return;
			} else {
				// now add the new waypoint
				wp = new Waypoint();
			}
			wp.setElevation(0.0);
			wp.setTime(new Date());
			wp.setLatitude(latBox.getValue().doubleValue());
			wp.setLongitude(lonBox.getValue().doubleValue());
			wp.setName(nameBox.getText());
			wp.setDescription(StringUtils.isNotBlank(descBox.getText())?descBox.getText():"?");
			// clear the boxes
			nameBox.setText("");
			descBox.setText("");
			gpx.getWaypoints().add(wp);
			// now resave
			new GPXParser().writeGPX(gpx, gpxFile);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		wptWindow.setVisible(false);
		Clients.evalJavaScript("refreshWaypoints()");
	}

	@Listen("onClick = button#gotoWpt")
	public void wptGotoWptClick(MouseEvent event) {
		logger.debug(" gotoWpt button event = " + event);
		wptWindow.setVisible(false);
		fromLat = curLat.getValue();
		fromLon = curLon.getValue();
		gotoLat = latBox.getValue().toPlainString();
		gotoLon = lonBox.getValue().toPlainString();
		nameBox.setText("");
		descBox.setText("");
		try {
			Util.getConfig(null).setProperty(Constants.GOTO_LAT, gotoLat);
			Util.getConfig(null).setProperty(Constants.GOTO_LON, gotoLon);
			Util.getConfig(null).setProperty(Constants.FROM_LAT, fromLat);
			Util.getConfig(null).setProperty(Constants.FROM_LON, fromLon);
			Util.saveConfig();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		// producer.sendBody(Constants.WPG+":"+latBox.getValue().doubleValue()+",");
		Clients.evalJavaScript("setGotoWpt(" + gotoLat + "," + gotoLon + "," + fromLat + "," + fromLon + ");");
	}

	@Listen("onClick = button#gotoCancel")
	public void wptGotoCancelClick(MouseEvent event) {
		logger.debug(" gotoCancel button event = " + event);
		wptWindow.setVisible(false);
		fromLat = null;
		fromLon = null;
		gotoLat = null;
		gotoLon = null;
		try {
			Util.getConfig(null).remove(Constants.GOTO_LAT);
			Util.getConfig(null).remove(Constants.GOTO_LON);
			Util.getConfig(null).remove(Constants.FROM_LAT);
			Util.getConfig(null).remove(Constants.FROM_LON);
			Util.saveConfig();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		Clients.evalJavaScript("setGotoWpt(null,null,null,null);");
	}

	//
	@Listen("onClick = button#cancel")
	public void wptCancelClick(MouseEvent event) {
		logger.debug(" cancel button event = " + event);
		wptWindow.setVisible(false);
	}

	@Listen("onClick = button#delete")
	public void wptDeleteClick(MouseEvent event) {
		logger.debug(" delete button event = " + event);
		try {
			GPX gpx = new GPXParser().parseGPX(gpxFile);
			deleteWaypoint(gpx, nameBox.getName());

			new GPXParser().writeGPX(gpx, gpxFile);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		wptWindow.setVisible(false);
		Clients.evalJavaScript("refreshWaypoints()");
	}

	private void deleteWaypoint(GPX gpx, String name) {
		try {
			Waypoint wptDelete = gpx.getWaypointByName(name);
			if (wptDelete != null) {
				gpx.getWaypoints().remove(wptDelete);
				new GPXParser().writeGPX(gpx, gpxFile);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Listen("onWaypoint = #wptWindow")
	public void onWaypoint(Event event) {
		try {
			Object[] latlon = (Object[]) event.getData();
			if (latlon.length == 5) {
				// its an edit find by name
				String name = String.valueOf(latlon[4]);
				GPX gpx = new GPXParser().parseGPX(gpxFile);
				Waypoint wp = gpx.getWaypointByName(name);
				if (wp != null) {
					nameBox.setValue(name);
					descBox.setValue(wp.getDescription());
					latBox.setValue(BigDecimal.valueOf(wp.getLatitude()));
					lonBox.setValue(BigDecimal.valueOf(wp.getLongitude()));
					editMark.setValue("true");
				}
				// TODO: what if its not found?
			} else {
				editMark.setValue("false");
				// new waypoint
				BigDecimal latitude = BigDecimal.valueOf((Double) latlon[0]);
				latBox.setValue(latitude);
				BigDecimal longitude = BigDecimal.valueOf((Double) latlon[1]);
				lonBox.setValue(longitude);
				logger.debug("Waypoint Lat:" + latitude + ", Lon:" + longitude);
			}
			curLat.setValue(String.valueOf(latlon[2]));
			curLon.setValue(String.valueOf(latlon[3]));
			wptWindow.setVisible(true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
