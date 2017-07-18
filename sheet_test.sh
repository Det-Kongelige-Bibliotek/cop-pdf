#!/bin/sh

ls  bin/executable.jar lib/*jar | sort > jar_files
LIB=`perl -ne 'chomp ; print $_.":" ; ' < jar_files|sed 's/:$//'`

# INPUT="http://code-01.kb.dk/svn/cop-pdf/trunk/examples/object43738.mods"
# INPUT="http://code-01.kb.dk/svn/cop-pdf/trunk/examples/object23942.mods"

# Arabic
# INPUT="http://code-01.kb.dk/svn/cop-pdf/trunk/examples/object61539.mods"

# Hebrew
INPUT="http://www.kb.dk/cop/syndication/manus/judsam/2010/maj/jsmss/object61024/da/?format=mods"

# Chinese
# INPUT="http://code-01.kb.dk/svn/cop-pdf/trunk/examples/object76351.mods"


XSLT="file:///home/slu/projects/cop-pdf/trunk/xsl-fo/mods2fo.xsl"
FOP="file:///home/slu/projects/cop-pdf/trunk/xsl-fo/fop.xml"

#
# This command reads the font directories available through fop.xml (or
# whatever configuration you use) and prints the font families available in
# the text file available-fonts.text
#

# java -classpath $LIB org.apache.fop.tools.fontlist.FontListMain  -c $FOP > available-fonts.text

#
# We can use Danish and English, just as in COP
#


java -classpath $LIB dk.kb.dis.cumulusdatajobs.pdf.ToPdfProcessor \
    --config $FOP \
    --output ./toc.pdf \
    --input $INPUT \
    --style $XSLT





