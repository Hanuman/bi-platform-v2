#!/bin/bash
### ====================================================================== ###
##                                                                          ##
##  Pentaho Start Script                                                    ##
##                                                                          ##
### ====================================================================== ###

bash promptuser.sh
if [ "$?" = 0 ]; then
  cd data
  bash start_hypersonic.sh &
  cd ../tomcat/bin
  export CATALINA_OPTS="-Xms128m -Xmx512m -XX:MaxPermSize=256m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000"
  bash startup.sh
fi
