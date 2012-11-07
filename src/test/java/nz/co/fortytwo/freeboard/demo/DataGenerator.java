package nz.co.fortytwo.freeboard.demo;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;

public class DataGenerator implements Runnable{

	private static String SERIAL_URL=null;

	public static void main(String[] args) throws Exception {
		if (args != null && args.length > 0 && StringUtils.isNotBlank(args[0])) {
			SERIAL_URL = args[0];
		}
		new DataGenerator().run();
	}

	private double logFactor=1;
	private double lastLog = 0;
	private double logLimit = 10;
	
	private double windDirFactor=4;
	private int lastWindDir = 0;
	private int windDirLimitLow = 45;
	private int windDirLimitHigh = 160;
	
	private double headingFactor=4;
	private int lastHeading = 0;
	private int headingLimitLow = 45;
	private int headingLimitHigh = 160;
	
	private double windSpeedFactor=1;
	private double lastWindSpeed = 0;
	private double windSpeedLimitLow = 0;
	private double windSpeedLimitHigh = 40;
	private OutputStream serialOut;

	public DataGenerator() {
		//set up an output stream
		//serialOut=new BufferedOutputStream(System.out);
		try {
			serialOut=new BufferedOutputStream(new FileOutputStream("/tmp/camel.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public void run() {
		while(true){
			//We want to output a stream of random data
			//WIND
			//	APP DIR
			doWindDir();
			//	APP SPEED
			doWindSpeed();
			//LAT
			//LON
			//HEADING
			doHeading();
			//LOG
			doLog();
			try {
				Thread.currentThread().sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	private void doHeading() {
		lastHeading =(int) (lastHeading+Math.round(Math.random()*headingFactor));
		if(lastHeading>=headingLimitHigh ){
			headingFactor=-headingFactor;
		}
		if(lastHeading<=headingLimitLow ){
			headingFactor=Math.abs(headingFactor);
		}
		write("HDG",lastHeading);		
	}

	private void doLog() {
		lastLog =lastLog+(Math.random()*logFactor);
		lastLog=(double)Math.round(lastLog*100)/100;
		if(lastLog>=logLimit ){
			logFactor=-logFactor;
		}
		if(lastLog<0 ){
			lastLog=0;
			logFactor=Math.abs(logFactor);
		}
		write("LOG",lastLog);
		
	}
	private void doWindDir() {
		lastWindDir =(int) (lastWindDir+Math.round(Math.random()*windDirFactor));
		if(lastWindDir>=windDirLimitHigh ){
			windDirFactor=-windDirFactor;
		}
		if(lastWindDir<=windDirLimitLow ){
			windDirFactor=Math.abs(windDirFactor);
		}
		write("WDA",lastWindDir);
		
	}
	private void doWindSpeed() {
		lastWindSpeed =lastWindSpeed+Math.round(Math.random()*windSpeedFactor);
		lastWindSpeed=(double)Math.round(lastWindSpeed*100)/100;
		if(lastWindSpeed>=windSpeedLimitHigh ){
			windSpeedFactor=-windSpeedFactor;
		}
		if(lastWindSpeed<=windSpeedLimitLow ){
			windSpeedFactor=Math.abs(windSpeedFactor);
		}
		write("WSA",lastWindSpeed);
	
	}

		public void write(String type, double val){
			try {
				serialOut.write(String.valueOf(type+"="+val+"\n").getBytes());
				serialOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public void write(String type, int val){
			try {
				serialOut.write(String.valueOf(type+"="+val+"\n").getBytes());
				serialOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
}
