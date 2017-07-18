<?xml version="1.0" encoding="UTF-8"?>

<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
               xmlns:tei="http://www.tei-c.org/ns/1.0"
               xmlns:xlink="http://www.w3.org/1999/xlink"
               xmlns:md="http://www.loc.gov/mods/v3"
	       xmlns:fo="http://www.w3.org/1999/XSL/Format"
               version="1.0" >

  <xsl:template name="break_semicolon">
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <xsl:template name="get_language">
    <xsl:param name="cataloging_language" select="'da'"/>
    <xsl:choose>
      <xsl:when test="@xml:lang">
	<xsl:value-of select="@xml:lang"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="$cataloging_language"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="render_residence">
    <xsl:for-each select="tei:residence">
      <xsl:variable name="country" select="tei:country"/>
      <xsl:if test="position()=1"><xsl:text>
	(</xsl:text>
      </xsl:if>
      <xsl:for-each select="tei:settlement">
	<xsl:element name="fo:inline">
	  <xsl:value-of select="."/>
	</xsl:element>
	<xsl:choose>
	  <xsl:when test="position()&lt;last()">
	    <xsl:text>; </xsl:text>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:text>, </xsl:text>
	    <xsl:value-of select="$isoplaces//country[@code=$country]"/>)
	  </xsl:otherwise>
	</xsl:choose>
	<xsl:comment>
	  <xsl:value-of select="$country"/>
	  <xsl:value-of select="$isoplaces//country[@code=$country]"/>
	</xsl:comment>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

</xsl:transform>
