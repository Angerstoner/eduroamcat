package de.gwdg.wifitool.backend

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiEnterpriseConfig
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi

const val ADD_WIFI_NETWORK_SUGGESTION_REQUEST_CODE = 501

class WifiConfig(private val activity: Activity) {
    private val logTag = "WifiConfig"

    private val wifiManager: WifiManager =
        activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager


    /**
     * Method checks if an eduroam network is already set up on the system by fake-adding a
     * new eduroam connection and obtaining the return of [WifiManager.addNetwork]
     *
     * The fake connection is removed after a successful check
     */
    @Deprecated("Deprecated for API >= Android Q")
    fun hasEduroamConfiguration(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return false
        }

        val fakeEnterpriseConfig = WifiEnterpriseConfig()
        val fakeWifiConfig = WifiConfiguration()
        fakeWifiConfig.SSID = "\"eduroam\""
        fakeWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP)
        fakeWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X)
        fakeWifiConfig.enterpriseConfig = fakeEnterpriseConfig
        val networkId = wifiManager.addNetwork(fakeWifiConfig)

        return if (networkId != -1) {
            wifiManager.removeNetwork(networkId)
            false
        } else true
    }

    internal fun connectToEapNetwork(enterpriseConfig: WifiEnterpriseConfig, ssidPairList: List<Pair<String, String>>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ssidPairList.forEach { ssidPair -> connectNetworkAndroidQ(enterpriseConfig, ssidPair.first) }
        } else {
            ssidPairList.forEach { ssidPair ->
                if (hasEduroamConfiguration()) {
                    // TODO: check if old config was successfully removed for feedback screen
                    removeOldEapConnectionsDeprecated(ssidPair.first)
                }
                connectNetworkDeprecated(enterpriseConfig, ssidPair.first, ssidPair.second)
            }
        }

    }

    @Deprecated("Deprecated for API >= Android Q")
    private fun removeOldEapConnectionsDeprecated(ssid: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            activity.checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
        ) {
            wifiManager.configuredNetworks.filter { it.SSID == "\"$ssid\"" }
                .forEach { wifiManager.removeNetwork(it.networkId) }
        }
    }

    @Deprecated("Deprecated for API >= Android Q", replaceWith = ReplaceWith("connectNetworkAndroidQ()"))
    private fun connectNetworkDeprecated(
        enterpriseConfig: WifiEnterpriseConfig, ssid: String, securityProtocol: String
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val addNetworkIntent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS)
            addNetworkIntent.putParcelableArrayListExtra(Settings.EXTRA_WIFI_NETWORK_LIST, suggestions)
            activity.startActivityForResult(addNetworkIntent, ADD_WIFI_NETWORK_SUGGESTION_REQUEST_CODE)
        } else {
            // TODO: check if needed
            val removeStatus = wifiManager.removeNetworkSuggestions(suggestions)
            val addStatus = wifiManager.addNetworkSuggestions(suggestions)
            Log.d(logTag, "Remove status $removeStatus, Add status $addStatus")
        }
    }

}