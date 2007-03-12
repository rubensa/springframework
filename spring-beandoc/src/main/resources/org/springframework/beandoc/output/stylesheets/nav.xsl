<?xml version="1.0" encoding="UTF-8"?>

<!--
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
-->

<!--
 *
 * Converts a consolidated DOM tree of Spring beans into an HTML file with a 
 * JavaDoc-ish feel to it.  This output is used in a navigation frame.
 *
 * @author Darren Davison
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		
	<xsl:import href="./i18n.xsl"/>

    <xsl:output 
        method="xml" 
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
        />

    <!--
     * Template structure of HTML output
    -->
    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="$i18n-allTitle"/></title>
                <link rel="stylesheet" href="{consolidated/beans[1]/@beandocCssLocation}" type="text/css"/>
            </head>
  
            <body>
      			<div id="contentWell">      
	                <h1><xsl:value-of select="$i18n-allTitle"/></h1>
	                <p>
	                <xsl:for-each select=".//bean">                    
	                    <xsl:sort select="@id | @name"/>
	                    <xsl:variable name="beandocId">
	                        <xsl:choose>
	                            <xsl:when test="@id"><xsl:value-of select="@id"/></xsl:when>
	                            <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
	                            <xsl:otherwise>ignore</xsl:otherwise>
	                        </xsl:choose>
	                    </xsl:variable>
	                    <xsl:if test="$beandocId != 'ignore'">
	                        <a href="{@beandocHtmlFileName}#{$beandocId}" target="mainframe"><xsl:value-of select="$beandocId"/></a>
	                        <br/>
	                    </xsl:if>
	                </xsl:for-each>
	                </p>
                </div>
            </body>
            
        </html>
    </xsl:template>
</xsl:stylesheet>
