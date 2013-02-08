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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import nz.co.fortytwo.freeboard.server.util.Constants;
import nz.co.fortytwo.freeboard.server.util.Util;
import nz.co.fortytwo.freeboard.zk.WindViewModel;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.TrackPoint;
import org.alternativevision.gpx.beans.Waypoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * Churns through incoming nav data and creates a GPX file
 * Uses code from http://gpxparser.alternativevision.ro to do GPX parsing
 * 
 * @author robert
 * 
 */
public class GPXProcessor extends FreeboardProcessor implements Processor {

	private static final String CURRENT = "current";
	private static Logger logger = Logger.getLogger(GPXProcessor.class);
	private File gpxFile;
	private File gpxDir;
	private GPX gpx;
	private Track currentTrack;
	private int count = 0;

	public GPXProcessor() {

		try {
			String gpxDirStr = Util.getConfig(null).getProperty(Constants.TRACKS_RESOURCE);
			gpxDir = new File(gpxDirStr);
			FileUtils.forceMkdir(gpxDir);
			gpxFile = new File(gpxDir, Util.getConfig(null).getProperty(Constants.TRACK_CURRENT));
			if (gpxFile.exists()) {
				InputStream in = FileUtils.openInputStream(gpxFile);
				gpx = new GPXParser().parseGPX(in);
				in.close();
			} else {
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
			e.printStackTrace();
			logger.error(e);
		}

	}

	public void process(Exchange exchange) throws Exception {
		if (exchange.getIn().getBody() == null)
			return;

		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = exchange.getIn().getBody(HashMap.class);

		// <trkpt lat="-41.19408333" lon="173.24741667"><ele>2376</ele><time>2007-10-14T10:09:57Z</time></trkpt>
		if (map.containsKey(Constants.LAT) && map.containsKey(Constants.LON)) {
			// write out to gpx track
			TrackPoint tp = new TrackPoint();
			tp.setElevation(0.0);
			tp.setTime(new Date());
			tp.setLatitude((Double) map.get(Constants.LAT));
			tp.setLongitude((Double) map.get(Constants.LON));
			currentTrack.getTrackPoints().add(tp);
		}
		count++;
		//write out every 120 points, eg 2 min at 1 sec GPS data
		if(count >120){
			count=0;
			//if its too long, truncate and save
			if(currentTrack.getTrackPoints().size()>1100){
				//write out the first 1000
				ArrayList<Waypoint> points = currentTrack.getTrackPoints();
				ArrayList<Waypoint> oldPoints = (ArrayList<Waypoint>) points.subList(0, 1000);
				GPX oldGpx = new GPX();
				Track oldTrack = new Track();
				oldTrack.setTrackPoints(oldPoints);
				oldGpx.addTrack(oldTrack);
				
				String fileName = DateFormat.getInstance().format(new Date());
				oldTrack.setName(CURRENT+" "+fileName);
				fileName=fileName.replaceAll(" ", "");
				OutputStream oldOut = FileUtils.openOutputStream(new File(gpxDir, fileName+".gpx"));
				new GPXParser().writeGPX(oldGpx,oldOut);
				oldOut.flush();
				oldOut.close();
				points.removeAll(oldPoints);
				currentTrack.setTrackPoints(points);
			}
			OutputStream out = FileUtils.openOutputStream(gpxFile);
			new GPXParser().writeGPX(gpx,out);
			out.flush();
			out.close();
		}

	}

}
