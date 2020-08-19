package de.unigoe.eduroamcat.backend.models

import android.net.wifi.WifiEnterpriseConfig
import de.unigoe.eduroamcat.backend.util.EapConfigParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.util.*

/**
 * JUnit Test for parsing of downloaded eap-config
 *
 * Some tests are missing, because they require calls to the Android API which is not available via
 * - Test for [EapConfigParser.getServerCertificates] depends on [android.util.Base64.decode]
 * - Test for [EapConfigParser.getProviderLogo] depends on [android.util.Base64.decode]
 * - Test for [EapConfigParser.getProviderLocations] depends on [android.location.Location]
 */
internal class EapConfigParserTest {
    private val eapConfigParser = EapConfigParser("src/test/res/XmlParserTestConfig.eap-config")
    private val eapConfigParserMissingFields =
        EapConfigParser("src/test/res/XmlParserTestConfigFull.eap-config")
    private val firstAuthMethodElement = eapConfigParser.getAuthenticationMethodElements()[0]
    private val firstInnerAuthMethod =
        eapConfigParser.getInnerAuthMethodElements(firstAuthMethodElement)[0]

    private fun getInnerAuthWithClientAndServerSide(): Element {
        val authMethod =
            eapConfigParserMissingFields.getAuthenticationMethodElements()[1]
        return eapConfigParserMissingFields
            .getInnerAuthMethodElements(authMethod)[0]
    }

    @Test(expected = NoSuchElementException::class)
    fun exceptionTest() {
        val eapParserWithExceptions = EapConfigParser("src/test/res/XmlParserTestConfigEmpty.eap-config")
        eapParserWithExceptions.getAuthenticationMethodElements() // this should raise a NoSuchElementException
    }

    @Test
    fun eapTypeTest() {
        val expectedOuterEapType = WifiEnterpriseConfig.Eap.PEAP
        val expectedInnerEapType = WifiEnterpriseConfig.Phase2.MSCHAPV2

        assertEquals(expectedOuterEapType, eapConfigParser.getEapType(firstAuthMethodElement))
        assertEquals(expectedInnerEapType, eapConfigParser.getEapType(firstInnerAuthMethod))
    }

    @Test
    fun nonEapTypeTest() {
        val authEltWithNonEapType =
            eapConfigParserMissingFields.getAuthenticationMethodElements()[2]
        val innerAuthEltWithNonEapType = eapConfigParserMissingFields
            .getInnerAuthMethodElements(authEltWithNonEapType)[0]

        val expectedNonEapType = 1
        assertEquals(
            expectedNonEapType,
            eapConfigParserMissingFields.getNonEapAuthMethod(innerAuthEltWithNonEapType)
        )
    }

    @Test
    fun serverIdTest() {
        val serverSideCredElt = eapConfigParser.getServerSideCredentialElements(firstAuthMethodElement)!!
        val expectedServerId = arrayListOf("eduroam.gwdg.de")

        assertEquals(expectedServerId, eapConfigParser.getServerIds(serverSideCredElt))


        val innerServerSideCredElt =
            eapConfigParserMissingFields.getServerSideCredentialElements(getInnerAuthWithClientAndServerSide())!!
        val expectedInnerServerId = arrayListOf("radius1.umk.pl", "radius2.umk.pl")

        assertEquals(expectedInnerServerId, eapConfigParserMissingFields.getServerIds(innerServerSideCredElt))
    }

    @Test
    fun allowSaveTest() {
        val clientSideCredElt = eapConfigParser.getClientSideCredentialElements(firstAuthMethodElement)!!
        assertTrue(eapConfigParser.getAllowSave(clientSideCredElt))
    }

    @Test
    fun anonymousIdentityTest() {
        val expectedOuterIdentity = "eduroam@gwdg.de"
        val clientSideCredElt = eapConfigParser.getClientSideCredentialElements(firstAuthMethodElement)!!
        assertEquals(expectedOuterIdentity, eapConfigParser.getAnonymousIdentity(clientSideCredElt))

        val innerClientSideCredElt =
            eapConfigParserMissingFields.getClientSideCredentialElements(getInnerAuthWithClientAndServerSide())!!
        val expectedOuterIdentityInnerAuth = "anonymous@umk.pl"

        assertEquals(
            expectedOuterIdentityInnerAuth,
            eapConfigParserMissingFields.getAnonymousIdentity(innerClientSideCredElt)
        )
    }

    @Test
    fun credentialApplicabilityTest() {
        assertEquals("eduroam", eapConfigParser.getSsid())
        assertEquals("CCMP", eapConfigParser.getMinRsnProto())
        assertEquals("001bc50460", eapConfigParser.getConsortiumOID())
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