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
            	
    <xsl:output 
    	method="xml" 
    	doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
    	doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
    	/>
    <xsl:param name="beandocXslGraphType">png</xsl:param> 

    <!--
     * Template structure of HTML output
    -->
    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="beans/@beandocFileName"/></title>
                <link rel="stylesheet" href="{beans/@beandocCssLocation}" type="text/css"/>
            </head>
  
            <body>
            	<xsl:call-template name="menuBar"/>
            	      
                <xsl:variable name="fileRoot">
                    <xsl:value-of select="substring-before(beans/@beandocFileName, '.xml')"/>
                </xsl:variable>
                
                <h1><xsl:value-of select="beans/@beandocFileName"/></h1>
                <p>
                    <a href="{$fileRoot}.{$beandocXslGraphType}" title="View full size image">
                        <img src="{$fileRoot}.{$beandocXslGraphType}" alt="Graph" id="inlineContextImage" />
                    </a>
                	
                	<strong>Description:</strong><br/>
                    <xsl:value-of select="beans/description"/>
                    <br style="clear:both"/>
                </p>
                
                <!-- do beandoc -->
                <a name="summary" />
                <h2>Summary of beans</h2>
		    	<table summary="Summary list and description of beans defined in this file">
		    		<xsl:apply-templates select="beans/bean" mode="summary"/>
                </table>
                
                <a name="detail" />
                <h2>Detail of beans</h2>
                <xsl:apply-templates select="beans/bean" mode="detail"/>
                
            </body>
        </html>

    </xsl:template>
    
    
    
	<xsl:template name="menuBar">
	    <div id="menuBar">
	      <a class="menuItem" href="main.html">home</a> ::
	      <a class="menuItem" href="#summary">summary</a> ::
	      <a class="menuItem" href="#detail">detail</a>
	    </div>
    </xsl:template>
    	
    <!--
     * Summary table of bean names and descriptions
    -->
    <xsl:template match="bean" mode="summary">
        <xsl:variable name="beandocId">
            <xsl:choose>
                <xsl:when test="@id"><xsl:value-of select="@id"/></xsl:when>
                <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
                <xsl:otherwise>[anonymous inner bean]</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <tr>
        	<td><a href="#{$beandocId}"><xsl:value-of select="$beandocId"/></a></td>
        	<td><xsl:value-of select="./description"/></td>
        </tr>
    </xsl:template>
    
    
    
    <!--
     * Bean description block showing title, properties and dependencies of each
     * bean in the context file.  A link to a graph showing the individual
     * context file that the bean was originally declared in is included
    -->
    <xsl:template match="bean" mode="detail">
        <xsl:variable name="beandocId">
            <xsl:choose>
                <xsl:when test="@id"><xsl:value-of select="@id"/></xsl:when>
                <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
                <xsl:otherwise>[anonymous inner bean]</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <a name="{$beandocId}" />
        <div style="background-color: {@beandocFillColour}" class="beanHeader">
                <img src="bean_local.gif" alt="Bean" />
        </div>
        <xsl:variable name="titleClass">
            <xsl:choose>
                <xsl:when test="@abstract='true'">abstractTitle</xsl:when>
                <xsl:otherwise>concreteTitle</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <span class="{$titleClass}">
            <xsl:value-of select="$beandocId"/> <xsl:if test="$titleClass='abstractTitle'"> (abstract bean)</xsl:if>
        </span>
      
        
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
            <a class="classValue mono" href="{@beandocJavaDoc}" target="secondary">
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
        <table class="invisibleTable" summary="List of constructor arguments">
            <tbody>
                <!-- ctor args -->
                <xsl:apply-templates select="./constructor-arg"/>                  
            </tbody>
        </table>
        </xsl:if>
        <xsl:if test="count(./property)>0">
        <p><strong>Dependencies and properties:</strong></p>
        <table class="invisibleTable" summary="List of dependencies and public properties">
            <tbody>
                <!-- properties -->
                <xsl:apply-templates select="./property"/>                  
            </tbody>
        </table>
        </xsl:if>
        
        <br/><hr/>
    </xsl:template>
    
    
    <!--
     * property names and values for each bean property.  Delegates to 
     * sub-templates for Maps, Lists, Props etc.
    -->
    <xsl:template match="property">
        <tr>
            <td class="keyLabel"><xsl:value-of select="@name"/></td>
            <td>
                <xsl:apply-templates mode="detail"/>              
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
                <xsl:apply-templates mode="detail"/>              
            </td>   
        </tr>       
    </xsl:template>
    
    

    <xsl:template match="value" name="value">
        <xsl:value-of select="."/><br/>
    </xsl:template>   
    
    
    
    <xsl:template match="props" mode="detail">
        <table class="invisibleTable" summary="Property list">
        <xsl:for-each select="./prop">
        <tr>
            <td><span class="keyLabel"><xsl:value-of select="@key"/></span></td>
            <td><xsl:value-of select="."/></td>
        </tr>
        </xsl:for-each>
        </table>
    </xsl:template>
    
    
    
    <xsl:template match="ref" mode="detail">
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
        
        <a class="classValue mono" style="font-size:100%" href="{@beandocHtmlFileName}#{$refName}">
        	<img align="middle" src="bean_local.gif" alt="bean"/> <xsl:value-of select="$refName"/>
        </a><br/>
    </xsl:template>
    
    <xsl:template match="map" mode="detail">
        <table class="invisibleTable" summary="List of map items">
        <xsl:for-each select="./entry">
        <tr>
            <td><span class="keyLabel"><xsl:value-of select="@key"/></span></td>
            <td><xsl:apply-templates select="."/></td>
        </tr>
        </xsl:for-each>
        </table>
    </xsl:template>

</xsl:stylesheet>
