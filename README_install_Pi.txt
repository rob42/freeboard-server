RPi install from scratch

Get Rpi.
Get 8Gb Class10 Micro SD card.
Plug in USB keyboard/mouse
Plug in HDMI screen

Use instructions to load Raspbian with NOOBS from https://www.raspberrypi.org/help/noobs-setup/
Select Raspbian, install
Select Desktop boot option
Select Overclock medium
Advanced options
  Enable ssh

Put micro SD back into laptop
  copy install-rpi.sh to /home/pi
  copy freeboard*.all.zip to /home/pi

Put micro SD back in Rpi, connect to the internet, and reboot
From top bar, open file manager, select install_rpi.sh
  right-click>Properties>Permissions
    set Execute to 'Anybody'
  Click OK to exit

From top bar click LXTerminal to open a commandline terminal, (expand it to make a decent size)
From here if you see [Tab] or [Enter] below it means hit Tab or Enter etc, but you probably worked that out :-)

The prompt should look like `pi@raspberrypi ~ $ ` at this stage

pi@raspberrypi ~ $ ./install_rpi.sh [Enter]

This will take some time as it will update all software and install some additional packages. Make tea, coffee, or whatever...
When it asks you to confirm you want to install additional software type Y[Enter]

If this is the second time you have run install_rpi.sh it will ask you if you want to overwrite various freeboard files, answer  A[Enter] (for All)

When completed you will return to the original prompt.

Reboot the Rpi (you can use the top bar menu). Its hostname will be 'freeboard' now

At this point you need to check if the server is running, so open Menu>Accessories>Task Manager
Scroll down the list of Commands and look for 'java'. If its there its running. If not:
  Open the LXTerminal commandline again

pi@freeboard ~ $ cd freeboard [Enter]
pi@freeboard ~ $ ./startpc.sh [Enter]

You should now see a scrolling output of logs rolling up the screen. 
There will be clues to problems in there, if you cant work it out cut and paste (not screen shots pls) the output and email with full details.


Assuming you see the java process running, from the top menu start the Web Browser.

Navigate to http://localhost:8080/freeboard
You should get the freeboard server displayed, you can plonk about in it, but the std browser wont work well, plus its hard work for the little pi to do both server and browser.
So if you see the first screen then you know the freeboard-server is running.

At the command prompt:

pi@freeboard ~ $ ifconfig [Enter]

The result will be something like:

eth0     Link encap:Ethernet  HWaddr 00:26:5e:50:5c:77  
          inet addr:10.1.1.49  Bcast:10.255.255.255  Mask:255.0.0.0
          inet6 addr: fe80::226:5eff:fe50:5c77/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1

The bit we need is `inet addr:10.1.1.49`. In my case the Rpi is at 10.1.1.49 on my home network.
From your PC open Firefox or Chrome (not IE) and browse http://10.1.1.49:8080/freeboard 

BTW: Java will speed up with use, its quite noticeable on the little pi :-)

Plug NMEA devices or the Arduino Mega/Freeboard into a USB plug to get real data.

++++++++++++++++++++++++++++++++++++++++++
To enable the built in WIFI Access Point
++++++++++++++++++++++++++++++++++++++++++

Shutdown the RPi.
Disconnect from the internet.
  If you are using ethernet pull out ethernet cable

Check the network configuration:

pi@freeboard ~ $sudo nano /etc/network/interface

Comment out these lines if they exist:

allow-hotplug wlan0
iface wlan0 inet manual
wpa-roam /etc/wpa_supplicant/wpa_supplicant.conf
iface default inet dhcp

So they look like this:

#allow-hotplug wlan0
#iface wlan0 inet manual
#wpa-roam /etc/wpa_supplicant/wpa_supplicant.conf
#iface default inet dhcp

Use [Cntrl]x to save the file.

Shutdown and reboot.
On your PC you should now see a new WIFI AccessPoint called 'freeboard'. Connect to it using password 'freeboard'. (It uses WPA Personal encryption).
(You may need to disconnect your PC from the internet to use the connection)

Open a web browser to http://192.168.0.1:8080/freeboard
http://freeboard:8080/freeboard should also work.

If you want to change back to the previous network setup, edit the /etc/network/interface back to the original version.





Plug in WIFI dongle, and reboot



