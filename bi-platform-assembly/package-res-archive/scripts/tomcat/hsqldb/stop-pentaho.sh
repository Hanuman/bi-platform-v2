#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  Pentaho Stop Script                                                     ##
##                                                                          ##
### ====================================================================== ###

cd $(dirname $0)
DIR=$PWD
cd -

. $DIR/set-pentaho-java.sh

if [ -d $DIR/jre ]; then
  setPentahoJava $DIR/jre
else 
  setPentahoJava
fi

cd $DIR/data 
sh stop_hypersonic.sh &
cd $DIR/tomcat/bin
JAVA_HOME=$_PENTAHO_JAVA_HOME
sh shutdown.sh
