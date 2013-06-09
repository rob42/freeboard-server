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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JTextArea;

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
	private boolean manager=false;
	private JTextArea textArea;
	private ImageFilter filter = new TransparentImageFilter();
	
	public ChartProcessor() throws Exception {
		config=Util.getConfig(null);
		
	}
	public ChartProcessor(boolean manager,JTextArea textArea) throws Exception {
		config=Util.getConfig(null);
		this.manager=manager;
		this.textArea=textArea;
	}

	public void processChart(String file, boolean reTile) throws Exception {
		File chartFile = new File(config.getProperty(Constants.MAPCACHE_RESOURCE)+"/"+file);
		processChart(chartFile, reTile);
	}
	public void processChart(File chartFile, boolean reTile) throws Exception {
		//make a file
				
				if(!chartFile.exists()){
					if(manager){
						System.out.print("No file at "+chartFile.getAbsolutePath()+"\n");
					}
					logger.error("No file at "+chartFile.getAbsolutePath());
				}
				//for WORLD.tif (Blue Marble) we need 
				//'gdal_translate -a_ullr -180.0 90.0 180.0 -90.0 -a_srs "EPSG:4326" -of vrt  WORLD.tif temp.vrt'
				//to add Georef info
				if(chartFile.getName().toUpperCase().equals("WORLD.TIF")){
					processWorldChart(chartFile,reTile);
				}else
				//we have a KAP file
				if(chartFile.getName().toUpperCase().endsWith("KAP")){
					processKapChart(chartFile,reTile);
				}else{
					System.out.print("File "+chartFile.getAbsolutePath()+" not recognised so not processed\n");
				}
				
	}
	private void processWorldChart(File chartFile, boolean reTile) throws Exception {
		String chartName = chartFile.getName();
		chartName = chartName.substring(0,chartName.lastIndexOf("."));
		File dir = new File(chartFile.getParentFile(),chartName);
		if(manager){
			System.out.print("Chart tag:"+chartName+"\n");
			System.out.print("Chart dir:"+dir.getPath()+"\n");
		}
		logger.debug("Chart tag:"+chartName);
		logger.debug("Chart dir:"+dir.getPath());
		//start by running the gdal script
		if(reTile){
			executeGdal(chartFile, chartName,
					Arrays.asList("gdal_translate", "-co","COMPRESS=PACKBITS", "-a_ullr","-180.0","90.0","180.0","-90.0","-a_srs","\"EPSG:4326\"", "-if","GTiff", "-of", "vrt", chartFile.getName(),"temp.vrt"),
					Arrays.asList("gdal2tiles.py", "-z", "0-6", "temp.vrt", chartName));
		}
		//write out freeboard.txt

		String text = "\n\tvar WORLD = L.tileLayer(\"http://{s}.{server}:8080/mapcache/WORLD/{z}/{x}/{y}.png\", {\n"+
    			"\t\tserver: host,\n"+
    			"\t\tsubdomains: 'abcd',\n"+
    			"\t\tattribution: 'Blue Marble',\n"+
    			"\t\tminZoom: 0,\n"+
    			"\t\tmaxZoom: 7,\n"+
    			"\t\ttms: true\n"+
    			"\t\t}).addTo(map);\n";
		if(manager){
			System.out.print(text+"\n");
		}
		File layers = new File(dir,"freeboard.txt");
        FileUtils.writeStringToFile(layers, text);
        System.out.print("Zipping directory...\n");
		//ZipUtils.zip(dir, new File(dir.getParentFile(),chartName+".zip"));
		System.out.print("Zipping directory complete\n");
	}

	/**
	 * Reads the .kap file, and the generated tilesresource.xml to get
	 * chart desc, bounding box, and zoom levels 
	 * @param chartFile
	 * @throws Exception
	 */
	public void processKapChart(File chartFile, boolean reTile) throws Exception {
		String chartName = chartFile.getName();
		chartName = chartName.substring(0,chartName.lastIndexOf("."));
		File dir = new File(chartFile.getParentFile(),chartName);
		if(manager){
			System.out.print("Chart tag:"+chartName+"\n");
			System.out.print("Chart dir:"+dir.getPath()+"\n");
		}
		logger.debug("Chart tag:"+chartName);
		logger.debug("Chart dir:"+dir.getPath());
		//start by running the gdal scripts
		if(reTile){
			executeGdal(chartFile, chartName, 
					//this was for NZ KAP charts
					//Arrays.asList("gdal_translate", "-if","GTiff", "-of", "vrt", "-expand", "rgba",chartFile.getName(),"temp.vrt"),
					//this for US NOAA charts
					Arrays.asList("gdal_translate", "-of", "vrt", "-expand", "rgba",chartFile.getName(),"temp.vrt"),
					Arrays.asList("gdal2tiles.py", "temp.vrt", chartName));
		}
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
		//we cant have + or , or = in name, as its used in storing ChartplotterViewModel
		//US50_2 BERING SEA CONTINUATION,NU=2401,RA=2746,3798,DU=254
		desc=desc.replaceAll("\\+", " ");
		desc=desc.replaceAll(",", " ");
		desc=desc.replaceAll("=", "/");
		//limit length too
		if(desc.length()>40){
			desc=desc.substring(0,40);
		}
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
        if(manager){
			System.out.print("Box:"+minx+","+miny+","+maxx+","+maxy+"\n");
		}
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
        if(manager){
			System.out.print("Zoom:"+minZoom+"-"+maxZoom+"\n");
		}
        logger.debug("Zoom:"+minZoom+"-"+maxZoom);
        
        String snippet = "\n\tvar "+chartName+" = L.tileLayer(\"http://{s}.{server}:8080/mapcache/"+chartName+"/{z}/{x}/{y}.png\", {\n"+
        			"\t\tserver: host,\n"+
        			"\t\tsubdomains: 'abcd',\n"+
        			"\t\tattribution: '"+chartName+" "+desc+"',\n"+
        			"\t\tminZoom: "+minZoom+ ",\n"+
        			"\t\tmaxZoom: "+maxZoom+",\n"+
        			"\t\ttms: true\n"+
        			"\t\t}).addTo(map);\n";
        		
        
        if(manager){
			System.out.print(snippet+"\n");
		}
        logger.debug(snippet);
		//add it to local freeboard.txt
        File layers = new File(dir,"freeboard.txt");
        FileUtils.writeStringToFile(layers, snippet);
        //now zip the result
        System.out.print("Zipping directory...\n");
		ZipUtils.zip(dir, new File(dir.getParentFile(),chartName+".zip"));
		System.out.print("Zipping directory complete, in "+new File(dir.getParentFile(),chartName+".zip").getAbsolutePath()+"\n");
		System.out.print("Conversion of "+chartName+" was completed successfully!\n");
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
		File processDir = chartFile.getParentFile();
		//mkdir $1
		//gdal_translate -of vrt -expand rgba $1.kap temp.vrt
		 ProcessBuilder pb = new ProcessBuilder(argList);
		 pb.directory(processDir);
		 //pb.inheritIO();
		 if(manager){
			 ForkWorker fork = new ForkWorker(textArea, pb);
			 fork.execute();
			 while(!fork.isDone()){
				 Thread.currentThread().sleep(500);
				 //System.out.print(".");
			 }
		 }else{
			 Process p = pb.start();
			 p.waitFor();
			 if(p.exitValue()>0){
				 if(manager){
						System.out.print("ERROR:gdal_translate did not complete normally\n");
					}
				 logger.error("gdal_translate did not complete normally");
				 return;
			 }else{
				 System.out.print("Completed gdal_translate\n");
			 }
		 }
		//gdal2tiles.py temp.vrt $1
		 File tileDir = new File(processDir,chartName);
		 tileDir.mkdir();
		 pb = new ProcessBuilder("gdal2tiles.py", "temp.vrt", chartName);
		 pb.directory(processDir);
		 //pb.inheritIO();
		 if(manager){
			 ForkWorker fork = new ForkWorker(textArea, pb);
			 fork.execute();
			 while(!fork.isDone()){
				 Thread.currentThread().sleep(500);
				 //System.out.print(".");
			 }
			 System.out.print("Completed gdal2tiles\n");
		 }else{
			 Process p = pb.start();
			 p.waitFor();
			 if(p.exitValue()>0){
				 if(manager){
						System.out.print("ERROR:gdal2tiles did not complete normally\n");
					}
				 logger.error("gdal2tiles did not complete normally");
				 return;
			 }else{
				 System.out.print("Completed gdal2tiles\n");
			 }
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
		if(manager){
			System.out.print("Process "+tileDir.getAbsolutePath());
		}
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
	//	File tmpPng = new File(dir.getAbsoluteFile()+".new");
		if(manager){
			System.out.print("      Convert "+dir.getName()+"\n");
		}
		logger.debug("      Convert "+dir.getName());
		
		BufferedImage img = ImageIO.read(dir);
		ImageProducer ip = new FilteredImageSource(img.getSource(), filter);
		Image transparentImage = Toolkit.getDefaultToolkit().createImage(ip);
		BufferedImage dest = new BufferedImage( img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = dest.createGraphics();
		g2.drawImage(transparentImage, 0, 0, null);
		g2.dispose();
		ImageIO.write(dest, "PNG", dir);
		
	}

	
	      
	public Image makeColorTransparent(Image im, final Color color) {
		ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
		return Toolkit.getDefaultToolkit().createImage(ip);
    }
	/**
	 * First arg is chart filename, second is boolean reTile. 
	 * reTile = true causes the tiles to be recreated,
	 * false just recreates the layers conf text.
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
		boolean reTile = true;
		if(args!=null && args.length>1 && StringUtils.isNotBlank(args[1])){
			reTile=Boolean.valueOf(args[1]);
		}
		//we have a file
		ChartProcessor chartProcessor = new ChartProcessor();
		chartProcessor.processChart(chartFile,reTile);
	}

	

}
