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
package nz.co.fortytwo.freeboard.server;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import nz.co.fortytwo.freeboard.server.util.Constants;
import nz.co.fortytwo.freeboard.server.util.SGImplify;
import nz.co.fortytwo.freeboard.server.util.Util;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.TrackPoint;
import org.alternativevision.gpx.beans.Waypoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * Churns through incoming nav data and creates a GPX file
 * Uses code from http://gpxparser.alternativevision.ro to do GPX parsing
 * Various strategies limit the amount of data collected
 * 
 * Writes to USB drive /tracks if one is found
 * Writes to freeboard/tracks if no drive is found
 * 
 * @author robert
 * 
 */
public class GPXProcessor extends FreeboardProcessor implements Processor, FreeboardHandler {

	private static final String CURRENT = "current";
	private static final double TOLERANCE = 0.000025; // About 3M out of line?
	private static Logger logger = Logger.getLogger(GPXProcessor.class);
	private File gpxFile;
	private File gpxDir;
	private GPX gpx;
	private Track currentTrack;
	private int count = 0;
	private int triggerCount = 60;

	public GPXProcessor() {

		try {
			String gpxDirStr = Util.getConfig(null).getProperty(Constants.TRACKS_RESOURCE);
			File usb = Util.getUSBFile();
			if (usb != null) {
				gpxDir = new File(usb, gpxDirStr);
				// write out every minute
				triggerCount = 60;
			} else {
				gpxDir = new File(gpxDirStr);
			}
			FileUtils.forceMkdir(gpxDir);
			gpxFile = new File(gpxDir, Util.getConfig(null).getProperty(Constants.TRACK_CURRENT));
			if (gpxFile.exists()) {
				try {
					gpx = new GPXParser().parseGPX(gpxFile);
				} catch (Exception e) {
					// may be unreadable
					logger.error(e.getMessage(), e);
				}
			}
			if (gpx == null) {
				gpx = new GPX();
			}
			for (Track t : gpx.getTracks()) {
				if (CURRENT.equals(t.getName())) {
					currentTrack = t;
					break;
				}
			}
			if (currentTrack == null) {
				currentTrack = new Track();
				currentTrack.setName(CURRENT);
				gpx.addTrack(currentTrack);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	public void process(Exchange exchange) {
		if (exchange.getIn().getBody() == null)
			return;

		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = exchange.getIn().getBody(HashMap.class);

		handle(map);

	}

	private void writeGPX(GPX gpx, File file) {
		try {
			new GPXParser().writeGPX(gpx, file);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	//@Override
	public HashMap<String, Object> handle(HashMap<String, Object> map) {
		// <trkpt lat="-41.19408333" lon="173.24741667"><ele>2376</ele><time>2007-10-14T10:09:57Z</time></trkpt>
		if (map.containsKey(Constants.LAT) && map.containsKey(Constants.LON)) {
			// check if they are worth saving
			double newLat = (Double) map.get(Constants.LAT);
			double newLon = (Double) map.get(Constants.LON);
			// write out to gpx track
			TrackPoint tp = new TrackPoint();
			tp.setElevation(0.0);
			tp.setTime(new Date());
			tp.setLatitude(newLat);
			tp.setLongitude(newLon);
			currentTrack.getTrackPoints().add(tp);

			// we count every occurrence, an easy way to count time!
			count++;
		}
		// write out every 60 points, eg 1 min at 1 sec GPS data, or 60sec to usb
		if (count > triggerCount) {
			count = 0;
			// simplify here
			currentTrack.setTrackPoints(SGImplify.simplifyLine2D(TOLERANCE, currentTrack.getTrackPoints()));
			// if its still too long, truncate and archive
			if (currentTrack.getTrackPoints().size() > 1100) {
				// write out the first 1000
				ArrayList<Waypoint> points = currentTrack.getTrackPoints();

				Waypoint[] oldPoints = Arrays.copyOfRange(points.toArray(new Waypoint[0]), 0, 1000);
				GPX oldGpx = new GPX();
				Track oldTrack = new Track();
				ArrayList<Waypoint> newPoints = new ArrayList<Waypoint>();
				newPoints.addAll(Arrays.asList(oldPoints));

				oldTrack.setTrackPoints(newPoints);
				oldGpx.addTrack(oldTrack);

				String fileName = Util.sdf.format(new Date());
				oldTrack.setName(CURRENT + " " + fileName);
				writeGPX(oldGpx, new File(gpxDir, fileName + ".gpx"));

				points.removeAll(newPoints);
				currentTrack.setTrackPoints(points);
			}

			writeGPX(gpx, gpxFile);

		}
		return map;
	}

}
