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

const int acc_off_x = 0;
const int acc_off_y = 0;
const int acc_off_z = 0;
const float acc_scale_x = 16384;
const float acc_scale_y = 16384;
const float acc_scale_z = 16384;


const int magn_off_x = 5*440;
const int magn_off_y = -100*440;
const int magn_off_z = -230*440;
const float magn_scale_x = 440;
const float magn_scale_y = 440;
const float magn_scale_z = 440;
