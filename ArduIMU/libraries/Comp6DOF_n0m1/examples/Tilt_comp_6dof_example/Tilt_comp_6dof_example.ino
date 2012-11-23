/************************************************************************************
 * 	
 * 	Name    : Comp6DOF_n0m1 Library Example: Tilt Compensation                       
 * 	Author  : Noah Shibley, Michael Grant, NoMi Design Ltd. http://n0m1.com                       
 * 	Date    : Feb 10th 2012                                    
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
int skip = 0;

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

}

void loop()
{

  // poll sensors for new data
  accel.update();
  MagnetometerRaw raw = compass.ReadRawAxis();

  // offset compass by hard iron
  // raw.XAxis += 36;
  // raw.YAxis += 68;
  // raw.ZAxis += 312;

  //enter compass data and accel data for calculation
  sixDOF.compCompass(-(raw.ZAxis), -(raw.XAxis), raw.YAxis, -(accel.z()), -(accel.x()), accel.y(), true);

  float compHeading = sixDOF.atan2Int(sixDOF.yAxisComp(), sixDOF.xAxisComp());
  compHeading = compHeading /100;
  compHeading += 180;

  //calculate the non tilt compensated heading for comparison. NOT necessary, just for example
  float head1, head2;
  nonCompHeading(raw,head1,head2);


  skip ++;
  if ( skip == 5 ) //print only every 5 loops.
  {
    skip = 0 ;

    Serial.print ("  raw1: ");
    Serial.print (head1); //non tilt comp heading, calculated using 6dof atan2
    Serial.print ("  raw2: ");
    Serial.print (head2); //non tilt comp heading, calculated using std atan2

    Serial.print ("  heading: ");
    Serial.print (compHeading); //compensated heading. 

    Serial.print ("  Roll: ");
    Serial.print (sixDOF.roll()/100); 
    Serial.print ("  Pitch: ");
    Serial.print (sixDOF.pitch()/100);      
    Serial.print ("  Yaw: ");
    Serial.print (sixDOF.yaw()/100);   

    Serial.print ("    ");	
    Serial.print (raw.XAxis); 
    Serial.print (" ");	
    Serial.print (raw.YAxis);
    Serial.print (" ");
    Serial.print (raw.ZAxis);

    Serial.println ("");

  }




  delay(50);

}


void nonCompHeading(MagnetometerRaw raw, float& head1, float& head2)
{
  
  // this is correct orientation for non tilt comp - notice the result is not the same as tilt comp
  head1 = sixDOF.atan2Int ( - raw.XAxis,   raw.ZAxis); //use the 6dof atan2 because its faster
  head1 = head1 /100; 
  if (head1 < 0 ) {
    head1 += 360;
  }

  head2 = atan2 ( - raw.XAxis,   raw.ZAxis); //compare it to the std atan2

  // Correct for when signs are reversed.
  if(head2 < 0)
    head2 += 2*PI;
  // Check for wrap due to addition of declination.
  if(head2 > 2*PI)
    head2 -= 2*PI;
  // Convert radians to degrees for readability.
  head2 = head2 * 180/M_PI; 

}




