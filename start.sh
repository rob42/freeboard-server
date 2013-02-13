#!/bin/bash
#
# make use we have loaded the usb after a cold boot
# make sure it unmounted first
MEDIA_HOME=/media/usb0
sudo umount -l $MEDIA_HOME >logs/start.log 2>&1
if [ ! -d "$MEDIA_HOME" ]; then  
        sudo mkdir $MEDIA_HOME >>logs/start.log 2>&1
fi
sudo mount -tvfat -osync,noexec,nodev,noatime,nodiratime,gid=floppy,umask=000 /dev/sda1 $MEDIA_HOME >>logs/start.log 2>&1

# start script for freeboard
FREEBOARD_HOME=/home/pi/freeboard

JAR=freeboard-server.jar
#
cd $FREEBOARD_HOME
# Check if we have an update to install
if [ -f "$MEDIA_HOME/updates/$JAR" ]; then
        #copy to working dir after backing up current one
        echo "**Updating freeboard-server" >>logs/update.log 2>&1
        echo "Backup old target/$JAR to target/$JAR.last" >>logs/update.log 2>&1
        mv $FREEBOARD_HOME/target/$JAR $FREEBOARD_HOME/target/$JAR.last >>logs/update.log 2>&1
        echo "Copy new $JAR to target/" >>logs/update.log 2>&1
        cp  $MEDIA_HOME/updates/$JAR $FREEBOARD_HOME/target/$JAR >>logs/update.log 2>&1
        # update any misc files
        if [ -f "$MEDIA_HOME/updates/freeboard-server.tar.gz" ]; then
                echo "Extracting new web files..." >>logs/update.log 2>&1
                tar xvzf $MEDIA_HOME/updates/freeboard-server.tar.gz  >>logs/update.log 2>&1    
        fi
        echo "Completed" >>logs/update.log 2>&1
else
        echo "**No update found" >>logs/update.log 2>&1
fi

#start server
JAVA_HOME=/home/pi/jdk1.7.0_06
export JAVA_HOME
JAVA=$JAVA_HOME/bin/java
EXT="-Djava.util.Arrays.useLegacyMergeSort=true"
MEM="-Xmx24m -XX:PermSize=32m -XX:MaxPermSize=32m"

#if we have a usb drive log to there
if [ -f "$MEDIA_HOME/conf/log4j.properties" ]; then
        LOG4J=-Dlog4j.configuration=file://$MEDIA_HOME/conf/log4j.properties
else
        LOG4J=-Dlog4j.configuration=file://$FREEBOARD_HOME/conf/log4j.properties
fi

cd $FREEBOARD_HOME
echo "Starting: $JAVA $EXT $LOG4J $MEM -jar target/$JAR >>logs/start.log 2>&1 &" >>logs/start.log 2>&1 &
$JAVA $EXT $LOG4J $MEM -jar target/$JAR >>logs/start.log 2>&1 &
