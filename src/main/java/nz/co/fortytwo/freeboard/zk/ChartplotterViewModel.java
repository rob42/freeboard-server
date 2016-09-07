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

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;
import javafx.geometry.BoundingBox;
import net.sf.marineapi.nmea.util.Position;
import nz.co.fortytwo.freeboard.server.util.Constants;
import nz.co.fortytwo.freeboard.server.util.Util;

import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

public class ChartplotterViewModel extends SelectorComposer<Window>{

	private static Logger logger = Logger.getLogger(ChartplotterViewModel.class);
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@WireVariable
        private Session sess;
	
	@Wire( "#mainWindow")
	private Window mainWindow;
	@Wire( "#firstLat")
	private Label firstLat;
	@Wire( "#firstLon")
	private Label firstLon;
	@Wire( "#firstZoom")
	private Label firstZoom;
	@Wire( "#layerVisibility")
	private Label layerVisibility;
        
        @Wire( "#chartIndex")
        public Label chartIndex;
        

        @Wire("#numCircles")
        private Label numCircles;
        @Wire("#radiiCircles")
        private Label radiiCircles;
        
	private String lat ="0.0";
	private String lon ="0.0";
	private String zoom ="1";
	private String visibility="";
        private String num = "0";
        private String radii = "100";
	
	public ChartplotterViewModel() {
		super();
		
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		if(logger.isDebugEnabled())logger.debug("Init..");
		if(Util.getConfig(null).containsKey(Constants.CHART_LAT)){
			lat=Util.getConfig(null).getProperty(Constants.CHART_LAT);
		}
		if(Util.getConfig(null).containsKey(Constants.CHART_LON)){
			lon=Util.getConfig(null).getProperty(Constants.CHART_LON);
		}
		if(Util.getConfig(null).containsKey(Constants.CHART_ZOOM)){
			zoom=Util.getConfig(null).getProperty(Constants.CHART_ZOOM);
		}
		if(Util.getConfig(null).containsKey(Constants.NUM_BOAT_CIRCLES)){
			num=Util.getConfig(null).getProperty(Constants.NUM_BOAT_CIRCLES);
		}
		if(Util.getConfig(null).containsKey(Constants.RADII_BOAT_CIRCLES)){
			radii=Util.getConfig(null).getProperty(Constants.RADII_BOAT_CIRCLES);
		}
		for(Object key:Util.getConfig(null).keySet()){
			if(((String)key).startsWith(Constants.CHART_LAYER)){
				String name = (String)key;
				name=name.substring(Constants.CHART_LAYER.length());
				name=name.replaceAll("\\+", " ");
				String val = Util.getConfig(null).getProperty((String)key);
				visibility=visibility+(name+"="+val+";");
			}
		}
		layerVisibility.setValue(visibility);
		firstLat.setValue(lat);
		firstLon.setValue(lon);
		firstZoom.setValue(zoom);
                numCircles.setValue(num);
                radiiCircles.setValue(radii);
                
                
		File mapDir = new File(Util.cfg.getParentFile().getParentFile(),Util.getConfig(null).getProperty(Constants.MAPCACHE));
                File[] dirList = mapDir.listFiles(File::isDirectory);
                BoundingBox[] bbList = new BoundingBox[dirList.length];
                for (int i = 0; i < dirList.length;i++){
                    bbList[i] = getChartBounds(((File)dirList[i]).getAbsolutePath()+File.separator+"tilemapresource.xml");
                    System.out.println(((File)dirList[i]).getName()+" "+bbList[i].toString());
                }				
	}
	
    public BoundingBox getChartBounds(String filnam) {

//        File tileMapResource = new File(Util.getConfig(null).getProperty(Constants.MAPCACHE_RESOURCE));
        BufferedReader reader = null;
        double minx = 0;
        double miny = 0;
        double maxx = 0;
        double maxy = 0;
        Point2D.Double upLeft;
        Point2D.Double lowRight;
        try{
        reader = new BufferedReader(new FileReader(filnam));
        String text = null;

            while ((text = reader.readLine()) != null) {
            if (text.contains("BoundingBox")) {
                String values = text.replace("<", "").replace("BoundingBox", "").replace("\"", "").trim();
                StringTokenizer st = new StringTokenizer(values, " =/>");
                while (st.hasMoreElements()) {
                    String id = (String) st.nextElement();
                    String val = (String) st.nextElement();
                    if (id.equals("minx")) {
                        minx = Double.parseDouble(val);
                        continue;
                    }
                    if (id.equals("maxx")) {
                        maxx = Double.parseDouble(val);
                        continue;
                    }
                    if (id.equals("miny")) {
                        miny = Double.parseDouble(val);
                        continue;
                    }
                    if (id.equals("maxy")) {
                        maxy = Double.parseDouble(val);
                        continue;
                    }
                }
            }
        }
        upLeft = new Point2D.Double(minx, maxy);
        lowRight = new Point2D.Double(maxx, miny);
        } catch (Exception e){
                System.out.println(e);
                }
        return new BoundingBox(minx, miny, (maxx-minx), (maxy-miny));
    }


//	[12:19:06.939] NZ6142_1 Nelson Harbour layer visibility changed to true
	@Listen("onLayerChange = #mainWindow")
	public void onLayerChange(Event event) {
		try{
		   Object[] layer = (Object[]) event.getData();
		   String name = (String)layer[0];
		   name=name.replaceAll(" ", "+");
		   Util.getConfig(null).setProperty(Constants.CHART_LAYER+name, String.valueOf(layer[1]));
		   if(logger.isDebugEnabled())logger.debug("onLayerChange:"+name+"="+String.valueOf(layer[1]));
		   Util.saveConfig();
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}
	}
	/**
	 * When the chart is zoomed,or moved we record the data so we can go back there on reload
	 * 
	 * @param event
	 */
	@Listen("onChartChange = #mainWindow")
	public void onChartChange(Event event) {
		try{
		   Object[] latlonZoom = (Object[]) event.getData();
		   //its an edit find by name
		   if(logger.isDebugEnabled())logger.debug("Zoom/pan event (lat/lon/zoom):"+latlonZoom[0]+","+latlonZoom[1]+","+latlonZoom[2]);
		   
		   lat= String.valueOf(latlonZoom[0]);
		   lon= String.valueOf(latlonZoom[1]);
		   zoom=String.valueOf(latlonZoom[2]);
		   Util.getConfig(null).setProperty(Constants.CHART_LAT, lat);
		   Util.getConfig(null).setProperty(Constants.CHART_LON, lon);
		   Util.getConfig(null).setProperty(Constants.CHART_ZOOM, zoom);
		   Util.saveConfig();
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}
	}

	

}
