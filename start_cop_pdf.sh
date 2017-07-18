#!/bin/sh

export PATH="/usr/java/jdk1.7.0_21/bin:$PATH"
export JAVA_HOME="/usr/java/jdk1.7.0_21/"

ls  bin/executable.jar lib/*jar | sort > jar_files
LIB=`perl -ne 'chomp ; print $_.":" ; ' < jar_files|sed 's/:$//'`

XSLT_URL="./xsl-fo/mods2fo.xsl"
MEM_SETTINGS="-Xms512m -Xmx2048m -Xss2048k"

# export CONFIG_NAME="/home/cop-pdf/conf/kb.cop-pdf.properties"
# export CONFIG_NAME="/home/slu/projects/cop-pdf/trunk/conf/kb.cop-pdf.properties"
export CONFIG_NAME="/home/apache/cop-pdf/conf/kb.cop-pdf.properties"

if [ -f $CONFIG_NAME ]
then
    echo "getting  $CONFIG_NAME"
    java $MEM_SETTINGS $CONFIG -classpath $LIB  dk.kb.dis.cumulusdatajobs.pdf.MqDriver
else 
    java  $MEM_SETTINGS -classpath $LIB  dk.kb.dis.cumulusdatajobs.pdf.MqDriver \
	--config ./xsl-fo/fop.xml \
	--server "tcp://disdev-01.kb.dk:61616" \
	--queue  "kb.cop.pdf " \
	--xsl $XSLT_URL
fi
