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
 * JavaDoc-ish feel to it.  This output is used as the main index page.
 *
 * @author Darren Davison
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output 
    	method="xml" 
    	doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
    	doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
    	/>
    	
    <xsl:param name="beandocGenerated">unknown</xsl:param> 

    <!--
     * Template structure of HTML output
    -->
    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="consolidated/beans[1]/@beandocContextTitle"/></title>
                <link rel="stylesheet" href="{consolidated/beans[1]/@beandocCssLocation}" type="text/css"/>
            </head>
  
            <body>
      			<div id="bannerBar">beandoc generated: <xsl:value-of select="$beandocGenerated"/></div>
                <h1><xsl:value-of select="consolidated/beans[1]/@beandocContextTitle"/></h1>

                <xsl:if test="consolidated/beans[1]/@beandocConsolidatedImage">
                <p>
                    <a href="{consolidated/beans[1]/@beandocConsolidatedImage}" title="View graph for {consolidated/beans[1]/@beandocContextTitle}">
                        <img src="{consolidated/beans[1]/@beandocConsolidatedImage}" width="100%" alt="View graph for {consolidated/beans[1]/@beandocContextTitle}"/>
                    </a>
                </p>
                </xsl:if>

				<h2>files making up this application context</h2>
                <table id="fileListTable" summary="List of individual context files and their descriptions that 
                    made up this application context">
                    <tbody>
                    <xsl:for-each select="consolidated/beans/description">
                        <tr>
                            <td style="width:30%">
                                <a href="{@beandocHtmlFileName}"><xsl:value-of select="../@beandocFileName"/></a>
                            </td>
                            <td><xsl:value-of select="."/></td>
                        </tr>           
                    </xsl:for-each>
                    </tbody>
                </table>
                <p/>
            </body>
            
        </html>
    </xsl:template>
</xsl:stylesheet>
