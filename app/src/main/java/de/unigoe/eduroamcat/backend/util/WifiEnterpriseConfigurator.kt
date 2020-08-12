package de.unigoe.eduroamcat.backend.util

import android.net.wifi.WifiEnterpriseConfig
import android.os.Build
import android.util.Log
import de.unigoe.eduroamcat.backend.models.EapConfigParser


class WifiEnterpriseConfigurator {
    private val logTag = "WifiEnterpriseConfigur."

    public fun getConfigFromFile(eapConfigFilePath: String) {
        val configParser = EapConfigParser(eapConfigFilePath)
        val wifiEnterpriseConfigList = ArrayList<WifiEnterpriseConfig>()

        val authenticationMethods = configParser.getAuthenticationMethodElements()
        authenticationMethods.iterator().forEach {
            val eapConfig = WifiEnterpriseConfig()
            val serverSideCredentials = configParser.getServerSideCredentialElements(it)

            eapConfig.eapMethod = configParser.getEapType(it)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                eapConfig.caCertificates = configParser.getServerCertificates(serverSideCredentials)
            } else {
                eapConfig.caCertificate = configParser.getServerCertificates(serverSideCredentials).first()
            }
            // TODO: I'm sure this needs to be used somewhere. Find out where
            val serverId = configParser.getServerId(serverSideCredentials)

            // since EAP-TLS uses certificates on both sides, there are no further ClientSideCredentials
            // additionally, EAP-TLS has no Phase2/InnerAuth
            if (eapConfig.eapMethod == WifiEnterpriseConfig.Eap.TTLS ||
                eapConfig.eapMethod == WifiEnterpriseConfig.Eap.PEAP
            ) {
                val clientSideCredentials = configParser.getClientSideCredentialElements(it)
                val anonIdentity = configParser.getAnonymousIdentity(clientSideCredentials)
                eapConfig.anonymousIdentity = anonIdentity

                // Phase2/InnerAuth
                val innerAuthMethods = configParser.getInnerAuthMethodElements(it)
                innerAuthMethods.forEachIndexed { index, innerAuthMethod ->
                    val currentEnterpriseConf = if (index >= 1) WifiEnterpriseConfig(eapConfig) else eapConfig
                    try {
                        currentEnterpriseConf.phase2Method = configParser.getEapType(innerAuthMethod)
                    } catch (e: NoSuchElementException) {
                        Log.i(logTag, "No inner EAP Method found, trying Non-EAP Methods")
                        try {
                            currentEnterpriseConf.phase2Method = configParser.getNonEapAuthMethod(innerAuthMethod)
                        } catch (e: NoSuchElementException) {
                            Log.e(logTag, "No inner EAP or Non-EAP Method found. This may lead to problems")
                            e.printStackTrace()
                        }
                        wifiEnterpriseConfigList.add(currentEnterpriseConf)
                    }

                }

            }


        }
    }

}