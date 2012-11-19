
void printdata(void)
{    

#if PRINT_BINARY != 1  //Print either Ascii or binary messages

	Serial.print("!!!VER:");
	Serial.print(SOFTWARE_VER);  //output the software version
	Serial.print(",");
	
	#if PRINT_ANALOGS==1
		Serial.print("AN0:");
		Serial.print(read_adc(0)); //Reversing the sign. 
		Serial.print(",AN1:");
		Serial.print(read_adc(1));
		Serial.print(",AN2:");
		Serial.print(read_adc(2));  
		Serial.print(",AN3:");
		Serial.print(read_adc(3));
		Serial.print (",AN4:");
		Serial.print(read_adc(4));
		Serial.print (",AN5:");
		Serial.print(read_adc(5));
		Serial.print (",");
	#endif
      
	#if PRINT_DCM == 1
		Serial.print ("EX0:");
		Serial.print(convert_to_dec(DCM_Matrix[0][0]));
		Serial.print (",EX1:");
		Serial.print(convert_to_dec(DCM_Matrix[0][1]));
		Serial.print (",EX2:");
		Serial.print(convert_to_dec(DCM_Matrix[0][2]));
		Serial.print (",EX3:");
		Serial.print(convert_to_dec(DCM_Matrix[1][0]));
		Serial.print (",EX4:");
		Serial.print(convert_to_dec(DCM_Matrix[1][1]));
		Serial.print (",EX5:");
		Serial.print(convert_to_dec(DCM_Matrix[1][2]));
		Serial.print (",EX6:");
		Serial.print(convert_to_dec(DCM_Matrix[2][0]));
		Serial.print (",EX7:");
		Serial.print(convert_to_dec(DCM_Matrix[2][1]));
		Serial.print (",EX8:");
		Serial.print(convert_to_dec(DCM_Matrix[2][2]));
		Serial.print (",");
	#endif

	#if PRINT_EULER == 1
		Serial.print("RLL:");
		Serial.print(ToDeg(roll));
		Serial.print(",PCH:");
		Serial.print(ToDeg(pitch));
		Serial.print(",YAW:");
		Serial.print(ToDeg(yaw));
		Serial.print(",IMUH:");
		Serial.print((imu_health>>8)&0xff);
		Serial.print (",");
	#endif
      
	#if PRINT_MAGNETOMETER == 1
                #if BOARD_VERSION < 3
		Serial.print("MGX:");
		Serial.print(APM_Compass.Mag_X);
		Serial.print (",MGY:");
		Serial.print(APM_Compass.Mag_Y);
		Serial.print (",MGZ:");
		Serial.print(APM_Compass.Mag_Z);
		Serial.print (",MGH:");
		Serial.print(ToDeg(APM_Compass.Heading));
		Serial.print (",");
                #endif
                #if BOARD_VERSION == 3
                Serial.print("MGX:");
		Serial.print(mag_x);
		Serial.print (",MGY:");
		Serial.print(mag_y);
		Serial.print (",MGZ:");
		Serial.print(mag_z);
		Serial.print (",MGH:");
		Serial.print(ToDeg(Heading));
		Serial.print (",");
                #endif
	#endif
      
	#if USE_BAROMETER == 1
		Serial.print("Temp:");
		Serial.print(temp_unfilt/20.0);      // Convert into degrees C
		alti();
		Serial.print(",Pressure: ");
		Serial.print(press);            
//		Serial.print(press>>2);       // Convert into Pa       
		Serial.print(",Alt: ");
		Serial.print(press_alt/1000);  // Original floating point full solution in meters
		Serial.print (",");
	#endif
      
	#if PRINT_GPS == 1
		if(GPS.new_data==1) {
			GPS.new_data=0;
			Serial.print("LAT:");
			Serial.print(GPS.latitude);
			Serial.print(",LON:");
			Serial.print(GPS.longitude);
			Serial.print(",ALT:");
			Serial.print(GPS.altitude/100);    // meters
			Serial.print(",COG:");
			Serial.print(GPS.ground_course/100);	// degrees
			Serial.print(",SOG:");
			Serial.print(GPS.ground_speed/100);
			Serial.print(",FIX:");
			Serial.print((int)GPS.fix);				// 1 = good fix
			Serial.print(",SAT:"); 
			Serial.print((int)GPS.num_sats);
			Serial.print (",");
			#if PERFORMANCE_REPORTING == 1
				gps_messages_sent++;
			#endif
		}
	#endif
      
	Serial.print("TOW:");
	Serial.print(GPS.time);
	Serial.println("***");    

#else
	//  This section outputs binary data messages
	//  Conforms to new binary message standard (12/31/09)
	byte IMU_buffer[22];
	int tempint;
	int ck;
	long templong;
	byte IMU_ck_a=0;
	byte IMU_ck_b=0;
      
	//  This section outputs the gps binary message when new gps data is available
	if(GPS.new_data==1) {
		#if PERFORMANCE_REPORTING == 1
			gps_messages_sent++;
		#endif
		GPS.new_data=0;
		Serial.print("DIYd");  // This is the message preamble
		IMU_buffer[0]=0x13;
		ck=19;
		IMU_buffer[1] = 0x03;      

		templong = GPS.longitude; //Longitude *10**7 in 4 bytes
		IMU_buffer[2]=templong&0xff;
		IMU_buffer[3]=(templong>>8)&0xff;
		IMU_buffer[4]=(templong>>16)&0xff;
		IMU_buffer[5]=(templong>>24)&0xff;
      
		templong = GPS.latitude; //Latitude *10**7 in 4 bytes
		IMU_buffer[6]=templong&0xff;
		IMU_buffer[7]=(templong>>8)&0xff;
		IMU_buffer[8]=(templong>>16)&0xff;
		IMU_buffer[9]=(templong>>24)&0xff;
      
		#if USE_BAROMETER==0
			tempint=GPS.altitude / 100;   // Altitude MSL in meters * 10 in 2 bytes
		#else
			alti();
			tempint = (press_alt * ALT_MIX + GPS.altitude * (100-ALT_MIX)) / 10000;	//Blended GPS and pressure altitude
		#endif
		IMU_buffer[10]=tempint&0xff;
		IMU_buffer[11]=(tempint>>8)&0xff;
      
		tempint=GPS.ground_speed;   // Speed in M/S * 100 in 2 bytes
		IMU_buffer[12]=tempint&0xff;
		IMU_buffer[13]=(tempint>>8)&0xff;
        
		tempint=GPS.ground_course;   // course in degreees * 100 in 2 bytes
		IMU_buffer[14]=tempint&0xff;
		IMU_buffer[15]=(tempint>>8)&0xff;
        
		IMU_buffer[16]=GPS.time&0xff;
		IMU_buffer[17]=(GPS.time>>8)&0xff;
		IMU_buffer[18]=(GPS.time>>16)&0xff;
		IMU_buffer[19]=(GPS.time>>24)&0xff;

        	IMU_buffer[20]=(imu_health>>8)&0xff;

		for (int i=0;i<ck+2;i++) Serial.print (IMU_buffer[i]);
		
		for (int i=0;i<ck+2;i++) {
			IMU_ck_a+=IMU_buffer[i];  //Calculates checksums
			IMU_ck_b+=IMU_ck_a;       
		}
      	Serial.print(IMU_ck_a);
      	Serial.print(IMU_ck_b);
  
	} else {
      
		// This section outputs the IMU orientatiom message
		Serial.print("DIYd");  // This is the message preamble
		IMU_buffer[0]=0x06;
		ck=6;
		IMU_buffer[1] = 0x02;      

		tempint=ToDeg(roll)*100;  //Roll (degrees) * 100 in 2 bytes
		IMU_buffer[2]=tempint&0xff;
		IMU_buffer[3]=(tempint>>8)&0xff;
      
		tempint=ToDeg(pitch)*100;   //Pitch (degrees) * 100 in 2 bytes
		IMU_buffer[4]=tempint&0xff;
		IMU_buffer[5]=(tempint>>8)&0xff;
      
		templong=ToDeg(yaw)*100;  //Yaw (degrees) * 100 in 2 bytes
		if(templong>18000) templong -=36000;
		if(templong<-18000) templong +=36000;
		tempint = templong;
		IMU_buffer[6]=tempint&0xff;
		IMU_buffer[7]=(tempint>>8)&0xff;
      
		for (int i=0;i<ck+2;i++) Serial.print (IMU_buffer[i]);
	  
		for (int i=0;i<ck+2;i++) {
			IMU_ck_a+=IMU_buffer[i];  //Calculates checksums
			IMU_ck_b+=IMU_ck_a;       
		}
		Serial.print(IMU_ck_a);
		Serial.print(IMU_ck_b);
          
    }
        
#endif  
}

#if PERFORMANCE_REPORTING == 1
void printPerfData(long time)
{

// This function outputs a performance monitoring message (used every 20 seconds) 
//  Can be either binary or human readable
#if PRINT_BINARY == 1
      byte IMU_buffer[30];
      int ck;
      byte IMU_ck_a=0;
      byte IMU_ck_b=0;
      
      	Serial.print("DIYd");  // This is the message preamble
		IMU_buffer[0]=0x11;
		ck=17;
      	IMU_buffer[1] = 0x0a;      

        	//Time for this reporting interval in millisecons
        	IMU_buffer[2]=time&0xff;
        	IMU_buffer[3]=(time>>8)&0xff;
        	IMU_buffer[4]=(time>>16)&0xff;
        	IMU_buffer[5]=(time>>24)&0xff;
      
        	IMU_buffer[6]=mainLoop_count&0xff;
        	IMU_buffer[7]=(mainLoop_count>>8)&0xff;
        
        	IMU_buffer[8]=G_Dt_max&0xff;
        	IMU_buffer[9]=(G_Dt_max>>8)&0xff;
      
        	IMU_buffer[10]=gyro_sat_count;
        	IMU_buffer[11]=adc_constraints;
        	IMU_buffer[12]=renorm_sqrt_count;
        	IMU_buffer[13]=renorm_blowup_count;
        	IMU_buffer[14]=0;								// gps_payload_error_count - We don't have access to this with GPS library
        	IMU_buffer[15]=0;								// gps_checksum_error_count - We don't have access to this with GPS library
        	IMU_buffer[16]=0;								// gps_pos_fix_count - We don't have access to this with GPS library
        	IMU_buffer[17]=0;								// gps_nav_fix_count - We don't have access to this with GPS library
        	IMU_buffer[18]=gps_messages_sent;				// This metric is equal to the number of gps fixes received

      	for (int i=0;i<ck+2;i++) Serial.print (IMU_buffer[i]);  
      	for (int i=0;i<ck+2;i++) {
          		IMU_ck_a+=IMU_buffer[i];  //Calculates checksums
          		IMU_ck_b+=IMU_ck_a;       
      	}
      	Serial.print(IMU_ck_a);
      	Serial.print(IMU_ck_b);
      
#else


    Serial.print("PPP");
    Serial.print("pTm:");
    Serial.print(time,DEC);
    Serial.print(",mLc:");
    Serial.print(mainLoop_count,DEC);
    Serial.print(",DtM:");
    Serial.print(G_Dt_max,DEC);
    Serial.print(",gsc:");
    Serial.print(gyro_sat_count,DEC);
    Serial.print(",adc:");
    Serial.print(adc_constraints,DEC);
    Serial.print(",rsc:");
    Serial.print(renorm_sqrt_count,DEC);
    Serial.print(",rbc:");
    Serial.print(renorm_blowup_count,DEC);
    Serial.print(",gms:");
    Serial.print(gps_messages_sent,DEC);
    Serial.print(",imu:");
    Serial.print((imu_health>>8),DEC);
    Serial.print(",***");
#endif
		// Reset counters
        mainLoop_count = 0;
        G_Dt_max  = 0;
        gyro_sat_count = 0;
        adc_constraints = 0;
        renorm_sqrt_count = 0;
        renorm_blowup_count = 0;
        gps_messages_sent = 0;
        
}

#endif


long convert_to_dec(float x)
{
  return x*10000000;
}

