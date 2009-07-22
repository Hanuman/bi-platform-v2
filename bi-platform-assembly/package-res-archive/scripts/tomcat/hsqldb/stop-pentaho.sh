#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  Pentaho Stop Script                                                     ##
##                                                                          ##
### ====================================================================== ###

DIR=$(dirname $0)
cd $DIR/data 
sh stop_hypersonic.sh &
cd ../tomcat/bin
sh shutdown.sh
