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

    <xsl:output method="html" />
    <xsl:param name="beandocXslGraphType">png</xsl:param> 

    <!--
     * Template structure of HTML output
    -->
    <xsl:template match="/">
        <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
            <head>
                <title><xsl:value-of select="beans/@beandocFileName"/></title>
                <link rel="stylesheet" href="{consolidated/beans[1]/@beandocCssLocation}" type="text/css"/>
            </head>
  
            <body>
      
                <h1>Application Context</h1>

                <xsl:if test="consolidated/beans[1]/@beandocConsolidatedImage">
                <p>
                    <a href="{consolidated/beans[1]/@beandocConsolidatedImage}" title="View graph for this context">
                        <img src="{consolidated/beans[1]/@beandocConsolidatedImage}" width="100%" alt="View graph for this context"/>
                    </a>
                </p>
                </xsl:if>

                <table id="fileListTable" summary="List of individual context files and their descriptions that 
                    made up this application context">
                    <thead><tr><th colspan="2">files making up this application context</th></tr></thead>
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
