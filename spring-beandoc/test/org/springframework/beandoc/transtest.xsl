<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output 
        method="xml" 
        doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
        doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
        />

    <xsl:template match="/">        
    	OUTPUT BY SPRING-BEANDOC UNIT TESTS.  This file is not important!
        <xsl:value-of select="@default-autowire"/>
    </xsl:template>
</xsl:stylesheet>
