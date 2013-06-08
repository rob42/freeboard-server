#!/bin/bash
#
# Copyright 2012,2013 Robert Huitema robert@42.co.nz
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

# Install after unpacking tar file
#
JAVA_HOME=/home/pi/jdk1.8.0
export JAVA_HOME
JAVA=$JAVA_HOME/bin/java
FREEBOARD_HOME=$HOME/freeboard
JAR=freeboard-server-*-jar-with-dependencies.jar

echo "Setting up into $FREEBOARD_HOME"
#make sure we are in the right place
cd $HOME
mkdir $FREEBOARD_HOME
mkdir $FREEBOARD_HOME/logs
mkdir $FREEBOARD_HOME/mapcache
mkdir $FREEBOARD_HOME/target
cd $FREEBOARD_HOME
cp ../$JAR target/freeboard-server.jar
echo "Done, use './start.sh' to start freeboard"
