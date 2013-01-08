#!/bin/bash
# Copyright 2012,2013 Robert Huitema robert@42.co.nz
# 
# This file is part of FreeBoard. (http://www.42.co.nz/freeboard)
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
# start script for freeboard
#
JAVA_HOME=/home/pi/jdk1.7.0_06
export JAVA_HOME
JAVA=$JAVA_HOME/bin/java
FREEBOARD_HOME=/home/pi/freeboard
JAR=freeboard-server-0.0.1-SNAPSHOT-jar-with-dependencies.jar
EXT="-Djava.util.Arrays.useLegacyMergeSort=true -Djava.library.path=/usr/lib/rxtx:/usr/lib/jni"
MEM=-Xmx128m
LOG4J=-Dlog4j.configuration=./conf/log4j.properties

cd $FREEBOARD_HOME
$JAVA $EXT $MEM $LOG4J -jar target/$JAR >logs/start.log 2>&1 &
