package de.unigoe.eduroamcat.backend.models

import org.junit.Assert.assertEquals
import org.junit.Test


internal class EapConfigParserTest {
    private val eapConfigParser = EapConfigParser("src/test/res/XmlParserTestConfig.eap-config")

    @Test
    fun providerDisplayNameTest() {
        assertEquals("GWDG Goettingen - GWDG", eapConfigParser.getProviderDisplayName())
    }

    @Test
    fun providerDescriptionTest() {
        val expectedProviderDescription =
            "Profile for students and faculty staff of the University of Göttingen as well as customers of the GWDG."
        assertEquals(expectedProviderDescription, eapConfigParser.getProviderDescription())
    }

    @Test
    fun providerTermsOfUseTest() {
        val expectedTermsOfUse = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n" +
                "Nullam id varius elit. Nullam eget molestie nulla, et imperdiet ante.\n" +
                "Vestibulum bibendum condimentum justo, in tempus dolor bibendum in."
        assertEquals(expectedTermsOfUse, eapConfigParser.getTermsOfUse())
    }
//    @Test
//    fun providerTest() {
//    }
//    @Test
//    fun providerTest() {
//    }
//    @Test
//    fun providerTest() {
//    }
//    @Test
//    fun providerTest() {
//    }
}