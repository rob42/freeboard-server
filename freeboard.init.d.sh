#!/bin/sh 
### BEGIN INIT INFO 
# Provides:          freeboard
# Required-Start:    $all 
# Required-Stop:     
# Default-Start:     2 3 4 5 
# Default-Stop:      0 1 6 
# Short-Description: Freeboard Nav
# Description:       FreeBoard web based navigation
### END INIT INFO 
# Author: Robert Huitema <robert@42.co.nz> 

. /lib/lsb/init-functions 

# Actions 
case "$1" in 
 start) 
  log_action_begin_msg "Starting freeboard" "freeboard"
  su - pi -c "/home/pi/freeboard/start.sh" 
  log_end_msg 0 
  ;; 
 stop) 
  su - pi -c "/home/pi/freeboard/stop.sh" 
  ;; 
# restart) 
# something else...
#  ;; 
esac
