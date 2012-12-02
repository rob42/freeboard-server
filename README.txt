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

Hostapd notes:
  Use a supported wifi dongle - try RT5370 USB - works for me.
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

/etc/network/interfaces
  #allow-hotplug wlan0
  iface wlan0 inet static
        address 192.168.0.1
        netmask 255.255.255.0
        gateway 192.168.0.1

/etc/dnsmasq.conf
  # Configuration file for dnsmasq.
  interface=wlan0
  no-hosts
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

  
