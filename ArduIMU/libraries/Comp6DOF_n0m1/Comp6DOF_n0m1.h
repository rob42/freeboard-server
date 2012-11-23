/************************************************************************************
 * 	
 * 	Name    : Comp6DOF_n0m1 Library                         
 * 	Author  : Noah Shibley, Michael Grant, NoMi Design Ltd.  n0m1.com                       
 * 	Date    : Feb 1st 2012                                    
 * 	Version : 0.1                                              
 * 	Notes   : Arduino Library for compass tilt compensation and hard iron offset.
			  Part of this code was ported to C from the Freescale appnote AN4248. 
 *			  http://www.freescale.com/files/sensors/doc/app_note/AN4248.pdf 
 *			  The sine function comes from Dave Dribin's TrigInt lib. 
 *			  https://bitbucket.org/ddribin/trigint    
 * 
 * 	This file is part of Comp6DOF_n0m1 Lib.
 * 
 * 		    Comp6DOF_n0m1 is free software: you can redistribute it and/or modify
 * 		    it under the terms of the GNU General Public License as published by
 * 		    the Free Software Foundation, either version 3 of the License, or
 * 		    (at your option) any later version.
 * 
 * 		    Comp6DOF_n0m1 is distributed in the hope that it will be useful,
 * 		    but WITHOUT ANY WARRANTY; without even the implied warranty of
 * 		    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * 		    GNU General Public License for more details.
 * 
 * 		    You should have received a copy of the GNU General Public License
 * 		    along with Comp6DOF_n0m1 lib.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ***********************************************************************************/


#ifndef COMP6DOF_N0M1_H
#define COMP6DOF_N0M1_H

#if defined(ARDUINO) && ARDUINO >= 100
#include "Arduino.h"    // for digitalRead, digitalWrite, pinMode, delayMicroseconds
#else
#include "WProgram.h"
#endif

const int K1 = 5701;
const int K2 = -1645;
const int K3 = 446;
const unsigned int MINDELTADIV = 1;              /* final step size for divInt */

#define SINE_INDEX_WIDTH 4
#define SINE_INTERP_WIDTH 8

#if (SINE_INDEX_WIDTH + SINE_INTERP_WIDTH > 12)
# error Invalid sine widths
#endif

#define SINE_INDEX_OFFSET (12 - SINE_INDEX_WIDTH)
#define SINE_INTERP_OFFSET (SINE_INDEX_OFFSET - SINE_INTERP_WIDTH)
#define QUADRANT_HIGH_MASK (1 << 13)
#define QUADRANT_LOW_MASK (1 << 12)
# define MAX(a, b) ((a) < (b) ? (b) : (a))

#if SINE_INTERP_OFFSET > 0
# define SINE_ROUNDING (1 << (SINE_INTERP_OFFSET-1))
#else
# define SINE_ROUNDING (0)
#endif

#define BITS(_VALUE_, _WIDTH_, _BIT_) (((_VALUE_) >> (_BIT_)) & ((1 << (_WIDTH_)) - 1))
#define SINE_TABLE_SIZE (1 << SINE_INDEX_WIDTH)
// Table of the first quadrant values.  Size is + 1 to store the first value of
// the second quadrant, hence we're storing 0 <= degrees <= 90.
static const int16_t sinLUT[SINE_TABLE_SIZE + 1] = {
        0,  3211,  6392,  9511, 12539, 15446, 18204, 20787,
    23169, 25329, 27244, 28897, 30272, 31356, 32137, 32609,
    32767
};

#define TRIGINT_ANGLES_PER_CYCLE 0x4000

//#define DEBUG


class Comp6DOF_n0m1 {

public:
 
 
  Comp6DOF_n0m1();

/***********************************************************
 * 
 * compCompass
 *
 * Input raw compass values, xyz, and accelerometer values xyz
 * 
 ***********************************************************/
	void compCompass(int xMagAxis, int yMagAxis, int zMagAxis, int xAccel, int yAccel, int zAccel, boolean lowpass);

/***********************************************************
 * 
 * roll
 * 
 * returns roll result -18000 to 18000 
 *
 ***********************************************************/
	int roll(){ return roll_; }
/***********************************************************
 * 
 * pitch
 *
 * returns pitch result -9000 to 9000
 * 
 ***********************************************************/	
	int pitch(){ return pitch_;}
/***********************************************************
 * 
 * yaw
 *
 * returns yaw result -18000 to 18000
 * 
 ***********************************************************/	
	int yaw(){ return yaw_; }
	
/***********************************************************
 * 
 * rollf
 *
 * returns roll as float value -180.00 to 180.00
 * 
 ***********************************************************/
	float rollf(){ return (float)roll_/100; }
	
/***********************************************************
 * 
 * pitchf
 *
 * returns pitch as float value -90.00 to 90.00
 * 
 ***********************************************************/	
	float pitchf(){ return (float)pitch_/100;}

/***********************************************************
 * 
 * yawf
 *
 *  returns yaw as float value -180.00 to 180.00
 * 
 ***********************************************************/
	float yawf(){ return (float)yaw_/100; }

/***********************************************************
 * 
 * xAxisComp
 *
 * return tilt compensated xAxis compass result 
 *  
 ***********************************************************/
	int xAxisComp(){ return xMagAxisComp_; }

/***********************************************************
 * 
 * yAxisComp
 *
 * return tilt compensated yAxis compass result 
 * 
 ***********************************************************/
	int yAxisComp(){ return yMagAxisComp_; }
	
/***********************************************************
 * 
 * zAxisComp
 *
 * return tilt compensated zAxis compass result 
 * 
 ***********************************************************/
	int zAxisComp(){ return zMagAxisComp_; }
	
/***********************************************************
 * 
 * xHardOff
 *
 * return hardiron xOffset for compass
 * 
 ***********************************************************/
	int xHardOff(){ return xOffset_; }
	
/***********************************************************
 * 
 * yHardOff
 *
 * return hardiron yOffset for compass
 * 
 ***********************************************************/
	int yHardOff(){ return yOffset_; }
	
/***********************************************************
 * 
 * zHardOff
 *
 * return hardiron zOffset for compass
 * 
 ***********************************************************/
	int zHardOff(){ return zOffset_; }

/***********************************************************
 * 
 * deviantSpread
 *
 * get all axis combos (8) & load array
 * 
 ***********************************************************/	
	boolean deviantSpread(int XAxis,int YAxis, int ZAxis);

/***********************************************************
 * 
 *  calOffsets
 *
 * 	solve for x,y,z max & min, filter results, pick the best and average
 * 
 ***********************************************************/	
	boolean calOffsets();

/***********************************************************
 * 
 * atan2Int
 *
 *	The function Atan2Int is a wrapper function which 
 *	implements the ATAN2 function by assigning the results 
 *	of an ATAN function to the correct quadrant. The result 
 *	is the angle in degrees times 100. Calculates 
 *	100*atan2(y/x)=100*atan2(y,x) in deg for x, y in range
 *	-32768 to 32767
 *
 ***********************************************************/
int atan2Int(int y, int x);
		
	
  
private:
	
	int roll_;
	int pitch_;
	int yaw_;
	
	int lpRoll_;
	int lpPitch_;
	int lpYaw_;
	
	int xMagAxisComp_;
	int yMagAxisComp_;
	int zMagAxisComp_;
	
	int ktrust1_;
	int ktrust2_;
	
	int xOffset_;
	int yOffset_;
	int zOffset_;	
	  
	int Xdata[8]; 
	int Ydata[8];
	int Zdata[8]; 

	byte comboCount; // combo counter 
	// comboflag - each binary position represents a combo 0b xxxx xxx8 7654 3210
	unsigned int comboFlag; // first postion represents bad or rejected data
	
	

	/***********************************************************
	 * 
	 * degToAngle
	 *
	 *	Angle is a 14-bit angle, 0 - 0x3FFFF. This divides the circle
	 *	into 16,384 angle units, instead of the standard 360 degrees.
	 *	Thus:
	 *	  - 1 angle unit =~ 360/16384 =~ 0.0219727 degrees
	 *	  - 1 angle unit =~ 2*M_PI/16384 =~ 0.0003835 radians
	 *	 
	 * 
	 ***********************************************************/
	unsigned int degToAngle(int degrees);

	/***********************************************************
	 * 
	 * atanInt
	 * 
	 *	The function iHundredAtanDeg computes the atan function 
	 *	for X and Y in the range 0 to 32767 calculates 
	 *	100*atan(y/x) range 0 to 9000 for all x, y positive in 
	 *	range 0 to 32767
	 *
	 ***********************************************************/
	int atanInt(int y, int x);

	/***********************************************************
	 * 
	 * divInt
	 *
	 *	The function divInt is an accurate integer division
	 *	function where it is given that both the numerator and 
	 *	denominator are non-negative, non-zero and where the 
	 *	denominator is greater than the numerator. The result 
	 *	is in the range 0 decimal to 32767 decimal which is 
	 *	interpreted in Q15 fractional arithmetic as the range 
	 *	0.0 to 0.9999695.function to calculate r = y / x with 
	 *	y <= x, and x, y both > 0
	 * 
	 ***********************************************************/
	int divInt(int y, int x);

	/***********************************************************
	 * 
	 *  sinInt
	 *	
	 *	Returns the sine of angle as signed 16-bit integer. 
	 *	It is scaled to an amplitude of 32,767, thus values 
	 *	will range from -32,767 to +32,767.
	 * 
	 ***********************************************************/
	int sinInt(unsigned int angle);

	/***********************************************************
	 * 
	 *  lowPassInt
	 * 
	 *  The code is written for filtering the yaw (compass) angle
	 *	ψ but can also be used for the roll angle. For the pitch 
	 *	angle θ, which is restricted to the range -90° to 90°, 
	 *	the final bounds check should be changed
	 *	
	 ***********************************************************/
	int lowPassInt(int input, int prevOut, int minBound, int maxBound);
	
	/***********************************************************
	 * 
	 * sin16LUT
	 * 
	 ***********************************************************/
	inline int sin16LUT(int index)
	{
	    return sinLUT[index];
	}
	
	/***********************************************************
	 * 
	 * LinearEquationsSolving
	 *
	 ***********************************************************/
	int linearEquationsSolving(int nDim, float* pfMatr, float* pfVect, float* pfSolution);
  
	/***********************************************************
	*
	*	calSense
	*
	***********************************************************/
	int calSense( int *x, int *y, int *z, int *S, int *O );
	
	/***********************************************************
	*
	*	intSqrt
	*
	*	fast integer sqrt
	* 
	***********************************************************/
	unsigned long intSqrt(unsigned long val); 
	
};

#endif

