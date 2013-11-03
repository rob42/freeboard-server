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

package nz.co.fortytwo.freeboard.server.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class SGImplifyTest {

	private GPX gpx;

	@Before
	public void setUp() throws Exception {
		String gpxDirStr = "./src/test/resources/";
		File gpxDir = new File(gpxDirStr);
		//File gpxFile = new File(gpxDir, Util.getConfig(null).getProperty(Constants.TRACK_CURRENT));
		File gpxFile = new File(gpxDir, "2013-02-12_04-45-44.gpx");
		
		gpx = new GPXParser().parseGPX(gpxFile);
	}

	@Test
	public void shouldSimplifyTrack(){
		Track track = gpx.getTracks().iterator().next();
		ArrayList<Waypoint> origPoints = track.getTrackPoints();
		ArrayList<Waypoint> newPoints = SGImplify.simplifyLine2D(0.000025, origPoints);
		assertTrue(origPoints.size()> newPoints.size());
		System.out.println("Orig Size:"+origPoints.size()+", simplified size:"+newPoints.size());
		//each newPoint should be in origPoints
		for(Waypoint wp: newPoints){
			System.out.println(wp);
			assertTrue(origPoints.contains(wp));
		}
	}
	
	@After
	public void tearDown() throws Exception {
	}

	

}
