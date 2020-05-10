package de.unigoe.eduroamcat.backend.models

import android.icu.util.RangeValueIterator
import android.util.Log
import android.util.Xml
import org.w3c.dom.Element
import org.xmlpull.v1.XmlPullParser
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

const val AUTHENTICATION_METHOD = "AuthenticationMethod"
const val OUTER_EAP_METHOD = "EAPMethod"
const val OUTER_EAP_METHOD_TYPE = "Type"
const val SERVER_SIDE_CREDENTIALS = "ServerSideCredential"
const val SERVER_SIDE_CERTIFICATE = "CA"
const val SERVER_ID = "ServerID"
const val CLIENT_SIDE_CREDENTIALS = "ClientSideCredential"
const val CLIENT_OUTER_IDENTITY = "OuterIdentity"
const val INNER_AUTHENTICATION_METHOD = "InnerAuthenticationMethod"
const val INNER_EAP_METHOD = "EAPMethod"
const val INNER_EAP_METHOD_TYPE = "Type"
const val CREDENTIAL_APPLICABILITY = "CredentialApplicability"
const val IEEE_80211 = "IEEE80211"
const val SSID = "SSID"

class EapConfigParser {
    companion object {
        private const val tag = "EAPConfigParser"
        private val ns: String? = null
        private val parsedConfig = EapConfig()

        fun parseXml(eapConfigFilePath: String) {
            val eapConfigFile = File(eapConfigFilePath)
            val configBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val parsedConfigFile = configBuilder.parse(eapConfigFile)

            //TODO: refactor, this was only a test
            val test = ((parsedConfigFile
                .getElementsByTagName(AUTHENTICATION_METHOD).item(0) as Element)
                .getElementsByTagName(OUTER_EAP_METHOD).item(0) as Element)
                .getElementsByTagName(OUTER_EAP_METHOD_TYPE).item(0).textContent
//                .getAttribute(OUTER_EAP_METHOD_TYPE)
            Log.d(tag, test)
        }
    }
}