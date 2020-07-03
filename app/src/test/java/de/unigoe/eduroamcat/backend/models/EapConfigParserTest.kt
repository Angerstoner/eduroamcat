package de.unigoe.eduroamcat.backend.models

import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element

/**
 * JUnit Test for parsing of downloaded eap-config
 *
 * Some tests are missing, because they require calls to the Android API which is not available via
 * - Test for [EapConfigParser.getServerCertificateList] depends on [android.util.Base64.decode]
 * - Test for [EapConfigParser.getProviderLogo] depends on [android.util.Base64.decode]
 * - Test for [EapConfigParser.getProviderLocations] depends on [android.location.Location]
 */
internal class EapConfigParserTest {
    private val eapConfigParser = EapConfigParser("src/test/res/XmlParserTestConfig.eap-config")
    private fun getFirstAuthMethod() = eapConfigParser.getAuthenticationMethodElements().item(0) as Element


    @Test
    fun outerEapTypeTest() {
        val expectedEapType = EapType.PEAP
        assertEquals(expectedEapType, eapConfigParser.getOuterEapType(getFirstAuthMethod()))
    }

    @Test
    fun serverIdTest() {
        val expectedServerId = "eduroam.gwdg.de"
        assertEquals(expectedServerId, eapConfigParser.getServerId(getFirstAuthMethod()))
    }

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

    @Test
    fun providerHelpdeskEmailTest() {
        val expectedEmail = "support@gwdg.de"
        assertEquals(expectedEmail, eapConfigParser.getHelpdeskEmailAddress())
    }

    @Test
    fun providerHelpdeskPhoneTest() {
        val expectedPhoneNumber = "+49 551 201 1523"
        assertEquals(expectedPhoneNumber, eapConfigParser.getHelpdeskPhoneNumber())
    }

    @Test
    fun providerWebAddressTest() {
        val expectedWebAddress = "https://www.gwdg.de/wlan"
        assertEquals(expectedWebAddress, eapConfigParser.getHelpdeskWebAddress())
    }
}