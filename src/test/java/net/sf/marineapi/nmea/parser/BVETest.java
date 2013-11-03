package net.sf.marineapi.nmea.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the VTG sentence parser.
 * 
 * @author Robert Huitema
 */
public class BVETest {


/** FU30/FU60 digital fuel gauge
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
	@Test
	public void shouldHandleFuelData(){
		try{
			//empty = new BVEParser(TalkerId.GP);
			BVEParser bve = new BVEParser("$PBVE,FCIEADCIAAJJAFFA");
			assertTrue(bve.isFuelGuage());
			assertTrue(!bve.isEngineRpm());
			assertTrue(!bve.isPressureGuage());
			assertTrue(!bve.isTempGuage());
			assertEquals(90.0,bve.getFuelUseRateUnitsPerHour(),0.001);
			assertEquals(4.0, bve.getFuelUsedOnTrip(),0.001);
			assertEquals(143.3, bve.getFuelRemaining(),0.001);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	/**
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
 */
	
	/*
	 * TODO: Currently this fails as the NMEA string is only 5 chars long
	 * Awaiting updated marineapi
	 */
	
	@Test
	public void shouldHandleTempData(){
		try{
			//empty = new BVEParser(TalkerId.GP);
			BVEParser bve = new BVEParser("$PBVE,EFOIADOIACABABAAAACAAACMABCFJKAADNAIAE");
			assertTrue(!bve.isFuelGuage());
			assertTrue(!bve.isEngineRpm());
			assertTrue(!bve.isPressureGuage());
			assertTrue(bve.isTempGuage());
			//public int getTempUnitsOfMeasure();
			
			assertEquals(32,bve.getLowTempAlarmValue());
			assertEquals(300, bve.getHighTempAlarmValue());
			assertEquals(154, bve.getEngineTemp());
			assertEquals(21.09, bve.getVoltage(),0.001);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	/**
	 * OP30/OP60 digital oil pressure gauge sample NMEA sentence:

$PBVE,DEDLAEOIACABABAAAACJAAJAABOGMLAADNAIPG

Parse as:
$PBVE, D  E  DLAE  OIAC  AB  AB  AA  AA  CJAA  JAAB  OG  MLAA  DNAI  PG

   D = Product Code, D = OP30/OP60
   E = Software Version #
DLAE = Oil Pressure Calibration Number 
OIAC = A2D gain 
  AB = Sender Type
  AB = Backlight Level
  AA = Units of Measure (0= psi, non-0= Bars)
  AA = Built-in Alarms Armed (AA=0 = NO)
CJAA = Low pressure alarm value
JAAB = High pressure alarm value
  OG = Checksum for Non-Volatile Memory
MLAA = Oil Pressure
DNAI = Sensor Volts
  PG = NMEA sentence checksum

Where A=0, B=1, C=2, ... , O=14, P=15

Decode Oil Pressure as:
MLAA = 16*M + L + 4096*A + 256*A
MLAA = 16*12 + 11 + 4096*0 +256*0
MLAA = 203 psi
	 */
	@Test
	public void shouldHandlePressureData(){
		try{
			//empty = new BVEParser(TalkerId.GP);
			BVEParser bve = new BVEParser("$PBVE,DEDLAEOIACABABAAAACJAAJAABOGMLAADNAIPG");
			assertTrue(!bve.isFuelGuage());
			assertTrue(!bve.isEngineRpm());
			assertTrue(bve.isPressureGuage());
			assertTrue(!bve.isTempGuage());

			assertEquals(41,bve.getLowPressureAlarmValue());
			assertEquals(400, bve.getHighPressureAlarmValue());
			assertEquals(203, bve.getPressure());
			assertEquals(0, bve.getPressureUnitsOfMeasure());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	/**
	 * RH30/RH60/RH110 Digital RPM/Engine hours gauge

$PBVE,BEAAADAAABFKBCIIBDAGOOABAAAAADAAAAOIACAAONFMAKCAADAAAAHG


Parse as:
$PBVE,  B  E  AA  ADAA  AB  FKBC  IIBD  AGOO  AB  AAAA  AD  AAAA  OIAC  AA  
ON  FMAK  CA  AD  AAAA  HG

   B = Product Code (B= RH30/RH60)
   E = Software Version
  AA = Spare NMV Byte (Ignore)
ADAA = Display Damping
  AB = Terminal C used as NMEA output (1) or as External Alarm Output (0)
FKBC = Maximum RPM seen from last reset
IIBD = High RPM Alarm value 
AGOO = Clock Speed Calibration #
  AB = Backlight Level
AAAA = Maintenance count-down alarm
  AD = Engine Minutes
AAAA = Engine Hours
OIAC = RPM Calibration number
  AA = Mode
  AN = Non voltatile memory checksum
FMAK = RPM
  CA = Elapsed Seconds
  AD = Elapsed Minutes
AAAA = Elapsed Hours
  HG = NMEA Sentence checksum

Where A=0, B=1, C=2, ... , O=14, P=15

Decode RPM as:
FMAK = 16*F + M + 4096*A + 256*K
FMAK = 16*5 + 12 + 4096*0 +256*10
FMAK = 2652 RPM
	 */
	@Test
	public void shouldHandleEngineData(){
		try{
			//empty = new BVEParser(TalkerId.GP);
			BVEParser bve = new BVEParser("$PBVE,BEAAADAAABFKBCIIBDAGOOABAAAAADAAAAOIACAAONFMAKCAADAAAAHG");
			assertTrue(!bve.isFuelGuage());
			assertTrue(bve.isEngineRpm());
			assertTrue(!bve.isPressureGuage());
			assertTrue(!bve.isTempGuage());

			assertEquals(4698,bve.getMaximumRpmSinceReset());
			assertEquals(5000, bve.getHighRpmAlarmValue());
			assertEquals(0, bve.getMaintenanceCountdownAlarm());
			assertEquals(0, bve.getEngineHours());
			assertEquals(3, bve.getEngineMinutes());
			assertEquals(2652, bve.getEngineRpm());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}

}
