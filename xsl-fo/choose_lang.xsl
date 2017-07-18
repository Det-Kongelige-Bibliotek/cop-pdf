<?xml version="1.0" encoding="UTF-8" ?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	       xmlns:xml="http://www.w3.org/XML/1998/namespace"
               version="1.0">

    <xsl:param name="lang" select="'en'"/>

    <xsl:param name="suppresslang">
      <xsl:choose>
	<xsl:when test="$lang = 'en'">da</xsl:when>
	<xsl:otherwise>en</xsl:otherwise>
      </xsl:choose>
    </xsl:param>

    <xsl:output
            encoding="UTF-8"
            indent="yes"
            method="xml"
            />

    <xsl:template match="/">
      <xsl:message>
	The selected language is <xsl:value-of select="$lang"/>
      </xsl:message>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="xsl:template[@name='mods_renderer']">
      <xsl:element name="xsl:template">
	<xsl:attribute name="name">
	  <xsl:value-of select="concat('mods_renderer_',$lang)"/>
	</xsl:attribute>
	<xsl:element name="xsl:param">
	  <xsl:attribute name="name">cataloging_language</xsl:attribute>
	  <xsl:attribute name="select">'<xsl:value-of select="$lang"/>'</xsl:attribute>
	</xsl:element>
	<xsl:apply-templates/>
      </xsl:element>
    </xsl:template>

    <xsl:template match="xsl:template[@name='event_renderer']">
      <xsl:element name="xsl:template">
	<xsl:attribute name="name">
	  <xsl:value-of select="concat('event_renderer_',$lang)"/>
	</xsl:attribute>
	<xsl:element name="xsl:param">
	  <xsl:attribute name="name">cataloging_language</xsl:attribute>
	  <xsl:attribute name="select">'<xsl:value-of select="$lang"/>'</xsl:attribute>
	</xsl:element>
	<xsl:apply-templates/>
      </xsl:element>
    </xsl:template>

    <xsl:template match="node()">
      <xsl:choose>
	<xsl:when test="@xml:lang">
	  <xsl:if test="@xml:lang=$lang">
	    <xsl:copy>
	      <xsl:apply-templates select="@*|node()"/>
	    </xsl:copy>
	  </xsl:if>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:copy>
	    <xsl:apply-templates select="@*|node()"/>
	  </xsl:copy>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template match="@*">
      <xsl:copy>
	<xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>

</xsl:transform>


        <!--
            $Log: not supported by cvs2svn $
            Revision 1.10  2009/06/26 11:50:26  slu
            implemented workaround for searching

            Revision 1.9  2009/06/04 07:36:32  slu
            Added an extensive exclude-result-prefixes

            Revision 1.8  2009/04/14 10:38:17  slu
            Now with proper metadata and RCS tags

            Revision 1.7  2009/04/14 10:36:53  slu
            no comments

            Revision 1.6  2009/04/14 10:36:32  slu
            No correctly handling URIs with a trailing language tag (da or en)
            without a trailing slash.
        -->
