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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

import nz.co.fortytwo.freeboard.server.CamelContextFactory;
import nz.co.fortytwo.freeboard.server.util.Constants;
import nz.co.fortytwo.freeboard.server.util.Util;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Waypoint;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.ListModelMap;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
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

	@WireVariable
	private Session sess;

	@Wire("button#cfgSave")
	private Button cfgSave;
	@Wire("button#cfgCancel")
	private Button cfgCancel;
	
	@Wire("textbox#cfgWindOffset")
	private Textbox cfgWindOffset; 
	
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
		logger.debug("Init..");
		
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
			logger.debug("Found:"+name);
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
			logger.debug("Processing :"+chart);
			logger.debug(snippet);
			
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
		logger.debug(layersStr);
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
		int c=0;
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
					logger.debug("Found:"+name);
				}else{
					logger.warn(layerFile.getAbsolutePath()+" does not exist");
				}
			}catch(Exception e){
				logger.error(e.getMessage(),e);
			}
			c++;
		}
		
		allChartsModel=new ListModelList<String>(allListMap.keySet());
		
	}

	@Listen("onClick = button#cfgSave")
	public void cfgSaveClick(MouseEvent event) {
		logger.debug(" cfgSave button event = " + event);
		try {
			Properties config = Util.getConfig(null);
			if(NumberUtils.isNumber(cfgWindOffset.getValue())){
				config.setProperty(Constants.WIND_ZERO_OFFSET, cfgWindOffset.getValue());
				Util.saveConfig();
				//notify others
				producer.sendBody(Constants.WIND_ZERO_ADJUST+":"+cfgWindOffset.getValue() +",");
			}else{
				Messagebox.show("Wind offset must be numeric");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		} 
		
	}
	
	@Listen("onClick = button#chartSave")
	public void chartSaveClick(MouseEvent event) {
		logger.debug(" chartSave button event = " + event);
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
		logger.debug(" chartCancel button event = " + event);
		try {
			selectedListArray.clear();
			setAllChartLayers();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		} 
		
	}

	

	@Listen("onClick = button#cfgCancel")
	public void cfgCancelClick(MouseEvent event) {
		logger.debug(" cfgCancel button event = " + event);
		setConfigDefaults();
	}
	
	@Listen("onClick = image#chooseAllBtn")
	public void chooseAllBtnClick(MouseEvent event) {
		logger.debug(" chooseAllBtn button event = " + event);
		//if its not null and we dont have it
		selectedChartsModel.clear();
		selectedChartsModel.addAll(allChartsModel.getInnerList());
	}
	@Listen("onClick = image#chooseBtn")
	public void chooseBtnClick(MouseEvent event) {
		logger.debug(" chooseBtn button event = " + event);
		//if its not null and we dont have it
		if(allCharts.getSelectedItem()!=null && !selectedChartsModel.contains(allCharts.getSelectedItem().getLabel())){
			selectedChartsModel.add(allCharts.getSelectedItem().getLabel());
		}
	}
	@Listen("onClick = image#removeBtn")
	public void removeBtnClick(MouseEvent event) {
		logger.debug(" removeBtn button event = " + event);
		//drop it
		if(selectedCharts.getSelectedItem()!=null){
			selectedChartsModel.remove(selectedCharts.getSelectedIndex());
		}
	}
	@Listen("onClick = image#removeAllBtn")
	public void removeAllBtnClick(MouseEvent event) {
		logger.debug(" removeAllBtn button event = " + event);
		//drop all
		selectedChartsModel.clear();
	}
	
	@Listen("onClick = image#topBtn")
	public void topBtnClick(MouseEvent event) {
		logger.debug(" topBtn button event = " + event);
		if(selectedCharts.getSelectedItem()!=null){
			String item = selectedChartsModel.remove(selectedCharts.getSelectedIndex());
			selectedChartsModel.add(0, item);
		}
	}
	
	@Listen("onClick = image#upBtn")
	public void upBtnClick(MouseEvent event) {
		logger.debug(" upBtn button event = " + event);
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
		logger.debug(" downBtn button event = " + event);
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
		logger.debug(" bottomBtn button event = " + event);
		if(selectedCharts.getSelectedItem()!=null){
			String item = selectedChartsModel.remove(selectedCharts.getSelectedIndex());
			selectedChartsModel.add(item);
		}
	}
	
	
	private void setConfigDefaults(){
		try{
			Properties config = Util.getConfig(null);
			cfgWindOffset.setValue(config.getProperty(Constants.WIND_ZERO_OFFSET));
			String useChoice = config.getProperty(Constants.DNS_USE_CHOICE);
			if(Constants.DNS_USE_BOAT.equals(useChoice)){
				useHomeGroup.setSelectedItem(useBoatRadio);
			}else{
				useHomeGroup.setSelectedItem(useHomeRadio);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			Messagebox.show("There has been a problem with loading the configuration:"+e.getMessage());
		} 
	}


}
