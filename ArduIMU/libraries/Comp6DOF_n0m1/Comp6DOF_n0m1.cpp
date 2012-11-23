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

#include "Comp6DOF_n0m1.h"




/***********************************************************
 * 
 * Comp6DOF_n0m1
 * 
 ***********************************************************/
Comp6DOF_n0m1::Comp6DOF_n0m1()
{
	ktrust1_ = 0; 
	ktrust2_ = 0;
	xOffset_ = 0;
	yOffset_ = 0;
	zOffset_ = 0;
	comboCount = 0;
	comboFlag = 1;
	

}

/***********************************************************
 * 
 * compCompass
 * 
 ***********************************************************/
void Comp6DOF_n0m1::compCompass(int xMagAxis, int yMagAxis, int zMagAxis, int xAccel, int yAccel, int zAccel, boolean lowpass)
{
  
int sinVal, cosVal;         /* sine and cosine */

/* calculate current roll angle Phi */
roll_ = atan2Int(yAccel, zAccel);

/* calculate sin and cosine of roll angle Phi */
unsigned int angle = degToAngle((roll_/100)+180);
sinVal = sinInt(angle);

 int cosAngle = atan2Int(zAccel,yAccel);
 angle = degToAngle((cosAngle/100)+180);
 cosVal = sinInt(angle);

/* de-rotate by roll angle Phi */
yMagAxisComp_ = (int)(((unsigned long)yMagAxis * cosVal - (unsigned long)zMagAxis * sinVal) >> 15);/* Eq 19 y component */
zMagAxis = (int)(((unsigned long)yMagAxis * sinVal + (unsigned long)zMagAxis * cosVal) >> 15);/* Bpy*sin(Phi)+Bpz*cos(Phi)*/
zAccel = (int)(((unsigned long)yAccel * sinVal) + ((unsigned long)zAccel * cosVal) >> 15);/* Eq 15 denominator */

/* calculate current pitch angle Theta */
pitch_ = atan2Int((int)-xAccel, zAccel);/* Eq 15 */
/* restrict pitch angle to range -90 to 90 degrees */
if (pitch_ > 9000) pitch_ = (int) (18000 - pitch_);
if (pitch_ < -9000) pitch_ = (int) (-18000 - pitch_);

/* calculate sin and cosine of pitch angle Theta */
 int sinAngle = atan2Int(xAccel, zAccel);
 angle = degToAngle((sinAngle/100)+180);
 sinVal = sinInt(angle);

 cosAngle = atan2Int(zAccel, xAccel);
 angle = degToAngle((cosAngle/100)+180);
 cosVal = sinInt(angle);

/* correct cosine if pitch not in range -90 to 90 degrees */
if (cosVal < 0) cosVal = (int)-cosVal;

/* de-rotate by pitch angle Theta */
xMagAxisComp_ = (int)(((unsigned long)xMagAxis * cosVal + (unsigned long)zMagAxis * sinVal) >> 15); /* Eq 19: x component */
zMagAxisComp_ = (int)(((unsigned long)-xMagAxis * sinVal + (unsigned long)zMagAxis * cosVal) >> 15);/* Eq 19: z component */

/* calculate current yaw = e-compass angle Psi */
yaw_ = atan2Int((int)-yMagAxisComp_, xMagAxisComp_);  /* Eq 22 */

if(lowpass == true)
{
	lpRoll_ = lowPassInt(roll_,lpRoll_,-180,180);
	lpPitch_ = lowPassInt(pitch_,lpPitch_,-90,90);
	lpYaw_ = lowPassInt(yaw_,lpYaw_,-180,180);
	
	roll_ = lpRoll_;
	pitch_ = lpPitch_;
	yaw_ = lpYaw_;
}


}

/***********************************************************
 * 
 * degToAngle
 * 
 ***********************************************************/
unsigned int Comp6DOF_n0m1::degToAngle(int degrees)
{
	unsigned int angle = (unsigned int)(((unsigned long)degrees * TRIGINT_ANGLES_PER_CYCLE) / 360);
	return angle;
}


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
int Comp6DOF_n0m1::atan2Int(int y, int x)
{

  int result;    /* angle in degrees times 100 */

  /* check for -32768 which is not handled correctly */
  if (x == -32768) x = -32767;
  if (y == -32768) y = -32767;

  /* check for quadrants */
  if ((x >= 0) && (y >= 0))  /* range 0 to 90 degrees */
  {
    result = atanInt(y, x);
  }
  else if ((x <= 0) && (y >= 0))  /* range 90 to 180 degrees */
  {
    result = (int)(18000 - (int)atanInt(y, (int)-x));
  }
  else if ((x <= 0) && (y <= 0))  /* range -180 to -90 degrees */
  {
    result = (int)((int)-18000 + atanInt((int)-y, (int)-x));
  }
  else /* x >=0 and y <= 0 giving range -90 to 0 degrees */
  {
    result = (int)(-atanInt((int)-y, x));
  }

  return result;

}

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
int Comp6DOF_n0m1::atanInt(int y, int x)
{

  long angle;   /* angle in degrees times 100 */
  int ratio;   /* ratio of y / x or vice versa */
  long tmp;     /* temporary variable */

  /* check for pathological cases */
  if ((x == 0) && (y == 0)) return (0);
  if ((x == 0) && (y != 0)) return (9000);

  /* check for non-pathological cases */
  if (y <= x)
  {
    ratio = divInt(y, x); /* return a fraction in range 0. to 32767 = 0. to 1. */
  }
  else
  {
    ratio = divInt(x, y); /* return a fraction in range 0. to 32767 = 0. to 1. */
  }

  /* first, third and fifth order polynomial approximation */
  angle = (long) K1 * (long) ratio;
  tmp = ((long) ratio >> 5) * ((long) ratio >> 5) * ((long) ratio >> 5);
  angle += (tmp >> 15) * (long) K2;
  tmp = (tmp >> 20) * ((long) ratio >> 5) * ((long) ratio >> 5);
  angle += (tmp >> 15) * (long) K3;
  angle = angle >> 15;

  /* check if above 45 degrees */
  if (y > x) angle = (int)(9000 - angle);

  /* for tidiness, limit result to range 0 to 9000 equals 0.0 to 90.0 degrees */
  if (angle < 0) angle = 0;
  if (angle > 9000) angle = 9000;

  return ((int) angle);

}

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
int Comp6DOF_n0m1::divInt(int y, int x)

{

  int tmp;               /* scratch */
  int r;                 /* result = y / x range 0., 1. returned in range 0 to 32767 */
  int delta;             /* delta on candidate result dividing each stage by factor of 2 */

  /* set result r to zero and binary search step to 16384 = 0.5 */
  r = 0;
  delta = 16384;                      /* set as 2^14 = 0.5 */

  /* to reduce quantization effects, boost x and y to the maximum signed 16 bit value */
  while ((x < 16384) && (y < 16384))
  {
    x = (int)(x + x);
    y = (int)(y + y);
  }

  /* loop over binary sub-division algorithm solving for ir*x = y */
  do
  {
    /* switch casts to int if everything works */
    unsigned long temper = (int)(r + delta);       /* itmp=ir+delta, the candidate solution */
    temper = (int)((temper * x) >> 15);
    tmp = (int)temper;

    if (tmp <= y) r += delta;
    
    delta = (int)(delta >> 1);     /* divide by 2 using right shift one bit */
    

  } while (delta >= MINDELTADIV);   /* last loop is performed for delta=MINDELTADIV */

  return (r);

}

/***********************************************************
 * 
 * sinInt
 * 
 ***********************************************************/
int Comp6DOF_n0m1::sinInt(unsigned int angle)
{
    angle += SINE_ROUNDING;
	long interp = BITS(angle, SINE_INTERP_WIDTH, SINE_INTERP_OFFSET);
	byte index = BITS(angle, SINE_INDEX_WIDTH, SINE_INDEX_OFFSET);

	bool isOddQuadrant = (angle & QUADRANT_LOW_MASK) == 0;
	bool isNegativeQuadrant = (angle & QUADRANT_HIGH_MASK) != 0;
    
	if (!isOddQuadrant) {
		index = SINE_TABLE_SIZE - 1 - index;
	}
	
    // Do calculations with 32 bits since the multiplication can overflow 16 bits
	long x1 = sin16LUT(index);
	long x2 = sin16LUT(index+1);
    long approximation = ((x2-x1) * interp) >> SINE_INTERP_WIDTH;
    
	int sine;
	if (isOddQuadrant) {
		sine = x1 + approximation;
	} else {
		sine = x2 - approximation;
	}
    
	if (isNegativeQuadrant) {
		sine *= -1;
	}

	return sine;
}

/***********************************************************
 * 
 *  lowPassInt
 * 
 *  The code is written for filtering the yaw (compass) angle
 *	ψ but can also be used for the roll angle.For the pitch 
 *	angle θ, which is restricted to the range -90° to 90°, 
 *	the final bounds check should be changed
 *	
 ***********************************************************/
int Comp6DOF_n0m1::lowPassInt(int input, int prevOut, int minBound, int maxBound)
{
  long tmpAngle;          /* temporary angle*100 deg: range -36000 to 36000 */
  int output = prevOut;             /* low pass filtered angle*100 deg: range -18000 to 18000 */
  unsigned int ANGLE_LPF = 32768 / 8; /* low pass filter: set to 32768 / N for N samples averaging */

  /* implement a modulo arithmetic exponential low pass filter on the yaw angle */
  /* compute the change in angle modulo 360 degrees */
  tmpAngle = (long)input - (long)output;

  if (tmpAngle > 18000) tmpAngle -= 36000;
  if (tmpAngle < -18000) tmpAngle += 36000;

  /* calculate the new low pass filtered angle */
  tmpAngle = (long)output + ((ANGLE_LPF * tmpAngle) >> 15);


  /* check that the angle remains in -180 to 180 deg bounds */
  if(maxBound == 180)
  {
    if (tmpAngle > 18000) tmpAngle -= 36000;
    if (tmpAngle < -18000) tmpAngle += 36000;
  }
  else if(maxBound == 90)
  {
    if (tmpAngle > 9000) tmpAngle = (int) (18000 - tmpAngle);
    if (tmpAngle < -9000) tmpAngle = (int) (-18000 - tmpAngle);
  }

  /* store the correctly bounded low pass filtered angle */
  return output = (int)tmpAngle;

}

/***********************************************************
 * 
 * LinearEquationsSolving
 *
 *
 *	 Matrix code snarfed from: http://www.hlevkin.com/NumAlg/LinearEquations.c
 * 
 *   return 1 if system not solving
 *	 nDim - system dimension
 *	 pfMatr - matrix with coefficients
 *	 pfVect - vector with free members
 *	 pfSolution - vector with system solution
 *	 pfMatr becames trianglular after function call
 *	 pfVect changes after function call
 *
 *	 Developer: Henry Guennadi Levkin
 *
 ***********************************************************/
int Comp6DOF_n0m1::linearEquationsSolving(int nDim, float* pfMatr, float* pfVect, float* pfSolution)
{
  float fMaxElem;
  float fAcc;
  int i , j, k, m;

  for(k=0; k<(nDim-1); k++) // base row of matrix
  {
    // search of line with max element
    fMaxElem = fabs( pfMatr[k*nDim + k] );
    m = k;
    for(i=k+1; i<nDim; i++)
    {
      if(fMaxElem < fabs(pfMatr[i*nDim + k]) )
      {
        fMaxElem = pfMatr[i*nDim + k];
        m = i;
      }
    }

    // permutation of base line (index k) and max element line(index m)
    if(m != k)
    {
      for(i=k; i<nDim; i++)
      {
        fAcc = pfMatr[k*nDim + i];
        pfMatr[k*nDim + i] = pfMatr[m*nDim + i];
        pfMatr[m*nDim + i] = fAcc;
      }
      fAcc = pfVect[k];
      pfVect[k] = pfVect[m];
      pfVect[m] = fAcc;
    }

    if( pfMatr[k*nDim + k] == 0.) return 1; // needs improvement !!!

    // triangulation of matrix with coefficients
    for(j=(k+1); j<nDim; j++) // current row of matrix
    {
      fAcc = - pfMatr[j*nDim + k] / pfMatr[k*nDim + k];
      for(i=k; i<nDim; i++)
      {
        pfMatr[j*nDim + i] = pfMatr[j*nDim + i] + fAcc*pfMatr[k*nDim + i];
      }
      pfVect[j] = pfVect[j] + fAcc*pfVect[k]; // free member recalculation
    }
  }

  for(k=(nDim-1); k>=0; k--)
  {
    pfSolution[k] = pfVect[k];
    for(i=(k+1); i<nDim; i++)
    {
      pfSolution[k] -= (pfMatr[k*nDim + i]*pfSolution[i]);
    }
    pfSolution[k] = pfSolution[k] / pfMatr[k*nDim + k];
  }

  return 0;
}

/***********************************************************
*
*	calSense
*
*   x, y, z are vectors of six measurements
* 
* Computes sensitivity and offset such that:
* 
* c = s * A + O
* 
* where c is the measurement, s is the sensitivity, O is the offset, and
* A is the field being measured  expressed as a ratio of the measured value
* to the field strength. aka a direction cosine.
* 
* A is what we really want and it is computed using the equation:
* 
* A = (c - O)/s
* 
***********************************************************/
int Comp6DOF_n0m1::calSense( int *x, int *y, int *z, int *S, int *O )
{
  int i;
  float A[25], *p;
  float f[5], X[5];
  float k1, k2;

  /* Fill in matrix A */

  p = A;
  for(i = 0; i < 5; i++)
  {
    *p++ = 2  * (x[i] - x[i+1]);
    *p++ = (long)y[i+1]*y[i+1] - (long)y[i]*y[i];
    *p++ = 2  * (y[i] - y[i+1]);
    *p++ = (long)z[i+1]*z[i+1] - (long)z[i]*z[i];
    *p++ = 2  * (z[i] - z[i+1]);
    f[i] = (long)x[i]*x[i] - (long)x[i+1]*x[i+1];
  }
  /* Solve AX=f */

  if(  linearEquationsSolving( 5, A, f, X) )
  {
	#ifdef DEBUG  
		Serial.println ("System not solvable");
	#endif
    return -1;
  }

  /* Compute sensitivities and offsets */

  k1 = X[1];
  k2 = X[3];
  O[0] = X[0];
  O[1] = X[2]/k1;
  O[2] = X[4]/k2;

  S[0] = intSqrt((long)(x[5]-O[0]) * (x[5]-O[0]) +
    k1*(long)(y[5]-O[1])*(y[5]-O[1]) +
    k2*(long)(z[5]-O[2])*(z[5]-O[2]));
  S[1] = intSqrt((long)S[0]*S[0]/k1);
  S[2] = intSqrt((long)S[0]*S[0]/k2);

  ktrust1_ = 1024 - (k1 * 1024);
  ktrust1_ = abs(ktrust1_);
  ktrust2_ = 1024 - (k2 * 1024);
  ktrust2_ = abs(ktrust2_);


  return 0;
}

/***********************************************************
*
*	magPos
*
*	get all axis combos (8) & load array
* 
***********************************************************/
boolean Comp6DOF_n0m1::deviantSpread(int XAxis, int YAxis, int ZAxis)
{
  
    byte combo = 0;
    // read raw compass data, do not use offsets
    //MagnetometerRaw  raw = compass.ReadRawAxis();

    // all combos filter - assigns polarity to a binary start (1 or 0) 
    int reject = 2;
    if ((XAxis > reject) && (abs(YAxis) > reject) && (abs(ZAxis) > reject)) { 
      combo += 1; 
    } 
    if ((abs(XAxis) > reject) && (YAxis > reject) && (abs(ZAxis) > reject)) { 
      combo += 2; 
    }
    if ((abs(XAxis) > reject) && (abs(YAxis) > reject) && (ZAxis > reject)) { 
      combo += 4; 
    }    
    if ((XAxis < -reject) && (YAxis < -reject) && (ZAxis < -reject))        { 
      combo  = 8; 
    }  

    // bad data filter - reject out of bounds - use sum of absolutes - similiar filter but less intensive then sqrt of sqrs
    // int Hyp = sqrt( ((long)raw.ZAxis*raw.ZAxis) + ((long)raw.YAxis*raw.YAxis) + ((long)raw.XAxis*raw.XAxis) );
    word Hyp =  abs(ZAxis) + abs(YAxis) + abs(XAxis);
    if ( (Hyp > (1000)) || (Hyp < (200)) ) {
      combo =0;
    }
    if ( (abs(XAxis) > 2047) || (abs(YAxis) > 2047) || (abs(ZAxis) > 2047)) {
      combo =0;
    }

    // if combination not done and data is ok (non zero) -> save result
    if (( bitRead(comboFlag, combo) == 0 ))
    {
      // store result 
      Xdata[comboCount] = XAxis;
      Ydata[comboCount] = YAxis;
      Zdata[comboCount] = ZAxis;
      // mark comboFlag position as done - set bit
      bitWrite(comboFlag, combo, 1) ;
      // increment index
      comboCount++;

#ifdef DEBUG
      Serial.print("Combo ");
      Serial.print(combo);
      Serial.print("  Count ");
      Serial.print(comboCount - 1);
      Serial.print("  xMag ");
      Serial.print(XAxis);
      Serial.print("  yMag ");
      Serial.print(YAxis);
      Serial.print("  zMag ");
      Serial.print(ZAxis);
      Serial.print("  Hyp ");
      Serial.print( Hyp );
      Serial.println("");  
#endif  
    }
 
 if(comboCount < 8)
 {
	return false;
 }
 else
 {
	return true;
 }

} 

/***********************************************************
*
*	Calculate Offsets
*
*	solve for x,y,z max & min, filter results, pick the best and average, return true if avgnum >3
* 
***********************************************************/
boolean Comp6DOF_n0m1::calOffsets()
{
  int Sens[3], Offset[3];
  int k =0; 
  int numtrusts = 0;

  // pre compute first run
  calSense( Xdata, Ydata, Zdata, Sens, Offset);
  // calculate an inverse trust value for data validity
  int invtrust = ktrust1_ + ktrust2_; 
  // store the last trust to determine if system improves
  int lastinvtrust = invtrust;

  // loop for each extra dataset over 6 (+2)
  for (int m = 0; m <2 ; m++)
  {
    k =0;

    // for all six datasets we compute offsets and trust while bubbling in new data   
    while( k < 6 )
    {
      // store the old data in temp
      int Xtemp =  Xdata[k];
      int Ytemp =  Ydata[k];
      int Ztemp =  Zdata[k];
      // now swap in new data 
      Xdata[k] =  Xdata[6 +m];
      Ydata[k] =  Ydata[6 +m];
      Zdata[k] =  Zdata[6 +m];    

      // compute next
      calSense( Xdata, Ydata, Zdata, Sens, Offset);
      // store the last trust to dtermine if system improves
      lastinvtrust = invtrust;
      // calculate an inverse trust value for data validity
      invtrust = ktrust1_ + ktrust2_;  

      // save items with inverse trust under 255 
      if ((invtrust >0) && (invtrust <255))
      {
        // increment counter for average
        numtrusts ++; 
        // sum up entires for average    
        xOffset_ += Offset[0];
        yOffset_ += Offset[1];
        zOffset_ += Offset[2];
      }    

#ifdef DEBUG  
      Serial.print   (invtrust); 
#endif     

      // if new trust is worse than last, swap old data back in
      if ( ( (invtrust > lastinvtrust) && (lastinvtrust > 0) ) || (invtrust == 0) )
      {
#ifdef DEBUG 
        Serial.print (" Worse ");
#endif
        Xdata[k] = Xtemp;  
        Ydata[k] = Ytemp;  
        Zdata[k] = Ztemp;
      }
      else // trust situation has improved - continue bubbling
      {
#ifdef DEBUG 
        Serial.print (" Better");
#endif
        Xdata[6 +m] = Xtemp; 
        Ydata[6 +m] = Ytemp;
        Zdata[6 +m] = Ztemp;   
      }

      // increment datset postion counter
      k++;

#ifdef DEBUG 
      Serial.print ("  Xo: ");
      Serial.print (Offset[0]); 
      Serial.print ("  Yo: ");
      Serial.print (Offset[1]);      
      Serial.print ("  Zo: ");
      Serial.print (Offset[2]);
      Serial.println ("");
#endif 

    } // end of while
  } // end of for

  // average offsets with number collected
  xOffset_ /= numtrusts;
  yOffset_ /= numtrusts;
  zOffset_ /= numtrusts;

if(numtrusts < 4) // returns false if not solved well enough
 {
	comboFlag = 1; // wipe combo flag for next pass
	comboCount = 0; // wipe combocount for next pass
	return false;
 }
 else
 {
	return true;
 }

#ifdef DEBUG      
  Serial.println ("");
  Serial.print(numtrusts);
  Serial.print  (" ");
  Serial.print ("  Xfin: ");
  Serial.print (xOffset_); 
  Serial.print ("  Yfin: ");
  Serial.print (yOffset_);      
  Serial.print ("  Zfin: ");
  Serial.println (zOffset_);   
  Serial.println ("");
  // reset offsets for debugging
  // xOffset_ =0;
  // yOffset_ =0;
  // zOffset_ =0;
#endif
}

/***********************************************************
*
*	intSqrt
*
*	fast integer sqrt
* 
***********************************************************/
unsigned long Comp6DOF_n0m1::intSqrt(unsigned long val) 
{ 
    unsigned long mulMask = 0x0008000; 
    unsigned long retVal = 0; 

    if (val > 0) { 
        while (mulMask != 0) { 
            retVal |= mulMask; 
            if ((retVal * retVal) > val) { 
                retVal &= ~mulMask; 
            } 

            mulMask >>= 1; 
        } 
    } 

    return retVal; 
}
