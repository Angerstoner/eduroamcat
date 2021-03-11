package de.gwdg.wifitool.backend.util

import android.net.wifi.WifiEnterpriseConfig
import android.os.Build
import android.util.Log
import org.w3c.dom.Element
import java.security.cert.X509Certificate


class WifiEnterpriseConfigurator {
    private val logTag = "WifiEnterpriseConfigur."

    internal fun getConfigFromFile(eapConfigFilePath: String): List<WifiEnterpriseConfig> {
        val configParser = EapConfigParser(eapConfigFilePath)
        return getConfigFromFile(configParser)
    }

    internal fun getConfigFromFile(configParser: EapConfigParser): List<WifiEnterpriseConfig> {
        val wifiEnterpriseConfigList = ArrayList<WifiEnterpriseConfig>()

        val authenticationMethods = configParser.getAuthenticationMethodElements()
        for (authenticationMethod in authenticationMethods) {
            val eapConfig = WifiEnterpriseConfig()

            val serverSideCredentials = configParser.getServerSideCredentialElements(authenticationMethod)

            eapConfig.eapMethod = configParser.getEapType(authenticationMethod)
            setCertificates(eapConfig, configParser.getServerCertificates(serverSideCredentials))
            setServerIds(eapConfig, configParser.getServerIds(serverSideCredentials))

            // since EAP-TLS uses certificates on both sides, there are no further ClientSideCredentials
            // additionally, EAP-TLS and EAP-PWD have no Phase2/InnerAuth
            if (eapConfig.eapMethod == WifiEnterpriseConfig.Eap.TTLS ||
                eapConfig.eapMethod == WifiEnterpriseConfig.Eap.PEAP
            ) {
                val clientSideCredentials = configParser.getClientSideCredentialElements(authenticationMethod)

                // if the allow-saved flag is set to false, this authentication method has to be skipped
                if (!configParser.getAllowSave(clientSideCredentials)) {
                    Log.e(
                        logTag, "The allow-saved flag is set to false, " +
                                "therefore no passwords or certificates can be safed on this device." +
                                "eduroamCAT cannot use this AuthenticationMethods"
                    )
                    continue
                    // TODO: display error message to the user
                }
                eapConfig.anonymousIdentity = configParser.getAnonymousIdentity(clientSideCredentials)

                // Phase2/InnerAuth
                wifiEnterpriseConfigList.addAll(
                    getEapConfigForInnerAuthMethods(
                        eapConfig, configParser.getInnerAuthMethodElements(authenticationMethod), configParser
                    )
                )
            }
        }
        return wifiEnterpriseConfigList
    }

    /**
     * Since each different inner EAP method results in a different [WifiEnterpriseConfig],
     * all EAP methods are checked and resulting configs will be returned as
     * [List] of [WifiEnterpriseConfig] objects
     *
     * TODO: refactor
     */
    private fun getEapConfigForInnerAuthMethods(
        eapConfig: WifiEnterpriseConfig, innerAuthElement: List<Element>, configParser: EapConfigParser
    ): List<WifiEnterpriseConfig> {
        val enterpriseConfigList = ArrayList<WifiEnterpriseConfig>()

        innerAuthElement.forEachIndexed { index, innerAuthMethod ->
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
            }
            enterpriseConfigList.add(currentEnterpriseConf)
        }
        return enterpriseConfigList
    }

    private fun setCertificates(eapConfig: WifiEnterpriseConfig, serverCertificates: Array<X509Certificate>) {
        // filter for root certificates
        // if the issuer is the same as the subject, the certificate is a root certificate
        val rootCertificates = serverCertificates
            .filter { it.subjectX500Principal == it.issuerX500Principal }
            .toTypedArray()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            eapConfig.caCertificates = rootCertificates
        } else {
            eapConfig.caCertificate = rootCertificates.first()
        }
    }

    private fun setServerIds(eapConfig: WifiEnterpriseConfig, serverIds: List<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val altSubjectMatch = serverIds.joinToString(";") { "DNS:$it" }
            eapConfig.altSubjectMatch = altSubjectMatch
        } else {
            eapConfig.subjectMatch = serverIds[0]
        }
    }

}