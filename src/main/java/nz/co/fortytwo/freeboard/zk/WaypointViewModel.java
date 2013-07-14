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

	public WaypointViewModel() {
		super();
		try {
			String gpxDirStr = Util.getConfig(null).getProperty(Constants.WAYPOINTS_RESOURCE);
			File gpxDir = new File(gpxDirStr);
			FileUtils.forceMkdir(gpxDir);
			gpxFile = new File(gpxDir, Util.getConfig(null).getProperty(Constants.WAYPOINT_CURRENT));
			producer = CamelContextFactory.getInstance().createProducerTemplate();
			producer.setDefaultEndpointUri("seda:input");

			logger.debug("Constructing..");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		logger.debug("Init..");
		String gotoLat = Util.getConfig(null).getProperty(Constants.GOTO_LAT);
		String gotoLon = Util.getConfig(null).getProperty(Constants.GOTO_LON);
		String fromLat = Util.getConfig(null).getProperty(Constants.FROM_LAT);
		String fromLon = Util.getConfig(null).getProperty(Constants.FROM_LON);
		if (gotoLat != null && gotoLon != null && fromLat != null && fromLon != null) {
			// need to do this directly, sending a message can get lost as the client may not yet be attached to websockets
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
			if (wp != null && wp.getLatitude()!=latBox.getValue().doubleValue() && wp.getLongitude()!=lonBox.getValue().doubleValue()) {
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
			wp.setDescription(StringUtils.isNotBlank(descBox.getText()) ? descBox.getText() : "?");
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
		producer.sendBody(Constants.WAYPOINT_CHANGE_EVENT + ":0,");

	}

	@Listen("onClick = button#gotoWpt")
	public void wptGotoWptClick(MouseEvent event) {
		logger.debug(" gotoWpt button event = " + event);
		wptWindow.setVisible(false);
		String fromLat = curLat.getValue();
		String fromLon = curLon.getValue();
		String gotoLat = latBox.getValue().toPlainString();
		String gotoLon = lonBox.getValue().toPlainString();
		nameBox.setText("");
		descBox.setText("");
		gotoDestination(gotoLat, gotoLon, fromLat, fromLon);

	}

	/**
	 * Sets/deletes a goto and informs all clients
	 * 
	 * @param gotoLat
	 * @param gotoLon
	 * @param fromLat
	 * @param fromLon
	 */
	private void gotoDestination(String gotoLat, String gotoLon, String fromLat, String fromLon) {
		try {
			Util.getConfig(null).setProperty(Constants.GOTO_LAT, gotoLat);
			Util.getConfig(null).setProperty(Constants.GOTO_LON, gotoLon);
			Util.getConfig(null).setProperty(Constants.FROM_LAT, fromLat);
			Util.getConfig(null).setProperty(Constants.FROM_LON, fromLon);
			Util.saveConfig();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if (gotoLat == null && gotoLon == null) {
			producer.sendBody(Constants.WAYPOINT_GOTO_EVENT + ":0,");
		} else {
			producer.sendBody(Constants.WAYPOINT_GOTO_EVENT + ":" + gotoLat + "|" + gotoLon + "|" + fromLat + "|" + fromLon + ",");
		}

	}

	@Listen("onClick = button#gotoCancel")
	public void wptGotoCancelClick(MouseEvent event) {
		logger.debug(" gotoCancel button event = " + event);
		wptWindow.setVisible(false);
		gotoDestination(null, null, null, null);

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
		producer.sendBody(Constants.WAYPOINT_CHANGE_EVENT + ":0,");
	}

	private void deleteWaypoint(GPX gpx, String name) {
		Waypoint wptDelete = gpx.getWaypointByName(name);
		deleteWaypoint(gpx, wptDelete);
	}
	private void deleteWaypoint(GPX gpx, Waypoint wptDelete) {
		try {
			
			if (wptDelete != null) {
				gpx.getWaypoints().remove(wptDelete);
				new GPXParser().writeGPX(gpx, gpxFile);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Triggered when waypoints are moved in a client
	 * 
	 * @param event
	 */
	@Listen("onWaypointMove = #wptWindow")
	public void onWaypointMove(Event event) {
		try {
			Object[] latlon = (Object[]) event.getData();
			logger.debug("Moving waypoints:" + (latlon.length / 4));
			GPX gpx = new GPXParser().parseGPX(gpxFile);
			// we have data in multiples of four
			for (int x = 0; x < latlon.length; x = x + 4) {

				// its a move, find by lat/lon
				double oldLat = (Double) latlon[x + 2];
				double oldLon = (Double) latlon[x + 3];
				logger.debug("Moving " + latlon[x + 2] + "," + latlon[x + 3] + " to " + latlon[x + 0] + "," + latlon[x + 1]);
				Waypoint wp = gpx.getWaypointByLocation(oldLat, oldLon);
				if (wp != null) {
					// update
					gpx.getWaypoints().remove(wp);
					wp.setLatitude((Double) latlon[x]);
					wp.setLongitude((Double) latlon[x + 1]);
					gpx.addWaypoint(wp);
					logger.debug("Moved " + wp.getName());
				}

			}
			new GPXParser().writeGPX(gpx, gpxFile);
			// refresh waypoints
			producer.sendBody(Constants.WAYPOINT_CHANGE_EVENT + ":0,");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Listen("onRequestGoto = #wptWindow")
	public void onRequestGoto(Event event) {
		try {
			Object[] latlon = (Object[]) event.getData();

			// request goto waypoints
			gotoDestination(String.valueOf(latlon[0]), String.valueOf(latlon[1]), String.valueOf(latlon[2]), String.valueOf(latlon[3]));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Listen("onWaypointEdit = #wptWindow")
	public void onWaypointEdit(Event event) {
		try {
			Object[] latlon = (Object[]) event.getData();
			GPX gpx = new GPXParser().parseGPX(gpxFile);
			Waypoint wp = gpx.getWaypointByLocation((Double) latlon[0], (Double) latlon[1]);
			if (wp != null) {
				nameBox.setValue(wp.getName());
				descBox.setValue(wp.getDescription());
				latBox.setValue(BigDecimal.valueOf(wp.getLatitude()));
				lonBox.setValue(BigDecimal.valueOf(wp.getLongitude()));
				editMark.setValue("true");
			}
			curLat.setValue(String.valueOf(latlon[2]));
			curLon.setValue(String.valueOf(latlon[3]));
			wptWindow.setVisible(true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Listen("onWaypointDelete = #wptWindow")
	public void onWaypointDelete(Event event) {
		try {
			Object[] latlon = (Object[]) event.getData();
			GPX gpx = new GPXParser().parseGPX(gpxFile);
			Waypoint wp = gpx.getWaypointByLocation((Double) latlon[0], (Double) latlon[1]);
			deleteWaypoint(gpx, wp);
			producer.sendBody(Constants.WAYPOINT_CHANGE_EVENT + ":0,");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Triggered when a waypoint is created or its data edited
	 * 
	 * @param event
	 */
	@Listen("onWaypointCreate = #wptWindow")
	public void onWaypointCreate(Event event) {
		try {
			Object[] latlon = (Object[]) event.getData();
			editMark.setValue("false");
			// new waypoint
			BigDecimal latitude = BigDecimal.valueOf((Double) latlon[0]);
			latBox.setValue(latitude);
			BigDecimal longitude = BigDecimal.valueOf((Double) latlon[1]);
			lonBox.setValue(longitude);
			logger.debug("Waypoint Lat:" + latitude + ", Lon:" + longitude);

			curLat.setValue(String.valueOf(latlon[2]));
			curLon.setValue(String.valueOf(latlon[3]));
			wptWindow.setVisible(true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
