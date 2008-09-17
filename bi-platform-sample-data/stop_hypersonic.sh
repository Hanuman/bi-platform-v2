#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  JBoss Bootstrap Script                                                  ##
##                                                                          ##
### ====================================================================== ###

### $Id: stop_hypersonic.sh,v 1.1.1.1 2005/11/13 06:31:43 gmoran Exp $ ###

#---------------------------------#
# dynamically build the classpath #
#---------------------------------#
THE_CLASSPATH=
for i in `ls ./lib/hsqldb*.jar`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done
echo "classpath is $THE_CLASSPATH"

java -cp $THE_CLASSPATH org.hsqldb.util.ShutdownServer -url "jdbc:hsqldb:hsql://localhost/sampledata" -user "SA" -password "" 
java -cp $THE_CLASSPATH org.hsqldb.util.ShutdownServer -url "jdbc:hsqldb:hsql://localhost/hibernate" -user "SA" -password ""
java -cp $THE_CLASSPATH org.hsqldb.util.ShutdownServer -url "jdbc:hsqldb:hsql://localhost/quartz" -user "sa" -password "" 
