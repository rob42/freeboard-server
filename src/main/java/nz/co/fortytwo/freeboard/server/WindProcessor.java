package nz.co.fortytwo.freeboard.server;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Calculates the true wind from apparent wind and vessel speed/heading
 * @author robert
 *
 */
public class WindProcessor implements Processor {

	public void process(Exchange exchange) throws Exception {
		String bodyStr = exchange.getIn().getBody(String.class).trim();
		
			try {
				int apparentWind=0;
				int apparentDirection=0;
				float vesselSpeed=0;
				//int heading=0;
				String [] bodyArray=bodyStr.split(",");
				for(String s:bodyArray){
					// we need HDG, and LOG
					if(s.startsWith(Constants.SOG)){
						vesselSpeed= Float.valueOf(s.substring(s.indexOf("=")+1));
					}
					if(s.startsWith(Constants.WSA)){
						apparentWind= Integer.valueOf(s.substring(s.indexOf("=")+1));
					}
					if(s.startsWith(Constants.WDA)){
						apparentDirection= Integer.valueOf(s.substring(s.indexOf("=")+1));
					}
					//if(s.startsWith(Constants.COG)){
					//	heading= Integer.valueOf(s.substring(s.indexOf("=")+1));
					//}
				}
				//now calc and add to body
				int trueWindSpeed = calcTrueWindSpeed(apparentWind, apparentDirection, vesselSpeed);
				int trueWindDir = calcTrueWindDirection(apparentWind,apparentDirection, vesselSpeed);
				bodyStr=bodyStr+",WDT="+trueWindDir+",WST="+trueWindSpeed;
				
				exchange.getOut().setBody(bodyStr);

			} catch (Exception e) {
				// e.printStackTrace();
			}
		

	}
	
	/**
	 * Calculates the true wind direction from apparent wind on vessel
	 * Result is relative to bow
	 * 
	 * @param apparentWind
	 * @param apparentDirection 0 to 360 deg to the bow
	 * @param vesselSpeed
	 * @return trueDirection 0 to 360 deg to the bow
	 */
	int calcTrueWindDirection(int apparentWind, int apparentDirection, float vesselSpeed){
		/*
			 Y = 90 - D
			a = AW * ( cos Y )
			bb = AW * ( sin Y )
			b = bb - BS
			True-Wind Speed = (( a * a ) + ( b * b )) 1/2
			True-Wind Angle = 90-arctangent ( b / a )
		*/
                apparentDirection=apparentDirection%360;
		boolean stbd = apparentDirection<180;
                if(!stbd){
                   apparentDirection=360-apparentDirection; 
                }
		double y = 90-apparentDirection;
		double a = apparentWind * Math.cos(Math.toRadians(y));
		double b = (apparentWind * Math.sin(Math.toRadians(y)))-vesselSpeed;
		double td = 90-Math.toDegrees(Math.atan((b/a)));
		if(!stbd)return (int)(360-td);
		return (int)td;
				
	}
	
	/**
	 * Calculates the true wind speed from apparent wind speed on vessel
	 * 
	 * @param apparentWind
	 * @param apparentDirection 0 to 360 deg to the bow
	 * @param vesselSpeed
	 * @return
	 */
        int calcTrueWindSpeed(int apparentWind, int apparentDirection, float vesselSpeed){
                apparentDirection=apparentDirection%360;
                if(apparentDirection>180){
                   apparentDirection=360-apparentDirection; 
                }
		double y = 90-apparentDirection;
		double a = apparentWind * Math.cos(Math.toRadians(y));
		double b = (apparentWind * Math.sin(Math.toRadians(y)))-vesselSpeed;
		return (int)(Math.sqrt((a*a)+(b*b)));
	}



}
