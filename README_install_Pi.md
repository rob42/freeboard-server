# HOWTO Setup Freeboard on a Raspberry Pi

## Required Hardware

 * Get Raspberry Pi
   * tested on the Pi 3 Model B, with built-in WiFi
 * Get 8Gb Class10 Micro SD card.
 * Plug in USB keyboard (may be needed to find ip address on local network)
 * Plug in HDMI screen (may be needed to find ip address on local network)
 * Plug the ethernet into your local network

## Raspbian Install

 * Install *Raspbian Jessie Lite* image
   * [Raspberry Pi Install Documentation](https://www.raspberrypi.org/help/noobs-setup/)
 * Login to with the user *pi* and the password *raspberry*
   * You can login with the monitor and keyboard or
   * using ssh over the local network
 * setup the pi with raspi-config
   * type: `sudo raspi-config` and press Enter
   * use Arrow Keys to navigate the menu and Enter to select
   * Expand filesystem
   * Change password
   * Overclock: medium
 * Finish raspi-config and reboot
   * Use Tab to navigate to `Finish` and hit Enter

## Freeboard Install

 * Login to the Pi with the user *pi* and your new password
 * type (or copy and paste into ssh terminal): `wget https://raw.githubusercontent.com/rob42/freeboard-server/master/setup_raspbian.sh` and hit Enter
 * type: `chmod 755 setup_raspbian.sh` and hit Enter
 * type: `./setup_raspbian.sh` and hit Enter
   * The script will ask you a few questions to start, but should not require user intervention until completion.
     * You must accept the Java License Agreement to continue
     * You may choose to setup the *Boat Mode* captive WiFi network right off the bat, if you like. You'll still be able to access the device over the wired network as well.
 * reboot

## Playing with it

This device is a server, and only has the text based interface for interaction.
To try out the application, you'll need to connect to it over a network from a
browser.

If your local network assigns DNS names with DHCP Leases, You may be able to
just load up [http://freeboard:8080/freeboard/](http://freeboard:8080/freeboard/)
to view the application!

If not, you'll have to go find the IP address in the console (See the
[Networking](#networking) section below.)

### Installing Charts

* prepare some charts per [freeboard-installer documentation](https://github.com/rob42/freeboard-installer/blob/master/README.md)
* copy prepared zip files to `/home/pi/maps/` on the server
* rerun `./setup_raspbian.sh` as the user pi.

### Networking

To find your server's IP address, you'll need to log into the console and
run the `ifconfig` command. If you're plugged into a wired ethernet connection
you should look for the `eth0` device. Here's an example:

    pi@freeboard:~ ifconfig
    eth0      Link encap:Ethernet  HWaddr b8:27:eb:cc:fe:4c
              inet addr:10.1.1.49  Bcast:10.1.1.255  Mask:255.255.255.0
              inet6 addr: fdd1:a6f7:4630:0:b8fc:2d34:edc5:e97c/64 Scope:Global
              inet6 addr: fdd1:a6f7:4630::976/128 Scope:Global
              inet6 addr: fe80::5d22:a534:69c5:4df1/64 Scope:Link
              UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
              RX packets:260586 errors:0 dropped:0 overruns:0 frame:0
              TX packets:22711 errors:0 dropped:0 overruns:0 carrier:0
              collisions:0 txqueuelen:1000
              RX bytes:385690624 (367.8 MiB)  TX bytes:4110796 (3.9 MiB)

The bit we need is `inet addr:10.1.1.49`. In my case the Rpi is at 10.1.1.49 on
my home network. From your PC open Firefox or Chrome (not IE) and browse to
http://10.1.1.49:8080/freeboard/

BTW: Java will speed up with use, it's quite noticeable on the little pi :-)

Plug NMEA devices or the Arduino Mega/Freeboard into a USB plug to get real data.

#### Using your Pi as a WiFi Access Point!

If you answered (y)es to boat mode during install, you should have a
captive wifi network up and running.

On your PC you should now see a new WIFI AccessPoint called 'freeboard'.
Connect to it using password 'freeboard'. (It uses WPA Personal encryption).
(You may need to disconnect your PC from the internet to use the connection)

Open a web browser to http://192.168.0.1:8080/freeboard
http://freeboard:8080/freeboard should also work.

If you want to change back to the previous network setup, just re-run the
setup script and answer (n)o to boat mode.

### Navigating the console (or an introduction to systemd services)

This section will give you a brief introduction to using the console. At this
point you should still have a monitor and keyboard plugged into your Pi. You
can login as the user *pi* with your password.

You can get the status of the freeboard service by running the command:
`sudo systemctl status freeboard`. Which will look something like this if
everything went okay:

    pi@freeboard:~ $ sudo systemctl status freeboard
    ● freeboard.service - Freeboard Server
       Loaded: loaded (/etc/systemd/system/freeboard.service; enabled)
       Active: active (running) since Fri 2016-05-13 01:36:52 UTC; 48min ago
     Main PID: 304 (java)
       CGroup: /system.slice/freeboard.service
               └─304 /usr/bin/java -Djava.util.Arrays.useLegacyMergeSort=true -Dl...

    May 13 01:39:20 freeboard java[304]: Saving config to /home/pi/freeboard-se...fg
    May 13 01:44:18 freeboard java[304]: Saving config to /home/pi/freeboard-se...fg
    May 13 01:49:18 freeboard java[304]: Saving config to /home/pi/freeboard-se...fg
    May 13 01:54:18 freeboard java[304]: Saving config to /home/pi/freeboard-se...fg
    May 13 01:59:18 freeboard java[304]: Saving config to /home/pi/freeboard-se...fg
    May 13 02:04:18 freeboard java[304]: Saving config to /home/pi/freeboard-se...fg
    May 13 02:09:18 freeboard java[304]: Saving config to /home/pi/freeboard-se...fg
    May 13 02:14:18 freeboard java[304]: Saving config to /home/pi/freeboard-se...fg
    May 13 02:19:18 freeboard java[304]: Saving config to /home/pi/freeboard-se...fg
    May 13 02:24:18 freeboard java[304]: Saving config to /home/pi/freeboard-se...fg
    Hint: Some lines were ellipsized, use -l to show in full.

You can also use the `sytemctl` command to stop, start or restart the running
service, like this: `sudo systemctl COMMAND freeboard`.

    pi@freeboard:~ $ sudo systemctl restart freeboard
    pi@freeboard:~ $ sudo systemctl status freeboard
    ● freeboard.service - Freeboard Server
       Loaded: loaded (/etc/systemd/system/freeboard.service; enabled)
       Active: active (running) since Fri 2016-05-13 02:29:52 UTC; 5s ago
     Main PID: 916 (java)
       CGroup: /system.slice/freeboard.service
               └─916 /usr/bin/java -Djava.util.Arrays.useLegacyMergeSort=true -Dlog4j...

    May 13 02:29:52 freeboard systemd[1]: Started Freeboard Server.
    May 13 02:29:56 freeboard java[916]: Creating config...
    May 13 02:29:56 freeboard java[916]: Setting config defaults...
    May 13 02:29:56 freeboard java[916]: Loading config from /home/pi/freeboard-ser...fg
    Hint: Some lines were ellipsized, use -l to show in full.

To view the current log output of the service, you can use the `journalctl`
command:

    pi@freeboard:~ $ sudo journalctl -fu freeboard
    -- Logs begin at Fri 2016-05-13 01:36:44 UTC. --
    May 13 02:30:42 freeboard java[916]: May 13, 2016 2:30:42 AM org.apache.camel.impl.DefaultCamelContext doStartOrResumeRouteConsumers
    May 13 02:30:42 freeboard java[916]: INFO: Route: route3 started and consuming from: Endpoint[seda://nmeaOutput]
    May 13 02:30:42 freeboard java[916]: May 13, 2016 2:30:42 AM org.apache.camel.impl.DefaultCamelContext doStartOrResumeRouteConsumers
    ...

The above command prints new log messages as they happen, until you send CTRL-c to
exit. If you want to see *all* logs, run `sudo journalctl -u freeboard`. You can
use the arrow keys to scroll around, and `q` to exit.

