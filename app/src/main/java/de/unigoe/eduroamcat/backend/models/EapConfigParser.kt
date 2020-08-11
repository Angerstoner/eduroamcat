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

    /**
     * Searches the given [AUTHENTICATION_METHOD_PARENT] block for the given AuthenticationsMethods.
     * Typically the [AUTHENTICATION_METHOD_PARENT] block contains one or more [AUTHENTICATION_METHOD]s
     *
     * Returns [NodeList] of given AuthenticationMethods
     */
    fun getAuthenticationMethodElements(): NodeList =
        eapConfig.getFirstElementByTag(AUTHENTICATION_METHOD_PARENT).getElementsByTagName(AUTHENTICATION_METHOD)

    /**
     * Returns [NodeList] of InnerAuthenticationsMethods within [authenticationMethodElt] block
     *
     * [EapType.EAP_TTLS] and [EapType.PEAP] require inner authentications
     * such as [EapType.MSCHAPv2], [EapType.PAP] or [EapType.GTC]
     */
    fun getInnerAuthMethodElements(authenticationMethodElt: Element): NodeList =
        authenticationMethodElt.getElementsByTagName(INNER_AUTHENTICATION_METHOD)

    /**
     * Returns ServerSideCredentials block as [Element] for further processing
     * ServerSideCredentials block contains [SERVER_SIDE_CERTIFICATE] and [SERVER_ID]
     */
    fun getServerSideCredentialElements(authenticationMethodElement: Element): Element? =
        authenticationMethodElement.getFirstElementByTag(SERVER_SIDE_CREDENTIALS)


    /**
     * Returns ClientSideCredentials block as [Element] for further processing
     * ClientSideCredentials block contains [CLIENT_SIDE_OUTER_IDENTITY] and [CLIENT_SIDE_ALLOW_SAVE]
     */
    fun getClientSideCredentialElements(authenticationMethodElt: Element): Element? =
        authenticationMethodElt.getFirstElementByTag(CLIENT_SIDE_CREDENTIALS)

    /**
     * Returns [EapType] for given [authenticationMethodElt] block.
     *
     * Gives inner EapType, if called with innerAuthenticationMethod
     */
    fun getEapType(authenticationMethodElt: Element): EapType =
        EapType.getEapType(authenticationMethodElt.getTextContentForXmlPath(EAP_METHOD, EAP_METHOD_TYPE).toInt())

    /**
     * Collects and returns [List] of ServerSideCertificates in [X509Certificate] format.
     *
     * Certificates are given as [Base64] in the eap-config
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
     * Returns [ArrayList] of ServerID (e.g. URL of the authentication server) found in the given [serverSideCredentialElt] block
     * Can contain multiple Server IDs, when multiple authentication servers are used
     */
    fun getServerId(serverSideCredentialElt: Element): List<String> {
        val serverIdList = ArrayList<String>()
        serverSideCredentialElt.getElementsByTagName(SERVER_ID).iterator()
            .forEach { serverIdList.add(it.textContent) }
        return serverIdList
    }

    /**
     * Returns [Boolean] if entered or parsed credentials are legal to store on the device
     *
     * Defaults to true, if the is no such element
     */
    fun getAllowSave(clientSideCredElt: Element): Boolean =
        try {
            clientSideCredElt.getTextContentForXmlPath(CLIENT_SIDE_ALLOW_SAVE).toBoolean()
        } catch (e: NoSuchElementException) {
            Log.i(tag, "Tag $CLIENT_SIDE_ALLOW_SAVE not found")
            true
        }

    /**
     * Returns Anonymous/OuterIdentity for the tunnel for disguising the real user
     *
     * Used in EAP-TTLS and PEAP
     */
    fun getAnonymousIdentity(clientSideCredElt: Element): String =
        clientSideCredElt.getTextContentForXmlPath(CLIENT_SIDE_OUTER_IDENTITY)

    /**
     * Returns NonEapType for given [innerAuthMethodElt] block.
     *
     * Mutual exclusive with [EapType] within InnerAuthentication block
     */
    fun getNonEapAuthMethod(innerAuthMethodElt: Element): Int =
        innerAuthMethodElt.getTextContentForXmlPath(NON_EAP_METHOD, EAP_METHOD_TYPE).toInt()

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
        val locationStringList =
            eapConfig.getFirstElementByTag(PROVIDER_INFO).getElementsByTagName(PROVIDER_LOCATION)
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