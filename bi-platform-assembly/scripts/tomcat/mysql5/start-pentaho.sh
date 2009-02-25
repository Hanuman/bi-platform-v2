#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  Pentaho Start Script                                                    ##
##                                                                          ##
### ====================================================================== ###

if [ -e promptuser.sh ]; then
  sh promptuser.sh
  rm promptuser.sh
fi
if [ "$?" = 0 ]; then
  cd tomcat/bin
  export CATALINA_OPTS="-Xms256m -Xmx768m -XX:MaxPermSize=256m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000"
  sh startup.sh
fi
