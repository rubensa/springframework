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
 * JavaDoc-ish feel to it.
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
                <link rel="stylesheet" href="{beans/@beandocCssLocation}" type="text/css"/>
            </head>
  
            <body>
      
                <h1><xsl:value-of select="beans/@beandocFileName"/></h1>
                <xsl:variable name="fileRoot">
                    <xsl:value-of select="substring-before(beans/@beandocFileName, '.xml')"/>
                </xsl:variable>
                <p>
                    <a href="{$fileRoot}.{$beandocXslGraphType}" title="View full size image">
                        <img src="{$fileRoot}.{$beandocXslGraphType}" alt="Graph" id="inlineContextImage" />
                    </a>
                    <strong>Description:</strong><br/>
                    <xsl:value-of select="beans/description"/>
                    <br style="clear:both"/>
                </p>
                
                <!-- do beandoc -->
                <xsl:apply-templates select="beans/bean"/>
                
            </body>
        </html>

    </xsl:template>
    
    
    <!--
     * Bean description block showing title, properties and dependencies of each
     * bean in the context file.  A link to a graph showing the individual
     * context file that the bean was originally declared in is included
    -->
    <xsl:template match="bean">
        <xsl:variable name="beandocId">
            <xsl:choose>
                <xsl:when test="@id"><xsl:value-of select="@id"/></xsl:when>
                <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
                <xsl:otherwise>[anonymous inner bean]</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <!-- title and link to individual context graph -->
        <a name="{$beandocId}"></a>
        <table class="beanHeader"><tr>
            <th style="background-color: {@beandocFillColour}; width: 20px">
                <xsl:choose>
                    <xsl:when test="@beandocGraphName">
                        <a href="{@beandocGraphName}" target="_secondary" title="View individual graph for this bean">
                            <img src="bean_local.gif" alt="View individual graph for this bean" />
                        </a>
                    </xsl:when>
                    <xsl:otherwise>
                        <img src="bean_local.gif" alt="Bean" />
                    </xsl:otherwise>
                </xsl:choose>
            </th>
            <xsl:variable name="titleClass">
                <xsl:choose>
                    <xsl:when test="@abstract='true'">abstractTitle</xsl:when>
                    <xsl:otherwise>concreteTitle</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <th class="{$titleClass}">
                <xsl:value-of select="$beandocId"/> <xsl:if test="$titleClass='abstractTitle'"> (abstract bean)</xsl:if>
            </th>
        </tr></table>
      
        
        <!-- bean description if available -->
        <xsl:if test="description">
        <p class="descriptionBlock">
            <strong>Description:</strong><br/>
            <xsl:value-of select="./description"/>
        </p>
        </xsl:if>       
        
                
        <!-- classname linked to JavaDoc where configured -->
        <xsl:if test="@class">
        <p><strong>Class:</strong><br/>
        <xsl:choose><xsl:when test="@beandocJavaDoc">
            <a class="classValue mono" href="{@beandocJavaDoc}" target="_secondary">
                <xsl:value-of select="@class"/>
            </a>
            </xsl:when>
            
            <xsl:otherwise>                     
            <span class="classValue mono"><xsl:value-of select="@class"/></span>
            </xsl:otherwise>
        </xsl:choose>
        </p>
        </xsl:if>
        
        
        <!-- parent link (usually when no class defined) -->
        <xsl:if test="@parent">
        <p><strong>Parent:</strong><br/>
            <a class="classValue mono" href="{@beandocHtmlFileName}#{@parent}">
                <xsl:value-of select="@parent"/>
            </a> 
        </p>            
        </xsl:if>
        
        
        <!-- ctor args / dependencies / properties -->
        <xsl:if test="count(./constructor-arg)>0">
        <p><strong>Constructor arguments:</strong></p>
        <table class="invisibleTable">
            <tbody>
                <!-- ctor args -->
                <xsl:apply-templates select="./constructor-arg"/>                  
            </tbody>
        </table>
        </xsl:if>
        <xsl:if test="count(./property)>0">
        <p><strong>Dependencies and properties:</strong></p>
        <table class="invisibleTable">
            <tbody>
                <!-- properties -->
                <xsl:apply-templates select="./property"/>                  
            </tbody>
        </table>
        </xsl:if>
        
        <p/><br/>
    </xsl:template>
    
    
    <!--
     * property names and values for each bean property.  Delegates to 
     * sub-templates for Maps, Lists, Props etc.
    -->
    <xsl:template match="property">
        <tr>
            <td class="keyLabel"><xsl:value-of select="@name"/></td>
            <td>
                <xsl:apply-templates/>              
            </td>   
        </tr>       
    </xsl:template>
    
    
    
    <xsl:template match="constructor-arg">
        <tr>
            <td class="keyLabel">
                <xsl:if test="@index">index <xsl:value-of select="@index"/></xsl:if>
                <xsl:if test="@type">type <xsl:value-of select="@type"/></xsl:if>
            </td>
            <td>
                <xsl:apply-templates/>              
            </td>   
        </tr>       
    </xsl:template>
    
    

    <xsl:template match="value" name="value">
        <span class="propertyValue"><xsl:value-of select="."/></span><br/>
    </xsl:template>   
    
    
    
    <xsl:template match="props">
        <table class="invisibleTable">
        <xsl:for-each select="./prop">
        <tr>
            <td><span class="keyLabel"><xsl:value-of select="@key"/></span></td>
            <td><span class="propertyValue"><xsl:value-of select="."/></span></td>
        </tr>
        </xsl:for-each>
        </table>
    </xsl:template>
    
    
    
    <xsl:template match="ref">
        <xsl:variable name="refName">
            <xsl:choose>
                <xsl:when test="@bean">
                    <xsl:value-of select="@bean"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="@local"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <a class="classValue mono" style="font-size:100%" href="{@beandocHtmlFileName}#{$refName}"><xsl:value-of select="$refName"/></a><br/>
    </xsl:template>
    
    <xsl:template match="map">
        <table class="invisibleTable">
        <xsl:for-each select="./entry">
        <tr>
            <td><span class="keyLabel"><xsl:value-of select="@key"/></span></td>
            <td><xsl:apply-templates select="."/></td>
        </tr>
        </xsl:for-each>
        </table>
    </xsl:template>

</xsl:stylesheet>
