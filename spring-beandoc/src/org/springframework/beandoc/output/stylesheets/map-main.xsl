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
 * Converts a decorated DOM tree of Spring beans into an HTML file with a 
 * an image map over the GraphViz generated image of this particular context.
 *
 * @author Darren Davison
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                
    <xsl:output 
        method="xml" 
        indent="no"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
        />

    <!--
     * Template structure of HTML output
    -->
    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="consolidated/beans[1]/@beandocFileName"/></title>
                <link rel="stylesheet" href="{consolidated/beans[1]/@beandocCssLocation}" type="text/css"/>
                <!-- hack using an MS extension to ensure the next stylesheet is *only* loaded by IE5 browsers -->
                <xsl:comment>[if IE 5]>
                    &lt;link rel="stylesheet" type="text/css" href="ie5.css" /&gt;
                    &lt;![endif]</xsl:comment>              
            </head>
  
            <body>
      			<div id="contentWell">
	                <img src="{consolidated/beans[1]/@beandocConsolidatedImage}" alt="Graph" usemap ="#G"/>
	                
	                <xsl:comment> ## imagemap ## </xsl:comment>
	                
	            </div>
            </body>
        </html>

    </xsl:template>

</xsl:stylesheet>
