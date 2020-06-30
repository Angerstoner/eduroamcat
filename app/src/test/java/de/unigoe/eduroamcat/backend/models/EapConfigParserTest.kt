package de.unigoe.eduroamcat.backend.models

import org.junit.Assert.assertEquals
import org.junit.Test

internal class EapConfigParserTest {
    private val eapConfigParser = EapConfigParser("src/test/res/XmlParserTestConfig.eap-config")

    @Test
    fun providerNameTest() {
        assertEquals("GWDG Goettingen - GWDG", eapConfigParser.getProviderName())
    }
}