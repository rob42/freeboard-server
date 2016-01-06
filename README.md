Freeboard Navigation Instruments and Chartplotter
=================================================

Freeboard is a new way to provide marine instruments and navigation tools over WIFI with low cost open hardware (Arduino and Raspberry Pi).

Freeboard interface pcb is available for hardware interfacing on your boat. See https://www.42.co.nz/freeboard/technical/interfacing/freeboardinterfaceboardv1.2/index.html

Goals
-----

 * minimal cost
 * maximum interoperability
 * support compass, wind, log, autopilot, charts, and other common uses.
 * use low cost commodity hardware for sensors and processors
 * support modern devices (PC, tablets, cellphones)
 * support for many simultaneous users
 * total system cost (less clients) <USD500
 * use common KAP/BSB or ENC map formats (US NOAA Raster charts work)
 * low power usage (currently 0.4A at 12v)

Thats achieved in a totally unique way, by providing the instruments and chartplotter via a web page over a local wifi link on your boat, so that any device with a web browser can access them.

This is the main server for the Freeboard server project.  

Install
-------

For a PC:

Select a suitable root directory, with no spaces in the path! eg /home/robert or C:\boat
Make sure you have sun (oracle) java7+ installed in the path somewhere
Make sure you have git installed
```
git clone --depth=1 https://github.com/rob42/freeboard-server.git
cd freeboard-server
./startpc.sh (or startpc.bat on windows)
```
For a Raspberry Pi:

Log in as pi
In the home directory (/home/pi), as pi user (not sudo)
```
git clone --depth=1 https://github.com/rob42/freeboard-server.git
cd freeboard-server
./install_rpi.sh
```
Reboot the Rpi, freeboard should be running at http://[IP_ADDRESS]:8080/freeboard, where [IP_ADDRESS] is the Rpis ip address or hostname.

See http://www.42.co.nz/freeboard for more.