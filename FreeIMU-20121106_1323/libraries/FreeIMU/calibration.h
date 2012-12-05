/**
 * FreeIMU calibration header. This file is overwritten by the FreeIMU calibration routine.
 * This file is for "perfect" sensors for the ArduIMU version of FreeIMU and sets the sensors up as follows.
 * It is not clear it is necessary to use this file to compile and upload a version of FreeIMU_Serial 
 * before starting the calibration but it can't hurt.

GyroZ clock
Sample rate = 50Hz
Gyro scale = 2000 dps
LPF = 20Hz
Accel scale = 4g (16384LSB/g)
Magnetometer scale = ± 4.0 Ga= 440 (440LSB/Ga)

(Note: In previopus posts I have made erroneosly stated the acceleromenter full scale as 2g... 4g is correct


*/
/*#define CALIBRATION_H
const int acc_off_x = 235;
const int acc_off_y = -150;
const int acc_off_z = -80;
const float acc_scale_x = 8300;
const float acc_scale_y = 8270;
const float acc_scale_z = 8245;


const int magn_off_x = 4;
const int magn_off_y = 115;
const int magn_off_z = -299;
const float magn_scale_x = 630;
const float magn_scale_y = 630;
const float magn_scale_z = 590;
*/
