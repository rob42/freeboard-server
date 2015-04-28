#!/bin/bash
#

# start script for freeboard
FREEBOARD_HOME=/home/pi/freeboard
#FREEBOARD_HOME=.

JAR=freeboard-server.jar
#
cd $FREEBOARD_HOME
mkdir logs

#temporary until linux-arm.jar is in purejavacom.jar
export LD_LIBRARY_PATH=$FREEBOARD_HOME/jna

#start server

#NOTE: you may need to explicitly set your JAVA_HOME for your environment
# Edit and uncomment a line like these  samples with your java path.
#JAVA_HOME=/home/pi/jdk1.8.0
#JAVA_HOME=/home/robert/java/jdk1.7.0_07
#JAVA_HOME=/usr/lib/jvm/jdk-8-oracle-arm-vfp-hflt
# and then uncomment this line
#export JAVA_HOME

JAVA=java
if [ -n "$JAVA_HOME" ]; then
	JAVA=$JAVA_HOME/bin/java
fi
EXT="-Djava.util.Arrays.useLegacyMergeSort=true"
#you may want to adjust the max memory for different RPis, this is for 512Mb Ram
MEM="-Xmx48m -XX:PermSize=32m -XX:MaxPermSize=32m"
LOG4J=-Dlog4j.configuration=file://$FREEBOARD_HOME/conf/log4j.properties

cd $FREEBOARD_HOME
echo "Starting: $JAVA $EXT $LOG4J $MEM -jar target/$JAR >>logs/start.log 2>&1 &" >>logs/start.log 2>&1 &
$JAVA $EXT $LOG4J $MEM -jar target/$JAR >>logs/start.log 2>&1 &
