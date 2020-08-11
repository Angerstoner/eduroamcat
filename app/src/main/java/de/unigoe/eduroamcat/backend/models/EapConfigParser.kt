package de.unigoe.eduroamcat.backend.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Base64
import android.util.Log
import de.unigoe.eduroamcat.backend.util.getFirstElementByTag
import de.unigoe.eduroamcat.backend.util.getTextContentForXmlPath
import de.unigoe.eduroamcat.backend.util.iterator
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.security.cert.X509Certificate
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Parser for .eap-config files
 *
 *
 * Some methods throw NoSuchElementExceptions if the user tries to access an Element not given in the eap-config.
 * The calling method must catch those exceptions and provide feedback to the user and the debug log
 *
 * Used tags can be found in [de.unigoe.eduroamcat.backend.models.EapConfigTags.kt]
 * Used util helpers can be found in [de.unigoe.eduroamcat.backend.util.XmlExtensions.kt]
 */
class EapConfigParser(eapConfigFilePath: String) {
    private val tag = "EAPConfigParser"
    private val parsedConfig = EapConfig()

    private val eapConfig: Document

    /**
     * Initialize a EapConfigParser instance with a given eap config
     * Since the eap config is given in XML format, it's accessed with [javax.xml.parsers.DocumentBuilder]
     * */
    init {
        val eapConfigFile = File(eapConfigFilePath)
        val configBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        eapConfig = configBuilder.parse(eapConfigFile)
    }

    /** Returns Authentication Methods from the 'AuthenticationMethods' block as NodeList for further processing */
    fun getAuthenticationMethodElements(): NodeList =
        eapConfig.getFirstElementByTag(AUTHENTICATION_METHOD_LIST).getElementsByTagName(AUTHENTICATION_METHOD)

    /** Returns the InnerAuthenticationMethod block as NodeList. This typically contains the EAPType used in Phase 2 */
    fun getInnerAuthMethodElements(authenticationMethodElt: Element): NodeList =
        authenticationMethodElt.getElementsByTagName(INNER_AUTHENTICATION_METHOD)

    /**
     * Returns the ServerSideCredential block as NodeList.
     * This typically contains the ServerID and the server side certificates used in TTLS, TLS and PEAP
     */
    fun getServerSideCredentialElements(authenticationMethodElt: Element): Element? =
        authenticationMethodElt.getFirstElementByTag(SERVER_SIDE_CREDENTIALS)

    /**
     * Returns the ClientSideCredential block as NodeList.
     * This typically contains the OuterIdentity/AnonymousIdentity and in some cases the allow-save flag
     */
    fun getClientSideCredentialElements(authenticationMethodElt: Element): Element? =
        authenticationMethodElt.getFirstElementByTag(CLIENT_SIDE_CREDENTIALS)

    /**
     * Returns the EapType in the given AuthenticationMethodElement
     */
    fun getEapType(authenticationMethodElement: Element): EapType =
        EapType.getEapType(authenticationMethodElement.getTextContentForXmlPath(EAP_METHOD, EAP_METHOD_TYPE).toInt())

    /**
     * Returns Base64-decoded certificates found in the given [serverSideCredentialElt] block as [ArrayList] containing [X509Certificate]
     */
    fun getServerCertificateList(serverSideCredentialElt: Element): List<X509Certificate> {
        val certificateList = ArrayList<X509Certificate>()
        val base64CertificateEltList = serverSideCredentialElt.getElementsByTagName(SERVER_SIDE_CERTIFICATE)

        base64CertificateEltList.iterator().forEach {
            val parsedCertificateBase64 = Base64.decode(it.textContent, Base64.DEFAULT)
            val parsedServerCertificate = X509Certificate.getInstance(parsedCertificateBase64)
            certificateList.add(parsedServerCertificate)
        }

        return certificateList
    }

    /**
     * Returns list of ServerID (e.g. URL of the authentication server) found in the given [serverSideCredentialElt] block
     * Can contain multiple Server IDs, when multiple authentication servers are used
     */
    fun getServerId(serverSideCredentialElt: Element): List<String> {
        val serverIdList = ArrayList<String>()
        serverSideCredentialElt.getElementsByTagName(SERVER_ID).iterator()
            .forEach { serverIdList.add(it.textContent) }
        return serverIdList
    }

    /**
     * Returns value of allow-save flag if set in the given [clientSideCredElt], otherwise it defaults to true
     */
    fun getAllowSave(clientSideCredElt: Element): Boolean {
        return try {
            clientSideCredElt.getTextContentForXmlPath(CLIENT_SIDE_ALLOW_SAVE).toBoolean()
        } catch (e: NoSuchElementException) {
            Log.i(tag, "Tag $CLIENT_SIDE_ALLOW_SAVE not found")
            true
        }
    }

    /**
     * Returns value of NonEAPAuthMethod if a non eap method is used for phase 2 authentication
     */
    fun getNonEapAuthMethod(innerAuthMethodElt: Element): Int =
        innerAuthMethodElt.getTextContentForXmlPath(NON_EAP_METHOD, EAP_METHOD_TYPE).toInt()

    /**
     * Returns outer/anonymous identity which should be used outside of the tunnel in TTLS or PEAP
     */
    fun getAnonymousIdentity(clientSideCredElt: Element): String =
        clientSideCredElt.getTextContentForXmlPath(CLIENT_SIDE_OUTER_IDENTITY)

    /**
     * Returns specified SSID (eduroam in most cases) from the CredentialApplicability block
     */
    fun getSsid(): String = getFromCredentialApplicability(SSID)

    /**
     * Returns minimal RSN protocol (TKIP/CCMP) version from the CredentialApplicability block
     */
    fun getMinRsnProto(): String = getFromCredentialApplicability(MIN_RSN_PROTO)

    /**
     *  Returns Consortium OID (Hotspot 2.0)
     *  Currently not used for this project
     */
    fun getConsortiumOID(): String = getFromCredentialApplicability(CONSORTIUM_OID)

    /**
     * Returns display name of the EAP provider
     */
    fun getProviderDisplayName(): String =
        eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_DISPLAY_NAME)


    /**
     * Returns description of the EAP provider
     */
    fun getProviderDescription(): String =
        eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_DESCRIPTION)


    /**
     * Returns location of the EAP provider (e.g. for the helpdesk)
     */
    fun getProviderLocations(): List<Location> {
        val locationStringList = eapConfig.getFirstElementByTag(PROVIDER_INFO).getElementsByTagName(PROVIDER_LOCATION)
        val locationList = ArrayList<Location>()
        locationStringList.iterator().asSequence()
            .filter { it.nodeType == Node.ELEMENT_NODE }.map { it as Element }
            .forEach {
                try {
                    val location = Location("")
                    with(location) {
                        longitude = it.getTextContentForXmlPath(PROVIDER_LOCATION_LONG).toDouble()
                        latitude = it.getTextContentForXmlPath(PROVIDER_LOCATION_LAT).toDouble()
                    }
                    locationList.add(location)
                } catch (e: NumberFormatException) {
                    Log.e(tag, "COULD NOT PARSE LONGITUDE/LATITUDE TO DOUBLE")
                }
            }
        return locationList
    }

    /**
     * Returns logo of the EAP provider as Bitmap (decoded from Base64)
     */
    fun getProviderLogo(): Bitmap {
        val base64LogoString = eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_LOGO)
        val base64Logo = Base64.decode(base64LogoString.toByteArray(), Base64.DEFAULT)

        return BitmapFactory.decodeByteArray(base64Logo, 0, base64Logo.size)
    }


    /**
     * Returns Terms of Use of the EAP provider
     */
    fun getTermsOfUse(): String =
        eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_TERMS_OF_USE)


    /**
     * Returns helpdesk email address of the EAP provider
     */
    fun getHelpdeskEmailAddress(): String =
        eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_HELPDESK, PROVIDER_EMAIL)


    /**
     * Returns helpdesk web address of the EAP provider
     */
    fun getHelpdeskWebAddress(): String =
        eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_HELPDESK, PROVIDER_WEB_ADDRESS)


    /**
     * Returns helpdesk phone number address of the EAP provider
     */
    fun getHelpdeskPhoneNumber(): String =
        eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_HELPDESK, PROVIDER_PHONE)

    /**
     * Returns content of the given [tag]
     *
     * Since the Credential Applicability block differs as bit from the other
     * blocks (such as the AuthenticationMethod), it is accessed a little bit different.
     * The Credential Applicability block can contain multiple occurrences of the same [IEEE_80211] block,
     * each containing different child elements. Therefore all these instances are searched
     * for the requested tag.
     *
     * If no such tag is to be found in any instance, a [NoSuchElementException] is raised.
     */
    private fun getFromCredentialApplicability(tag: String): String {
        eapConfig.getFirstElementByTag(CREDENTIAL_APPLICABILITY).getElementsByTagName(IEEE_80211)
            .iterator().forEach {
                try {
                    return (it as Element).getTextContentForXmlPath(tag)
                } catch (e: NoSuchElementException) { //do nothing, check the next element
                }
            }
        throw NoSuchElementException()
    }
}