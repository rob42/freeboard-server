#!/bin/bash
#
# Copyright 2012,2013 Robert Huitema robert@42.co.nz
# 
#
# This file is part of FreeBoard.
#
#  FreeBoard is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  FreeBoard is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with FreeBoard.  If not, see <http://www.gnu.org/licenses/>.
#
# Script to setup and install freeboard to the raspberry pi
#
# What this tries to do :
# assumes you have git cloned the freeboard-server repo, and are in it
# updates os
# Checks and adds user groups
# Adds apps we need
# Sets up freebaord autostart
# Setup wifi and networking ready for the boat.

# Now you can run ./start.sh,  go to http://???:8080/freeboard to see.
#
# logs will be in /home/pi/freeboard-server/logs/start.log - send that if its still not up
#
#

# Check the pi user, need  adm dialout cdrom floppy audio dip video admin

echo "Modifying user groups.."
sudo usermod -G adm,dialout,cdrom,floppy,audio,dip,video,admin pi

####################
#make sure we are up to date
echo "Update to most current version.."
sudo apt-get update
sudo apt-get upgrade
echo "Install apps we need.."
# add software we need
sudo apt-get install zip unzip dnsmasq
sudo apt-get install wpasupplicant usbutils wireless-tools iw hostapd

###################

# extract the freeboard-server archive
#FREEBOARD_CURRENT=freeboard-server-0.5.12-SNAPSHOT
echo "Extract the freeboard-server archive"
#FREEBOARD_CURRENT=freeboard-server-0.5.15-SNAPSHOT
FREEBOARD_CURRENT=$(find -maxdepth 1 -type f -name 'freeboard-server-*-SNAPSHOT-all.zip'| head -n1)
echo "#FREEBOARD_CURRENT = "$FREEBOARD_CURRENT
unzip $FREEBOARD_CURRENT
file_name=${FREEBOARD_CURRENT##*/}
dir_name=${file_name%-all.*}
echo "#    Directory name = "$dir_name

#copy the freeboard directory back to here
#cp -rf $FREEBOARD_CURRENT/freeboard .
cp -rf $dir_name/freeboard .

cd freeboard
echo "#   Strip out the Win line endings on all *.sh and *.bat files"
echo "#   *.sh files"
find ./ -maxdepth 1 -name '*.sh' -exec echo "{}" \;
find ./ -maxdepth 1 -name '*.sh' -exec sed -i -e 's/\r$//' {} \;
echo "#   *.bat files"
find ./ -maxdepth 1 -name '*.bat' -exec echo "{}" \;
find ./ -maxdepth 1 -name '*.bat' -exec sed -i -e 's/\r$//' {} \;

cd ..


#we need the logs directory
echo "Make sure we have a logs dir.."
mkdir logs

###################
#Install autostart
echo "#   Install autostart"
#copy and rename the file to /etc/init.d - you must do this as root hence 'sudo'
echo "Install autostart for freeboard.."
sudo cp -rf ./freeboard.init.d.sh /etc/init.d/freeboard
# change owner and permissions
sudo chmod 755 /etc/init.d/freeboard
sudo chown root:root /etc/init.d/freeboard
# add to startup process
sudo update-rc.d freeboard defaults

# stop the apache server, its in the way

echo "#   Stop the apache server, its in the way"

sudo update-rc.d -f apache2 remove
sudo update-rc.d -f udhcpd remove

##################
#Optional: if you are having problems exit here

#Setup networking
echo "#   Setup networking"

#/etc/network/interfaces
if ! grep -Fq "#Run as wireless access point" /etc/network/interfaces 
then
    sudo sh -c 'echo "" >> /etc/network/interfaces'
    sudo sh -c 'echo "#Run as wireless access point" >> /etc/network/interfaces'
    sudo sh -c 'echo "auto wlan0" >> /etc/network/interfaces'
    sudo sh -c 'echo "iface wlan0 inet static" >> /etc/network/interfaces'
    sudo sh -c 'echo "    address 192.168.0.1" >> /etc/network/interfaces'
    sudo sh -c 'echo "    netmask 255.255.255.0" >> /etc/network/interfaces'
    sudo sh -c 'echo "    gateway 192.168.0.1" >> /etc/network/interfaces'
fi
echo "#   Setup hostname"
#/etc/hostname
echo "Set hostname to freeboard, and adjust hosts..."
sudo hostname freeboard
sudo sh -c 'echo "freeboard" > /etc/hostname'

#/etc/hosts
echo "#   Setup /etc/hosts"
sudo sh -c 'echo "# www.zkoss.org is in here to speed the web interface when there is no internet connection" > /etc/hosts'
sudo sh -c 'echo "127.0.0.1       localhost www.zkoss.org" >> /etc/hosts'
sudo sh -c 'echo "# Note: the a.freeboard, b.freeboard, etc entries are used by the Leaflet mapping lib to speed up map loading, you only need them if you are using the chartplotter." >> /etc/hosts'
sudo sh -c 'echo "192.168.0.1	freeboard a.freeboard b.freeboard c.freeboard d.freeboard" >> /etc/hosts'

#dnsmasq
echo "#   Setup dnsmasq"
# Configuration file for dnsmasq.
echo "Setup dnsmasq.."
sudo sh -c 'echo "interface=wlan0" > /etc/dnsmasq.conf'
sudo sh -c 'echo "dhcp-range=192.168.0.10,192.168.0.128,12h" >> /etc/dnsmasq.conf'
sudo sh -c 'echo "" >> /etc/dnsmasq.conf'
#sudo sh -c 'echo "#usb interface " >> /etc/dnsmasq.conf'
#sudo sh -c 'echo "interface  usb0" >> /etc/dnsmasq.conf'
#sudo sh -c 'echo "dhcp-range=192.168.7.1,192.168.7.1,12h" >> /etc/dnsmasq.conf'


##################
#set up wifi

echo "#   Set up wifi"

sudo mkdir /etc/hostapd
sudo mv /etc/hostapd/hostapd.conf /etc/hostapd/hostapd.conf.bak

# write hostapd.conf
echo "#   Write hostapd.conf"
sudo sh -c 'echo "interface=wlan0" > /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "driver=nl80211" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "ctrl_interface=/var/run/hostapd" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "ctrl_interface_group=0" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "#use YOUR boatname here!" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "ssid=freeboard" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "hw_mode=g" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "channel=10" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "wpa=1" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "#use your passphrase here!" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "wpa_passphrase=freeboard" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "wpa_key_mgmt=WPA-PSK" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "wpa_pairwise=TKIP" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "rsn_pairwise=CCMP" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "beacon_int=100" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "auth_algs=3" >> /etc/hostapd/hostapd.conf'
sudo sh -c 'echo "wmm_enabled=1" >> /etc/hostapd/hostapd.conf'

#/etc/default/hostapd

echo "#   Setup /etc/default/hostapd"
sudo sh -c 'echo "DAEMON_CONF=\"/etc/hostapd/hostapd.conf\"" > /etc/default/hostapd'
