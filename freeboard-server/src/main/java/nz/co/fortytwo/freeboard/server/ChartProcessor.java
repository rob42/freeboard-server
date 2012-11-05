package nz.co.fortytwo.freeboard.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ChartProcessor {

	Logger logger = Logger.getLogger(ChartProcessor.class);
	Properties config = null;

	public ChartProcessor(String file) throws Exception {
		config=ServerMain.getConfig(null);
		//make a file
		File chartFile = new File(config.getProperty(ServerMain.MAPCACHE_RESOURCE)+"/"+file);
		if(!chartFile.exists()){
			logger.error("No file at "+chartFile.getAbsolutePath());
		}
		//we have the file - assume kap for now
		processKapFile(chartFile);
	}

	private void processKapFile(File chartFile) throws Exception {
		//start by running the gdal script
		//TODO: launch the script
		
		//now get the Chart Name from the kap file
		FileReader fileReader = new FileReader(chartFile);
		char[] chars = new char[4096];
		fileReader.read(chars);
		fileReader.close();
		String header = new String(chars);
		int pos=header.indexOf("BSB/NA=")+7;
		String name = header.substring(pos,header.indexOf("\n",pos));
		logger.debug("Name:"+name);
		//process the layer data
		String dirName = chartFile.getName();
		dirName = dirName.substring(0,dirName.lastIndexOf("."));
		File dir = new File(chartFile.getParentFile(),dirName);
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
		new ChartProcessor(chartFile);
	}

}
