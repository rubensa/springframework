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
 * Converts a decorated DOM tree into a .dot file for consumption by GraphViz.
 *
 * @author Darren Davison, credit to Mike Thomas whose SpringViz program 
 * provided some of the ideas.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text"/>
	
    <!--
     *
     * sets digraph and applies templates for beans and refs.  The xpath expression
     * used to select beans will automatically filter out any anonymous inner 
     * beans in the tree.
     *
    -->
    <xsl:template match="/">
	/*
	 * directed graph dot input file.
	 *
	 * generated by Spring BeanDoc
	 */
	digraph G {
        <xsl:apply-templates select="beans"/>
        
        /* bean definitions */
        <xsl:apply-templates select="beans/bean"/>
        
        /* dependencies */
        <xsl:apply-templates select="beans/bean/property//ref"/>
	}
    </xsl:template>


    <!--
     *
     * sets defaults for graph, node and edge elements
     *
    -->
    <xsl:template match="beans">
        graph [ 
        	<xsl:if test="@beandocGraphSize">size="<xsl:value-of select="@beandocGraphSize"/>"</xsl:if>
        	ratio="<xsl:value-of select="@beandocGraphRatio"/>" 
        	label="<xsl:value-of select="@beandocFileName"/>" 
        	labelloc=<xsl:value-of select="@beandocGraphLabelLocation"/>
        ];
    	node  [ 
    		shape="<xsl:value-of select="@beandocGraphBeanShape"/>", 
    		style="dotted", 
    		fontname="<xsl:value-of select="@beandocGraphFontName"/>", 
    		fontsize="<xsl:value-of select="@beandocGraphFontSize"/>"
    	];
    	edge  [ ];
    </xsl:template>
    
    
    <!--
     *
     * override node styles for beans in the DOM and draw parent/child
     * relationships.  Anonymous inner beans are not graphed since it
     * reduces clarity in the graph.
     *
    -->
    <xsl:template match="bean">
    	<xsl:variable name="beandocId">
    		<xsl:choose>
		  		<xsl:when test="@id">
					<xsl:value-of select="@id"/>
		  		</xsl:when>
			  	<xsl:otherwise>
					<xsl:value-of select="@name"/>
			  	</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		"<xsl:value-of select="$beandocId"/>" [
        	peripheries=2, 
	        style=filled, 
	        color="<xsl:value-of select="@beandocFillColour"/>"
	        <xsl:if test="@abstract = 'true'">shape=polygon, sides=4, skew=.3</xsl:if>
	    ];
        <xsl:if test="@parent">
        	<!-- special type of relationship - dotted line, no arrow -->
        "<xsl:value-of select="@parent"/>" -&gt; "<xsl:value-of select="@id"/><xsl:value-of select="@name"/>" [
        	style=dotted arrowhead=none
        ];
        </xsl:if>
    </xsl:template>


    <!--
     *
     * show relationships (dependencies) indicated by 'ref' tags in the DOM
     *
    -->
    <xsl:template match="ref">
        "<xsl:value-of select="ancestor::bean/@id"/><xsl:value-of select="ancestor::bean/@name"/>" -&gt; "<xsl:value-of select="@bean"/><xsl:value-of select="@local"/>" [ ]
    </xsl:template>

</xsl:stylesheet>
