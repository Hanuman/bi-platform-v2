#!/bin/sh
### ====================================================================== ###
##                                                                          ##
##  HSQLDB Start Script                                                     ##
##                                                                          ##
### ====================================================================== ###

cd $(dirname $0)
DIR=$PWD
cd -

. $DIR/set-pentaho-java.sh

if [ -d $DIR/../jre ]; then
  setPentahoJava $DIR/../jre
else 
  setPentahoJava
fi

#---------------------------------#
# dynamically build the classpath #
#---------------------------------#
THE_CLASSPATH=
for i in `ls $DIR/lib/hsqldb*.jar`
do
  THE_CLASSPATH=${THE_CLASSPATH}:${i}
done
echo "classpath is $THE_CLASSPATH"

$_PENTAHO_JAVA -cp $THE_CLASSPATH org.hsqldb.Server -database.0 hsqldb/sampledata -dbname.0 sampledata -database.1 hsqldb/hibernate -dbname.1 hibernate -database.2 hsqldb/quartz -dbname.2 quartz