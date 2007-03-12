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
    
	<xsl:import href="./i18n.xsl"/>	

    <xsl:output 
    	method="xml" 
    	indent="no"
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
                <!-- hack using an MS extension to ensure the next stylesheet is only loaded by IE5 browsers -->
                <xsl:comment>[if IE 5]>
					&lt;link rel="stylesheet" type="text/css" href="ie5.css" /&gt;
					&lt;![endif]</xsl:comment>				
            </head>
  
            <body>
      			<div id="bannerBar"><xsl:value-of select="$i18n-generated"/>: <xsl:value-of select="$beandocGenerated"/></div>
      			<div id="contentWell">
	                <h1><xsl:value-of select="consolidated/beans[1]/@beandocContextTitle"/></h1>
	
	                <xsl:if test="consolidated/beans[1]/@beandocConsolidatedImage and not(consolidated/beans[1]/@beandocNoGraphs)">
	                <p>
	                    <a href="consolidated.xml.graph.html" title="View graph for {consolidated/beans[1]/@beandocContextTitle}">
	                        <img src="{consolidated/beans[1]/@beandocConsolidatedImage}" width="100%" alt="View graph for {consolidated/beans[1]/@beandocContextTitle}"/>
	                    </a>
	                </p>
	                </xsl:if>
	
					<h2><xsl:value-of select="$i18n-filelist"/></h2>
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
	                
	            </div>            
                <p id="pageFooter">
                	<xsl:value-of select="consolidated/beans[1]/@beandocPageFooter"/>
                	<br/><br/>                
                	<a href="http://validator.w3.org/check?uri=referer">
                		<img src="http://www.w3.org/Icons/valid-xhtml10" alt="Valid XHTML 1.0!" height="31" width="88" />
          			</a>
          			<a href="http://jigsaw.w3.org/css-validator/">
						<img style="border:0;width:88px;height:31px" src="http://jigsaw.w3.org/css-validator/images/vcss" alt="Valid CSS!" />
					</a>
				</p>
            </body>
            
        </html>
    </xsl:template>
</xsl:stylesheet>
