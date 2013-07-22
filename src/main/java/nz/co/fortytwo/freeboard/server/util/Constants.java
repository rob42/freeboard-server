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
package nz.co.fortytwo.freeboard.server.util;

public class Constants {
	/** version */
	public static final String VER = "VER";
	/** Roll degrees */
	public static final String ROLL = "RLL";
	/** Pitch degrees */
	public static final String PITCH = "PCH";
	/** Yaw degrees */
	public static final String YAW = "YAW";
	/** IMU Health? */
	public static final String IMU_HEALTH = "IMH";
	/** Mag X - roll */
	public static final String MAG_ROLLX = "MGX";
	/** Mag Y - pitch */
	public static final String MAG_ROLLY = "MGY";
	/** Mag Z - heading */
	public static final String MAG_ROLLZ = "MGZ";
	/** Mag Heading - magnetic */
	public static final String MAG_HEADING = "MGH";
	/** Mag Heading - true */
	public static final String MAG_HEADING_TRUE = "MGT";
	/** Latitude */
	public static final String LAT = "LAT";
	/** Longitude */
	public static final String LON = "LON";
	/** Altitude */
	public static final String ALTITUDE = "ALT";
	/** CourseOverGround - true */
	public static final String COURSE_OVER_GND = "COG";

	/** Speed over ground */
	public static final String SPEED_OVER_GND = "SOG";
	/** GPS fix true/false */
	public static final String GPS_FIX = "FIX";
	/** satellites */
	public static final String GPS_SAT = "SAT";
	/** Time of week */
	public static final String TIME_OF_WEEK = "TOW";
	/** mag declination */
	public static final String MAG_DECLINATION = "DEC";
	/** Wind speed apparent */
	public static final String WIND_SPEED_APPARENT = "WSA";
	/** Wind speed true */
	public static final String WIND_SPEED_TRUE = "WST";
	/** Wind dir apparent */
	public static final String WIND_DIR_APPARENT = "WDA";
	/** Wind dir true */
	public static final String WIND_DIR_TRUE = "WDT";
	/** Wind speed units */
	public static final String WIND_SPEED_UNITS = "WSU";
	/** Wind alarm speed (knots) */
	public static final String WIND_ALARM_SPEED_KNOTS = "WSK";
	/** Wind speed alarm state */
	public static final String WIND_ALARM_SPEED_STATE = "WSX";

	/** Autopilot state (on/off) 0=off, 1=on */
	public static final String AUTOPILOT_STATE = "APX";
	/** Autopilot offset from source, +/- deg - used for wind */
	public static final String AUTOPILOT_OFFSET = "APT";
	/** Autopilot goal in deg, 0-360 - used for compass */
	public static final String AUTOPILOT_GOAL = "APG";
	/** Autopilot source, W wind or C compass */
	public static final String AUTOPILOT_SOURCE = "APS";

	/** Anchor alarm state */
	public static final String ANCHOR_ALARM_STATE = "AAX";
	/** Anchor alarm radius */
	public static final String ANCHOR_ALARM_RADIUS = "AAR";
	/** Anchor alarm Lat */
	public static final String ANCHOR_ALARM_LAT = "AAN";
	/** Anchor alarm Lon */
	public static final String ANCHOR_ALARM_LON = "AAE";

	// attached device types
	public static final String UID = "UID";
	public static final String IMU = "IMU";
	public static final String MEGA = "MEGA";

	// waypoints
	/** Waypoint change event */
	public static final String WAYPOINT_CHANGE_EVENT = "WPC";
	/** Waypoint goto event, followed by toLat|toLon|fromLat|fromLon */
	public static final String WAYPOINT_GOTO_EVENT = "WPG";
	// Commands
	public static final String VERSION_CMD = "#VER";
	/** constant for AUTOPILOT_SOURCE = wind */
	public static final String AUTOPILOT_WIND = "W";
	/** constant for AUTOPILOT_SOURCE = wind */
	public static final String AUTOPILOT_COMPASS = "C";
	/** Autopilot state (on/off) 0=off, 1=on */
	public static final String AUTOPILOT_STATE_CMD = "#APX";
	/** Autopilot offset from source, +/- deg - used for wind */
	public static final String AUTOPILOT_TARGET_CMD = "#APT";
	/** Autopilot goal in deg, 0-360 - used for compass */
	public static final String AUTOPILOT_GOAL_CMD = "#APG";
	/** Autopilot adjust +/- deg */
	public static final String AUTOPILOT_ADJUST_CMD = "#APJ";
	/** Autopilot source, W wind or C compass */
	public static final String AUTOPILOT_SOURCE_CMD = "#APS";

	public static final String ANCHOR_ALARM_STATE_CMD = "#AAX";
	public static final String ANCHOR_ALARM_RADIUS_CMD = "#AAR";
	public static final String ANCHOR_ALARM_ADJUST_CMD = "#AAJ";
	public static final String ANCHOR_ALARM_LAT_CMD = "#AAN";
	public static final String ANCHOR_ALARM_LON_CMD = "#AAE";

	public static final String WIND_SPEED_ALARM_STATE_CMD = "#WSX";
	public static final String WIND_ALARM_KNOTS_CMD = "#WSK";
	/** +/- in degrees to adjust zero wind dir reading on Mega */
	public static final String WIND_ZERO_ADJUST_CMD = "#WZJ";
	public static final String NMEA = "NMEA";

	// Fuel gauge constants
	public static final String FUEL_REMAINING = "FFV";
	public static final String FUEL_USED = "FFU";
	public static final String FUEL_USE_RATE = "FFR";

	public static final String ENGINE_RPM = "RPM";
	public static final String ENGINE_HOURS = "EHH";
	public static final String ENGINE_MINUTES = "EMM";
	public static final String ENGINE_TEMP = "ETT";
	public static final String ENGINE_VOLTS = "EVV";
	public static final String ENGINE_OIL_PRESSURE = "EPP";
	
	//depth
	public static final String DEPTH_BELOW_TRANSDUCER = "DBT";
	
	// config constants
	public static final String DEMO = "freeboard.demo";
	public static final String FREEBOARD_URL = "freeboard.web.url";
	public static final String FREEBOARD_RESOURCE = "freeboard.web.dir";
	public static final String MAPCACHE_RESOURCE = "freeboard.mapcache.dir";
	public static final String MAPCACHE = "freeboard.mapcache.url";

	public static final String TRACKS_RESOURCE = "freeboard.tracks.dir";
	public static final String TRACKS = "freeboard.tracks.url";
	public static final String TRACK_CURRENT = "freeboard.tracks.current";

	public static final String WAYPOINTS_RESOURCE = "freeboard.waypoints.dir";
	public static final String WAYPOINTS = "freeboard.waypoints.url";
	public static final String WAYPOINT_CURRENT = "freeboard.waypoints.current";

	public static final String HTTP_PORT = "freeboard.http.port";
	public static final String WEBSOCKET_PORT = "freeboard.websocket.port";
	public static final String CFG_DIR = "freeboard.cfg.dir";
	public static final String CFG_FILE = "freeboard.cfg.file";
	public static final String SERIAL_URL = "freeboard.serial.demo.file";
	public static final String VIRTUAL_URL = "freeboard.virtual.url";

	public static final String WIND_ZERO_OFFSET = "freeboard.wind.offset";

	// gui constants
	public static final String LOGG_X = "freeboard.logg.x";
	public static final String LOGG_Y = "freeboard.logg.y";
	public static final String LOGG_W = "freeboard.logg.w";
	public static final String LOGG_H = "freeboard.logg.h";
	public static final String LOGG_SCALE = "freeboard.logg.scale";

	public static final String AUTOPILOT_X = "freeboard.autopilot.x";
	public static final String AUTOPILOT_Y = "freeboard.autopilot.y";

	public static final String ANCHOR_X = "freeboard.anchor.x";
	public static final String ANCHOR_Y = "freeboard.anchor.y";

	public static final String WIND_SCALE = "freeboard.wind.scale";
	public static final String WIND_X = "freeboard.wind.x";
	public static final String WIND_Y = "freeboard.wind.y";
	public static final String WIND_W = "freeboard.wind.w";
	public static final String WIND_H = "freeboard.wind.h";

	public static final String USBDRIVE = "freeboard.usb.usbdrive";
	public static final String CHART_LAT = "freeboard.chart.lat";
	public static final String CHART_LON = "freeboard.chart.lon";
	public static final String CHART_ZOOM = "freeboard.chart.zoom";
	public static final String CHART_LAYER = "freeboard.chart.layer.";
	public static final String GOTO_LAT = "freeboard.goto.lat";
	public static final String GOTO_LON = "freeboard.goto.lon";
	public static final String FROM_LAT = "freeboard.from.lat";
	public static final String FROM_LON = "freeboard.from.lon";

	public static final String SERIAL_PORTS = "freeboard.serial.ports";
	public static final String SERIAL_PORT_BAUD = "freeboard.serial.port.baud";

	public static final String DNS_USE_CHOICE = "freeboard.dns.use";
	public static final String DNS_USE_BOAT = "boat";
	public static final String DNS_USE_HOME = "home";
	public static final String NMEA_XDR = "freeboard.nmea.";
	public static final String XDR =  "XDR.";

	public Constants() {
		// TODO Auto-generated constructor stub
	}

}
