package de.unigoe.eduroamcat.backend.models

import android.util.Log
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


class EapConfigParser {
    companion object {

        /**
         * Adds a method for getting the first element with given [tag]
         */
        private fun Element.getFirstElementByTagName(tag: String): Element =
            getElementsByTagName(tag).item(0) as Element

        private fun Document.getFirstElementByTagName(tag: String): Element =
            getElementsByTagName(tag).item(0) as Element


        private const val tag = "EAPConfigParser"
        private val ns: String? = null
        private val parsedConfig = EapConfig()

        fun parseXml(eapConfigFilePath: String) {
            val eapConfigFile = File(eapConfigFilePath)
            val configBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val parsedConfigFile = configBuilder.parse(eapConfigFile)

            Log.i(tag, parseProviderName(parsedConfigFile))

            //TODO: refactor, this was only a test
//            val test = ((parsedConfigFile
//                .getElementsByTagName(AUTHENTICATION_METHOD).item(0) as Element)
//                .getElementsByTagName(OUTER_EAP_METHOD).item(0) as Element)
//                .getElementsByTagName(OUTER_EAP_METHOD_TYPE).item(0).textContent
//            Log.d(tag, test)
        }

        private fun parseProviderName(eapConfig: Document): String {
            return eapConfig.getFirstElementByTagName(PROVIDER_INFO)
                .getFirstElementByTagName(PROVIDER_DISPLAY_NAME).textContent
        }
    }
}