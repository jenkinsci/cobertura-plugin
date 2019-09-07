<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml"/>

    <xsl:template match="/">
        <report>
            <xsl:attribute name="name">cobertura</xsl:attribute>
            <group>
                <xsl:apply-templates select="coverage/packages/package"/>
            </group>
            <xsl:apply-templates select="coverage/sources/source"/>
        </report>
    </xsl:template>


    <xsl:template match="coverage/sources/source">
        <additionalProperty name="source-file-path">
            <xsl:attribute name="value">
                <xsl:value-of select="text()"/>
            </xsl:attribute>
        </additionalProperty>
    </xsl:template>

    <xsl:template match="coverage/packages/package">
        <package>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:apply-templates select="classes"/>
        </package>
    </xsl:template>

    <xsl:template match="classes">
        <xsl:for-each-group select="class" group-by="@filename">
            <file>
                <xsl:attribute name="name">
                    <xsl:value-of select="current-grouping-key()"/>
                </xsl:attribute>
                <xsl:for-each select="current-group()">
                    <class>
                        <xsl:attribute name="name">
                            <xsl:value-of select="@name"/>
                        </xsl:attribute>
                        <xsl:apply-templates select="methods/method"/>
                        <xsl:apply-templates select="lines/line"/>
                    </class>
                </xsl:for-each>
            </file>
        </xsl:for-each-group>
    </xsl:template>

    <xsl:template match="methods/method">
        <method>
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:attribute name="signature">
                <xsl:value-of select="@signature"/>
            </xsl:attribute>
            <xsl:apply-templates select="lines/line"/>
        </method>
    </xsl:template>


    <xsl:template match="lines/line">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>