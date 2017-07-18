#!/bin/sh

#
# We can use Danish and English as cataloging languages in COP
#

for LANG in $(eval echo {'da','en'}) 
do

#
# For each language generate the style sheets needed
#

    xsltproc --param lang "'$LANG'" choose_lang.xsl mods_renderer.xsl_in  > "mods_renderer.xsl_$LANG"

done



