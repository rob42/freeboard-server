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
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Processes charts into tile pyramids, and adds config to chartplotter javascript.
 * Currently only handles BSB/KAP
 * 
 * @author robert
 *
 */
public class ChartProcessor {

	Logger logger = Logger.getLogger(ChartProcessor.class);
	Properties config = null;

	public ChartProcessor() throws Exception {
		config=Util.getConfig(null);
		
	}

	public void processChart(String file) throws Exception {
		//make a file
				File chartFile = new File(config.getProperty(Constants.MAPCACHE_RESOURCE)+"/"+file);
				if(!chartFile.exists()){
					logger.error("No file at "+chartFile.getAbsolutePath());
				}
				//for WORLD.tif (Blue Marble) we need 
				//'gdal_translate -a_ullr -180.0 90.0 180.0 -90.0 -a_srs "EPSG:4326" -of vrt  WORLD.tif temp.vrt'
				//to add Georef info
				if(chartFile.getName().toUpperCase().equals("WORLD.tif")){
					processWorldChart(chartFile);
				}
				//we have a KAP file
				if(chartFile.getName().toUpperCase().endsWith("KAP")){
					processKapChart(chartFile);
				}
		
	}
	private void processWorldChart(File chartFile) throws Exception {
		String chartName = chartFile.getName();
		chartName = chartName.substring(0,chartName.lastIndexOf("."));
		File dir = new File(chartFile.getParentFile(),chartName);
		logger.debug("Chart tag:"+chartName);
		logger.debug("Chart dir:"+dir.getPath());
		//start by running the gdal script
		executeGdal(chartFile, chartName,
				Arrays.asList("gdal_translate", "-co","COMPRESS=PACKBITS", "-a_ullr","-180.0","90.0","180.0","-90.0","-a_srs","\"EPSG:4326\"", "-if","GTiff", "-of", "vrt", chartFile.getName(),"temp.vrt"),
				Arrays.asList("gdal2tiles.py", "-z", "0-6", "temp.vrt", chartName));
		
	}

	/**
	 * Reads the .kap file, and the generated tilesresource.xml to get
	 * chart desc, bounding box, and zoom levels 
	 * @param chartFile
	 * @throws Exception
	 */
	public void processKapChart(File chartFile) throws Exception {
		String chartName = chartFile.getName();
		chartName = chartName.substring(0,chartName.lastIndexOf("."));
		File dir = new File(chartFile.getParentFile(),chartName);
		logger.debug("Chart tag:"+chartName);
		logger.debug("Chart dir:"+dir.getPath());
		//start by running the gdal script
		
		executeGdal(chartFile, chartName, 
				Arrays.asList("gdal_translate", "-if","GTiff", "-of", "vrt", "-expand", "rgba",chartFile.getName(),"temp.vrt"),
				Arrays.asList("gdal2tiles.py", "temp.vrt", chartName));
		//now get the Chart Name from the kap file
		FileReader fileReader = new FileReader(chartFile);
		char[] chars = new char[4096];
		fileReader.read(chars);
		fileReader.close();
		String header = new String(chars);
		int pos=header.indexOf("BSB/NA=")+7;
		String desc = header.substring(pos,header.indexOf("\n",pos)).trim();
		//if(desc.endsWith("\n"))desc=desc.substring(0,desc.length()-1);
		logger.debug("Name:"+desc);
		//we cant have + in name, as its used in storing ChartplotterViewModel
		desc=desc.replaceAll("+", " ");
		//process the layer data
		
		//read data from dirName/tilelayers.xml
		SAXReader reader = new SAXReader();
        Document document = reader.read(new File(dir,"tilemapresource.xml"));
        //we need BoundingBox
        Element box = (Element) document.selectSingleNode( "//BoundingBox" );
        String minx = box.attribute("minx").getValue();
        String miny = box.attribute("miny").getValue();
        String maxx = box.attribute("maxx").getValue();
        String maxy = box.attribute("maxy").getValue();
        logger.debug("Box:"+minx+","+miny+","+maxx+","+maxy);

        //we need TileSets, each tileset has an href, we need first and last for zooms
        List<Attribute> list = document.selectNodes( "//TileSets/TileSet/@href" );
        int minZoom = 18;
        int maxZoom = 0;
        for (Attribute attribute : list){
            int zoom = Integer.valueOf(attribute.getValue());
            if(zoom<minZoom)minZoom=zoom;
            if(zoom>maxZoom)maxZoom=zoom;
        }
        logger.debug("Zoom:"+minZoom+"-"+maxZoom);
        
        //make the javascript snippets
        String bounds = "\tvar "+chartName+"Bounds = new OpenLayers.Bounds( "+miny+", "+minx+", "+maxy+", "+maxx+");\n"+
        				"\tmapBounds.extend("+chartName+"Bounds );\n";
        logger.debug(bounds);
        String relPath ="../../mapcache/";
        String snippet = "\n\tvar "+chartName+" = new OpenLayers.Layer.TMS( \""+chartName+" "+desc+"\", \""+relPath+chartName+"/\",\n"+
        		"\t\t{ layername: '"+relPath+chartName+"/',\n"+
        		"\t\ttype: 'png', getURL: overlay_getTileURL, alpha: true,\n"+ 
        		"\t\tisBaseLayer: false,\n"+
        		"\t\tvisibility: false,\n"+
        		"\t\tnumZoomLevels: 18,\n"+
        		"\t\tminZoomLevel: "+minZoom+",\n"+
        		"\t\tmaxZoomLevel: "+maxZoom+",\n"+
        		"\t\t});\n" +
        		"\tmap.addLayer("+chartName+");\n";
        logger.debug(snippet);
		//add it to layers.js
        File layers = new File("../freeboard/js/layers.js");
        String layersStr = FileUtils.readFileToString(layers);
        //remove the closing }
        if(layersStr.trim().endsWith("}")){
        	layersStr = layersStr.substring(0,layersStr.lastIndexOf("}"));
        }
        layersStr = "\n"+layersStr+"\n"+bounds+"\n"+snippet+"\n}\n";
        FileUtils.writeStringToFile(layers, layersStr);
	}

	/**
	 * Executes a script which invokes GDAL and imagemagick to process the chart
	 * into a tile pyramid
	 * 
	 * @param config2
	 * @param chartFile
	 * @param chartName
	 * @param list 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	private void executeGdal( File chartFile, String chartName, List<String> argList, List<String> tilesList ) throws IOException, InterruptedException {
		File dir = new File(config.getProperty(Constants.MAPCACHE_RESOURCE));
		//mkdir $1
		//gdal_translate -of vrt -expand rgba $1.kap temp.vrt
		 ProcessBuilder pb = new ProcessBuilder(argList);
		 pb.directory(dir);
		 pb.inheritIO();
		 Process p = pb.start();
		 p.waitFor();
		 if(p.exitValue()>0){
			 logger.error("gdal_translate did not complete normally");
			 return;
		 }
		//gdal2tiles.py temp.vrt $1
		 File tileDir = new File(dir,chartName);
		 tileDir.mkdir();
		 pb = new ProcessBuilder("gdal2tiles.py", "temp.vrt", chartName);
		 pb.directory(dir);
		 pb.inheritIO();
		 p = pb.start();
		 p.waitFor();
		 if(p.exitValue()>0){
			 logger.error("gdal2tiles did not complete normally");
			 return;
		 }
		 //now make images transparent
		 //recurse dirs
		 recurseDirs(tileDir);
		 
	}

	/**
	 * Recurse tile stack an process images
	 * @param tileDir
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	private void recurseDirs(File tileDir) throws IOException, InterruptedException {
		logger.debug("Process "+tileDir.getAbsolutePath());
		for(File dir: tileDir.listFiles()){
			if(dir.isDirectory()){
				recurseDirs(dir);
			}else{
				if(dir.getName().endsWith(".png")){
					processPng(dir);
				}
			}
			
		}
		
	}

	/**
	 * Use Imagemajick convert to make white transparent, so we can overlay charts
	 * @param dir
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void processPng(File dir) throws IOException, InterruptedException {
		File tmpPng = new File(dir.getAbsoluteFile()+".new");
		logger.debug("Convert "+dir.getName());
		ProcessBuilder pb = new ProcessBuilder("convert", dir.getName(), "-transparent", "white", "-fuzz", "10%", tmpPng.getName());
		 pb.directory(dir.getParentFile());
		 Process p = pb.start();
		 p.waitFor();
		 tmpPng.renameTo(dir);
		 if(p.exitValue()>0){
			 logger.error("Imagemagick convert did not complete normally");
			 return;
		 }
		
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		//arg0 = chartfile
		String chartFile = null;
		if(args!=null && args.length>0 && StringUtils.isNotBlank(args[0])){
			chartFile=args[0];
		}
		if(StringUtils.isBlank(chartFile)){
			System.out.print("No file provided");
			System.exit(1);
		}
		//we have a file
		ChartProcessor chartProcessor = new ChartProcessor();
		chartProcessor.processChart(chartFile);
	}

	

}
