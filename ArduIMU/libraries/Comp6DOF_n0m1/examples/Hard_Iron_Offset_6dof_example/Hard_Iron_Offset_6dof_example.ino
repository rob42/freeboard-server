/************************************************************************************
 * 	
 * 	Name    : Comp6DOF_n0m1 Library Example: Hard Iron Offset                       
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

  //Hard iron offset calculations 
  int doneoffset =0;
  while (doneoffset ==0)
  {
    // a blink would be nice right here
    int donecombo = 0;
    while ( donecombo == 0 )   // load tuning array 
    {  
      delay (50);
      MagnetometerRaw  raw = compass.ReadRawAxis();
      donecombo = sixDOF.deviantSpread (raw.XAxis, raw.YAxis, raw.ZAxis);
    }

    doneoffset = sixDOF.calOffsets();
  }

  // print offsets
  Serial.println ("");
  Serial.print ("  Xoff: ");
  Serial.print (sixDOF.xHardOff());   
  Serial.print ("  Yoff: ");
  Serial.print (sixDOF.yHardOff());   
  Serial.print ("  Zoff: ");
  Serial.print (sixDOF.zHardOff());   
  Serial.println ("");

}

void loop()
{

  // spin wheels, do nothing, hard iron offset done in setup
  delay (5);

}




