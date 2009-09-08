#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  HSQLDB Start Script                                                     ##
##                                                                          ##
### ====================================================================== ###

cd $(dirname $0)
DIR=$PWD
cd -

. "$DIR/set-pentaho-java.sh"

if [ -d "$DIR/../jre" ]; then
  setPentahoJava "$DIR/../jre"
else 
  setPentahoJava
fi

#---------------------------------#
# dynamically build the classpath #
#---------------------------------#
THE_CLASSPATH=
for i in `ls ./lib/hsqldb*.jar`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done
echo "classpath is $THE_CLASSPATH"

"$_PENTAHO_JAVA" -cp $THE_CLASSPATH org.hsqldb.util.ShutdownServer -url "jdbc:hsqldb:hsql://localhost/sampledata" -user "SA" -password "" 
"$_PENTAHO_JAVA" -cp $THE_CLASSPATH org.hsqldb.util.ShutdownServer -url "jdbc:hsqldb:hsql://localhost/hibernate" -user "SA" -password ""
"$_PENTAHO_JAVA" -cp $THE_CLASSPATH org.hsqldb.util.ShutdownServer -url "jdbc:hsqldb:hsql://localhost/quartz" -user "sa" -password "" 
