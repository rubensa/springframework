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

	<xsl:import href="./i18n.xsl"/>
                
    <xsl:output 
        method="xml" 
        indent="no"
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
        />
		
	<xsl:variable name="pathRel"><xsl:value-of select="/beans/@beandocPathRelative"/></xsl:variable>
	
    <!--
     * Template structure of HTML output
    -->
    <xsl:template match="/">
		
        <html>
            <head>
                <title><xsl:value-of select="beans/@beandocFileName"/></title>
                <link rel="stylesheet" href="{$pathRel}{beans/@beandocCssLocation}" type="text/css"/>
                <!-- hack using an MS extension to ensure the next stylesheet is *only* loaded by IE5 browsers -->
                <xsl:comment>[if IE 5]>
                    &lt;link rel="stylesheet" type="text/css" href="ie5.css" /&gt;
                    &lt;![endif]</xsl:comment>              
            </head>
  
            <body>
                <a name="top"><xsl:comment>::</xsl:comment></a>
                <xsl:call-template name="menuBar"/>
                
      			<div id="contentWell">
	                <h1><xsl:value-of select="beans/@beandocFileName"/></h1>
	                <p>
	                	<xsl:if test="not(beans/@beandocNoGraphs)">
		                    <a href="{$pathRel}{beans/@beandocFileName}.graph.html" title="View full size image">
		                        <img src="{$pathRel}{beans/@beandocFileName}.{beans/@beandocGraphType}" alt="Graph" id="inlineContextImage" />
		                    </a>
	                    </xsl:if>
	                    
	                    <strong><xsl:value-of select="$i18n-description"/>:</strong><br/>
	                    <xsl:value-of select="beans/description"/>
	                </p>
	                        
	                <p class="beanAttributeSummary">
	                    <strong><xsl:value-of select="$i18n-attributes"/></strong>
	                    <table class="invisibleTable" summary="Attribute list for this context file">
	                    	<tbody>
		                        <xsl:apply-templates select="beans/@*"/>
		                    </tbody>
	                    </table>
	                </p>    
	                
	                <br style="clear:both"/>
	
	                <a name="summary"><xsl:comment>::</xsl:comment></a>
	                <h2><xsl:value-of select="$i18n-summaryTitle"/></h2>
	                <table class="invisibleTable" summary="Summary list and description of beans defined in this file">
	                    <xsl:for-each select="beans/bean">
	                        <xsl:variable name="beandocId">
	                            <xsl:choose>
	                                <xsl:when test="@id"><xsl:value-of select="@id"/></xsl:when>
	                                <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
	                                <xsl:otherwise><xsl:value-of select="$i18n-innerbean"/></xsl:otherwise>
	                            </xsl:choose>
	                        </xsl:variable>
	                        <tr>
	                            <td><a href="#{$beandocId}"><xsl:value-of select="$beandocId"/></a></td>
	                            <td><xsl:value-of select="./description"/></td>
	                        </tr>                    
	                    </xsl:for-each>
	                </table>
	                
	                <a name="detail"><xsl:comment>::</xsl:comment></a>
	                <h2><xsl:value-of select="$i18n-detailTitle"/></h2>
	                <xsl:apply-templates select="beans/bean"/>
                </div>
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
                <a class="classValue mono" href="{$pathRel}{../@beandocHtmlFileName}#{.}">
                    <img align="middle" src="{$pathRel}bean_local.gif" alt="bean"/> <xsl:value-of select="."/>
                </a> 
            </xsl:when>
            <xsl:when test="name()='depends-on'">
                <a class="classValue mono" href="{$pathRel}{../@beandocHtmlFileName}#{.}">
                    <img align="middle" src="{$pathRel}bean_local.gif" alt="bean"/> <xsl:value-of select="."/>
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
          <a class="menuItem" href="{$pathRel}main.html"><xsl:value-of select="$i18n-home"/></a> ::
          <a class="menuItem" href="#summary"><xsl:value-of select="$i18n-summary"/></a> ::
          <a class="menuItem" href="#detail"><xsl:value-of select="$i18n-detail"/></a>
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
                <xsl:otherwise><xsl:value-of select="$i18n-innerbean"/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <a name="{$beandocId}"><xsl:comment>::</xsl:comment></a>
        <div style="background-color: {@beandocFillColour}" class="beanHeader">
                <img src="{$pathRel}bean_local.gif" alt="Bean" />
        </div>
        <xsl:variable name="titleClass">
            <xsl:choose>
                <xsl:when test="@abstract='true'">abstractTitle</xsl:when>
                <xsl:otherwise>concreteTitle</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <span class="{$titleClass}">
            <xsl:value-of select="$beandocId"/> <xsl:if test="$titleClass='abstractTitle'"> (<xsl:value-of select="$i18n-abstractbean"/>)</xsl:if>
        </span>
      
        
        <!-- bean description if available -->
        <xsl:if test="description">
        <p class="descriptionBlock">
            <strong><xsl:value-of select="$i18n-description"/>:</strong><br/>
            <xsl:value-of select="./description"/>
        </p>
        </xsl:if>       
        
        <p class="beanAttributeSummary">
            <strong><xsl:value-of select="$i18n-attributes"/>:</strong>
            <table class="invisibleTable" summary="Attribute list for this bean">
                <xsl:apply-templates select="./@*"/>
            </table>
        </p>
        
        <!-- ctor args / dependencies / properties -->
        <xsl:if test="count(./constructor-arg)>0">
        <p><strong><xsl:value-of select="$i18n-constructorargs"/>:</strong></p>
        <table class="invisibleTable" summary="List of constructor arguments">
            <tbody>
                <!-- ctor args -->
                <xsl:apply-templates select="./constructor-arg"/>                  
            </tbody>
        </table>
        </xsl:if>
        
        <xsl:if test="count(./property)>0 or count(./lookup-method)>0 or count(./replaced-method)>0">
        <p><strong><xsl:value-of select="$i18n-deps"/>:</strong></p>
        <table class="invisibleTable" summary="List of dependencies and public properties">
            <tbody>
                <!-- properties -->
                <xsl:apply-templates select="./property"/>
                <xsl:apply-templates select="./lookup-method"/>
                <xsl:apply-templates select="./replaced-method"/>
            </tbody>
        </table>
        </xsl:if>
        
        <xsl:if test="parent::*[name()='beans']">
	        <div class="beanFooter"><a href="#top"><xsl:value-of select="$i18n-backtotop"/></a></div>
	        <hr/>
	    </xsl:if>
    </xsl:template>
    
    
    <!--
     * property names and values for each bean property.  Delegates to 
     * sub-templates for Maps, Lists, Props etc.
    -->
    <xsl:template match="property">
        <tr>
            <td class="keyLabel">
				<xsl:value-of select="@name"/>
				<xsl:if test="./description"><br />(<xsl:value-of select="./description"/>)</xsl:if>
			</td>
            <td>
                <xsl:value-of select="@value"/>
				<xsl:if test="@ref">
					<xsl:call-template name="anyRef">
						<xsl:with-param name="refName"><xsl:value-of select="@ref"/></xsl:with-param>
					</xsl:call-template>
				</xsl:if>
				<xsl:apply-templates/> 
            </td>   
        </tr>       
    </xsl:template>
    
    
    
    <xsl:template match="constructor-arg">
        <tr>
            <td class="keyLabel">
                <xsl:if test="@index">index <xsl:value-of select="@index"/></xsl:if>
                <xsl:if test="@type">type <xsl:value-of select="@type"/></xsl:if>           
				<xsl:if test="./description"><br />(<xsl:value-of select="./description"/>)</xsl:if>
            </td>
            <td>
                <xsl:value-of select="@value"/>
				<xsl:if test="@ref">
					<xsl:call-template name="anyRef">
						<xsl:with-param name="refName"><xsl:value-of select="@ref"/></xsl:with-param>
					</xsl:call-template>
				</xsl:if>
				<xsl:apply-templates/>              
            </td>   
        </tr>       
    </xsl:template>
        

    <xsl:template match="lookup-method">
        <tr>
            <td class="keyLabel">
                lookup-method
            </td>
            <td>
                <xsl:value-of select="$i18n-name"/> <xsl:value-of select="@name"/><br/>
                <xsl:value-of select="$i18n-bean"/> 
                <a class="classValue mono" style="font-size:100%" href="{@beandocHtmlFileName}#{@bean}">
                    <img align="middle" src="{$pathRel}bean_local.gif" alt="bean"/> <xsl:value-of select="@bean"/>
                </a>
            </td>   
        </tr>
    </xsl:template>
    
    
    <xsl:template match="replaced-method">
        <tr>
            <td class="keyLabel">
                replaced-method
            </td>
            <td>
                <xsl:value-of select="$i18n-name"/> <xsl:value-of select="@name"/><br/>
                <xsl:value-of select="$i18n-replacer"/> 
                <a class="classValue mono" style="font-size:100%" href="{@beandocHtmlFileName}#{@replacer}">
                    <img align="middle" src="{$pathRel}bean_local.gif" alt="bean"/> <xsl:value-of select="@replacer"/>
                </a>
                <xsl:if test="count(./arg-type)>0">
                    <br/><br/><strong><xsl:value-of select="$i18n-argtypes"/>:</strong><br/>
                    <xsl:apply-templates/>
                </xsl:if>
            </td>   
        </tr>
    </xsl:template>    
    
    
    <xsl:template match="value | arg-type" name="value">
        <xsl:value-of select="."/>
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
        <xsl:call-template name="anyRef">
			<xsl:with-param name="refName"><xsl:value-of select="$refName"/></xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
	
	<xsl:template name="anyRef">
		<xsl:param name="refName" select="missing"/>		
        <a class="classValue mono" style="font-size:100%" href="{$pathRel}{@beandocHtmlFileName}#{$refName}">
            <img align="middle" src="{$pathRel}bean_local.gif" alt="bean"/> <xsl:value-of select="$refName"/>
        </a>
	</xsl:template>
    
    
    <xsl:template match="entry">
        <xsl:if test="@key">
			<xsl:value-of select="@key"/> --&gt; <xsl:value-of select="@value"/>
		</xsl:if>
        <xsl:if test="@key-ref">
			<xsl:call-template name="anyRef">
				<xsl:with-param name="refName">
					<xsl:value-of select="@key-ref"/>
				</xsl:with-param>
			</xsl:call-template> --&gt; <xsl:value-of select="@value"/>
		</xsl:if>
        <xsl:if test="@value-ref">
			<xsl:call-template name="anyRef">
				<xsl:with-param name="refName">
					<xsl:value-of select="@value-ref"/>
				</xsl:with-param>
			</xsl:call-template>
		</xsl:if>
        <xsl:apply-templates/><br/>
    </xsl:template>
    
    
    <xsl:template match="key">
        <xsl:apply-templates/> --&gt;
    </xsl:template>
    
    
    <xsl:template match="list|map|set">
        <xsl:apply-templates/>
    </xsl:template>
	
	
    <xsl:template match="description">
        <xsl:text></xsl:text>
    </xsl:template>

</xsl:stylesheet>
