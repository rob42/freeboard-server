#!/bin/bash
#
# Script to setup and install freeboard to the beaglebone
#
#

# Make the pi user, need ubuntu adm dialout cdrom floppy audio dip video admin
# sudo adduser pi --ingroup ubuntu
sudo usermod -G adm,dialout,cdrom,floppy,audio,dip,video,admin pi

####################
#get the date!


sudo apt-get update
sudo apt-get upgrade

# add software
sudo apt-get install zip unzip dnsmasq
sudo apt-get install wpasupplicant usbutils wireless-tools iw hostapd

#we assume the java and freeboard archives are here already.
###################
# install java
tar xvzf jdk-8-ea-b124-linux-arm-vfp-hflt-17_jan_2014.tar.gz 
#link a predictable dir to this one
JDK_CURRENT=$(find -maxdepth 1 -type d -name 'jdk*'| head -n1)
rm -rf jdk1.8
ln -s $JDK_CURRENT jdk1.8

###################
# extract the freeboard-server archive
unzip freeboard-server-*-all.zip
#copy the freeboard directory back to here
FREEBOARD_CURRENT=$(find -maxdepth 1 -type d -name 'freeboard-server-*'| head -n1)
mkdir freeboard
cp -rf $FREEBOARD_CURRENT/freeboard/* freeboard/
#we need the logs directory
mkdir freeboard/logs

###################
#Install autostart
#copy and rename the file to /etc/init.d - you must do this as root hence 'sudo'
sudo cp -rf ./freeboard/freeboard.init.d.sh /etc/init.d/freeboard
# change owner and permissions
sudo chmod 755 /etc/init.d/freeboard
sudo chown root:root /etc/init.d/freeboard
# add to startup process
sudo update-rc.d freeboard defaults

# stop the apache server, its in the way
sudo update-rc.d -f apache2 remove
sudo update-rc.d -f udhcpd remove

##################
#Setup networking
#/etc/network/interfaces
if ! grep -Fq "#Run as wireless access point" /etc/network/interfaces 
then
    sudo echo "" >> /etc/network/interfaces
    sudo echo "#Run as wireless access point" >> /etc/network/interfaces
    sudo echo "auto wlan0" >> /etc/network/interfaces
    sudo echo "iface wlan0 inet static" >> /etc/network/interfaces
    sudo echo "    address 192.168.0.1" >> /etc/network/interfaces
    sudo echo "    netmask 255.255.255.0" >> /etc/network/interfaces
    sudo echo "    gateway 192.168.0.1" >> /etc/network/interfaces
fi
#/etc/hostname
sudo echo "freeboard" > /etc/hostname

#/etc/hosts
sudo echo "# www.zkoss.org is in here to speed the web interface when there is no internet connection" > /etc/hosts
sudo echo "127.0.0.1       localhost www.zkoss.org" >> /etc/hosts
sudo echo "# Note: the a.freeboard, b.freeboard, etc entries are used by the Leaflet mapping lib to speed up map loading, you only need them if you are using the chartplotter." >> /etc/hosts
sudo echo "192.168.0.1	freeboard a.freeboard b.freeboard c.freeboard d.freeboard" >> /etc/hosts

#dnsmasq
# Configuration file for dnsmasq.
sudo echo "interface=wlan0" > /etc/dnsmasq.conf
sudo echo "dhcp-range=192.168.0.10,192.168.0.128,12h" >> /etc/dnsmasq.conf
sudo echo "" >> /etc/dnsmasq.conf
sudo echo "#usb interface " >> /etc/dnsmasq.conf
sudo echo "interface  usb0" >> /etc/dnsmasq.conf
sudo echo "dhcp-range=192.168.7.1,192.168.7.1,12h" >> /etc/dnsmasq.conf


##################
#set up wifi

sudo mkdir /etc/hostapd
sudo mv /etc/hostapd/hostapd.conf /etc/hostapd/hostapd.conf.bak

# write hostapd.conf
sudo echo "interface=wlan0" > /etc/hostapd/hostapd.conf
sudo echo "driver=nl80211" >> /etc/hostapd/hostapd.conf
sudo echo "ctrl_interface=/var/run/hostapd" >> /etc/hostapd/hostapd.conf
sudo echo "ctrl_interface_group=0" >> /etc/hostapd/hostapd.conf
sudo echo "#use YOUR boatname here!" >> /etc/hostapd/hostapd.conf
sudo echo "ssid=freeboard" >> /etc/hostapd/hostapd.conf
sudo echo "hw_mode=g" >> /etc/hostapd/hostapd.conf
sudo echo "channel=10" >> /etc/hostapd/hostapd.conf
sudo echo "wpa=1" >> /etc/hostapd/hostapd.conf
sudo echo "#use your passphrase here!" >> /etc/hostapd/hostapd.conf
sudo echo "wpa_passphrase=freeboard" >> /etc/hostapd/hostapd.conf
sudo echo "wpa_key_mgmt=WPA-PSK" >> /etc/hostapd/hostapd.conf
sudo echo "wpa_pairwise=TKIP" >> /etc/hostapd/hostapd.conf
sudo echo "rsn_pairwise=CCMP" >> /etc/hostapd/hostapd.conf
sudo echo "beacon_int=100" >> /etc/hostapd/hostapd.conf
sudo echo "auth_algs=3" >> /etc/hostapd/hostapd.conf
sudo echo "wmm_enabled=1" >> /etc/hostapd/hostapd.conf

#/etc/default/hostapd
sudo echo "DAEMON_CONF=\"/etc/hostapd/hostapd.conf\"" > /etc/default/hostapd