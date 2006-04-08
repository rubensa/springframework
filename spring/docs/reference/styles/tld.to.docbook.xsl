<?xml version="1.0"?>
<!--
   Copyright 2002-2006 the original author or authors.
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
         http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

<!--
   Effects the transformation of taglib XML to DocBook XML.

   Author: Rick Evans (based on Tim - katentim - Nolan's original stylesheet)
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output
            method="xml"
            indent="yes"
            omit-xml-declaration="no"/>

    <xsl:param name="title"/>

    <xsl:template match="taglib">
        <xsl:element name="appendix">
            <xsl:attribute name="id">
                <xsl:value-of select="$title"/>
            </xsl:attribute>
            <xsl:element name="title">
                <xsl:element name="literal">
                    <xsl:value-of select="$title"/>
                </xsl:element>
            </xsl:element>

            <xsl:element name="section">
                <xsl:attribute name="id">
                    <xsl:value-of select="$title"/>
                    <xsl:text>-intro</xsl:text>
                </xsl:attribute>
                <xsl:element name="title">
                    <xsl:text>Introduction</xsl:text>
                </xsl:element>
            </xsl:element>

            <xsl:element name="para">
                <xsl:text>This appendix describes the </xsl:text>
                <xsl:element name="literal">
                    <xsl:value-of select="$title"/>
                </xsl:element>
                <xsl:text> tag library descriptor.</xsl:text>
            </xsl:element>
            <xsl:element name="itemizedlist">
                <xsl:apply-templates select="tag" mode="mini-toc">
                    <xsl:sort select="name" order="ascending"/>
                </xsl:apply-templates>
            </xsl:element>
            <xsl:apply-templates select="tag">
                <xsl:sort select="name" order="ascending"/>
            </xsl:apply-templates>
        </xsl:element>
    </xsl:template>

    <xsl:template match="tag" mode="mini-toc">
        <xsl:element name="listitem">
            <xsl:element name="xref">
                <xsl:attribute name="linkend">
                    <xsl:value-of select="$title"/>
                    <xsl:value-of select="./name"/>
                </xsl:attribute>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="tag">
        <xsl:element name="section">
            <xsl:attribute name="id">
                <xsl:value-of select="$title"/>
                <xsl:value-of select="./name"/>
            </xsl:attribute>
            <xsl:element name="title">
                <xsl:text>The </xsl:text>
                <xsl:element name="literal">
                    <xsl:value-of select="./name"/>
                </xsl:element>
                <xsl:text> tag</xsl:text>
            </xsl:element>
            <xsl:element name="para">
                <xsl:value-of select="./description"/>
            </xsl:element>
            <xsl:element name="table">
                <xsl:element name="title">
                    <xsl:text>Attributes</xsl:text>
                </xsl:element>
                <xsl:element name="tgroup">
                    <xsl:attribute name="cols">
                        <xsl:text>4</xsl:text>
                    </xsl:attribute>
                    <xsl:element name="colspec">
                        <xsl:attribute name="align">
                            <xsl:text>center</xsl:text>
                        </xsl:attribute>
                    </xsl:element>
                    <xsl:element name="colspec">
                        <xsl:attribute name="align">
                            <xsl:text>center</xsl:text>
                        </xsl:attribute>
                    </xsl:element>
                    <xsl:element name="colspec">
                        <xsl:attribute name="align">
                            <xsl:text>center</xsl:text>
                        </xsl:attribute>
                    </xsl:element>
                    <xsl:element name="colspec">
                        <xsl:attribute name="align">
                            <xsl:text>left</xsl:text>
                        </xsl:attribute>
                    </xsl:element>
                    <xsl:element name="thead">
                        <xsl:element name="row">
                            <xsl:element name="entry">
                                <xsl:attribute name="align">
                                    <xsl:text>left</xsl:text>
                                </xsl:attribute>
                                <xsl:text>Attribute</xsl:text>
                            </xsl:element>
                            <xsl:element name="entry">
                                <xsl:attribute name="align">
                                    <xsl:text>center</xsl:text>
                                </xsl:attribute>
                                <xsl:text>Required?</xsl:text>
                            </xsl:element>
                            <xsl:element name="entry">
                                <xsl:attribute name="align">
                                    <xsl:text>center</xsl:text>
                                </xsl:attribute>
                                <xsl:text>Runtime Expression?</xsl:text>
                            </xsl:element>
                            <xsl:element name="entry">
                                <xsl:attribute name="align">
                                    <xsl:text>center</xsl:text>
                                </xsl:attribute>
                                <xsl:text>Description</xsl:text>
                            </xsl:element>
                        </xsl:element>
                    </xsl:element>
                    <xsl:element name="tbody">
                        <xsl:apply-templates select="attribute">
                            <xsl:sort select="name" order="ascending"/>
                        </xsl:apply-templates>
                    </xsl:element>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="attribute">
        <xsl:element name="row">
            <xsl:element name="entry">
                <xsl:element name="para">
                    <xsl:value-of select="name"/>
                </xsl:element>
            </xsl:element>
            <xsl:element name="entry">
                <xsl:element name="para">
                    <xsl:value-of select="required"/>
                </xsl:element>
            </xsl:element>
            <xsl:element name="entry">
                <xsl:element name="para">
                    <xsl:value-of select="rtexprvalue"/>
                </xsl:element>
            </xsl:element>
            <xsl:element name="entry">
                <xsl:element name="para">
                    <xsl:value-of select="description"/>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>
    
</xsl:stylesheet>
