/**
 * FreeIMU library serial communication protocol
*/

#include <ADXL345.h>
#include <bma180.h>
#include <HMC58X3.h>
#include <ITG3200.h>
#include <MS561101BA.h>
#include <I2Cdev.h>
#include <MPU60X0.h>
#include <EEPROM.h>
#include <FlexiTimer2.h>
#include <AverageList.h>

//#define DEBUG
#include "DebugUtils.h"
#include "CommunicationUtils.h"
#include "FreeIMU.h"
#include <Wire.h>
#include <SPI.h>

volatile boolean execute = false;
volatile int interval = 0;
float q[4];
float qm[7];
//float m[3];
int raw_values[11];
float ypr[3]; // yaw pitch roll
float yprm[4]; // yaw, pitch, roll, mag heading
char str[256];
float val[9];

typedef volatile float rval; //change float to the datatype you want to use
const byte MAX_NUMBER_OF_READINGS = 5;
rval mghStorage[MAX_NUMBER_OF_READINGS] = {0.0};
AverageList<rval> mghList = AverageList<rval>( mghStorage, MAX_NUMBER_OF_READINGS );

// Set the FreeIMU object
FreeIMU my3IMU = FreeIMU();
//The command from the PC
char cmd;

/*
 * Timer interrupt driven method to do time sensitive calculations
 * The calc flag causes the main loop to execute other less sensitive calls.
 */
void calculate() {
	//we create 100ms pings here
	execute = true;
	//we record the ping count out to 2 secs
	interval++;
	interval = interval % 20;
}

void setup() {
  Serial.begin(38400);
  Wire.begin();
  
  my3IMU.init(true);
  
  //setup timers
  FlexiTimer2::set(100, calculate); // 100ms period
  FlexiTimer2::start();
  // LED
  pinMode(13, OUTPUT);
  mghList.reset();
}


void loop() {
  if(Serial.available()) {
    cmd = Serial.read();
    //version
    if(cmd=='v') {
      sprintf(str, "FreeIMU library by %s, FREQ:%s, LIB_VERSION: %s, IMU: %s", FREEIMU_DEVELOPER, FREEIMU_FREQ, FREEIMU_LIB_VERSION, FREEIMU_ID);
      Serial.print(str);
      Serial.print('\n');
    }
    //used for calibration
    else if(cmd=='b') {
      uint8_t count = serial_busy_wait();
      for(uint8_t i=0; i<count; i++) {
        #if HAS_ITG3200()
          my3IMU.acc.readAccel(&raw_values[0], &raw_values[1], &raw_values[2]);
          my3IMU.gyro.readGyroRaw(&raw_values[3], &raw_values[4], &raw_values[5]);
        #else // MPU6050
          my3IMU.accgyro.getMotion6(&raw_values[0], &raw_values[1], &raw_values[2], &raw_values[3], &raw_values[4], &raw_values[5]);
        #endif
        writeArr(raw_values, 6, sizeof(int)); // writes accelerometer and gyro values
        #if IS_9DOM()
          my3IMU.magn.getValues(&raw_values[0], &raw_values[1], &raw_values[2]);
          writeArr(raw_values, 3, sizeof(int));
        #endif
        Serial.println();
      }
    }
    //processing gui
    else if(cmd == 'q') {
      uint8_t count = serial_busy_wait();
      for(uint8_t i=0; i<count; i++) {
        my3IMU.getQ(q);
        qm[0]=q[0];
        qm[1]=q[1];
        qm[2]=q[2];
        qm[3]=q[3];
        my3IMU.getValues(val);
        qm[4]=val[6];
        qm[5]=val[7];
        qm[6]=val[8];
        //convert to YPR
        //my3IMU.getYawPitchRoll(yprm);
        //convert to magnetic heading
        //calcMagHeading();
        serialPrintFloatArr(qm, 7);
        Serial.println("");
      }
    }
   //write eeprom
    else if(cmd == 'c') {
      const uint8_t eepromsize = sizeof(float) * 6 + sizeof(int) * 6;
      while(Serial.available() < eepromsize) ; // wait until all calibration data are received
      EEPROM.write(FREEIMU_EEPROM_BASE, FREEIMU_EEPROM_SIGNATURE);
      for(uint8_t i = 1; i<(eepromsize + 1); i++) {
        EEPROM.write(FREEIMU_EEPROM_BASE + i, (char) Serial.read());
      }
      //my3IMU.calLoad(); // reload calibration
      // toggle LED after calibration store.
      digitalWrite(13, HIGH);
      delay(1000);
      digitalWrite(13, LOW);
    }
    else if(cmd == 'C') { // check calibration values
    //my3IMU.magn.calibrate(1,75);
      Serial.print("acc offset: ");
      Serial.print(my3IMU.acc_off_x);
      Serial.print(",");
      Serial.print(my3IMU.acc_off_y);
      Serial.print(",");
      Serial.print(my3IMU.acc_off_z);
      Serial.print("\n");
      
      Serial.print("magn offset: ");
      Serial.print(my3IMU.magn_off_x);
      Serial.print(",");
      Serial.print(my3IMU.magn_off_y);
      Serial.print(",");
      Serial.print(my3IMU.magn_off_z);
      Serial.print("\n");
      
      Serial.print("acc scale: ");
      Serial.print(my3IMU.acc_scale_x);
      Serial.print(",");
      Serial.print(my3IMU.acc_scale_y);
      Serial.print(",");
      Serial.print(my3IMU.acc_scale_z);
      Serial.print("\n");
      
      Serial.print("magn scale: ");
      Serial.print(my3IMU.magn_scale_x);
      Serial.print(",");
      Serial.print(my3IMU.magn_scale_y);
      Serial.print(",");
      Serial.print(my3IMU.magn_scale_z);
      Serial.print("\n");
    }
    else if(cmd == 'd') { // debugging outputs
      while(1) {
        my3IMU.getRawValues(raw_values);
        sprintf(str, "%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,", raw_values[0], raw_values[1], raw_values[2], raw_values[3], raw_values[4], raw_values[5], raw_values[6], raw_values[7], raw_values[8], raw_values[9], raw_values[10]);
        Serial.print(str);
        Serial.print('\n');
        my3IMU.getQ(q);
        serialPrintFloatArr(q, 4);
        Serial.println("");
        my3IMU.getYawPitchRoll(ypr);
        Serial.print("Yaw: ");
        Serial.print(ypr[0]);
        Serial.print(" Pitch: ");
        Serial.print(ypr[1]);
        Serial.print(" Roll: ");
        Serial.print(ypr[2]);
        Serial.println("");
      }
    }
  }
  if (execute) {
    //do these every 100ms
    //quarternary, updates AHRS
    my3IMU.getQ(q);
    if (interval % 2 == 0) {
      // do every 200ms
          //mag 
          my3IMU.getValues(val);
       
          //convert to YPR
          my3IMU.getYawPitchRollRad(yprm);
          //convert to magnetic heading
          calcMagHeading();
          mghList.addValue(yprm[3]);
    }
    if (interval % 5 == 0) {
	//do every 500ms

         //ArduIMU output format
         //!!!VER:1.9,RLL:-0.52,PCH:0.06,YAW:80.24,IMUH:253,MGX:44,MGY:-254,MGZ:-257,MGH:80.11,LAT:-412937350,LON:1732472000,ALT:14,COG:116,SOG:0,FIX:1,SAT:5,TOW:22504700***
 
          Serial.print("!!!VER:1.9,");
          Serial.print("MGH:");
          float h = degrees(mghList.getTotalAverage());
          if(h<0.0){
            Serial.print(360.0+h);
          }else{
            Serial.print(h);
          }
          Serial.print(",YAW:");
          Serial.print(degrees(yprm[0]));
          Serial.print(",PCH:");
          Serial.print(degrees(yprm[1]));
          Serial.print(",RLL:");
          Serial.print(degrees(yprm[2]));
          Serial.println("***");
          
        }
        execute = false;
  }
    
  
}

void calcMagHeading(){
  float Head_X;
  float Head_Y;
  float cos_roll;
  float sin_roll;
  float cos_pitch;
  float sin_pitch;
  
  cos_roll = cos(-yprm[2]);
  sin_roll = sin(-yprm[2]);
  cos_pitch = cos(yprm[1]);
  sin_pitch = sin(yprm[1]);
  
  //Example calc
  //Xh = bx * cos(theta) + by * sin(phi) * sin(theta) + bz * cos(phi) * sin(theta)
  //Yh = by * cos(phi) - bz * sin(phi)
  //return wrap((atan2(-Yh, Xh) + variation))
    
  // Tilt compensated Magnetic field X component:
  Head_X = val[6]*cos_pitch+val[7]*sin_roll*sin_pitch+val[8]*cos_roll*sin_pitch;
  // Tilt compensated Magnetic field Y component:
  Head_Y = val[7]*cos_roll-val[8]*sin_roll;
  // Magnetic Heading
  yprm[3] = atan2(-Head_Y,-Head_X);
  
}

char serial_busy_wait() {
  while(!Serial.available()) {
    ; // do nothing until ready
  }
  return Serial.read();
}

const int EEPROM_MIN_ADDR = 0;
const int EEPROM_MAX_ADDR = 511;

void eeprom_serial_dump_column() {
  // counter
  int i;

  // byte read from eeprom
  byte b;

  // buffer used by sprintf
  char buf[10];

  for (i = EEPROM_MIN_ADDR; i <= EEPROM_MAX_ADDR; i++) {
    b = EEPROM.read(i);
    sprintf(buf, "%03X: %02X", i, b);
    Serial.println(buf);
  }
}
