#!/bin/bash
#
# start script for freeboard
#
JAVA_HOME=/home/pi/jdk1.7.0_06
export JAVA_HOME
JAVA=$JAVA_HOME/bin/java
FREEBOARD_HOME=/home/pi/freeboard
JAR=freeboard-server-0.0.1-SNAPSHOT-jar-with-dependencies.jar
EXT="-Djava.util.Arrays.useLegacyMergeSort=true -Djava.library.path=/usr/lib/rxtx:/usr/lib/jni"

cd $FREEBOARD_HOME
$JAVA $EXT -jar target/$JAR
