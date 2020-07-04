package de.unigoe.eduroamcat.backend.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Base64
import android.util.Log
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.security.cert.X509Certificate
import javax.xml.parsers.DocumentBuilderFactory


// TODO: outsource to different file
// AUTHENTICATION METHOD KEYS START
const val AUTHENTICATION_METHOD_LIST = "AuthenticationMethods"
const val AUTHENTICATION_METHOD = "AuthenticationMethod"
const val EAP_METHOD = "EAPMethod"
const val EAP_METHOD_TYPE = "Type"
const val NON_EAP_METHOD = "NonEAPAuthMethod"
const val SERVER_SIDE_CREDENTIALS = "ServerSideCredential"
const val SERVER_SIDE_CERTIFICATE = "CA"
const val SERVER_ID = "ServerID"
const val CLIENT_SIDE_CREDENTIALS = "ClientSideCredential"
const val CLIENT_SIDE_ALLOW_SAVE = "allow-save"
const val CLIENT_SIDE_OUTER_IDENTITY = "OuterIdentity"
const val INNER_AUTHENTICATION_METHOD = "InnerAuthenticationMethod"
// AUTHENTICATION METHOD KEYS END

// CREDENTIAL APPLICABILITY KEYS START
const val CREDENTIAL_APPLICABILITY = "CredentialApplicability"
const val IEEE_80211 = "IEEE80211"
const val SSID = "SSID"
const val MIN_RSN_PROTO = "MinRSNProto"
const val CONSORTIUM_OID = "ConsortiumOID"
// CREDENTIAL APPLICABILITY KEYS END

// PROVIDER INFO KEYS START
const val PROVIDER_INFO = "ProviderInfo"
const val PROVIDER_DISPLAY_NAME = "DisplayName"
const val PROVIDER_DESCRIPTION = "Description"
const val PROVIDER_LOCATION = "ProviderLocation"
const val PROVIDER_LOCATION_LONG = "Longitude"
const val PROVIDER_LOCATION_LAT = "Latitude"
const val PROVIDER_LOGO = "ProviderLogo"
const val PROVIDER_TERMS_OF_USE = "TermsOfUse"
const val PROVIDER_HELPDESK = "Helpdesk"
const val PROVIDER_EMAIL = "EmailAddress"
const val PROVIDER_WEB_ADDRESS = "WebAddress"
const val PROVIDER_PHONE = "Phone"
// PROVIDER INFO KEYS END

// Adds a method for getting the first element with given [tag]
private fun Element.getFirstElementByTag(tag: String): Element? {
    val element = getElementsByTagName(tag)
    return if (element.length > 0) element.item(0) as Element
    else null
}

private fun Document.getFirstElementByTag(tag: String): Element {
    val element = getElementsByTagName(tag)
    if (element.length > 0)
        return element.item(0) as Element
    else
        throw NoSuchElementException()
}

private operator fun NodeList.iterator(): Iterator<Node> =
    (0 until length).asSequence().map { item(it) as Node }.iterator()

// highly likely, that this will be used withs paths not starting with ProviderInfo
@Suppress("SameParameterValue")
private fun Document.getTextContentForXmlPath(vararg tags: String): String {
    if (tags.isEmpty()) throw IllegalArgumentException()
    var currentElement: Element = this.getFirstElementByTag(tags[0]) ?: throw NoSuchElementException()

    (1 until tags.size).forEach {
        val nextElement = currentElement.getFirstElementByTag(tags[it])
        if (nextElement != null)
            currentElement = nextElement
    }
    return currentElement.textContent
}

private fun Element.getTextContentForXmlPath(vararg tags: String): String {
    if (tags.isEmpty()) throw IllegalArgumentException()
    var currentElement: Element = this.getFirstElementByTag(tags[0]) ?: throw NoSuchElementException()

    (1 until tags.size).forEach {
        val nextElement = currentElement.getFirstElementByTag(tags[it])
        if (nextElement != null)
            currentElement = nextElement
    }
    return currentElement.textContent
}

/**
 * Parser for .eap-config files
 *
 *
 * Some methods throw NoSuchElementExceptions if the user tries to access an Element not given in the eap-config
 * The calling method must catch those exceptions and provide feedback to the user and the debug log
 */
class EapConfigParser(eapConfigFilePath: String) {
    private val tag = "EAPConfigParser"
    private val parsedConfig = EapConfig()

    private val eapConfig: Document

    init {
        val eapConfigFile = File(eapConfigFilePath)
        val configBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        eapConfig = configBuilder.parse(eapConfigFile)
    }

    fun getAuthenticationMethodElements(): NodeList =
        eapConfig.getFirstElementByTag(AUTHENTICATION_METHOD_LIST)!!.getElementsByTagName(AUTHENTICATION_METHOD)

    fun getInnerAuthMethodElements(authenticationMethodElement: Element): NodeList =
        authenticationMethodElement.getElementsByTagName(INNER_AUTHENTICATION_METHOD)

    fun getServerSideCredentialElements(authenticationMethodElement: Element): Element? =
        authenticationMethodElement.getFirstElementByTag(SERVER_SIDE_CREDENTIALS)

    fun getClientSideCredentialElements(authenticationMethodElement: Element): Element? =
        authenticationMethodElement.getFirstElementByTag(CLIENT_SIDE_CREDENTIALS)

    fun getEapType(authenticationMethodElement: Element): EapType =
        EapType.getEapType(authenticationMethodElement.getTextContentForXmlPath(EAP_METHOD, EAP_METHOD_TYPE).toInt())

    fun getServerCertificateList(serverSideCredentialElement: Element): List<X509Certificate> {
        val certificateList = ArrayList<X509Certificate>()
        val base64CertificateElementList = serverSideCredentialElement.getElementsByTagName(SERVER_SIDE_CERTIFICATE)

        base64CertificateElementList.iterator().forEach {
            val parsedCertificateBase64 = Base64.decode(it.textContent, Base64.DEFAULT)
            val parsedServerCertificate = X509Certificate.getInstance(parsedCertificateBase64)
            certificateList.add(parsedServerCertificate)
        }

        return certificateList
    }


    fun getServerId(serverSideCredentialElement: Element): List<String> {
        val serverIdList = ArrayList<String>()
        serverSideCredentialElement.getElementsByTagName(SERVER_ID).iterator()
            .forEach { serverIdList.add(it.textContent) }
        return serverIdList
    }

    fun getAllowSave(clientSideCredElt: Element): Boolean {
        return try {
            clientSideCredElt.getTextContentForXmlPath(CLIENT_SIDE_ALLOW_SAVE).toBoolean()
        } catch (e: NoSuchElementException) {
            Log.i(tag, "Tag $CLIENT_SIDE_ALLOW_SAVE not found")
            true
        }
    }

    fun getNonEapAuthMethod(innerAuthMethodElt: Element): Int =
        innerAuthMethodElt.getTextContentForXmlPath(NON_EAP_METHOD, EAP_METHOD_TYPE).toInt()


    fun getAnonymousIdentity(clientSideCredElt: Element): String =
        clientSideCredElt.getTextContentForXmlPath(CLIENT_SIDE_OUTER_IDENTITY)

    fun getSsid(): String = getFromCredentialApplicability(SSID)
    fun getMinRsnProto(): String = getFromCredentialApplicability(MIN_RSN_PROTO)
    fun getConsortiumOID(): String = getFromCredentialApplicability(CONSORTIUM_OID)

    fun getProviderDisplayName(): String =
        eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_DISPLAY_NAME)


    fun getProviderDescription(): String =
        eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_DESCRIPTION)

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

    fun getProviderLogo(): Bitmap {
        val base64LogoString = eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_LOGO)
        val base64Logo = Base64.decode(base64LogoString.toByteArray(), Base64.DEFAULT)

        return BitmapFactory.decodeByteArray(base64Logo, 0, base64Logo.size)
    }

    fun getTermsOfUse(): String =
        eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_TERMS_OF_USE)

    fun getHelpdeskEmailAddress(): String =
        eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_HELPDESK, PROVIDER_EMAIL)

    fun getHelpdeskWebAddress(): String =
        eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_HELPDESK, PROVIDER_WEB_ADDRESS)

    fun getHelpdeskPhoneNumber(): String =
        eapConfig.getTextContentForXmlPath(PROVIDER_INFO, PROVIDER_HELPDESK, PROVIDER_PHONE)

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