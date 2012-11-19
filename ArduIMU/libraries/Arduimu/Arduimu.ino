// Released under Creative Commons License 
// Code by Jordi Munoz and William Premerlani, Supported by Chris Anderson and Doug Weibel
// Version 1.0 for flat board updated by Doug Weibel, Jose Julio and Ahmad Byagowi
// Version 1.7 includes support for SCP1000 absolute pressure sensor
// Version 1.8 uses DIYDrones GPS, FastSerial, and Compass libraries
// Version 1.9 Support for ArduIMU V3 Hardware with MPU6000 and HMC5883 magnetometer (SCP1000 absolute pressure sensor is not supported in this version)
// Version 1.9.6 Works on Arduino 1.0.1 IDE
// Version 1.9.7 Roll and Patch are Calibrates

// Axis definition: X axis pointing forward, Y axis pointing to the right and Z axis pointing down.
// Positive pitch : nose up                                                                                   
// Positive roll : right wing down
// Positive yaw : clockwise

#include <avr/eeprom.h>
#include <Wire.h>
#include <FastSerial.h>		// ArduPilot Fast Serial Library
#include <AP_GPS.h>			// ArduPilot GPS library


//**********************************************************************
//  This section contains USER PARAMETERS !!!
//
//**********************************************************************

// *** NOTE!   Hardware version - Can be used for v1 (daughterboards) , v2 (flat) or new v3 (MPU6000)
#define BOARD_VERSION 3 // 1 For V1 and 2 for V2 and 3 for new V3

#if BOARD_VERSION == 3
#include "MPU6000.h"
#endif

#define GPS_CONNECTION 0 // 0 for GPS pins, 1 for programming pins

// GPS Type Selection - Note Ublox or MediaTek is recommended.  Support for NMEA is limited.
#define GPS_PROTOCOL 4    // 1 - NMEA,  2 - EM406,  3 - Ublox, 4 -- MediaTek  

// Enable Air Start uses Remove Before Fly flag - connection to pin 6 on ArduPilot 
#define ENABLE_AIR_START 0  //  1 if using airstart/groundstart signaling, 0 if not
#define GROUNDSTART_PIN 8    //  Pin number used for ground start signal (recommend 10 on v1 and 8 on v2 hardware)

/*Min Speed Filter for Yaw drift Correction*/
#define SPEEDFILT 2 // >1 use min speed filter for yaw drift cancellation (m/s), 0=do not use speed filter

/*For debugging propurses*/
#define PRINT_DEBUG 1   //Will print Debug messages

//OUTPUTMODE=1 will print the corrected data, 0 will print uncorrected data of the gyros (with drift), 2 will print accelerometer only data
#define OUTPUTMODE 1

#define PRINT_DCM 1     //Will print the whole direction cosine matrix
#define PRINT_ANALOGS 1 //Will print the analog raw data
#define PRINT_EULER 1   //Will print the Euler angles Roll, Pitch and Yaw
#define PRINT_GPS 1     //Will print GPS data
#define PRINT_MAGNETOMETER 1     //Will print Magnetometer data (if magnetometer is enabled)

// *** NOTE!   To use ArduIMU with ArduPilot you must select binary output messages (change to 1 here)
#define PRINT_BINARY 0  //Will print binary message and suppress ASCII messages (above)

// *** NOTE!   Performance reporting is only supported for Ublox.  Set to 0 for others
#define PERFORMANCE_REPORTING 1  //Will include performance reports in the binary output ~ 1/2 min

/* Support for optional magnetometer (1 enabled, 0 dissabled) */
#define USE_MAGNETOMETER 1 // use 1 if you want to make yaw gyro drift corrections using the optional magnetometer   
 
// Local magnetic declination (in degrees)
// I use this web : http://www.ngdc.noaa.gov/geomagmodels/Declination.jsp
#define MAGNETIC_DECLINATION -6.0    // corrects magnetic bearing to true north
// Magnetometer OFFSETS (magnetometer calibration) (only for ArduIMU v3)
#define MAG_OFFSET_X 0
#define MAG_OFFSET_Y 0
#define MAG_OFFSET_Z 0

/* Support for optional barometer (1 enabled, 0 dissabled) */
#define USE_BAROMETER 0 	// use 1 if you want to get altitude using the optional absolute pressure sensor                  
#define ALT_MIX	50			// For binary messages: GPS or barometric altitude.  0 to 100 = % of barometric.  For example 75 gives 25% GPS alt and 75% baro

//**********************************************************************
//  End of user parameters
//**********************************************************************

#define SOFTWARE_VER "1.9"

// GPS Selection
FastSerialPort0(Serial);		// Instantiate the fast serial driver
#if   GPS_PROTOCOL == 1
AP_GPS_NMEA		GPS(&Serial);
#elif GPS_PROTOCOL == 2
AP_GPS_406		GPS(&Serial);
#elif GPS_PROTOCOL == 3
AP_GPS_UBLOX	GPS(&Serial);
#elif GPS_PROTOCOL == 4
AP_GPS_MTK		GPS(&Serial);
#else
# error Must define GPS_PROTOCOL with a valid value.
#endif

#define ToRad(x) (x*0.01745329252)  // *pi/180
#define ToDeg(x) (x*57.2957795131)  // *180/pi

#if BOARD_VERSION < 3
#define SERIAL_MUX_PIN 2
#define RED_LED_PIN 5
#define BLUE_LED_PIN 6
#define YELLOW_LED_PIN 7
// ADC : Voltage reference 3.3v / 10bits(1024 steps) => 3.22mV/ADC step
// ADXL335 Sensitivity(from datasheet) => 330mV/g, 3.22mV/ADC step => 330/3.22 = 102.48
// Tested value : 101
#define GRAVITY 101 //this equivalent to 1G in the raw data coming from the accelerometer 
#define Accel_Scale(x) x*(GRAVITY/9.81)//Scaling the raw data of the accel to actual acceleration in meters for seconds square

// LPR530 & LY530 Sensitivity (from datasheet) => 3.33mV/º/s, 3.22mV/ADC step => 1.03
// Tested values : 0.96,0.96,0.94
#define Gyro_Gain_X 0.92 //X axis Gyro gain
#define Gyro_Gain_Y 0.92 //Y axis Gyro gain
#define Gyro_Gain_Z 0.94 //Z axis Gyro gain
#define Gyro_Scaled_X(x) x*ToRad(Gyro_Gain_X) //Return the scaled ADC raw data of the gyro in radians for second
#define Gyro_Scaled_Y(x) x*ToRad(Gyro_Gain_Y) //Return the scaled ADC raw data of the gyro in radians for second
#define Gyro_Scaled_Z(x) x*ToRad(Gyro_Gain_Z) //Return the scaled ADC raw data of the gyro in radians for second
#endif

#if BOARD_VERSION == 3
#define SERIAL_MUX_PIN 7
#define RED_LED_PIN 5
#define BLUE_LED_PIN 6
#define YELLOW_LED_PIN 5   // Yellow led is not used on ArduIMU v3
// MPU6000 4g range => g = 4096
#define GRAVITY 4096  // This equivalent to 1G in the raw data coming from the accelerometer 
#define Accel_Scale(x) x*(GRAVITY/9.81)//Scaling the raw data of the accel to actual acceleration in meters for seconds square

// MPU6000 sensibility  (theorical 0.0152 => 1/65.6LSB/deg/s at 500deg/s) (theorical 0.0305 => 1/32.8LSB/deg/s at 1000deg/s) ( 0.0609 => 1/16.4LSB/deg/s at 2000deg/s)
#define Gyro_Gain_X 0.0609
#define Gyro_Gain_Y 0.0609
#define Gyro_Gain_Z 0.0609
#define Gyro_Scaled_X(x) x*ToRad(Gyro_Gain_X) //Return the scaled ADC raw data of the gyro in radians for second
#define Gyro_Scaled_Y(x) x*ToRad(Gyro_Gain_Y) //Return the scaled ADC raw data of the gyro in radians for second
#define Gyro_Scaled_Z(x) x*ToRad(Gyro_Gain_Z) //Return the scaled ADC raw data of the gyro in radians for second
#endif


#define Kp_ROLLPITCH 0.015
#define Ki_ROLLPITCH 0.000010
#define Kp_YAW 1.2
//#define Kp_YAW 2.5      //High yaw drift correction gain - use with caution!
#define Ki_YAW 0.00005

#define ADC_WARM_CYCLES 75

#define FALSE 0
#define TRUE 1

float G_Dt=0.02;    // Integration time (DCM algorithm)

long timeNow=0; // Hold the milliseond value for now
long timer=0;   //general purpuse timer
long timer_old;
long timer24=0; //Second timer used to print values 
boolean groundstartDone = false;    // Used to not repeat ground start

float AN[8]; //array that store the 6 ADC filtered data
float AN_OFFSET[8]; //Array that stores the Offset of the gyros

float Accel_Vector[3]= {0,0,0}; //Store the acceleration in a vector
float Gyro_Vector[3]= {0,0,0};//Store the gyros rutn rate in a vector
float Omega_Vector[3]= {0,0,0}; //Corrected Gyro_Vector data
float Omega_P[3]= {0,0,0};//Omega Proportional correction
float Omega_I[3]= {0,0,0};//Omega Integrator
float Omega[3]= {0,0,0};

// Euler angles
float roll;
float pitch;
float yaw;

int toggleMode=0;

float errorRollPitch[3]= {0,0,0}; 
float errorYaw[3]= {0,0,0};
float errorCourse=180; 
float COGX=0; //Course overground X axis
float COGY=1; //Course overground Y axis

unsigned int cycleCount=0;
byte gyro_sat=0;

float DCM_Matrix[3][3]= {
  {
    1,0,0  }
  ,{
    0,1,0  }
  ,{
    0,0,1  }
}; 
float Update_Matrix[3][3]={{0,1,2},{3,4,5},{6,7,8}}; //Gyros here

float Temporary_Matrix[3][3]={
  {
    0,0,0  }
  ,{
    0,0,0  }
  ,{
    0,0,0  }
};
 
// Startup GPS variables
int gps_fix_count = 5;		//used to count 5 good fixes at ground startup

//ADC variables
volatile uint8_t MuxSel=0;
volatile uint8_t analog_reference = DEFAULT;
volatile uint16_t analog_buffer[8];
volatile uint8_t analog_count[8];


 #if BOARD_VERSION == 1
  uint8_t sensors[6] = {0,2,1,3,5,4};   // Use these two lines for Hardware v1 (w/ daughterboards)
  int SENSOR_SIGN[]= {1,-1,1,-1,1,-1,-1,-1,-1};  //Sensor: GYROX, GYROY, GYROZ, ACCELX, ACCELY, ACCELZ
 #endif
 
 #if BOARD_VERSION == 2
  uint8_t sensors[6] = {6,7,3,0,1,2};  // For Hardware v2 flat
  int SENSOR_SIGN[] = {1,-1,-1,1,-1,1,-1,-1,-1};
 #endif
 
 #if BOARD_VERSION == 3
  uint8_t sensors[6] = {0,1,2,3,4,5};  // For Hardware v3 (MPU6000)
  int SENSOR_SIGN[] = {1,-1,-1,-1,1,1,-1,1,-1};
 #endif
 
 
 // Performance Monitoring variables
 // Data collected and reported for ~1/2 minute intervals
 //#if PERFORMANCE_REPORTING == 1
 int mainLoop_count = 0;              //Main loop cycles since last report
 int G_Dt_max = 0.0;                  //Max main loop cycle time in milliseconds
 byte gyro_sat_count = 0;
 byte adc_constraints = 0;
 byte renorm_sqrt_count = 0;
 byte renorm_blowup_count = 0;
 byte gps_messages_sent = 0;
 long perf_mon_timer = 0;
 //#endif
 unsigned int imu_health = 65012;
 
 #if USE_MAGNETOMETER==1
 // Magnetometer variables definition
 #if BOARD_VERSION < 3
 APM_Compass_Class APM_Compass;
 #endif
 int mag_x;
 int mag_y;
 int mag_z;
 int mag_offset[3];
 float Heading;
 float Heading_X;
 float Heading_Y;
 #endif
//*****************************************************************************************
void setup()
{ 
  Serial.begin(38400, 128, 16);
  pinMode(SERIAL_MUX_PIN,OUTPUT); //Serial Mux
  if (GPS_CONNECTION == 0){
    digitalWrite(SERIAL_MUX_PIN,HIGH); //Serial Mux
  } else {
    digitalWrite(SERIAL_MUX_PIN,LOW); //Serial Mux
  }

  pinMode(RED_LED_PIN,OUTPUT); //Red LED
  pinMode(BLUE_LED_PIN,OUTPUT); // Blue LED
  pinMode(YELLOW_LED_PIN,OUTPUT); // Yellow LED
  pinMode(GROUNDSTART_PIN,INPUT);  // Remove Before Fly flag (pin 6 on ArduPilot)
  digitalWrite(GROUNDSTART_PIN,HIGH);
  
  #if BOARD_VERSION == 3
  MPU6000_Init();       // MPU6000 initialization
  #endif
  
  digitalWrite(RED_LED_PIN,HIGH);
  delay(500);
  digitalWrite(BLUE_LED_PIN,HIGH);
  delay(500);
  digitalWrite(YELLOW_LED_PIN,HIGH);
  delay(500);
  digitalWrite(RED_LED_PIN,LOW);
  delay(500);
  digitalWrite(BLUE_LED_PIN,LOW);
  delay(500);
  digitalWrite(YELLOW_LED_PIN,LOW);
  
  #if BOARD_VERSION < 3
  Analog_Reference(EXTERNAL);//Using external analog reference
  Analog_Init();
  #endif
  
  debug_print("Welcome...");
  
  #if BOARD_VERSION == 1
  debug_print("You are using Hardware Version 1...");
  #endif 
 
  #if BOARD_VERSION == 2
  debug_print("You are using Hardware Version 2...");
  #endif 
  
  GPS.init();			// GPS Initialization
  
  debug_handler(0);		//Printing version
  
  #if USE_MAGNETOMETER == 1
    #if BOARD_VERSION < 3       // Support for old magnetometer (HMC5843) on ArduIMU v2
      APM_Compass.Init();	// I2C initialization
      APM_Compass.SetOrientation(APM_COMPASS_COMPONENTS_UP_PINS_RIGHT);  // Orientation for magnetometer soldered to main board
    #endif
    #if BOARD_VERSION == 3
      HMC5883_init();
      HMC5883_set_offset(MAG_OFFSET_X,MAG_OFFSET_Y,MAG_OFFSET_Z);
    #endif
      debug_handler(3);
  #endif

  if(ENABLE_AIR_START){
      debug_handler(1);
      startup_air();
  }else{
      debug_handler(2);
      startup_ground();
  }
 
  
  delay(250);
    
  Read_adc_raw();     // ADC initialization
  timer=millis();
  delay(20);
  
}

//***************************************************************************************
void loop() //Main Loop
{
  timeNow = millis();
 
  if((timeNow-timer)>=20)  // Main loop runs at 50Hz
  {
    timer_old = timer;
    timer = timeNow;
	
#if PERFORMANCE_REPORTING == 1
    mainLoop_count++;
    if (timer-timer_old > G_Dt_max) G_Dt_max = timer-timer_old;
#endif

    G_Dt = (timer-timer_old)/1000.0;    // Real time of loop run. We use this on the DCM algorithm (gyro integration time)
    if(G_Dt > 1)
        G_Dt = 0;  //Something is wrong - keeps dt from blowing up, goes to zero to keep gyros from departing
    
    // *** DCM algorithm
   
    Read_adc_raw();

    Matrix_update(); 

    Normalize();

    Drift_correction();
   
    Euler_angles();
    
    //Serial.print(timer-timer_old);
    //Serial.print("\t");
    //Serial.print(ToDeg(roll));
    //Serial.print("\t");
    //Serial.print(ToDeg(pitch));
    //Serial.print("\t");
    //Serial.print(ToDeg(yaw));
    //Serial.println();
    
    #if PRINT_BINARY == 1
      printdata(); //Send info via serial
    #endif

    //Turn on the LED when you saturate any of the gyros.
    if((abs(Gyro_Vector[0])>=ToRad(300))||(abs(Gyro_Vector[1])>=ToRad(300))||(abs(Gyro_Vector[2])>=ToRad(300)))
    {
      gyro_sat=1;
#if PERFORMANCE_REPORTING == 1
      gyro_sat_count++;
#endif
      digitalWrite(RED_LED_PIN,HIGH);  
    }
    
	cycleCount++;

        // Do these things every 6th time through the main cycle 
        // This section gets called every 1000/(20*6) = 8 1/3 Hz
        // doing it this way removes the need for another 'millis()' call
		// and balances the processing load across main loop cycles.
		switch (cycleCount) {
			case(0):
				GPS.update();
				break;
				
			case(1):
				//Here we will check if we are getting a signal to ground start
				if(digitalRead(GROUNDSTART_PIN) == LOW && groundstartDone == false) 
					startup_ground();
				break;
				
			case(2):
				
				break;
				
			case(3):
				#if USE_MAGNETOMETER==1
                                  #if BOARD_VERSION < 3
				    APM_Compass.Read();     // Read magnetometer
				    APM_Compass.Calculate(roll,pitch);  // Calculate heading 
                                  #endif
                                  #if BOARD_VERSION == 3
                                    HMC5883_read();                   // Read magnetometer
                                    HMC5883_calculate(roll, pitch);   // Calculate heading 
                                  #endif
				#endif
				break;
			
			case(4):
				// Display Status on LEDs
				// GYRO Saturation indication
				if(gyro_sat>=1) {
					digitalWrite(RED_LED_PIN,HIGH); //Turn Red LED when gyro is saturated. 
					if(gyro_sat>=8)  // keep the LED on for 8/10ths of a second
						gyro_sat=0;
					else
						gyro_sat++;
				} else {
					digitalWrite(RED_LED_PIN,LOW);
				}
      
				// YAW drift correction indication
				if(GPS.ground_speed<SPEEDFILT*100) {
					digitalWrite(YELLOW_LED_PIN,HIGH);    //  Turn on yellow LED if speed too slow and yaw correction supressed
				} else {
					digitalWrite(YELLOW_LED_PIN,LOW);
				}
      
				// GPS Fix indication
                                switch (GPS.status()) {
                                        case(2):
					      digitalWrite(BLUE_LED_PIN,HIGH);  //Turn Blue LED when gps is fixed. 
                                              break;
                                              
                                        case(1):
                                              if (GPS.valid_read == true){
                                                    toggleMode = abs(toggleMode-1); // Toggle blue light on and off to indicate NMEA sentences exist, but no GPS fix lock
                                                    if (toggleMode==0){
                                                          digitalWrite(BLUE_LED_PIN, LOW); // Blue light off
                                                    } else {
                                                          digitalWrite(BLUE_LED_PIN, HIGH); // Blue light on
                                                    }
                                                    GPS.valid_read = false;
                                              }
                                              break;
                                              
                                        default:
                                              digitalWrite(BLUE_LED_PIN,LOW);
                                              break;
				}
				break;
				
			case(5):
                                
				cycleCount = -1;
		// Reset case counter, will be incremented to zero before switch statement
				#if !PRINT_BINARY
					printdata(); //Send info via serial
				#endif
				break;
		}
     
  
#if PERFORMANCE_REPORTING == 1
    if (timeNow-perf_mon_timer > 20000) 
    {
      printPerfData(timeNow-perf_mon_timer);
      perf_mon_timer=timeNow;
    }
#endif

  }

}

//********************************************************************************
void startup_ground(void)
{
	uint16_t store=0;
	int flashcount = 0;
 
	debug_handler(2);
	for(int c=0; c<ADC_WARM_CYCLES; c++)
	{ 
		digitalWrite(YELLOW_LED_PIN,LOW);
		digitalWrite(BLUE_LED_PIN,HIGH);
		digitalWrite(RED_LED_PIN,LOW);
		delay(50);
		Read_adc_raw();
		digitalWrite(YELLOW_LED_PIN,HIGH);
		digitalWrite(BLUE_LED_PIN,LOW);
		digitalWrite(RED_LED_PIN,HIGH);
		delay(50);
	}
  
	Read_adc_raw();
	delay(20);
	Read_adc_raw();
	for(int y=0; y<=5; y++)   // Read first initial ADC values for offset.
		AN_OFFSET[y]=AN[y];

	for(int i=0;i<400;i++)    // We take some readings...
	{
		Read_adc_raw();
		for(int y=0; y<=5; y++)   // Read initial ADC values for offset (averaging).
			AN_OFFSET[y]=AN_OFFSET[y]*0.8 + AN[y]*0.2;
		delay(20);
		if(flashcount == 5) {
			digitalWrite(YELLOW_LED_PIN,LOW);
			digitalWrite(BLUE_LED_PIN,HIGH);
			digitalWrite(RED_LED_PIN,LOW);
		}
		if(flashcount >= 10) {
			flashcount = 0;

			digitalWrite(YELLOW_LED_PIN,HIGH);
			digitalWrite(BLUE_LED_PIN,LOW);
			digitalWrite(RED_LED_PIN,HIGH);
		}
		flashcount++;
		
        }
	digitalWrite(RED_LED_PIN,LOW);
	digitalWrite(BLUE_LED_PIN,LOW);
	digitalWrite(YELLOW_LED_PIN,LOW);
	
	AN_OFFSET[5]-=GRAVITY*SENSOR_SIGN[5];
  
	for(int y=0; y<=5; y++)
	{
		Serial.println(AN_OFFSET[y]);
                #if BOARD_VERSION < 3
		store = ((AN_OFFSET[y]-200.f)*100.0f);
                #endif
                #if BOARD_VERSION == 3
                store = AN_OFFSET[y];
                #endif
		eeprom_busy_wait();
		eeprom_write_word((uint16_t *)	(y*2+2), store);	
	}

	while (gps_fix_count > 0 && USE_BAROMETER) {
		GPS.update();
//  Serial.print(gpsFix);
//  Serial.print(", ");
//  Serial.println(gpsFixnew);
		if (GPS.fix == 1 && GPS.new_data == 1) {
			GPS.new_data = 0;
			gps_fix_count--;
		}
	}

	groundstartDone = true;
	debug_handler(6);
}

//************************************************************************************
void startup_air(void)
{
  uint16_t temp=0;

  for(int y=0; y<=5; y++)
  {
    eeprom_busy_wait();
    temp = eeprom_read_word((uint16_t *)	(y*2+2));
    #if BOARD_VERSION < 3
    AN_OFFSET[y] = temp/100.f+200.f;
    #endif
    #if BOARD_VERSION == 3
    AN_OFFSET[y] = temp;
    #endif	
    Serial.println(AN_OFFSET[y]);
  }
  Serial.println("***Air Start complete");
}    


void debug_print(char string[])
{
  #if PRINT_DEBUG != 0 
  Serial.print("???");
  Serial.print(string);
  Serial.println("***");
  #endif
}

void debug_handler(byte message)
{
  #if PRINT_DEBUG != 0 
  
  static unsigned long BAD_Checksum=0;
  
	switch(message) 
	{
		case 0:
		Serial.print("???Software Version ");
		Serial.print(SOFTWARE_VER);
		Serial.println("***");
		break;
      
		case 1:
		Serial.println("???Air Start!***");
		break;
      
		case 2:
		Serial.println("???Ground Start!***");
		break;      
      
		case 3:
		Serial.println("???Enabling Magneto...***");
		break;  
	  
		case 4:
		Serial.println("???Enabling Pressure Altitude...***");
		break;         
	  
		case 5:
		Serial.println("???Air Start complete");	
		break;     
	  
		case 6:
		Serial.println("???Ground Start complete");	
		break;
     
		case 10:
		BAD_Checksum++;
		Serial.print("???GPS Bad Checksum: "); 
		Serial.print(BAD_Checksum);
		Serial.println("...***");
		break;
      
		default:
		Serial.println("???Invalid debug ID...***");
		break;
   
	}
	#endif
  
}
   
/*
EEPROM memory map

0 0x00		Unused
1 0x01 		..
2 0x02 		AN_OFFSET[0]
3 0x03 		..
4 0x04 		AN_OFFSET[1]
5 0x05 		..
6 0x06 		AN_OFFSET[2]
7 0x07 		..
8 0x08 		AN_OFFSET[3]
9 0x09 		..
10 0x0A		AN_OFFSET[4]
11 0x0B		..
12 0x0C		AN_OFFSET[5]
13 0x0D		..	
14 0x0E		Unused
15 0x0F		..

*/
