#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  JBoss Bootstrap Script                                                  ##
##                                                                          ##
### ====================================================================== ###

### $Id: start_hypersonic.sh,v 1.1.1.1 2005/11/13 06:31:43 gmoran Exp $ ###

echo "JAVA_HOME set to $JAVA_HOME"
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi
echo "JAVA is $JAVA"

#---------------------------------#
# dynamically build the classpath #
#---------------------------------#
THE_CLASSPATH=
for i in `ls ./lib/hsqldb*.jar`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done
echo "classpath is $THE_CLASSPATH"

$JAVA -cp $THE_CLASSPATH org.hsqldb.Server -database.0 hsqldb/sampledata -dbname.0 sampledata -database.1 hsqldb/hibernate -dbname.1 hibernate -database.2 hsqldb/quartz -dbname.2 quartz

