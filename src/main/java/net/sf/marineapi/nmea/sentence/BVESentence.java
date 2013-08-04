package net.sf.marineapi.nmea.sentence;

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
 * FU30/FU60 digital fuel gauge
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
 * 
 * Temp gauge
 * 
 * Parse as:
 * $PBVE, E F OIAD OIAC AB AB AA AA CAAA CMAB CF JKAA DNAI AE
 * 
 * E = Product Code, E = T30/T60
 * D = Software Version #
 * OIAD = Temperature Calibration Number
 * OKAC = A2D gain
 * AB = Sender Type
 * AB = Backlight Level
 * AA = Units of Measure (0= deg F, non-0= deg C)
 * AA = Built-in Alarms Armed (AA=0 = NO)
 * CAAA = Low temperature alarm value
 * CMAB = High temperature alarm value
 * CF = Checksum for Non-Volatile Memory
 * JKAA = Engine Temperature
 * DNAI = Sensor Volts
 * AE = NMEA sentence checksum
 * 
 * OP30/OP60 digital oil pressure gauge sample NMEA sentence:
 * 
 * $PBVE,DEDLAEOIACABABAAAACJAAJAABOGMLAADNAIPG
 * 
 * Parse as:
 * $PBVE, D E DLAE OIAC AB AB AA AA CJAA JAAB OG MLAA DNAI PG
 * 
 * D = Product Code, D = OP30/OP60
 * E = Software Version #
 * DLAE = Oil Pressure Calibration Number
 * OIAC = A2D gain
 * AB = Sender Type
 * AB = Backlight Level
 * AA = Units of Measure (0= psi, non-0= Bars)
 * AA = Built-in Alarms Armed (AA=0 = NO)
 * CJAA = Low pressure alarm value
 * JAAB = High pressure alarm value
 * OG = Checksum for Non-Volatile Memory
 * MLAA = Oil Pressure
 * DNAI = Sensor Volts
 * PG = NMEA sentence checksum
 * 
 * Where A=0, B=1, C=2, ... , O=14, P=15
 * 
 * Decode Oil Pressure as:
 * MLAA = 16*M + L + 4096*A + 256*A
 * MLAA = 16*12 + 11 + 4096*0 +256*0
 * MLAA = 203 psi
 * 
 * RH30/RH60/RH110 Digital RPM/Engine hours gauge
 * 
 * $PBVE,BEAAADAAABFKBCIIBDAGOOABAAAAADAAAAOIACAAONFMAKCAADAAAAHG
 * 
 * 
 * Parse as:
 * $PBVE,  B  E  AA  ADAA  AB  FKBC  IIBD  AGOO  AB  AAAA  AD  AAAA  OIAC  AA  
 * ON  FMAK  CA  AD  AAAA  HG
 * 
 *    B = Product Code (B= RH30/RH60)
 *    E = Software Version
 *   AA = Spare NMV Byte (Ignore)
 * ADAA = Display Damping
 *   AB = Terminal C used as NMEA output (1) or as External Alarm Output (0)
 * FKBC = Maximum RPM seen from last reset
 * IIBD = High RPM Alarm value 
 * AGOO = Clock Speed Calibration #
 *   AB = Backlight Level
 * AAAA = Maintenance count-down alarm
 *   AD = Engine Minutes
 * AAAA = Engine Hours
 * OIAC = RPM Calibration number
 *   AA = Mode
 *   AN = Non voltatile memory checksum
 * FMAK = RPM
 *   CA = Elapsed Seconds
 *   AD = Elapsed Minutes
 * AAAA = Elapsed Hours
 *   HG = NMEA Sentence checksum
 * 
 * Where A=0, B=1, C=2, ... , O=14, P=15
 * 
 * Decode RPM as:
 * FMAK = 16*F + M + 4096*A + 256*K
 * FMAK = 16*5 + 12 + 4096*0 +256*10
 * FMAK = 2652 RPM
 */
public interface BVESentence extends Sentence {

	public boolean isFuelGuage();

	public boolean isTempGuage();

	public boolean isPressureGuage();
	
	public boolean isEngineRpm();
	// fuel
	public float getFuelUseRateUnitsPerHour();
	public float getFuelUsedOnTrip();
	public float getFuelRemaining();

	// temp
	public int getTempUnitsOfMeasure();
	public int getLowTempAlarmValue();
	public int getHighTempAlarmValue();
	public int getEngineTemp();
	public float getVoltage();

	// pressure
	public int getPressureUnitsOfMeasure();
	public int getLowPressureAlarmValue();
	public int getHighPressureAlarmValue();
	public int getPressure();
	//rpm
	public int getMaximumRpmSinceReset();
	public int getHighRpmAlarmValue(); 
	public int getMaintenanceCountdownAlarm();
	public int getEngineHours();
	public int getEngineMinutes(); 
	public int getEngineRpm();
	
}
