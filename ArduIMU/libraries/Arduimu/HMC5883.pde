// -*- tab-width: 4; Mode: C++; c-basic-offset: 3; indent-tabs-mode: t -*-
/*
    AP_Compass_HMC5883L.cpp - Code based on Arduino Library for HMC5883L I2C magnetometer
 Code by Jordi Muñoz and Jose Julio. DIYDrones.com
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 Sensor is conected to I2C port
 Sensor is initialized in Continuos mode (10Hz)
 
 */
 
#if USE_MAGNETOMETER == 1

// AVR LibC Includes
#include <math.h>
//#include "WConstants.h"

#include <Wire.h>

#define COMPASS_ADDRESS      0x1E
#define ConfigRegA           0x00
#define ConfigRegB           0x01
#define ModeRegister         0x02
#define DataOutputXMSB       0x03
#define DataOutputXLSB       0x04
#define DataOutputZMSB       0x05
#define DataOutputZLSB       0x06
#define DataOutputYMSB       0x07
#define DataOutputYLSB       0x08
#define StatusRegister       0x09
#define IDRegisterA          0x0A
#define IDRegisterB          0x0B
#define IDRegisterC          0x0C

// default gain value
#define magGain              0x20

// ModeRegister valid modes
#define ContinuousConversion 0x00
#define SingleConversion     0x01

// ConfigRegA valid sample averaging
#define SampleAveraging_1    0x00
#define SampleAveraging_2    0x01
#define SampleAveraging_4    0x02
#define SampleAveraging_8    0x03

// ConfigRegA valid data output rates
#define DataOutputRate_0_75HZ 0x00
#define DataOutputRate_1_5HZ  0x01
#define DataOutputRate_3HZ    0x02
#define DataOutputRate_7_5HZ  0x03
#define DataOutputRate_15HZ   0x04
#define DataOutputRate_30HZ   0x05
#define DataOutputRate_75HZ   0x06

// ConfigRegA valid measurement configuration bits
#define NormalOperation      0x10
#define PositiveBiasConfig   0x11
#define NegativeBiasConfig   0x12

bool HMC5883_init()
{
  int success = 0;
  uint8_t aux_byte;

  Wire.begin();
  delay(10);

  //mag_offset[0] = 0;
  //mag_offset[1] = 0;
  //mag_offset[2] = 0;

  Wire.beginTransmission(COMPASS_ADDRESS);
  Wire.write((uint8_t)ConfigRegA);
  aux_byte = (SampleAveraging_8<<5 | DataOutputRate_75HZ<<2 | NormalOperation);
  Wire.write(aux_byte);
  Wire.endTransmission();
  delay(50);

  Wire.beginTransmission(COMPASS_ADDRESS);
  Wire.write((uint8_t)ModeRegister);
  Wire.write((uint8_t)ContinuousConversion);        // Set continuous mode (default to 10Hz)
  Wire.endTransmission();                 // End transmission
  delay(50);
  
  return(1);
}

// set mag offsets
void HMC5883_set_offset(int offsetx, int offsety, int offsetz)
{
  mag_offset[0] = offsetx;
  mag_offset[1] = offsety;
  mag_offset[2] = offsetz;
}

// Read Sensor data in chip axis
void HMC5883_read()
{
  int i = 0;
  byte buff[6];

  Wire.beginTransmission(COMPASS_ADDRESS);
  Wire.write(0x03);        //sends address to read from
  Wire.endTransmission(); //end transmission

    //Wire.beginTransmission(COMPASS_ADDRESS);
  Wire.requestFrom(COMPASS_ADDRESS, 6);    // request 6 bytes from device
  while(Wire.available())
  {
    buff[i] = Wire.read();  // receive one byte
    i++;
  }
  Wire.endTransmission(); //end transmission

  if (i==6){  // All bytes received?
    // MSB byte first, then LSB
    mag_x = ((((int)buff[0]) << 8) | buff[1])*SENSOR_SIGN[6] + mag_offset[0];    // X axis
    mag_y = ((((int)buff[4]) << 8) | buff[5])*SENSOR_SIGN[7] + mag_offset[1];    // Y axis
    mag_z = ((((int)buff[2]) << 8) | buff[3])*SENSOR_SIGN[8] + mag_offset[2];    // Z axis
  }
}

void HMC5883_calculate(float roll, float pitch)
{
  float Head_X;
  float Head_Y;
  float cos_roll;
  float sin_roll;
  float cos_pitch;
  float sin_pitch;
  
  cos_roll = cos(roll);
  sin_roll = sin(roll);
  cos_pitch = cos(pitch);
  sin_pitch = sin(pitch);
  
  // Tilt compensated Magnetic field X component:
  Head_X = mag_x*cos_pitch+mag_y*sin_roll*sin_pitch+mag_z*cos_roll*sin_pitch;
  // Tilt compensated Magnetic field Y component:
  Head_Y = mag_y*cos_roll-mag_z*sin_roll;
  // Magnetic Heading
  Heading = atan2(-Head_Y,Head_X);
  
  // Declination correction (if supplied)
  if( MAGNETIC_DECLINATION != 0 ) 
  {
      Heading = Heading + ToRad(MAGNETIC_DECLINATION);
      if (Heading > M_PI)    // Angle normalization (-180 deg, 180 deg)
          Heading -= (2.0 * M_PI);
      else if (Heading < -M_PI)
          Heading += (2.0 * M_PI);
  }
	
  // Optimization for external DCM use. Calculate normalized components
  Heading_X = cos(Heading);
  Heading_Y = sin(Heading);
}

#endif
