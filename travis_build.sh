#!/bin/sh
echo "Doing Main Build"
set -e
jdk_switcher use default
mvn install 
mvn -Pbuild-app
mvn -Prun &
sleep 30
mvn -Pintegration test
 
