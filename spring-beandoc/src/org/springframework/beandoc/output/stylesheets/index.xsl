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
 * Generates the frameset with the user configured title
 *
 * @author Darren Davison
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output 
        method="xml" 
        indent="no"
        doctype-public="-//W3C//DTD XHTML 1.0 Frameset//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"
        />


    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="consolidated/beans[1]/@beandocContextTitle"/></title>
            </head>

            <frameset cols="20%,80%">
                <frame src="nav.html" name="navframe" style="border-right:2px solid #000000"/>
                <frame src="main.html" name="mainframe"/>

                <noframes>
                    <body>
                        <h1>Frame Alert</h1>
                        <p>
                            This document is designed to be viewed using the frames feature. If you see this message, 
                            you are using a non-frame-capable web client.
                        </p>
                        Link to <a href="main.html">Non-frame version.</a>
                    </body>
                </noframes>
            </frameset>
        </html>
    </xsl:template>
    
</xsl:stylesheet>
