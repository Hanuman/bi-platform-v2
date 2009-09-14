#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  Pentaho Start Script                                                    ##
##                                                                          ##
### ====================================================================== ###

DIR_REL=`dirname $0`
cd $DIR_REL
DIR=`pwd`
cd -

. "$DIR/set-pentaho-java.sh"

if [ -d "$DIR/jre" ]; then
  setPentahoJava "$DIR/jre"
else 
  setPentahoJava
fi

if [ -e "$DIR/promptuser.sh" ]; then
  sh "$DIR/promptuser.sh"
  rm "$DIR/promptuser.sh"
fi
if [ "$?" = 0 ]; then
  cd "$DIR/data"
  sh start_hypersonic.sh &
  cd "$DIR/tomcat/bin"
  export CATALINA_OPTS="-Xms256m -Xmx768m -XX:MaxPermSize=256m -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000"
  JAVA_HOME=$_PENTAHO_JAVA_HOME
  sh startup.sh
fi
