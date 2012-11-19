// MPU6000 support for ArduIMU V3
#if BOARD_VERSION == 3
#include <SPI.h>

// MPU6000 SPI functions
byte MPU6000_SPI_read(byte reg)
{
  byte dump;
  byte return_value;
  byte addr = reg | 0x80; // Set most significant bit
  digitalWrite(MPU6000_CHIP_SELECT_PIN, LOW);
  dump = SPI.transfer(addr);
  return_value = SPI.transfer(0);
  digitalWrite(MPU6000_CHIP_SELECT_PIN, HIGH);
  return(return_value);
}

void MPU6000_SPI_write(byte reg, byte data)
{
  byte dump;
  digitalWrite(MPU6000_CHIP_SELECT_PIN, LOW);
  dump = SPI.transfer(reg);
  dump = SPI.transfer(data);
  digitalWrite(MPU6000_CHIP_SELECT_PIN, HIGH);
}

// MPU6000 INTERRUPT ON INT0
void MPU6000_data_int()
{
  MPU6000_newdata++;
}

// MPU6000 Initialization and configuration
void MPU6000_Init(void)
{
    // MPU6000 chip select setup
    pinMode(MPU6000_CHIP_SELECT_PIN, OUTPUT);
    digitalWrite(MPU6000_CHIP_SELECT_PIN, HIGH);
    
    // SPI initialization
    SPI.begin();
    SPI.setClockDivider(SPI_CLOCK_DIV16);      // SPI at 1Mhz (on 16Mhz clock)
    delay(10);
    
    // Chip reset
    MPU6000_SPI_write(MPUREG_PWR_MGMT_1, BIT_H_RESET);
    delay(100);
    // Wake up device and select GyroZ clock (better performance)
    MPU6000_SPI_write(MPUREG_PWR_MGMT_1, MPU_CLK_SEL_PLLGYROZ);
    delay(1);
    // Disable I2C bus (recommended on datasheet)
    MPU6000_SPI_write(MPUREG_USER_CTRL, BIT_I2C_IF_DIS);
    delay(1);
    // SAMPLE RATE
    //MPU6000_SPI_write(MPUREG_SMPLRT_DIV,0x04);     // Sample rate = 200Hz    Fsample= 1Khz/(4+1) = 200Hz     
    MPU6000_SPI_write(MPUREG_SMPLRT_DIV,19);     // Sample rate = 50Hz    Fsample= 1Khz/(19+1) = 50Hz     
    delay(1);
    // FS & DLPF   FS=2000º/s, DLPF = 20Hz (low pass filter)
    MPU6000_SPI_write(MPUREG_CONFIG, BITS_DLPF_CFG_20HZ);  
    delay(1);
    MPU6000_SPI_write(MPUREG_GYRO_CONFIG,BITS_FS_2000DPS);  // Gyro scale 2000º/s
    delay(1);
    MPU6000_SPI_write(MPUREG_ACCEL_CONFIG,0x08);            // Accel scale 4g (4096LSB/g)
    delay(1);   
    // INT CFG => Interrupt on Data Ready
    MPU6000_SPI_write(MPUREG_INT_ENABLE,BIT_RAW_RDY_EN);         // INT: Raw data ready
    delay(1);
    MPU6000_SPI_write(MPUREG_INT_PIN_CFG,BIT_INT_ANYRD_2CLEAR);  // INT: Clear on any read
    delay(1);
    // Oscillator set
    // MPU6000_SPI_write(MPUREG_PWR_MGMT_1,MPU_CLK_SEL_PLLGYROZ);
    delay(1);
  
    // MPU_INT is connected to INT 0. Enable interrupt on INT0
    attachInterrupt(0,MPU6000_data_int,RISING);
}

// Read gyros and accel sensors on MPU6000
void MPU6000_Read()
{
  int byte_H;
  int byte_L;
  
  // Read AccelX
    byte_H = MPU6000_SPI_read(MPUREG_ACCEL_XOUT_H);
    byte_L = MPU6000_SPI_read(MPUREG_ACCEL_XOUT_L);
    accelX = (byte_H<<8)| byte_L;
    // Read AccelY
    byte_H = MPU6000_SPI_read(MPUREG_ACCEL_YOUT_H);
    byte_L = MPU6000_SPI_read(MPUREG_ACCEL_YOUT_L);
    accelY = (byte_H<<8)| byte_L;
    // Read AccelZ
    byte_H = MPU6000_SPI_read(MPUREG_ACCEL_ZOUT_H);
    byte_L = MPU6000_SPI_read(MPUREG_ACCEL_ZOUT_L);
    accelZ = (byte_H<<8)| byte_L;
    
    // Read Temp
    //byte_H = MPU6000_SPI_read(MPUREG_TEMP_OUT_H);
    //byte_L = MPU6000_SPI_read(MPUREG_TEMP_OUT_L);
    //temp = (byte_H<<8)| byte_L; 
     
    // Read GyroX
    byte_H = MPU6000_SPI_read(MPUREG_GYRO_XOUT_H);
    byte_L = MPU6000_SPI_read(MPUREG_GYRO_XOUT_L);
    gyroX = (byte_H<<8)| byte_L;
    // Read GyroY
    byte_H = MPU6000_SPI_read(MPUREG_GYRO_YOUT_H);
    byte_L = MPU6000_SPI_read(MPUREG_GYRO_YOUT_L);
    gyroY = (byte_H<<8)| byte_L;
    // Read GyroZ
    byte_H = MPU6000_SPI_read(MPUREG_GYRO_ZOUT_H);
    byte_L = MPU6000_SPI_read(MPUREG_GYRO_ZOUT_L);
    gyroZ = (byte_H<<8)| byte_L;
}
#endif
