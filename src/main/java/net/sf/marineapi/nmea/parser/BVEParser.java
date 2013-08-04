/*
 * BVEParser.java
 * Copyright (C) 2013 Robert Huitema
 * 
 * This file is derived from Java Marine API.
 * <http://ktuukkan.github.io/marine-api/>
 * 
 * Java Marine API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
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
 * 
 * This implementation of the CruzPro PBVE sentence provided by R T Huitema (www.42.co.nz/FreeBoard)
 * cos I need it for FreeBoard :-)
 * 
 * The following CruzPro instruments use the "$PBVE" proprietary NMEA sentences
 * assigned to BV Engineering (CruzPro Ltd.) by the National Marine Electronics
 * Association: 4800 BAUD, 8 bits, one start bit, one stop bit, no parity
 * 
 * FU30/FU60 digital fuel gauge
 * T30/T60 digital engine temperature gauge
 * OP30/OP60 digital oil pressure gauge
 * RH30/RH60/RH110 digital RPM gauge/ Engine hours gauge/alarm
 * EH60 Engine Hours Gauge
 * CH30/CH60 Chain Counter
 * CT30/CT60 Ships Clock/Timer
 * EH60 Engine Hours Gauge
 * 
 * The proprietary sentences are used because NMEA has never assigned standard
 * sentences that fit these type instruments well.
 * 
 * * FU30/FU60 digital fuel gauge
 * Sample decoding:
 * 
 * $PBVE,FCIEADCIAAJJAFFA
 * 
 * Parse as:
 * $PBVE, F C IEAD CIAA JJAF FA
 * 
 * Where:
 * 
 * F = Product Designator F=FU30/FU60
 * C = Software Version #
 * IEAD = Fuel Use Rate (Units per hour)
 * CIAA = Fuel used this trip (trip fuel)
 * JJAF = Fuel Remaining
 * FA = Checksum
 * 
 * and A=0, B=1, C=2, ... , O=14, P=15
 * 
 * So:
 * 
 * IEAD decodes to 16*I + E + 4096*A + 256*D
 * IEAD = 16*8 + 4 + 4096*0 + 256*3
 * IEAD = 132 + 768 = 900 = 90.0 units per hour
 * 
 * CIAA decodes to 16*C + I + 4096*A +256* A
 * CIAA = 16*2 + 8 + 4096*0 + 256*0
 * CIAA = 40+0=4.0 units used for this trip so far
 * 
 * JJAF decodes to 16*J + J + 4096*A + 256*F
 * JJAF = 16*9 + 9 + 4096*0 + 256*5
 * JJAF = 153 + 1280 = 1433 = 143.3 units of fuel remaining
 */
package net.sf.marineapi.nmea.parser;

import net.sf.marineapi.nmea.sentence.BVESentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;

/**
 * BVE sentence parser.
 * 
 * @author Robert Huitema
 */
class BVEParser extends SentenceParser implements BVESentence {

	// types
	public static final String CHAIN_COUNTER = "A";
	public static final String ENGINE = "B";
	public static final String SHIPS_CLOCK = "C";
	public static final String PRESSURE = "D";
	public static final String ENGINE_TEMP = "E";
	public static final String FUEL_GUAGE = "F";

	// field indexes
	//private static final int PRODUCT_DESIGNATOR = 0;

	private static final int FUEL_USE_RATE = 2;
	private static final int FUEL_USED_TRIP = 6;
	private static final int FUEL_REMAINING = 10;
	
	private static final int TEMP_UNITS_OF_MEASURE = 14;//2
	private static final int TEMP_HIGH_ALARM = 22;
	private static final int TEMP_LOW_ALARM = 18;
	private static final int TEMP = 28;
	private static final int VOLTAGE = 32;

	private static final int PRESSURE_UNITS = 14;
	private static final int LOW_PRESSURE_ALARM = 18;
	private static final int HIGH_PRESSURE_ALARM = 22;
	private static final int CURRENT_PRESSURE=28;
	
	private static final int MAX_RPM = 10;
	private static final int HIGH_RPM_ALARM = 14;
	private static final int MAINT_COUNTDOWN = 24;
	private static final int ENGINE_MINUTES = 28;
	private static final int ENGINE_HOURS = 30;
	private static final int RPM=42;
	
	
	
	public String type = null;

	/**
	 * Constructor.
	 * 
	 * @param nmea
	 *            BVE sentence string
	 */
	public BVEParser(String nmea) {
		// has no *checksum
		super(nmea, "BVE");
		type = getStringValue(0).substring(0,1);
	}

	/**
	 * Creates BVE parser with empty sentence.
	 * 
	 * @param talker
	 *            TalkerId to set
	 */
	public BVEParser(TalkerId talker) {
		super(talker, SentenceId.BVE, 1);
	}

	public boolean isFuelGuage(){
		if(FUEL_GUAGE.equals(type))return true;
		return false;
		
	}
	public boolean isTempGuage(){
		if(ENGINE_TEMP.equals(type))return true;
		return false;
		
	}
	
	public boolean isPressureGuage(){
		if(PRESSURE.equals(type))return true;
		return false;
		
	}
	
	public boolean isEngineRpm(){
		if(ENGINE.equals(type))return true;
		return false;
		
	}
	
	public float getFuelUseRateUnitsPerHour() {
		String rate = getStringValue(0).substring(FUEL_USE_RATE, FUEL_USE_RATE + 4);
		return getFloatFromCruzPro(rate);
	}

	public float getFuelUsedOnTrip() {
		String used = getStringValue(0).substring(FUEL_USED_TRIP, FUEL_USED_TRIP + 4);
		return getFloatFromCruzPro(used);
	}

	public float getFuelRemaining() {
		String remains = getStringValue(0).substring(FUEL_REMAINING, FUEL_REMAINING + 4);
		return getFloatFromCruzPro(remains);
	}



	public String getType() {
		return type;
	}

	public int getTempUnitsOfMeasure() {
		String temp = getStringValue(0).substring(TEMP_UNITS_OF_MEASURE, TEMP_UNITS_OF_MEASURE + 2);
		return getShortFromCruzPro(temp);
	}

	public int getLowTempAlarmValue() {
		String temp = getStringValue(0).substring(TEMP_LOW_ALARM, TEMP_LOW_ALARM + 4);
		return getIntFromCruzPro(temp);
	}

	public int getHighTempAlarmValue() {
		String temp = getStringValue(0).substring(TEMP_HIGH_ALARM, TEMP_HIGH_ALARM + 4);
		return getIntFromCruzPro(temp);
	}

	public int getEngineTemp() {
		String temp = getStringValue(0).substring(TEMP, TEMP + 4);
		return getIntFromCruzPro(temp);
	}

	public float getVoltage() {
		String volt = getStringValue(0).substring(VOLTAGE, VOLTAGE + 4);
		float v = getFloatFromCruzPro(volt);
		if (v == 0)
			return 0;
		return v / 10;
	}

	public int getPressureUnitsOfMeasure() {
		String temp = getStringValue(0).substring(PRESSURE_UNITS, PRESSURE_UNITS + 2);
		return getShortFromCruzPro(temp);
	}

	public int getLowPressureAlarmValue() {
		String temp = getStringValue(0).substring(LOW_PRESSURE_ALARM, LOW_PRESSURE_ALARM + 4);
		return getIntFromCruzPro(temp);
	}

	public int getHighPressureAlarmValue() {
		String temp = getStringValue(0).substring(HIGH_PRESSURE_ALARM, HIGH_PRESSURE_ALARM + 4);
		return getIntFromCruzPro(temp);
	}

	public int getPressure() {
		String temp = getStringValue(0).substring(CURRENT_PRESSURE, CURRENT_PRESSURE + 4);
		return getIntFromCruzPro(temp);
	}

	public int getMaximumRpmSinceReset() {
		String temp = getStringValue(0).substring(MAX_RPM, MAX_RPM + 4);
		return getIntFromCruzPro(temp);
	}

	public int getHighRpmAlarmValue() {
		String rpm = getStringValue(0).substring(HIGH_RPM_ALARM, HIGH_RPM_ALARM + 4);
		return getIntFromCruzPro(rpm);
	}

	public int getMaintenanceCountdownAlarm() {
		String alarm = getStringValue(0).substring(MAINT_COUNTDOWN, MAINT_COUNTDOWN + 4);
		return getIntFromCruzPro(alarm);
	}

	public int getEngineHours() {
		String hrs = getStringValue(0).substring(ENGINE_HOURS, ENGINE_HOURS + 4);
		return getIntFromCruzPro(hrs);
	}
	
	public int getEngineMinutes() { 
		String min = getStringValue(0).substring(ENGINE_MINUTES, ENGINE_MINUTES + 2);
		return getShortFromCruzPro(min);
	}

	public int getEngineRpm() {
		String rpm = getStringValue(0).substring(RPM, RPM + 4);
		return getIntFromCruzPro(rpm);
	}
	
	/**
	 * Cruzpro alpha format:
	 * eg
	 * IEAD decodes to 16*I + E + 4096*A + 256*D
	 * IEAD = 16*8 + 4 + 4096*0 + 256*3
	 * IEAD = 132 + 768 = 900 = 90.0 units per hour
	 * 
	 * @param str
	 * @return
	 */
	private float getFloatFromCruzPro(String str) {
		float result = getIntFromCruzPro(str);
		if (result == 0)
			return 0;
		return result / 10;
	}

	/**
	 * Cruzpro alpha format:
	 * eg
	 * IEAD decodes to 16*I + E + 4096*A + 256*D
	 * IEAD = 16*8 + 4 + 4096*0 + 256*3
	 * IEAD = 132 + 768 = 900
	 * 
	 * @param str
	 * @return
	 */
	private int getIntFromCruzPro(String str) {
		// 'A' is 65 decimal, '0' in cruzpro
		char[] chars = str.toCharArray();
		// first char
		return ((chars[0] - 65) * 16) + (chars[1] - 65) + ((chars[2] - 65) * 4096) + ((chars[3] - 65) * 256);
	}
	private int getShortFromCruzPro(String str) {
		// 'A' is 65 decimal, '0' in cruzpro
		char[] chars = str.toCharArray();
		// first char
		return ((chars[0] - 65) * 16) + (chars[1] - 65) ;
	}

}
