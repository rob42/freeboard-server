/************************************************************************************
 * 	
 * 	Name    : Comp6DOF_n0m1 Library Example: Yaw, Pitch, Roll                       
 * 	Author  : Noah Shibley, Michael Grant, NoMi Design Ltd. http://n0m1.com                       
 * 	Date    : Feb 27th 2012                                    
 * 	Version : 0.1                                              
 * 	Notes   : Arduino Library for compass tilt compensation and hard iron offset 
 *
 ***********************************************************************************/

#include <I2C.h>
#include <MMA8453_n0m1.h>
#include <Comp6DOF_n0m1.h>
#include <HMC5883L.h> // Reference the HMC5883L Compass Library

MMA8453_n0m1 accel;
Comp6DOF_n0m1 sixDOF;
HMC5883L compass;

// Record any errors that may occur in the compass.
int error = 0;


void setup()
{
  Serial.begin(9600);
  accel.dataMode(true, 2); //enable highRes 10bit, 2g range [2g,4g,8g]

  compass = HMC5883L(); // Construct a new HMC5883 compass.

  error = compass.SetScale(1.3); // Set the scale of the compass.
  if(error != 0) // If there is an error, print it out.
    Serial.println(compass.GetErrorText(error));

  error = compass.SetMeasurementMode(Measurement_Continuous); // Set the measurement mode to Continuous
  if(error != 0) // If there is an error, print it out.
    Serial.println(compass.GetErrorText(error));

  Serial.println("n0m1.com");

  /* hard iron offset finder
   int doneoffset =0;
   while (doneoffset ==0)
   {
   // Should indicate with a blink right here
   int donecombo = 0;
   while ( donecombo == 0 )   // load tuning array 
   {  
   delay (50);
   MagnetometerRaw  raw = compass.ReadRawAxis();
   donecombo = sixDOF.deviantSpread (raw.XAxis, raw.YAxis, raw.ZAxis);
   }
   
   doneoffset = sixDOF.calOffsets();
   }
   */

}

void loop()
{

  // poll sensors for new data
  accel.update();
  MagnetometerRaw raw = compass.ReadRawAxis();

  // offset compass by hard iron
  // raw.XAxis -= 40;
  // raw.YAxis -= 100;
  // raw.ZAxis -= 350;

  //enter compass data and accel data for calculation
  sixDOF.compCompass(raw.XAxis, raw.YAxis, raw.ZAxis, accel.x(), accel.y(), accel.z(), false);


  Serial.print ("  Roll: ");
  Serial.print (sixDOF.roll()/100); 
  Serial.print ("  Pitch: ");
  Serial.print (sixDOF.pitch()/100);      
  Serial.print ("  Yaw: ");
  Serial.print (sixDOF.yaw()/100);   
  Serial.println ("");

  delay(250);

}




