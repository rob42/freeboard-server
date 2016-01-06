
REM. start script for freeboard on windows pc
REM. Assume we are starting in the freeboard directory
REM.set FREEBOARD_HOME=C:\freeboard-server

set FREEBOARD_HOME=%CD%

set JAR=freeboard-server.jar
REM.
cd %FREEBOARD_HOME%
mkdir logs

REM.start server
set JAVA=java

REM. You may need to set the java version specifically
REM. If so uncomment and edit the following to suit your install
REM. set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_07
REM. set JAVA=%JAVA_HOME%\bin\java

set EXT="-Djava.util.Arrays.useLegacyMergeSort=true"

REM. optionally limit memory here
REM. set MEM="-Xmx48m -XX:PermSize=32m -XX:MaxPermSize=32m"

set LOG4J=-Dlog4j.configuration=file:/%FREEBOARD_HOME%/conf/log4j.properties

echo "Starting: %JAVA% %EXT% %LOG4J% %MEM% -jar target/%JAR%" >logs\start.log 2>&1 
"%JAVA%" %EXT% %LOG4J% %MEM% -jar target\%JAR% >>logs\start.log 2>&1 

pause 