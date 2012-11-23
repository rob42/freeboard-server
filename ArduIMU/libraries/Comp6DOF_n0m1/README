Name    : MMA8453_n0m1 Library                         
Author  : Noah Shibley, Michael Grant, NoMi Design Ltd. http://n0m1.com                       
Date    : Feb 1st 2012                                     
Version : 0.1                                              
Notes   : Arduino Library for compass tilt compensation and hard iron offset.
	  Part of this code was ported to C from the Freescale appnote AN4248. 
	  http://www.freescale.com/files/sensors/doc/app_note/AN4248.pdf 
	  The sine function comes from Dave Dribin's TrigInt lib. 
	  https://bitbucket.org/ddribin/trigint
			
    Dependencies:	
	none
	
	
			
List of Functions:

Function: compCompass
Description: compass compensation calculation
Parameters: Input raw compass values, xyz, 
	    and accelerometer values xyz,
	    boolean lowpass enable	 		
------------------------------------------------------
Function: roll
Return: (int) returns roll result -18000 to 18000 
------------------------------------------------------
Function: pitch
Return: (int) returns pitch result -9000 to 9000
------------------------------------------------------ 
Function: yaw
Return: (int) returns yaw result -18000 to 18000
------------------------------------------------------
Function: rollf
Return: (float) returns roll as float value -180.00 to 180.00 
------------------------------------------------------
Function: pitchf
Return: (float) returns pitch as float value -90.00 to 90.00
------------------------------------------------------ 
Function: yawf
Return: (float) returns yaw as float value -180.00 to 180.00
------------------------------------------------------
Function: xAxisComp
Return: (int) return tilt compensated xAxis compass result
------------------------------------------------------
Function: yAxisComp
Return: (int) return tilt compensated yAxis compass result
------------------------------------------------------
Function: zAxisComp
Return: (int) return tilt compensated zAxis compass result 
------------------------------------------------------ 
Function: xHardOff
Return: (int) return hardiron xOffset for compass
------------------------------------------------------
Function: yHardOff
Return: (int) return hardiron yOffset for compass
------------------------------------------------------
Function: zHardOff
Return: (int) return hardiron zOffset for compass
------------------------------------------------------
Function: deviantSpread
Description: get all axis combos (8) & load array
Parameters: int xAxis, int yAxis, int zAxis
------------------------------------------------------  
Function: calOffsets
Description: solve for x,y,z max & min, filter results, pick the best and average
Return: (boolean) return true if avgnum >3
------------------------------------------------------ 
Function: atan2Int
Parameters: int xInput, int yInput