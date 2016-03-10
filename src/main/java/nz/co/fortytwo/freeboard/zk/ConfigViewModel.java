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
import java.util.ArrayList;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nz.co.fortytwo.freeboard.server.CamelContextFactory;
import nz.co.fortytwo.freeboard.server.util.Constants;
import nz.co.fortytwo.freeboard.server.util.Util;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Image;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class ConfigViewModel extends SelectorComposer<Window> {

	private static Logger logger = Logger.getLogger(ConfigViewModel.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Pattern portNamesRegexLinux = Pattern.compile("(/dev/ttyUSB|/dev/ttyACM|/dev/ttyAMA|/dev/cu)[0-9]{1,3}");
	private Pattern portNamesRegexWindows = Pattern.compile("COM[0-9]{1,2}");

	@WireVariable
	private Session sess;

	@Wire("button#cfgSave")
	private Button cfgSave;
	@Wire("button#cfgCancel")
	private Button cfgCancel;
	
	@Wire("textbox#cfgWindOffset")
	private Textbox cfgWindOffset; 
	
	@Wire("textbox#cfgDepthOffset")
	private Textbox cfgDepthOffset; 
	
	@Wire("textbox#cfgDepthUnit")
	private Textbox cfgDepthUnit; 
	
	@Wire("textbox#portsToScan")
	private Textbox portsToScan;
	
	@Wire("combobox#portBaudRate")
	private Combobox portBaudRate;
	
	@Wire("image#chooseAllBtn")
	private Image chooseAllBtn;
	@Wire("image#chooseBtn")
	private Image chooseBtn;
	@Wire("image#removeBtn")
	private Image removeBtn;
	@Wire("image#removeAllBtn")
	private Image removeAllBtn;
	
	@Wire("image#topBtn")
	private Image topBtn;
	@Wire("image#upBtn")
	private Image upBtn;
	@Wire("image#downBtn")
	private Image downBtn;
	@Wire("image#bottomBtn")
	private Image bottomBtn;
	
	@Wire("listbox#allCharts")
	private Listbox allCharts;
	private ListModelList<String> allChartsModel;
	
	
	@Wire("listbox#selectedCharts")
	private Listbox selectedCharts;
	private ListModelList<String> selectedChartsModel;
	
	@Wire ("radioGroup#useRmcGroup")
	Radiogroup useRmcGroup;
	
	@Wire ("radio#useRmcRadio")
	Radio useRmcRadio;
	@Wire ("radio#useHHdgRadio")
	Radio useHdgRadio;
	
	@Wire ("radioGroup#useHomeGroup")
	Radiogroup useHomeGroup;
	
	@Wire ("radio#useBoatRadio")
	Radio useBoatRadio;
	@Wire ("radio#useHomeRadio")
	Radio useHomeRadio;
	
	private ProducerTemplate producer;

	private TreeMap<String,String> allListMap;
	private ArrayList<String> selectedListArray;

	public ConfigViewModel() {
		super();
		producer = CamelContextFactory.getInstance().createProducerTemplate();
		producer.setDefaultEndpointUri("seda:input");
		allListMap = new TreeMap<String,String>();
		selectedListArray = new ArrayList<String>();
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);
		if(logger.isDebugEnabled())logger.debug("Init..");
		
		setConfigDefaults();
		setAllChartLayers();
		setSelectedChartLayers();
		allCharts.setModel(allChartsModel);
		selectedChartsModel=new ListModelList<String>(selectedListArray);
		selectedCharts.setModel(selectedChartsModel);
	}

	private void setSelectedChartLayers() throws Exception{
		File layersFile = new File(Util.getConfig(null).getProperty(Constants.FREEBOARD_RESOURCE)+"/js/layers.js");
		String layer = FileUtils.readFileToString(layersFile);
		//get all the layers in the file
		int pos =layer.indexOf("L.tileLayer(");
		int pos1=0;
		while(pos>-1){
			pos=layer.indexOf("attribution",pos);
			pos=layer.indexOf("'",pos)+1;
			pos1=layer.indexOf("'",pos);
			String name = layer.substring(pos,pos1);
			selectedListArray.add(name);
			if(logger.isDebugEnabled())logger.debug("Found:"+name);
			pos = layer.indexOf("L.tileLayer(",pos1+1);
		}
		
	}

	private void saveLayers(boolean useHomeChoice) throws Exception {
		
		File layersFile = new File(Util.getConfig(null).getProperty(Constants.FREEBOARD_RESOURCE)+"/js/layers.js");
		StringBuffer layers = new StringBuffer();
		layers.append("function addLayers(map) {\n\tvar host = window.location.hostname;\n");
		for(String chart: selectedChartsModel){
			layers.append(allListMap.get(chart));
			layers.append("\n");
		}
		//layers.append("}\n");
		//now add the layers data
		layers.append("\tbaseLayers = {\n"+
		    "\t\t\"World\": WORLD,\n"+
			"\t};\n"+
		"\toverlays = {\n");
		//add the overlays
		for(String chart: selectedChartsModel){
			String snippet = allListMap.get(chart);
			if(StringUtils.isBlank(snippet)){
				continue;
			}
			if(logger.isDebugEnabled())logger.debug("Processing :"+chart);
			if(logger.isDebugEnabled())logger.debug(snippet);
			
			String chartVar = snippet.substring(0,snippet.indexOf("="));
			chartVar = chartVar.substring(chartVar.indexOf("var")+3).trim();
			snippet=getChartName(snippet,chart);
			layers.append("\t\t\""+snippet+"\": "+chartVar+",\n");
		}
		layers.append("\t};\n");
		layers.append("\tlayers = L.control.layers(baseLayers, overlays).addTo(map);\n");
		layers.append("\t};\n");
		String layersStr = layers.toString();
		if(useHomeChoice){
			//we parse out all refs to the subdomains
			// remove {s}.
			layersStr=StringUtils.remove(layersStr, "{s}.");
			layersStr=StringUtils.remove(layersStr, "subdomains: 'abcd',");
		}
		if(logger.isDebugEnabled())logger.debug(layersStr);
		FileUtils.writeStringToFile(layersFile,layersStr );
	}

	private String getChartName(String snippet, String chart) {
		int startPos=snippet.indexOf("attribution:");
		if(startPos<0){
			snippet=chart;
		}else{
			startPos=snippet.indexOf("'",startPos)+1;
			int endPos = snippet.indexOf("'",startPos);
			if(endPos<0){
				snippet=chart;
			}else{
				snippet=snippet.substring(startPos,endPos);
			}
		}
		return snippet;
	}

	private void setAllChartLayers() throws Exception {
		File layersDir = new File(Util.getConfig(null).getProperty(Constants.MAPCACHE_RESOURCE));
		for(File layerDir: layersDir.listFiles()){
			//avoid files starting with dot (.)
			if(layerDir.getName().startsWith("."))continue;
			if(layerDir.isFile())continue;
			try{
				File layerFile = new File(layerDir,"freeboard.txt");
				if(layerFile.exists()){
					String layer = FileUtils.readFileToString(layerFile);
					String chart = layerFile.getName();
					chart=chart.substring(0,chart.indexOf("."));
					String name = getChartName(layer, chart);
					allListMap.put(name, layer);
					if(logger.isDebugEnabled())logger.debug("Found:"+name);
				}else{
					logger.warn(layerFile.getAbsolutePath()+" does not exist");
				}
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
			
		}
		
		allChartsModel=new ListModelList<String>(allListMap.keySet());
		
	}

	@Listen("onClick = button#cfgSave")
	public void cfgSaveClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" cfgSave button event = " + event);
		try {
			Properties config = Util.getConfig(null);
			if(isValidPort(portsToScan.getText())){
				config.setProperty(Constants.SERIAL_PORTS, portsToScan.getValue());
			}else{
				Messagebox.show("Device ports to scan is invalid. In Linux (pi) '/dev/ttyUSB0,/dev/ttyUSB1,etc', in MS Windows 'COM1,COM2,COM3,etc'");
			}
			if(isValidBaudRate(portBaudRate.getValue())){
				config.setProperty(Constants.SERIAL_PORT_BAUD, portBaudRate.getValue());
				Util.saveConfig();
			}else{
				Messagebox.show("Device baud rate (speed) must be one of 4800,9600,19200,38400,57600");
			}
			if(NumberUtils.isNumber(cfgWindOffset.getValue())){
				config.setProperty(Constants.WIND_ZERO_OFFSET, cfgWindOffset.getValue());
				Util.saveConfig();
				//notify others
				producer.sendBody(Constants.WIND_ZERO_ADJUST_CMD+":"+cfgWindOffset.getValue() +",");
			}else{
				Messagebox.show("Wind offset must be numeric");
			}
			config.setProperty(Constants.PREFER_RMC, (String)useRmcGroup.getSelectedItem().getValue());
			config.setProperty(Constants.DNS_USE_CHOICE, (String)useHomeGroup.getSelectedItem().getValue());
			if(NumberUtils.isNumber(cfgDepthOffset.getValue())){
				config.setProperty(Constants.DEPTH_ZERO_OFFSET, cfgDepthOffset.getValue());
				Util.saveConfig();
				//notify others
				producer.sendBody(Constants.DEPTH_ZERO_ADJUST_CMD+":"+cfgDepthOffset.getValue() +",");
			}else{
				Messagebox.show("Depth offset must be numeric");
			}
			if(isValidDepthUnit(cfgDepthUnit.getText())){
				config.setProperty(Constants.DEPTH_UNIT, cfgDepthUnit.getText());
				//notify others
//				producer.sendBody(Constants.DEPTH_UNIT+":"+cfgDepthUnit.getValue() +",");
			}else{
				Messagebox.show("Depth unit must be 'f' for feet, 'M or m' for meters or 'F' for fathoms");
			}
			Util.saveConfig();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		} 
		
	}
	
	private boolean isValidPort(String text) {
		if(StringUtils.isBlank(text))return false;
		String[] ports = text.trim().split(",");
		boolean ok = false;
		//linux (and OSX?)
		if(SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_MAC_OSX){
			//each will be /dev/tty* or /dev/cu*
			for(String port:ports){
				if(StringUtils.isBlank(port))continue;
				if(portNamesRegexLinux.matcher(port.trim()).matches()){
					//must be at least one good
					ok=true;
				}else{
					//doesnt match, its bad so outa here.
					return false;
				}
				
			}
		}
		
		//windows
		if(SystemUtils.IS_OS_WINDOWS){
			//each will be COM*
			for(String port:ports){
				if(StringUtils.isBlank(port))continue;
				if(portNamesRegexWindows.matcher(port.trim()).matches()){
					//must be at least one good
					ok=true;
				}else{
					//doesnt match, its bad so outa here.
					return false;
				}
				
			}
		}
		return ok;
	}

	private boolean isValidDepthUnit(String text) {
		if(StringUtils.isBlank(text))return false;
		String unit = text.trim();
		boolean ok = false;
		
      if ((unit.equals("f"))||(unit.equals("F"))||(unit.equals("m"))|(unit.equals("M"))) {
            return true;
      } else {
         return false;
      }
	}

	private boolean isValidBaudRate(String value) {
		if(!NumberUtils.isNumber(value)) return false;
		int rate = Integer.valueOf(value);
		if( rate== 4800 || rate== 9600 || rate== 19200 || rate== 38400 || rate== 57600 )return true;
		return false;
	}

	@Listen("onClick = button#chartSave")
	public void chartSaveClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" chartSave button event = " + event);
		try {
			boolean useHomeChoice = Boolean.valueOf( (String)useHomeGroup.getSelectedItem().getValue());
			if(useHomeChoice){
				Util.getConfig(null).setProperty(Constants.DNS_USE_CHOICE, Constants.DNS_USE_HOME);
			}else{
				Util.getConfig(null).setProperty(Constants.DNS_USE_CHOICE, Constants.DNS_USE_BOAT);
			}
			
			saveLayers(useHomeChoice);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		} 
		
	}
	
	@Listen("onClick = button#chartCancel")
	public void chartCancelClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" chartCancel button event = " + event);
		try {
			selectedListArray.clear();
			setAllChartLayers();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		} 
		
	}

	

	@Listen("onClick = button#cfgCancel")
	public void cfgCancelClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" cfgCancel button event = " + event);
		setConfigDefaults();
	}
	
	@Listen("onClick = image#chooseAllBtn")
	public void chooseAllBtnClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" chooseAllBtn button event = " + event);
		//if its not null and we dont have it
		selectedChartsModel.clear();
		selectedChartsModel.addAll(allChartsModel.getInnerList());
	}
	@Listen("onClick = image#chooseBtn")
	public void chooseBtnClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" chooseBtn button event = " + event);
		//if its not null and we dont have it
		if(allCharts.getSelectedItem()!=null && !selectedChartsModel.contains(allCharts.getSelectedItem().getLabel())){
			selectedChartsModel.add(allCharts.getSelectedItem().getLabel());
		}
	}
	@Listen("onClick = image#removeBtn")
	public void removeBtnClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" removeBtn button event = " + event);
		//drop it
		if(selectedCharts.getSelectedItem()!=null){
			selectedChartsModel.remove(selectedCharts.getSelectedIndex());
		}
	}
	@Listen("onClick = image#removeAllBtn")
	public void removeAllBtnClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" removeAllBtn button event = " + event);
		//drop all
		selectedChartsModel.clear();
	}
	
	@Listen("onClick = image#topBtn")
	public void topBtnClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" topBtn button event = " + event);
		if(selectedCharts.getSelectedItem()!=null){
			String item = selectedChartsModel.remove(selectedCharts.getSelectedIndex());
			selectedChartsModel.add(0, item);
		}
	}
	
	@Listen("onClick = image#upBtn")
	public void upBtnClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" upBtn button event = " + event);
		if(selectedCharts.getSelectedItem()!=null){
			int pos = selectedCharts.getSelectedIndex();
			//dont go past the top!
			if(pos==0)return;
			String item = selectedChartsModel.remove(selectedCharts.getSelectedIndex());
			selectedChartsModel.add(pos-1,item);
		}
	}
	
	@Listen("onClick = image#downBtn")
	public void downBtnClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" downBtn button event = " + event);
		if(selectedCharts.getSelectedItem()!=null){
			int pos = selectedCharts.getSelectedIndex();
			//dont go past the bottom!
			if(pos==selectedChartsModel.size()-1)return;
			String item = selectedChartsModel.remove(selectedCharts.getSelectedIndex());
			selectedChartsModel.add(pos+1,item);
		}
	}
	
	@Listen("onClick = image#bottomBtn")
	public void bottomBtnClick(MouseEvent event) {
		if(logger.isDebugEnabled())logger.debug(" bottomBtn button event = " + event);
		if(selectedCharts.getSelectedItem()!=null){
			String item = selectedChartsModel.remove(selectedCharts.getSelectedIndex());
			selectedChartsModel.add(item);
		}
	}
	
	
	private void setConfigDefaults(){
		try{
			Properties config = Util.getConfig(null);
			
			for(Comboitem item: portBaudRate.getItems()){
				if(config.getProperty(Constants.SERIAL_PORT_BAUD).equals(item.getValue())){
					portBaudRate.setSelectedItem(item);
				}
			}
			portsToScan.setValue(config.getProperty(Constants.SERIAL_PORTS));
			cfgWindOffset.setValue(config.getProperty(Constants.WIND_ZERO_OFFSET));
			String useChoice = config.getProperty(Constants.DNS_USE_CHOICE);
			if(Constants.DNS_USE_BOAT.equals(useChoice)){
				useHomeGroup.setSelectedItem(useBoatRadio);
			}else{
				useHomeGroup.setSelectedItem(useHomeRadio);
			}
			if(new Boolean(config.getProperty(Constants.PREFER_RMC))){
				useRmcGroup.setSelectedItem(useRmcRadio);
			}else{
				useRmcGroup.setSelectedItem(useHdgRadio);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			Messagebox.show("There has been a problem with loading the configuration:"+e.getMessage());
		} 
	}


}


