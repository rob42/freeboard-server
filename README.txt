Freeboard Notes

This is the main freeboard server, which runs in java on the Raspberry Pi. The arduino based ArduIMU and FreeboardPLC connect to this via a (powered) USB hub. Using a decent powered hub is important - see the Raspberry Pi site.

This is only just uploaded, so expect some pain getting it all setup/building/etc. Email me for help.

Hardware:
	Raspberry Pi (Model B, 256 Mb RAM) - currently using soft-fp image (for sun java)
		make sure to 'sudo apt-get install  gdal-bin python-gdal imagemagik librxtx-java'
		add sun (Oracle) jdk7 manually, into /home/pi/jdk1.7.0_06
		create dir /home/pi/freeboard
		copy the following from your dev system to /home/pi/freeboard
		 conf/  
                 freeboard/  
		 install.sh  
		 logs/  
		 mapcache/  - you create this when you import charts, I currently only have NZ charts, I will import a world map shortly, so you can at least see something basic.
		 start.sh  
		 stop.sh  
		 target/freeboard-server-0.0.1-SNAPSHOT-jar-with-dependencies.jar  
		
		cd /home/pi/freeboard
		execute ./start.sh

	Arduino - Mega 1260 - load as per  freeboardPLC project
		connect via USB

	ArduIMU - load as per FreeBoard ArduIMU project
		connect via USB
	
	Assuming IP_ADDRESS is either the ip address or dns name of your raspberry pi (eg your boat name)
	freeboard will be available on http://IP_ADDRESS:8080/freeboard

Hostapd notes:
  Use a supported wifi dongle - try RT5370 USB - works but seems to suffer arbitrary freezes (possibly due to a known driver problem)
  I am trying an atheros ath9k based dongle - will report outcome. If you are really stuck, use ethernet for dev, and a real Wifi access point on the boat for now.

  'sudo apt-get install wpasupplicant usbutils wireless-tools iw hostapd dnsmasq'
  'iw list' must return 
	      Supported interface modes:
                 * IBSS
                 * managed
                 * AP         ** wont work if you dont get this!!**
                 * AP/VLAN
                 * WDS
                 * monitor
                 * mesh point

Setup files:
/etc/hostname
 #use YOUR boats name here!
  motu 
/etc/hosts
  127.0.0.1  	  localhost www.zkoss.org
  #use YOUR boats name here!
  192.168.0.1     motu 
  224.0.0.1       all-systems.mcast.net

  ::1             localhost ip6-localhost ip6-loopback
  fe00::0         ip6-localnet
  ff00::0         ip6-mcastprefix
  ff02::1         ip6-allnodes
  ff02::2         ip6-allrouters




/etc/network/interfaces
  #allow-hotplug wlan0
  iface wlan0 inet static
        address 192.168.0.1
        netmask 255.255.255.0
        gateway 192.168.0.1

/etc/dnsmasq.conf
  # Configuration file for dnsmasq.
  interface=wlan0
  dhcp-range=192.168.0.10,192.168.0.128,12hinterface=wlan0

/etc/hostapd/hostapd.conf
  driver=nl80211
  ctrl_interface=/var/run/hostapd
  ctrl_interface_group=0
  ssid=Motu
  hw_mode=g
  channel=10
  beacon_int=100
  auth_algs=3
  wmm_enabled=1
  #enable for WPA
  #wpa=2
  #wpa_psk=928519398acf811e96f5dcac68a11d6aa876140599be3dd49612e760a2aaac0e
  #wpa_passphrase=raspiwlan
  #wpa_key_mgmt=WPA-PSK
  #wpa_pairwise=CCMP
  #rsn_pairwise=CCMP
  
/etc/default/hostapd
  DAEMON_CONF="/etc/hostapd/hostapd.conf"

  
