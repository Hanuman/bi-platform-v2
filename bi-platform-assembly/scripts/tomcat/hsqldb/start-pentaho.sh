#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  Pentaho Start Script                                                    ##
##                                                                          ##
### ====================================================================== ###

sh promptuser.sh
if [ "$?" = 0 ]; then
  cd data
  sh start_hypersonic.sh &
  cd ../tomcat/bin
  export CATALINA_OPTS="-Xms128m -Xmx512m -XX:MaxPermSize=256m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000"
  sh startup.sh
fi
