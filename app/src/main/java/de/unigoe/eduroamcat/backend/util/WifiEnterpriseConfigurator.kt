package de.unigoe.eduroamcat.backend.util

import android.net.wifi.WifiEnterpriseConfig
import de.unigoe.eduroamcat.backend.models.EapConfigParser


class WifiEnterpriseConfigurator {
    
    public fun getConfigFromFile(eapConfigFilePath: String) {
        val configParser = EapConfigParser(eapConfigFilePath)

        val authenticationMethods = configParser.getAuthenticationMethodElements()
        authenticationMethods.iterator().forEach { 
            val eapConfig = WifiEnterpriseConfig()
            val outerEapType = configParser.getEapType(it)
        }

    }

}