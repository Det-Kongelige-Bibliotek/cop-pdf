<?xml version="1.0" encoding="UTF-8" ?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	       version="1.0">

  <xsl:output method="text"/>
  <!--substring-after(link,'http://www.kb.dk/'),'?format=mods')"/><xsl:text>-->
  <xsl:template match="/">
    
    <xsl:for-each select="//item">
      <xsl:value-of select="concat(
			    'http://www.kb.dk/',
			    'cop/syndication/',
			    substring-after(link,'http://www.kb.dk/'),'')"/><xsl:text>
</xsl:text>
    </xsl:for-each>
    
  </xsl:template>

</xsl:transform>
