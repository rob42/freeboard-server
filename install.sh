#!/bin/bash
#
# Install after unpacking tar file
#
JAVA_HOME=/home/pi/jdk1.7.0_06
export JAVA_HOME
JAVA=$JAVA_HOME/bin/java
FREEBOARD_HOME=$HOME/freeboard
JAR=freeboard-server-*-jar-with-dependencies.jar

echo "Setting up into $FREEBOARD_HOME"
#make sure we are in the right place
cd $HOME
mkdirs $FREEBOARD_HOME
cd $FREEBOARD_HOME
mkdir target
cp ../$JAR target/
echo "Done, use './start.sh' to start freeboard"
