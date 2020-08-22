package de.unigoe.eduroamcat.backend

import android.app.Activity
import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiEnterpriseConfig
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.annotation.RequiresApi

class WifiConfig(activity: Activity) {
    private val wifiManager: WifiManager =
        activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager


    internal fun connectToEapNetwork(
        enterpriseConfig: WifiEnterpriseConfig,
        securityProtocol: String,
        ssidPairList: List<Pair<String, String>>
    ) {
        // TODO: check permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ssidPairList.forEach { ssidPair -> connectNetworkAndroidQ(enterpriseConfig, ssidPair.first) }
        } else {
            ssidPairList.forEach { ssidPair ->
                removeOldEapConnectionsDeprecated(ssidPair.first)
                connectNetworkDeprecated(enterpriseConfig, ssidPair.first, ssidPair.second)
            }
        }
    }

    @Deprecated("Deprecated for API >= Android Q")
    private fun removeOldEapConnectionsDeprecated(ssid: String) {
        wifiManager.configuredNetworks.filter { it.SSID == "\"$ssid\"" }
            .forEach { wifiManager.removeNetwork(it.networkId) }
    }

    @Deprecated("Deprecated for API >= Android Q", replaceWith = ReplaceWith("connectNetworkAndroidQ()"))
    private fun connectNetworkDeprecated(
        enterpriseConfig: WifiEnterpriseConfig,
        ssid: String,
        securityProtocol: String
    ) {
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = "\"$ssid\""
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP)
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X)
        wifiConfig.enterpriseConfig = enterpriseConfig

        when (securityProtocol) {
            "CCMP" -> {
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
            }
            "TKIP" -> wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
        }


        val networkId = wifiManager.addNetwork(wifiConfig)
        wifiManager.enableNetwork(networkId, true)
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun connectNetworkAndroidQ(enterpriseConfig: WifiEnterpriseConfig, ssid: String) {
        val suggestions: ArrayList<WifiNetworkSuggestion> = ArrayList()
        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .setWpa2EnterpriseConfig(enterpriseConfig)
            .build()
        suggestions.add(suggestion)
        // TODO: check if needed
        wifiManager.removeNetworkSuggestions(suggestions)
        wifiManager.addNetworkSuggestions(suggestions)
    }

}