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
                <title><xsl:value-of select="beans/@beandocFileName"/></title>
                <link rel="stylesheet" href="{beans/@beandocCssLocation}" type="text/css"/>
                <!-- hack using an MS extension to ensure the next stylesheet is *only* loaded by IE5 browsers -->
                <xsl:comment>[if IE 5]>
                    &lt;link rel="stylesheet" type="text/css" href="ie5.css" /&gt;
                    &lt;![endif]</xsl:comment>              
            </head>
  
            <body>
                <xsl:call-template name="menuBar"/>
                      
                <xsl:variable name="fileRoot">
                    <xsl:value-of select="substring-before(beans/@beandocFileName, '.xml')"/>
                </xsl:variable>
                
                <h1><xsl:value-of select="beans/@beandocFileName"/></h1>
                <p>
                    <a href="{$fileRoot}.{beans/@beandocGraphType}" title="View full size image">
                        <img src="{$fileRoot}.{beans/@beandocGraphType}" alt="Graph" id="inlineContextImage" />
                    </a>
                    
                    <strong>Description:</strong><br/>
                    <xsl:value-of select="beans/description"/>
                </p>
		                
			    <p class="beanAttributeSummary">
					<strong>Attributes</strong>
					<table summary="Attribute list for this context file">
						<xsl:apply-templates select="beans/@*"/>
					</table>
				</p>	
				
                <br style="clear:both"/>

                <a name="summary"><xsl:comment>::</xsl:comment></a>
                <h2>Summary of beans</h2>
                <table summary="Summary list and description of beans defined in this file">
                    <xsl:for-each select="beans/bean">
                        <xsl:variable name="beandocId">
                            <xsl:choose>
                                <xsl:when test="@id"><xsl:value-of select="@id"/></xsl:when>
                                <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
                                <xsl:otherwise>anonymous-inner-bean</xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <tr>
                            <td><a href="#{$beandocId}"><xsl:value-of select="$beandocId"/></a></td>
                            <td><xsl:value-of select="./description"/></td>
                        </tr>                    
                    </xsl:for-each>
                </table>
                
                <a name="detail"><xsl:comment>::</xsl:comment></a>
                <h2>Detail of beans</h2>
                <xsl:apply-templates select="beans/bean"/>
                
                <p id="pageFooter">
                	<xsl:value-of select="beans/@beandocPageFooter"/>
                </p>
            </body>
        </html>

    </xsl:template>
    
    
    <!--
     * Table of all attributes (inluding implicit values but excluding 'beandoc'
     * attributes) for a given node
    -->
    <xsl:template match="@*">
		<xsl:if test="substring(name(), 1, 7) != 'beandoc'">
		<tr><td class="keyLabel"><xsl:value-of select="name()"/></td>
		<td>
		<xsl:choose>
			<xsl:when test="name()='class'">
				<xsl:choose>
					<xsl:when test="../@beandocJavaDoc">
						<a class="classValue mono" href="{../@beandocJavaDoc}" target="secondary">
							<xsl:value-of select="."/>
						</a>
					</xsl:when>
					<xsl:otherwise>                     
						<span class="classValue mono"><xsl:value-of select="."/></span>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="name()='parent'">
				<a class="classValue mono" href="{../@beandocHtmlFileName}#{.}">
					<img align="middle" src="bean_local.gif" alt="bean"/> <xsl:value-of select="."/>
				</a> 
			</xsl:when>
			<xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
		</xsl:choose>
		</td></tr>
		</xsl:if>
    </xsl:template>
    
    
    
    <!--
     * Menu bar for top, possibly bottom, of each page
    -->
    <xsl:template name="menuBar">
        <div id="menuBar">
          <a class="menuItem" href="main.html">home</a> ::
          <a class="menuItem" href="#summary">summary</a> ::
          <a class="menuItem" href="#detail">detail</a>
        </div>
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
                <xsl:otherwise>anonymous-inner-bean</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <a name="{$beandocId}"><xsl:comment>::</xsl:comment></a>
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
        
        <p class="beanAttributeSummary">
			<strong>Attributes</strong>
			<table summary="Attribute list for this bean">
				<xsl:apply-templates select="./@*"/>
			</table>
		</p>
        
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
        <xsl:value-of select="."/><br/>
    </xsl:template>   
    
    
    
    <xsl:template match="props">
        <xsl:for-each select="./prop">
            <span class="keyLabel"><xsl:value-of select="@key"/></span> = <xsl:value-of select="."/><br/>
        </xsl:for-each>
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
        
        <a class="classValue mono" style="font-size:100%" href="{@beandocHtmlFileName}#{$refName}">
            <img align="middle" src="bean_local.gif" alt="bean"/> <xsl:value-of select="$refName"/>
        </a><br/>
    </xsl:template>
    
    
    
    <xsl:template match="map">
        <xsl:for-each select="./entry">
            <span class="keyLabel"><xsl:value-of select="@key"/></span> --&gt;
            <xsl:apply-templates/>
        </xsl:for-each>
    </xsl:template>
    
    
    <xsl:template match="list">
        <xsl:apply-templates/><br/>
    </xsl:template>

</xsl:stylesheet>
