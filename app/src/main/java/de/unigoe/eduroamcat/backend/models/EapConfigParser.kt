package de.unigoe.eduroamcat.backend.models

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.File

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

        fun parseXml(eapConfigFile: String) {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(File(eapConfigFile).inputStream().reader())
            parser.nextTag()


            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        AUTHENTICATION_METHOD -> readAuthenticationMethod(parser)
                        CREDENTIAL_APPLICABILITY -> readCredentialApplicability(parser)

                    }
                }
            }
        }

        private fun readAuthenticationMethod(parser: XmlPullParser) {
            parser.require(XmlPullParser.START_TAG, ns, "AuthenticationMethod")
            parser.next()
            while (parser.eventType != XmlPullParser.END_TAG) {
                when (parser.name) {
                    OUTER_EAP_METHOD -> readOuterEapMethod(parser)
                    SERVER_SIDE_CREDENTIALS -> readServerSideCredentials(parser)
                    CLIENT_SIDE_CREDENTIALS -> readClientSideCredentials(parser)
                    INNER_AUTHENTICATION_METHOD -> readInnerAuthentication(parser)
                    else -> Log.i(tag, parser.name)
                }
            }
            Log.i(tag, parsedConfig.toString())
        }

        private fun readCredentialApplicability(parser: XmlPullParser?) {

        }

        private fun readOuterEapMethod(parser: XmlPullParser) {
            parser.require(XmlPullParser.START_TAG, ns, OUTER_EAP_METHOD)
            parser.next()
            parser.require(XmlPullParser.START_TAG, ns, OUTER_EAP_METHOD_TYPE)
            parsedConfig.outerEapMethod = readText(parser)
            parser.next()
            parser.require(XmlPullParser.END_TAG, ns, OUTER_EAP_METHOD)
            parser.next()
        }

        private fun readServerSideCredentials(parser: XmlPullParser) {
            parser.require(XmlPullParser.START_TAG, ns, SERVER_SIDE_CREDENTIALS)
            parser.next()
            parser.require(XmlPullParser.START_TAG, ns, SERVER_SIDE_CERTIFICATE)
            parsedConfig.serverCertificate = readText(parser)
            while (parser.name == null || parser.name == SERVER_SIDE_CERTIFICATE) parser.next()
            parser.require(XmlPullParser.START_TAG, ns, SERVER_ID)
            parsedConfig.serverId = readText(parser)
            parser.next()
            parser.require(XmlPullParser.END_TAG, ns, SERVER_SIDE_CREDENTIALS)
            parser.next()
        }


        private fun readClientSideCredentials(parser: XmlPullParser) {
            parser.require(XmlPullParser.START_TAG, ns, CLIENT_SIDE_CREDENTIALS)
            parser.next()
            parser.require(XmlPullParser.START_TAG, ns, CLIENT_OUTER_IDENTITY)
            parsedConfig.outerIdentity = readText(parser)
            parser.next()
            parser.require(XmlPullParser.END_TAG, ns, CLIENT_SIDE_CREDENTIALS)
            parser.next()
        }

        private fun readInnerAuthentication(parser: XmlPullParser) {
            parser.next()
        }

        private fun readText(parser: XmlPullParser): String {
            var result = ""
            if (parser.next() == XmlPullParser.TEXT) {
                result = parser.text
                parser.nextTag()
            }
            return result
        }
    }
}