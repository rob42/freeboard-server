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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import net.sf.marineapi.nmea.sentence.RMCSentence;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Place for all the left over bits that are used across freeboard
 * @author robert
 *
 */
public class Util {
	
	private static Logger logger = Logger.getLogger(Util.class);
	private static Properties props;
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
	public static File cfg = null;
	private static boolean timeSet=false;
	/**
	 * Smooth the data a bit
	 * @param prev
	 * @param current
	 * @return
	 */
	public static  double movingAverage(double ALPHA, double prev, double current) {
	    prev = ALPHA * prev + (1-ALPHA) * current;
	    return prev;
	}

	/**
	 * Load the config from the named dir, or if the named dir is null, from the default location
	 * The config is cached, subsequent calls get the same object 
	 * @param dir
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static Properties getConfig(String dir) throws FileNotFoundException, IOException{
		if(props==null){
			//we do a quick override so we get nice sorted output :-)
			props = new Properties() {
			    @Override
			    public Set<Object> keySet(){
			        return Collections.unmodifiableSet(new TreeSet<Object>(super.keySet()));
			    }

			    @Override
			    public synchronized Enumeration<Object> keys() {
			        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
			    }
			};
			Util.setDefaults(props);
			if(StringUtils.isNotBlank(dir)){
				//we provided a config dir, so we use it
				props.setProperty(Constants.CFG_DIR, dir);
				cfg = new File(props.getProperty(Constants.CFG_DIR)+props.getProperty(Constants.CFG_FILE));
			}else if(Util.getUSBFile()!=null){
				//nothing provided, but we have a usb config dir, so use it
				cfg = new File(Util.getUSBFile(),props.getProperty(Constants.CFG_DIR)+props.getProperty(Constants.CFG_FILE));
			}else{
				//use the default config
				cfg = new File(props.getProperty(Constants.CFG_DIR)+props.getProperty(Constants.CFG_FILE));
			}
			
			if(cfg.exists()){
				props.load(new FileReader(cfg));
			}
		}
		return props;
	}
	
	/**
	 * Save the current config to disk.
	 * @throws IOException
	 */
	public static void saveConfig() throws IOException{
		if(props==null)return;
		props.store(new FileWriter(cfg), null);
		
	}

	/**
	 * Config defaults
	 * 
	 * @param props
	 */
	public static void setDefaults(Properties props) {
		//populate sensible defaults here
		props.setProperty(Constants.FREEBOARD_URL,"/freeboard");
		props.setProperty(Constants.FREEBOARD_RESOURCE,"freeboard/");
		props.setProperty(Constants.MAPCACHE_RESOURCE,"./mapcache");
		props.setProperty(Constants.MAPCACHE,"/mapcache");
		props.setProperty(Constants.HTTP_PORT,"8080");
		props.setProperty(Constants.WEBSOCKET_PORT,"9090");
		props.setProperty(Constants.CFG_DIR,"./conf/");
		props.setProperty(Constants.CFG_FILE,"freeboard.cfg");
		props.setProperty(Constants.DEMO,"false");
		props.setProperty(Constants.SERIAL_URL,"./src/test/resources/motu.log&scanStream=true&scanStreamDelay=500");
		props.setProperty(Constants.VIRTUAL_URL,"");
		props.setProperty(Constants.USBDRIVE,"/media/usb0");
		props.setProperty(Constants.TRACKS,"/tracks");
		props.setProperty(Constants.TRACKS_RESOURCE,"./tracks");
		props.setProperty(Constants.TRACK_CURRENT,"current.gpx");
		props.setProperty(Constants.WAYPOINTS,"/tracks");
		props.setProperty(Constants.WAYPOINTS_RESOURCE,"./tracks");
		props.setProperty(Constants.WAYPOINT_CURRENT,"waypoints.gpx");
		props.setProperty(Constants.SERIAL_PORTS,"/dev/ttyUSB0,/dev/ttyUSB1,/dev/ttyUSB2");
		props.setProperty(Constants.DNS_USE_CHOICE,Constants.DNS_USE_BOAT);
	}
	

	/**
	 * Round to specified decimals
	 * @param val
	 * @param places
	 * @return
	 */
	public static double round(double val, int places){
		double scale = Math.pow(10, places);
		long iVal = Math.round (val*scale);
		return iVal/scale;
	}
	
	/**
	 * Updates and saves the scaling values for instruments
	 * @param scaleKey
	 * @param amount
	 * @param scaleValue
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static double updateScale(String scaleKey, double amount, double scaleValue) throws FileNotFoundException, IOException {
			scaleValue = scaleValue*amount;
			scaleValue= Util.round(scaleValue, 2);
			//logger.debug(" scale now = "+scale);
			
			//write out to config
			Util.getConfig(null).setProperty(scaleKey, String.valueOf(scaleValue));
			Util.saveConfig();
			
		return scaleValue;
	}

	/**
	 * Checks if a usb drive is inserted, and returns the root dir.
	 * Returns null if its not there
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static File getUSBFile() throws FileNotFoundException, IOException {
		File usbDrive = new File(Util.getConfig(null).getProperty(Constants.USBDRIVE));
		if(usbDrive.exists() && usbDrive.list().length>0){
			//we return it
			return usbDrive;
		}
		return null;
	}

	/**
	 * Attempt to set the system time using the GPS time
	 * @param sen
	 */
	public static void checkTime(RMCSentence sen) {
			if(timeSet)return;
			try {
				net.sf.marineapi.nmea.util.Date dayNow = sen.getDate();
				//if we need to set the time, we will be WAAYYY out
				//we only try once, so we dont get lots of native processes spawning if we fail
				timeSet=true;
				Date date = new Date();
				if((date.getYear()+1900)==dayNow.getYear()){
					logger.debug("Current date is " + date);
					return;
				}
				//so we need to set the date and time
				net.sf.marineapi.nmea.util.Time timeNow = sen.getTime();
				String yy = String.valueOf(dayNow.getYear());
				String MM = pad(2,String.valueOf(dayNow.getMonth()));
				String dd = pad(2,String.valueOf(dayNow.getDay()));
				String hh = pad(2,String.valueOf(timeNow.getHour()));
				String mm = pad(2,String.valueOf(timeNow.getMinutes()));
				String ss = pad(2,String.valueOf(timeNow.getSeconds()));
				logger.debug("Setting current date to " + dayNow + " "+timeNow);
				String cmd = "sudo date --utc " + MM+dd+hh+mm+yy+"."+ss;
				Runtime.getRuntime().exec(cmd.split(" "));// MMddhhmm[[yy]yy]
				logger.debug("Executed date setting command:"+cmd);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			} 
			
		}

	/**
	 * pad the value to i places, eg 2 >> 02
	 * @param i
	 * @param valueOf
	 * @return
	 */
	private static String pad(int i, String value) {
		while(value.length()<i){
			value="0"+value;
		}
		return value;
	}
	
	
	
}
