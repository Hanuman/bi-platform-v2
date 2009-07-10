#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  Pentaho Start Script                                                    ##
##                                                                          ##
### ====================================================================== ###

cd $(dirname $0)
DIR=$PWD
cd -

if [ -e $DIR/promptuser.sh ]; then
  sh $DIR/promptuser.sh
  rm $DIR/promptuser.sh
fi
if [ "$?" = 0 ]; then
  cd $DIR/tomcat/bin
  export CATALINA_OPTS="-Xms256m -Xmx768m -XX:MaxPermSize=256m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000"
  sh startup.sh
fi
