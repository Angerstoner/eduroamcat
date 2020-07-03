package de.unigoe.eduroamcat.backend.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

// AUTHENTICATION METHOD KEYS START
const val AUTHENTICATION_METHOD = "AuthenticationMethod"
const val OUTER_EAP_METHOD = "EAPMethod"
const val OUTER_EAP_METHOD_TYPE = "Type"
const val SERVER_SIDE_CREDENTIALS = "ServerSideCredential"
const val SERVER_SIDE_CERTIFICATE = "CA"
const val SERVER_ID = "ServerID"
const val CLIENT_SIDE_CREDENTIALS = "ClientSideCredential"
const val CLIENT_OUTER_IDENTITY = "OuterIdentity"
const val INNER_AUTHENTICATION_METHOD = "InnerAuthenticationMethod"
// AUTHENTICATION METHOD KEYS END

// CREDENTIAL APPLICABILITY KEYS START
const val CREDENTIAL_APPLICABILITY = "CredentialApplicability"
const val IEEE_80211 = "IEEE80211"
const val SSID = "SSID"
// CREDENTIAL APPLICABILITY KEYS END

// PROVIDER INFO KEYS START
const val PROVIDER_INFO = "ProviderInfo"
const val PROVIDER_DISPLAY_NAME = "DisplayName"
const val PROVIDER_DESCRIPTION = "Description"
const val PROVIDER_LOCATION = "ProviderLocation"
const val PROVIDER_LOGO = "ProviderLogo"
const val PROVIDER_TERMS_OF_USE = "TermsOfUse"
const val PROVIDER_HELPDESK = "Helpdesk"
const val PROVIDER_EMAIL = "EmailAddress"
const val PROVIDER_WEB_ADDRESS = "WebAddress"
const val PROVIDER_PHONE = "Phone"
// PROVIDER INFO KEYS END

// Adds a method for getting the first element with given [tag]
private fun Element.getFirstElementByTagName(tag: String): Element =
    getElementsByTagName(tag).item(0) as Element

private fun Document.getFirstElementByTagName(tag: String): Element =
    getElementsByTagName(tag).item(0) as Element


class EapConfigParser(eapConfigFilePath: String) {
    private val tag = "EAPConfigParser"
    private val ns: String? = null
    private val parsedConfig = EapConfig()

    private val eapConfig: Document

    init {
        val eapConfigFile = File(eapConfigFilePath)
        val configBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        eapConfig = configBuilder.parse(eapConfigFile)
    }

    fun getProviderDisplayName(): String = eapConfig.getFirstElementByTagName(PROVIDER_INFO)
        .getFirstElementByTagName(PROVIDER_DISPLAY_NAME).textContent

    fun getProviderDescription(): String =
        eapConfig.getFirstElementByTagName(PROVIDER_INFO)
            .getFirstElementByTagName(PROVIDER_DESCRIPTION).textContent

    fun getProviderLogo(): Bitmap {
        val base64LogoString = eapConfig.getFirstElementByTagName(PROVIDER_INFO)
            .getFirstElementByTagName(PROVIDER_LOGO).textContent
        val base64Logo = Base64.decode(base64LogoString.toByteArray(), Base64.DEFAULT)

        return BitmapFactory.decodeByteArray(base64Logo, 0, base64Logo.size)
    }

    fun getTermsOfUse(): String = eapConfig.getFirstElementByTagName(PROVIDER_INFO)
        .getFirstElementByTagName(PROVIDER_TERMS_OF_USE).textContent

    fun getHelpdeskEmailAddress(): String = eapConfig.getFirstElementByTagName(PROVIDER_INFO)
        .getFirstElementByTagName(PROVIDER_HELPDESK)
        .getFirstElementByTagName(PROVIDER_EMAIL).textContent

    fun getHelpdeskWebAddress(): String = eapConfig.getFirstElementByTagName(PROVIDER_INFO)
        .getFirstElementByTagName(PROVIDER_HELPDESK)
        .getFirstElementByTagName(PROVIDER_WEB_ADDRESS).textContent

    fun getHelpdeskPhoneNumber(): String = eapConfig.getFirstElementByTagName(PROVIDER_INFO)
        .getFirstElementByTagName(PROVIDER_HELPDESK)
        .getFirstElementByTagName(PROVIDER_PHONE).textContent
}