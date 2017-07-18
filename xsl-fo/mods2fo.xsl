<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		 xmlns:fo="http://www.w3.org/1999/XSL/Format"
		 xmlns:fox="http://xmlgraphics.apache.org/fop/extensions"
		 xmlns:md="http://www.loc.gov/mods/v3"
		 version="1.0">


  <xsl:param name="scaling"       select="'uniform'" />
  <xsl:param name="contentHeight" select="'22.0cm'"  />
  <xsl:param name="contentWidth"  select="'15.0cm'"  />

  <xsl:param name="pages"         select="count(//md:relatedItem[@type='constituent'])"/>

  <xsl:param name="pixelWidth">
    <xsl:call-template name="resolution-resolver">
      <xsl:with-param name="pages" select="$pages"/>
    </xsl:call-template>
  </xsl:param>

  <xsl:param name="imageService"  select="'http://www.kb.dk/imageService/'" />
  <xsl:param name="lang"          select="'en'"/>

  <xsl:param name="cataloging_language">
    <xsl:choose>
      <xsl:when test="//md:recordInfo/md:languageOfCataloging/md:languageTerm">
	<xsl:value-of select="//md:recordInfo/md:languageOfCataloging/md:languageTerm"/>
      </xsl:when>
      <xsl:otherwise><xsl:text>da</xsl:text></xsl:otherwise>
    </xsl:choose>
  </xsl:param>

  <xsl:include href="font_selection.xsl"/>
  <xsl:include href="common.xsl"/>
  <xsl:include href="mods_renderer.xsl_en"/>
  <xsl:include href="mods_renderer.xsl_da"/>

  <xsl:template name="resolution-resolver">
    <xsl:param name="pages" select="'2'"/>
    <xsl:choose>
      <xsl:when test="$pages &lt;= 250">w600/</xsl:when>
      <xsl:when test="$pages &gt;  250 and $pages &lt;= 500">w550/</xsl:when>
      <xsl:when test="$pages &gt;  500 and $pages &lt;=750">w500/</xsl:when>
      <xsl:when test="$pages &gt;  750 and $pages &lt;=1000">w450/</xsl:when>
      <xsl:when test="$pages &gt; 1000">w400/</xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" 
	     font-size="12pt" 
	     text-align="left">
      <xsl:attribute name="font-family">
	<xsl:value-of select="$defaultFont"/>
      </xsl:attribute>

      <!-- font-family="DejaVu Sans" -->

      <xsl:call-template name="layout"/>

      <xsl:apply-templates/>

    </fo:root>
  </xsl:template>

  <xsl:template match="md:modsCollection">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="md:mods">
    <fo:page-sequence id="{generate-id(.)}" master-name="pmsoddeven"
		      master-reference="right">
      <fo:static-content flow-name="page-header">
	<fo:block text-align-last="center" font-size="10pt">
	</fo:block>
      </fo:static-content>
      <fo:static-content flow-name="page-footer">
	<fo:block text-align-last="center" font-size="10pt">
	  <fo:page-number/>
	</fo:block>
      </fo:static-content>

      <fo:flow flow-name="xsl-region-body">

	<!-- If we know the read direction, we use that knowledge, or we assume that it is a left to right text -->

	<xsl:variable name="read_direction">
	  <xsl:choose>
	    <xsl:when test="md:physicalDescription/md:note[@type='pageOrientation']/text()">
	      <xsl:value-of select="md:physicalDescription/md:note[@type='pageOrientation']"/>
	    </xsl:when>
	    <xsl:otherwise>LTR</xsl:otherwise>
	  </xsl:choose>
	</xsl:variable>

	<xsl:call-template name="cover-image">
	  <xsl:with-param name="imageuri" select="'./xsl-fo/DOD-kb-cover.png'"/>
	</xsl:call-template>

	<xsl:call-template name="mods_renderer">
	  <xsl:with-param name="read_direction" select="$read_direction"/>
	</xsl:call-template>

	<xsl:if test="count(//md:relatedItem[md:titleInfo/md:title/text()]) &gt; 1">
	  <fo:block font-size="12pt" linefeed-treatment="preserve">
	    <xsl:for-each select="//md:relatedItem[md:titleInfo/md:title/text()]">
	      <xsl:if test="md:identifier">
		<fo:basic-link color="blue" internal-destination="{md:identifier}">
		  <xsl:choose>
		    <xsl:when test="contains(md:titleInfo/md:title,'|')">
		      <xsl:value-of select="substring-after(md:titleInfo/md:title,'|')"/>
		    </xsl:when>
		    <xsl:otherwise>
		      <xsl:apply-templates select="md:titleInfo/md:title"/>
		    </xsl:otherwise>
		  </xsl:choose>
		</fo:basic-link><xsl:text> </xsl:text><fo:page-number-citation ref-id="{md:identifier}"/>
		<xsl:text>&#xA;</xsl:text>
	      </xsl:if>
	    </xsl:for-each>
	  </fo:block>
	</xsl:if>
	<xsl:apply-templates mode="recurse" select="md:relatedItem">
	  <xsl:with-param name="read_direction"  select="$read_direction"/>
	</xsl:apply-templates>

	<xsl:call-template name="cover-image">
	  <xsl:with-param name="imageuri" select="'./xsl-fo/DOD-back.png'"/>
	</xsl:call-template>

      </fo:flow>
    </fo:page-sequence>
  </xsl:template>

  <xsl:template name="mods_renderer">
    <xsl:param name="read_direction" select="'LTR'"/>    
    <xsl:choose>
      <xsl:when test="$cataloging_language = 'en'">
	<xsl:call-template name="mods_renderer_en">
	  <xsl:with-param name="read_direction" select="$read_direction"/>
	</xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
	<xsl:call-template name="mods_renderer_da">
	  <xsl:with-param name="read_direction" select="$read_direction"/>
	</xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template mode="recurse" match="md:relatedItem">
    <xsl:param name="read_direction" select="'LTR'"/>

    <xsl:call-template name="process_item">
      <xsl:with-param name="read_direction">RTL</xsl:with-param>
    </xsl:call-template>

    <xsl:choose>
      <xsl:when test="$read_direction = 'LTR'">
	<xsl:apply-templates mode="traverse" select="md:relatedItem[1]">
	  <xsl:with-param name="read_direction">LTR</xsl:with-param>
	</xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
	<xsl:apply-templates mode="traverse" select="md:relatedItem[last()]">
	  <xsl:with-param name="read_direction">RTL</xsl:with-param>
	</xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


  <xsl:template mode="traverse" match="md:relatedItem">
    <xsl:param name="read_direction" select="'LTR'"/>
    
    <xsl:call-template name="process_item">
      <xsl:with-param name="read_direction">RTL</xsl:with-param>
    </xsl:call-template>

    <xsl:choose>
      <xsl:when test="$read_direction = 'LTR'">
	<xsl:apply-templates mode="traverse" select="following-sibling::md:relatedItem[1]">
	  <xsl:with-param name="read_direction">LTR</xsl:with-param>
	</xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
	<xsl:apply-templates mode="traverse" select="preceding-sibling::md:relatedItem[1]">
	  <xsl:with-param name="read_direction">RTL</xsl:with-param>
	</xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


  <xsl:template name="process_item">

    <xsl:param name="read_direction" select="'LTR'"/>
    <xsl:comment>
      Read direction is <xsl:value-of select="$read_direction"/>
    </xsl:comment>
    <xsl:if test="md:identifier/text()">
      <fo:block linefeed-treatment="preserve" break-before="page"   text-align="center">
	<xsl:if test="md:identifier/text()">
	  <xsl:attribute name="id">
	    <xsl:value-of select="md:identifier"/>
	  </xsl:attribute>
	</xsl:if>
	<xsl:if test="md:titleInfo/md:title/text()">
	  <xsl:choose>
	    <xsl:when test="contains(md:titleInfo/md:title,'|')">
	      <xsl:value-of select="substring-after(md:titleInfo/md:title,'|')"/>
	    </xsl:when>
	    <xsl:otherwise>
	      <xsl:apply-templates select="md:titleInfo/md:title"/>
	    </xsl:otherwise>
	  </xsl:choose><xsl:text>
</xsl:text>
	</xsl:if>

	<xsl:element name="fo:external-graphic">
	  <xsl:attribute name="scaling">
	    <xsl:value-of select="$scaling"/>
	  </xsl:attribute>
	  <xsl:attribute name="content-height">
	    <xsl:value-of select="$contentHeight"/>
	  </xsl:attribute> 
	  <xsl:attribute name="content-width">
	    <xsl:value-of select="$contentWidth"/>
	  </xsl:attribute> 
	  <xsl:attribute name="src">
	    <xsl:value-of select="concat($imageService,$pixelWidth,substring-before(md:identifier,'.tif'),'.jpg')"/>
	  </xsl:attribute>
	</xsl:element>
      </fo:block>
    </xsl:if>

  </xsl:template>

  <xsl:template name="cover-image">
    <xsl:param name="imageuri" select="''"/>
    
    <xsl:element name="fo:block">
      <xsl:attribute name="page-break-after">always</xsl:attribute>
      <xsl:element name="fo:external-graphic">
	<xsl:attribute name="scaling">
	  <xsl:value-of select="$scaling"/>
	</xsl:attribute>
	<xsl:attribute name="content-height">
	  <xsl:value-of select="$contentHeight"/>
	</xsl:attribute> 
	<xsl:attribute name="content-width">
	  <xsl:value-of select="$contentWidth"/>
	</xsl:attribute> 
	<xsl:attribute name="src">
	  <xsl:value-of select="$imageuri"/>
	</xsl:attribute>
      </xsl:element>
    </xsl:element>
  </xsl:template>


  <xsl:template name="layout">

    <fo:layout-master-set>
      <fo:simple-page-master master-name="right" 
			     page-width="21.0cm"
			     page-height="29.7cm"
			     margin-top="1.8cm" 
			     margin-bottom="2.2cm" 
			     margin-left="1.5cm" 
			     margin-right="1.8">
	<fo:region-body   margin-bottom="1.5cm"/>
	<fo:region-before region-name="page-header" extent="2.0cm"/>
	<fo:region-after  region-name="page-footer" extent="2.0cm"/>
      </fo:simple-page-master>

      <fo:simple-page-master master-name="left" 
			     page-width="21.0cm"
			     page-height="29.7cm"
			     margin-top="1.8cm" 
			     margin-bottom="2.2cm" 
			     margin-left="1.8cm" 
			     margin-right="1.5">
	<fo:region-body   margin-bottom="1.5cm"/>
	<fo:region-before region-name="page-header" extent="2.0cm"/>
	<fo:region-after  region-name="page-footer" extent="2.0cm"/>
      </fo:simple-page-master>

      <fo:simple-page-master master-name="rest" 
			     page-width="21.0cm"
			     page-height="29.7cm"
			     margin-top="1.8" 
			     margin-bottom="2.2cm" 
			     margin-left="1.5" 
			     margin-right="1.5">
	<fo:region-body   margin-bottom="1.5cm"/>
	<fo:region-before region-name="page-header" extent="2.0cm"/>
	<fo:region-after  region-name="page-footer" extent="2.0cm"/>
      </fo:simple-page-master>

      <fo:page-sequence-master master-name="pmsoddeven">
	<fo:repeatable-page-master-alternatives>
	  <fo:conditional-page-master-reference master-name="right" 
						page-position="first" 
						master-reference="right"  />
	  <fo:conditional-page-master-reference master-name="left" 
						odd-or-even="even" 
						master-reference="left" />
	  <fo:conditional-page-master-reference master-name="right" 
						odd-or-even="odd" 
						master-reference="right"/>
	</fo:repeatable-page-master-alternatives>
      </fo:page-sequence-master>
    </fo:layout-master-set>

  </xsl:template>

  <xsl:template name="font-resolver">
    <xsl:choose>
      <xsl:when test="../@xml:lang = 'he'"><xsl:value-of select="$hebrewFont"/></xsl:when>
      <xsl:when test="../@xml:lang = 'ar'"><xsl:value-of select="$arabicFont"/></xsl:when>
      <xsl:when test="../@xml:lang = 'zh'"><xsl:value-of select="$chineseFont"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$defaultFont"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>


</xsl:stylesheet>
