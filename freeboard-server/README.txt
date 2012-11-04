Freeboard Notes

Hardware:
	Raspberry Pi (Model B, 256 Mb RAM) - currently using soft-fp image (for sun java)
		make sure to 'sudo apt-get install  gdal-bin python-gdal imagemagik librxtx-java'
		add sun (Oracle) jdk7 manually, into /home/pi/jdk1.7.0_06
		create dir /home/pi/freeboard
		create /home/pi/freeboard/target
		unpack freeboard.tar.gz in /home/pi/freeboard
		create /home/pi/freeboard/target
		copy freeboard-server-0.0.1-SNAPSHOT-jar-with-dependencies.jar to target/
		cd /home/pi/freeboard
		execute ./start.sh
	Arduino - Mega 1260
		load with image
		connect as per details
	
	Assuming IP_ADDRESS is either the ip address or dns name of your raspberry pi
	freeboard will be available on http://IP_ADDRESS:8080