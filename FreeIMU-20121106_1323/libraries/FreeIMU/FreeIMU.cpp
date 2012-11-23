/*
FreeIMU.cpp - A libre and easy to use orientation sensing library for Arduino
Copyright (C) 2011-2012 Fabio Varesano <fabio at varesano dot net>

Development of this code has been supported by the Department of Computer Science,
Universita' degli Studi di Torino, Italy within the Piemonte Project
http://www.piemonte.di.unito.it/


This program is free software: you can redistribute it and/or modify
it under the terms of the version 3 GNU General Public License as
published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

#include <inttypes.h>
#include <stdint.h>
//#define DEBUG
#include "FreeIMU.h"
// #include "WireUtils.h"
#include "DebugUtils.h"

//#include "vector_math.h"

FreeIMU::FreeIMU() {
  #if HAS_ADXL345()
    acc = ADXL345();
  #elif HAS_BMA180()
    acc = BMA180();
  #endif
  
  #if HAS_HMC5883L()
    magn = HMC58X3();
  #endif
  
  #if HAS_ITG3200()
    gyro = ITG3200();
  #elif HAS_MPU6050()
    accgyro = MPU60X0(); // I2C
  #elif HAS_MPU6000()
    accgyro = MPU60X0(); // SPI for Arduimu v3
  #endif
    
  #if HAS_MS5611()
    baro = MS561101BA();
  #endif
  
  // initialize quaternion
  q0 = 1.0f;
  q1 = 0.0f;
  q2 = 0.0f;
  q3 = 0.0f;
  exInt = 0.0;
  eyInt = 0.0;
  ezInt = 0.0;
  twoKp = twoKpDef;
  twoKi = twoKiDef;
  integralFBx = 0.0f,  integralFBy = 0.0f, integralFBz = 0.0f;
  lastUpdate = 0;
  now = 0;
  
  // initialize scale factors to neutral values
  acc_scale_x = 1;
  acc_scale_y = 1;
  acc_scale_z = 1;
  magn_scale_x = 1;
  magn_scale_y = 1;
  magn_scale_z = 1;
}

void FreeIMU::init() {
  #if HAS_ITG3200()
  init(FIMU_ACC_ADDR, FIMU_ITG3200_DEF_ADDR, false);
  #else
  init(FIMU_ACCGYRO_ADDR, false);
  #endif
}

void FreeIMU::init(bool fastmode) {
  #if HAS_ITG3200()
  init(FIMU_ACC_ADDR, FIMU_ITG3200_DEF_ADDR, fastmode);
  #else
  init(FIMU_ACCGYRO_ADDR, fastmode);
  #endif
}


/**
 * Initialize the FreeIMU I2C bus, sensors and performs gyro offsets calibration
*/
#if HAS_ITG3200()
void FreeIMU::init(int acc_addr, int gyro_addr, bool fastmode) {
#else
void FreeIMU::init(int accgyro_addr, bool fastmode) {
#endif
  delay(5);
  
  // disable internal pullups of the ATMEGA which Wire enable by default
  #if defined(__AVR_ATmega168__) || defined(__AVR_ATmega8__) || defined(__AVR_ATmega328P__)
    // deactivate internal pull-ups for twi
    // as per note from atmega8 manual pg167
    cbi(PORTC, 4);
    cbi(PORTC, 5);
  #else
    // deactivate internal pull-ups for twi
    // as per note from atmega128 manual pg204
    cbi(PORTD, 0);
    cbi(PORTD, 1);
  #endif
  
  if(fastmode) { // switch to 400KHz I2C - eheheh
    TWBR = ((F_CPU / 400000L) - 16) / 2; // see twi_init in Wire/utility/twi.c
  }
  
  #if HAS_ADXL345()
    // init ADXL345
    acc.init(acc_addr);
  #elif HAS_BMA180()
    // init BMA180
    acc.setAddress(acc_addr);
    acc.SoftReset();
    acc.enableWrite();
    acc.SetFilter(acc.F10HZ);
    acc.setGSensitivty(acc.G15);
    acc.SetSMPSkip();
    acc.SetISRMode();
    acc.disableWrite();
  #endif

  #if HAS_ITG3200()
  // init ITG3200
  gyro.init(gyro_addr);
  delay(1000);
  // calibrate the ITG3200
  gyro.zeroCalibrate(128,5);
  #endif
  
  
  #if HAS_MPU6050()
  accgyro = MPU60X0(false, accgyro_addr);
  accgyro.initialize();
  accgyro.setI2CMasterModeEnabled(0);
  accgyro.setI2CBypassEnabled(1);
  accgyro.setFullScaleGyroRange(MPU60X0_GYRO_FS_2000);
  delay(5);
  #endif
  
  #if HAS_MPU6000()
  accgyro = MPU60X0(true, accgyro_addr);
  accgyro.initialize();
  accgyro.setFullScaleGyroRange(MPU60X0_GYRO_FS_2000);
  delay(5);
  #endif 
  
  
  #if HAS_HMC5883L()
  // init HMC5843
 
  magn.init(false); // Don't set mode yet, we'll do that later on.
  // Calibrate HMC using self test, not recommended to change the gain after calibration.
  magn.calibrate(1); // Use gain 1=default, valid 0-7, 7 not recommended.
  // Single mode conversion was used in calibration, now set continuous mode
  magn.setMode(0);
  delay(10);
  magn.setDOR(B110);
  #endif
  
  
  #if HAS_MS5611()
    baro.init(FIMU_BARO_ADDR);
  #endif
    
  // zero gyro
  zeroGyro();
  
  #ifndef CALIBRATION_H
  // load calibration from eeprom
  calLoad();
  #endif
}

#ifndef CALIBRATION_H

static uint8_t location; // assuming ordered reads

void eeprom_read_var(uint8_t size, byte * var) {
  for(uint8_t i = 0; i<size; i++) {
    var[i] = EEPROM.read(location + i);
  }
  location += size;
}

void FreeIMU::calLoad() {
  if(EEPROM.read(FREEIMU_EEPROM_BASE) == FREEIMU_EEPROM_SIGNATURE) { // check if signature is ok so we have good data
    location = FREEIMU_EEPROM_BASE + 1; // reset location
    
    eeprom_read_var(sizeof(acc_off_x), (byte *) &acc_off_x);
    eeprom_read_var(sizeof(acc_off_y), (byte *) &acc_off_y);
    eeprom_read_var(sizeof(acc_off_z), (byte *) &acc_off_z);
    
    eeprom_read_var(sizeof(magn_off_x), (byte *) &magn_off_x);
    eeprom_read_var(sizeof(magn_off_y), (byte *) &magn_off_y);
    eeprom_read_var(sizeof(magn_off_z), (byte *) &magn_off_z);
    
    eeprom_read_var(sizeof(acc_scale_x), (byte *) &acc_scale_x);
    eeprom_read_var(sizeof(acc_scale_y), (byte *) &acc_scale_y);
    eeprom_read_var(sizeof(acc_scale_z), (byte *) &acc_scale_z);
    
    eeprom_read_var(sizeof(magn_scale_x), (byte *) &magn_scale_x);
    eeprom_read_var(sizeof(magn_scale_y), (byte *) &magn_scale_y);
    eeprom_read_var(sizeof(magn_scale_z), (byte *) &magn_scale_z);
  }
  else {
    acc_off_x = 0;
    acc_off_y = 0;
    acc_off_z = 0;
    acc_scale_x = 1;
    acc_scale_y = 1;
    acc_scale_z = 1;

    magn_off_x = 0;
    magn_off_y = 0;
    magn_off_z = 0;
    magn_scale_x = 1;
    magn_scale_y = 1;
    magn_scale_z = 1;
  }
}
#endif

/**
 * Populates raw_values with the raw_values from the sensors
*/
void FreeIMU::getRawValues(int * raw_values) {
  #if HAS_ITG3200()
    acc.readAccel(&raw_values[0], &raw_values[1], &raw_values[2]);
    gyro.readGyroRaw(&raw_values[3], &raw_values[4], &raw_values[5]);
  #else
    accgyro.getMotion6(&raw_values[0], &raw_values[1], &raw_values[2], &raw_values[3], &raw_values[4], &raw_values[5]);
  #endif
  #if HAS_HMC5883L()
    magn.getValues(&raw_values[6], &raw_values[7], &raw_values[8]);
  #endif
  
  #if HAS_MS5611()
    int temp, press;
    
    //TODO: possible loss of precision
    temp = baro.rawTemperature(MS561101BA_OSR_4096);
    raw_values[9] = temp;
    press = baro.rawPressure(MS561101BA_OSR_4096);
    raw_values[10] = press;
  # endif
}


/**
 * Populates values with calibrated readings from the sensors
*/
void FreeIMU::getValues(float * values) {  
  #if HAS_ITG3200()
    int accval[3];
    acc.readAccel(&accval[0], &accval[1], &accval[2]);
    values[0] = (float) accval[0];
    values[1] = (float) accval[1];
    values[2] = (float) accval[2];
    gyro.readGyro(&values[3]);
  #else // MPU6050
    int16_t accgyroval[6];
    accgyro.getMotion6(&accgyroval[0], &accgyroval[1], &accgyroval[2], &accgyroval[3], &accgyroval[4], &accgyroval[5]);
    
    // remove offsets from the gyroscope
    accgyroval[3] = accgyroval[3] - gyro_off_x;
    accgyroval[4] = accgyroval[4] - gyro_off_y;
    accgyroval[5] = accgyroval[5] - gyro_off_z;

    for(int i = 0; i<6; i++) {
      if(i < 3) {
        values[i] = (float) accgyroval[i];
      }
      else {
        values[i] = ((float) accgyroval[i]) / 16.4f; // NOTE: this depends on the sensitivity chosen
      }
    }
  #endif
  
  
  #warning Accelerometer calibration active: have you calibrated your device?
  // remove offsets and scale accelerometer (calibration)
  values[0] = (values[0] - acc_off_x) / acc_scale_x;
  values[1] = (values[1] - acc_off_y) / acc_scale_y;
  values[2] = (values[2] - acc_off_z) / acc_scale_z;
  
  
  #if HAS_HMC5883L()
    magn.getValues(&values[6]);
    // calibration 
    #warning Magnetometer calibration active: have you calibrated your device?
    values[6] = (values[6] - magn_off_x) / magn_scale_x;
    values[7] = (values[7] - magn_off_y) / magn_scale_y;
    values[8] = (values[8] - magn_off_z) / magn_scale_z;
  #endif
}


/**
 * Computes gyro offsets
*/
void FreeIMU::zeroGyro() {
  const int totSamples = 3;
  int raw[11];
  float tmpOffsets[] = {0,0,0};
  
  for (int i = 0; i < totSamples; i++){
    getRawValues(raw);
    tmpOffsets[0] += raw[3];
    tmpOffsets[1] += raw[4];
    tmpOffsets[2] += raw[5];
  }
  
  gyro_off_x = tmpOffsets[0] / totSamples;
  gyro_off_y = tmpOffsets[1] / totSamples;
  gyro_off_z = tmpOffsets[2] / totSamples;
}


/**
 * Quaternion implementation of the 'DCM filter' [Mayhony et al].  Incorporates the magnetic distortion
 * compensation algorithms from Sebastian Madgwick's filter which eliminates the need for a reference
 * direction of flux (bx bz) to be predefined and limits the effect of magnetic distortions to yaw
 * axis only.
 * 
 * @see: http://www.x-io.co.uk/node/8#open_source_ahrs_and_imu_algorithms
*/
#if IS_9DOM()
void  FreeIMU::AHRSupdate(float gx, float gy, float gz, float ax, float ay, float az, float mx, float my, float mz) {
#elif IS_6DOM()
void  FreeIMU::AHRSupdate(float gx, float gy, float gz, float ax, float ay, float az) {
#endif
  float recipNorm;
  float q0q0, q0q1, q0q2, q0q3, q1q1, q1q2, q1q3, q2q2, q2q3, q3q3;
  float halfex = 0.0f, halfey = 0.0f, halfez = 0.0f;
  float qa, qb, qc;

  // Auxiliary variables to avoid repeated arithmetic
  q0q0 = q0 * q0;
  q0q1 = q0 * q1;
  q0q2 = q0 * q2;
  q0q3 = q0 * q3;
  q1q1 = q1 * q1;
  q1q2 = q1 * q2;
  q1q3 = q1 * q3;
  q2q2 = q2 * q2;
  q2q3 = q2 * q3;
  q3q3 = q3 * q3;
  
  #if IS_9DOM()
  // Use magnetometer measurement only when valid (avoids NaN in magnetometer normalisation)
  if((mx != 0.0f) && (my != 0.0f) && (mz != 0.0f)) {
    float hx, hy, bx, bz;
    float halfwx, halfwy, halfwz;
    
    // Normalise magnetometer measurement
    recipNorm = invSqrt(mx * mx + my * my + mz * mz);
    mx *= recipNorm;
    my *= recipNorm;
    mz *= recipNorm;
    
    // Reference direction of Earth's magnetic field
    hx = 2.0f * (mx * (0.5f - q2q2 - q3q3) + my * (q1q2 - q0q3) + mz * (q1q3 + q0q2));
    hy = 2.0f * (mx * (q1q2 + q0q3) + my * (0.5f - q1q1 - q3q3) + mz * (q2q3 - q0q1));
    bx = sqrt(hx * hx + hy * hy);
    bz = 2.0f * (mx * (q1q3 - q0q2) + my * (q2q3 + q0q1) + mz * (0.5f - q1q1 - q2q2));
    
    // Estimated direction of magnetic field
    halfwx = bx * (0.5f - q2q2 - q3q3) + bz * (q1q3 - q0q2);
    halfwy = bx * (q1q2 - q0q3) + bz * (q0q1 + q2q3);
    halfwz = bx * (q0q2 + q1q3) + bz * (0.5f - q1q1 - q2q2);
    
    // Error is sum of cross product between estimated direction and measured direction of field vectors
    halfex = (my * halfwz - mz * halfwy);
    halfey = (mz * halfwx - mx * halfwz);
    halfez = (mx * halfwy - my * halfwx);
  }
  #endif

  // Compute feedback only if accelerometer measurement valid (avoids NaN in accelerometer normalisation)
  if((ax != 0.0f) && (ay != 0.0f) && (az != 0.0f)) {
    float halfvx, halfvy, halfvz;
    
    // Normalise accelerometer measurement
    recipNorm = invSqrt(ax * ax + ay * ay + az * az);
    ax *= recipNorm;
    ay *= recipNorm;
    az *= recipNorm;
    
    // Estimated direction of gravity
    halfvx = q1q3 - q0q2;
    halfvy = q0q1 + q2q3;
    halfvz = q0q0 - 0.5f + q3q3;
  
    // Error is sum of cross product between estimated direction and measured direction of field vectors
    halfex += (ay * halfvz - az * halfvy);
    halfey += (az * halfvx - ax * halfvz);
    halfez += (ax * halfvy - ay * halfvx);
  }

  // Apply feedback only when valid data has been gathered from the accelerometer or magnetometer
  if(halfex != 0.0f && halfey != 0.0f && halfez != 0.0f) {
    // Compute and apply integral feedback if enabled
    if(twoKi > 0.0f) {
      integralFBx += twoKi * halfex * (1.0f / sampleFreq);  // integral error scaled by Ki
      integralFBy += twoKi * halfey * (1.0f / sampleFreq);
      integralFBz += twoKi * halfez * (1.0f / sampleFreq);
      gx += integralFBx;  // apply integral feedback
      gy += integralFBy;
      gz += integralFBz;
    }
    else {
      integralFBx = 0.0f; // prevent integral windup
      integralFBy = 0.0f;
      integralFBz = 0.0f;
    }

    // Apply proportional feedback
    gx += twoKp * halfex;
    gy += twoKp * halfey;
    gz += twoKp * halfez;
  }
  
  // Integrate rate of change of quaternion
  gx *= (0.5f * (1.0f / sampleFreq));   // pre-multiply common factors
  gy *= (0.5f * (1.0f / sampleFreq));
  gz *= (0.5f * (1.0f / sampleFreq));
  qa = q0;
  qb = q1;
  qc = q2;
  q0 += (-qb * gx - qc * gy - q3 * gz);
  q1 += (qa * gx + qc * gz - q3 * gy);
  q2 += (qa * gy - qb * gz + q3 * gx);
  q3 += (qa * gz + qb * gy - qc * gx);
  
  // Normalise quaternion
  recipNorm = invSqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
  q0 *= recipNorm;
  q1 *= recipNorm;
  q2 *= recipNorm;
  q3 *= recipNorm;
}


/**
 * Populates array q with a quaternion representing the IMU orientation with respect to the Earth
 * 
 * @param q the quaternion to populate
*/
void FreeIMU::getQ(float * q) {
  float val[9];
  getValues(val);
  
  DEBUG_PRINT(val[3] * M_PI/180);
  DEBUG_PRINT(val[4] * M_PI/180);
  DEBUG_PRINT(val[5] * M_PI/180);
  DEBUG_PRINT(val[0]);
  DEBUG_PRINT(val[1]);
  DEBUG_PRINT(val[2]);
  DEBUG_PRINT(val[6]);
  DEBUG_PRINT(val[7]);
  DEBUG_PRINT(val[8]);
  
  now = micros();
  sampleFreq = 1.0 / ((now - lastUpdate) / 1000000.0);
  lastUpdate = now;
  // gyro values are expressed in deg/sec, the * M_PI/180 will convert it to radians/sec
  #if IS_9DOM()
    #if HAS_AXIS_ALIGNED()
      AHRSupdate(val[3] * M_PI/180, val[4] * M_PI/180, val[5] * M_PI/180, val[0], val[1], val[2], val[6], val[7], val[8]);
    #elif defined(SEN_10724)
      AHRSupdate(val[3] * M_PI/180, val[4] * M_PI/180, val[5] * M_PI/180, val[0], val[1], val[2], val[7], -val[6], val[8]);
    #elif defined(ARDUIMU_v3)
      AHRSupdate(val[3] * M_PI/180, val[4] * M_PI/180, val[5] * M_PI/180, val[0], val[1], val[2], -val[6], -val[7], val[8]);
    #endif
  #else
    AHRSupdate(val[3] * M_PI/180, val[4] * M_PI/180, val[5] * M_PI/180, val[0], val[1], val[2]);
  #endif
  
  q[0] = q0;
  q[1] = q1;
  q[2] = q2;
  q[3] = q3;
}


#if HAS_MS5611()

const float def_sea_press = 1013.25;

/**
 * Returns an altitude estimate from baromether readings only using sea_press as current sea level pressure
*/
float FreeIMU::getBaroAlt(float sea_press) {
  float temp = baro.getTemperature(MS561101BA_OSR_4096);
  float press = baro.getPressure(MS561101BA_OSR_4096);
  return ((pow((sea_press / press), 1/5.257) - 1.0) * (temp + 273.15)) / 0.0065;
}

/**
 * Returns an altitude estimate from baromether readings only using a default sea level pressure
*/
float FreeIMU::getBaroAlt() {
  return getBaroAlt(def_sea_press);
}


/**
 * Compensates the accelerometer readings in the 3D vector acc expressed in the sensor frame for gravity
 * @param acc the accelerometer readings to compensate for gravity
 * @param q the quaternion orientation of the sensor board with respect to the world
*/
void FreeIMU::gravityCompensateAcc(float * acc, float * q) {
  float g[3];
  
  // get expected direction of gravity in the sensor frame
  g[0] = 2 * (q[1] * q[3] - q[0] * q[2]);
  g[1] = 2 * (q[0] * q[1] + q[2] * q[3]);
  g[2] = q[0] * q[0] - q[1] * q[1] - q[2] * q[2] + q[3] * q[3];
  
  // compensate accelerometer readings with the expected direction of gravity
  acc[0] = acc[0] - g[0];
  acc[1] = acc[1] - g[1];
  acc[2] = acc[2] - g[2];
}
// 
// 
// // complementary filter from MultiWii project v1.9
// 
// #define UPDATE_INTERVAL 25000    // 40hz update rate (20hz LPF on acc)
// #define INIT_DELAY      4000000  // 4 sec initialization delay
// #define Kp1 0.55f                // PI observer velocity gain 
// #define Kp2 1.0f                 // PI observer position gain
// #define Ki  0.001f               // PI observer integral gain (bias cancellation)
// #define dt  (UPDATE_INTERVAL / 1000000.0f)
// 
// /**
// * Returns an altitude estimate from baromether fused with accelerometer readings
// */
// float FreeIMU::getEstimatedAlt() {
//   static uint8_t inited = 0;
//   static int16_t AltErrorI = 0;
//   static float AccScale  = 0.0f;
//   static uint32_t deadLine = INIT_DELAY;
//   int16_t AltError;
//   int16_t InstAcc;
//   int16_t Delta;
//   static int32_t  BaroAlt;
//   static int32_t  EstVelocity;
//   static int32_t  EstAlt;
//   
//   long currentTime = micros();
//   
//   if (currentTime < deadLine) return BaroAlt;
//   deadLine = currentTime + UPDATE_INTERVAL; 
//   // Soft start
// 
//   if (!inited) {
//     inited = 1;
//     EstAlt = getBaroAlt();
//     EstVelocity = 0;
//     AltErrorI = 0;
//   }
//   // Estimation Error
//   AltError = getBaroAlt() - EstAlt;
//   AltErrorI += AltError;
//   AltErrorI = constrain(AltErrorI,-25000,+25000);
//   // Gravity vector correction and projection to the local Z
//   //InstAcc = (accADC[YAW] * (1 - acc_1G * InvSqrt(isq(accADC[ROLL]) + isq(accADC[PITCH]) + isq(accADC[YAW])))) * AccScale + (Ki) * AltErrorI;
//   #if defined(TRUSTED_ACCZ)
//     InstAcc = (accADC[YAW] * (1 - acc_1G * InvSqrt(isq(accADC[ROLL]) + isq(accADC[PITCH]) + isq(accADC[YAW])))) * AccScale +  AltErrorI / 1000;
//   #else
//     InstAcc = AltErrorI / 1000;
//   #endif
//   
//   // Integrators
//   Delta = InstAcc * dt + (Kp1 * dt) * AltError;
//   EstAlt += (EstVelocity/5 + Delta) * (dt / 2) + (Kp2 * dt) * AltError;
//   EstVelocity += Delta*10;
//   
//   
//   vmath::quat<float> ciao = vmath::quat<float>(0.0, 0.1, 0.2, 0.3);
//   vmath::quat<float> ciao2;
//   
//   ciao = ciao * ciao2;
//   
//   
//   Serial.print(":::");
//   Serial.println(ciao.w);
//   
//   return EstAlt;
// }

#endif


/**
 * Returns the Euler angles in radians defined in the Aerospace sequence.
 * See Sebastian O.H. Madwick report "An efficient orientation filter for 
 * inertial and intertial/magnetic sensor arrays" Chapter 2 Quaternion representation
 * 
 * @param angles three floats array which will be populated by the Euler angles in radians
*/
void FreeIMU::getEulerRad(float * angles) {
  float q[4]; // quaternion
  getQ(q);
  angles[0] = atan2(2 * q[1] * q[2] - 2 * q[0] * q[3], 2 * q[0]*q[0] + 2 * q[1] * q[1] - 1); // psi
  angles[1] = -asin(2 * q[1] * q[3] + 2 * q[0] * q[2]); // theta
  angles[2] = atan2(2 * q[2] * q[3] - 2 * q[0] * q[1], 2 * q[0] * q[0] + 2 * q[3] * q[3] - 1); // phi
}


/**
 * Returns the Euler angles in degrees defined with the Aerospace sequence.
 * See Sebastian O.H. Madwick report "An efficient orientation filter for 
 * inertial and intertial/magnetic sensor arrays" Chapter 2 Quaternion representation
 * 
 * @param angles three floats array which will be populated by the Euler angles in degrees
*/
void FreeIMU::getEuler(float * angles) {
  getEulerRad(angles);
  arr3_rad_to_deg(angles);
}


/**
 * Returns the yaw pitch and roll angles, respectively defined as the angles in radians between
 * the Earth North and the IMU X axis (yaw), the Earth ground plane and the IMU X axis (pitch)
 * and the Earth ground plane and the IMU Y axis.
 * 
 * @note This is not an Euler representation: the rotations aren't consecutive rotations but only
 * angles from Earth and the IMU. For Euler representation Yaw, Pitch and Roll see FreeIMU::getEuler
 * 
 * @param ypr three floats array which will be populated by Yaw, Pitch and Roll angles in radians
*/
void FreeIMU::getYawPitchRollRad(float * ypr) {
  float q[4]; // quaternion
  float gx, gy, gz; // estimated gravity direction
  getQ(q);
  
  gx = 2 * (q[1]*q[3] - q[0]*q[2]);
  gy = 2 * (q[0]*q[1] + q[2]*q[3]);
  gz = q[0]*q[0] - q[1]*q[1] - q[2]*q[2] + q[3]*q[3];
  
  ypr[0] = atan2(2 * q[1] * q[2] - 2 * q[0] * q[3], 2 * q[0]*q[0] + 2 * q[1] * q[1] - 1);
  ypr[1] = atan(gx / sqrt(gy*gy + gz*gz));
  ypr[2] = atan(gy / sqrt(gx*gx + gz*gz));
}


/**
 * Returns the yaw pitch and roll angles, respectively defined as the angles in degrees between
 * the Earth North and the IMU X axis (yaw), the Earth ground plane and the IMU X axis (pitch)
 * and the Earth ground plane and the IMU Y axis.
 * 
 * @note This is not an Euler representation: the rotations aren't consecutive rotations but only
 * angles from Earth and the IMU. For Euler representation Yaw, Pitch and Roll see FreeIMU::getEuler
 * 
 * @param ypr three floats array which will be populated by Yaw, Pitch and Roll angles in degrees
*/
void FreeIMU::getYawPitchRoll(float * ypr) {
  getYawPitchRollRad(ypr);
  arr3_rad_to_deg(ypr);
}


/**
 * Converts a 3 elements array arr of angles expressed in radians into degrees
*/
void arr3_rad_to_deg(float * arr) {
  arr[0] *= 180/M_PI;
  arr[1] *= 180/M_PI;
  arr[2] *= 180/M_PI;
}


/**
 * Fast inverse square root implementation
 * @see http://en.wikipedia.org/wiki/Fast_inverse_square_root
*/
float invSqrt(float number) {
  volatile long i;
  volatile float x, y;
  volatile const float f = 1.5F;

  x = number * 0.5F;
  y = number;
  i = * ( long * ) &y;
  i = 0x5f375a86 - ( i >> 1 );
  y = * ( float * ) &i;
  y = y * ( f - ( x * y * y ) );
  return y;
}



