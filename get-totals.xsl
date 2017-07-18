<?xml version="1.0" encoding="UTF-8" ?>
<xsl:transform xmlns:o="http://a9.com/-/spec/opensearch/1.1/"
	       xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	       xmlns:exsl="http://exslt.org/common"
	       exclude-result-prefixes="o xsl exsl"
	       version="1.0">

  <xsl:output method="xml"
	      indent="yes"/>

  <!-- http://www.kb.dk/cop/directory/editions/any/2009/jul/editions/en/ -->

  <xsl:variable name="guestimations">
    <params>
      <param name="manus">40.0</param>
      <param name="books">40.0</param>
      <param name="letters">0.5</param>
      <param name="pamphlets">15.0</param>
    </params>
  </xsl:variable>

  <xsl:template match="/">
    <xsl:variable name="sizes">
      <table>
	<xsl:for-each select="//outline[@nodeId]">
	  <xsl:choose>
	    <xsl:when test="contains(@htmlUrl,'manus')">
	      <xsl:call-template name="getresults">
		<xsl:with-param name="medium" select="'manus'"/>
	      </xsl:call-template>
	    </xsl:when>
	    <xsl:when test="contains(@htmlUrl,'letters')">
	      <xsl:call-template name="getresults">
		<xsl:with-param name="medium" select="'letters'"/>
	      </xsl:call-template>
	    </xsl:when>
	    <xsl:when test="contains(@htmlUrl,'books')">
	      <xsl:call-template name="getresults">
		<xsl:with-param name="medium" select="'books'"/>
	      </xsl:call-template>
	    </xsl:when>
	    <xsl:when test="contains(@htmlUrl,'pamphlets')">
	      <xsl:call-template name="getresults">
		<xsl:with-param name="medium" select="'pamphlets'"/>
	      </xsl:call-template>
	    </xsl:when>
	  </xsl:choose>
	</xsl:for-each>
      </table>
    </xsl:variable>
    <table>
      <xsl:copy-of select="exsl:node-set($sizes)/table/*"/>
      <tr>
	<td>Total</td>
	<td>
	  <xsl:value-of select="sum(exsl:node-set($sizes)//tr/td[2])"/>
	</td>
	<td>
	</td>
	<td>
	  <xsl:value-of select="sum(exsl:node-set($sizes)//tr/td[4])"/>
	</td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template name="getresults">
    <xsl:param name="medium" select="$medium"/>
    <tr>
      <xsl:variable name="uri">
	<xsl:value-of select="concat('http://www.kb.dk/cop/syndication',@htmlUrl,'/en/?format=mods')"/>
      </xsl:variable>
      <td><xsl:value-of select="@htmlUrl"/></td>
      <xsl:variable name="count">
	<xsl:value-of 
	    select="document($uri)//o:totalResults"/>
      </xsl:variable>
      <td>
	<xsl:value-of select="$count"/>
      </td>
      <td>
	<xsl:value-of select="exsl:node-set($guestimations)/params/param[@name=$medium]/text()"/>
      </td>
      <td>
	<xsl:value-of select="$count * exsl:node-set($guestimations)/params/param[@name=$medium]/text()"/>
      </td>
    </tr>
  </xsl:template>

</xsl:transform>
