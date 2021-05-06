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

    fun isWifiEnabled() = wifiManager.isWifiEnabled

    /**
     * Method checks if an eduroam network is already set up on the system by fake-adding a
     * new eduroam connection and obtaining the return of [WifiManager.addNetwork]. This only
     * works, if Wi-Fi is enabled. With disabled Wi-Fi this defaults to false.
     *
     * The fake connection is removed after a successful check
     */
    @Deprecated("Deprecated for API >= Android Q")
    fun hasEduroamConfiguration(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return false
        }
        if (wifiManager.setWifiEnabled(true)) {
            val fakeEnterpriseConfig = WifiEnterpriseConfig()
            val fakeWifiConfig = WifiConfiguration()
            fakeWifiConfig.SSID = "\"eduroam\""
            fakeWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP)
            fakeWifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X)
            fakeWifiConfig.enterpriseConfig = fakeEnterpriseConfig
            val networkId = wifiManager.addNetwork(fakeWifiConfig)

            // network id will be -1 if network could not be added
            return if (wifiManager.isWifiEnabled) {
                networkId == -1
            } else false
        }
        return false
    }

    internal fun connectToEapNetwork(
        enterpriseConfig: WifiEnterpriseConfig,
        ssidPairList: List<Pair<String, String>>,
    ): ArrayList<WifiConfigResult> {

        val results = ArrayList<WifiConfigResult>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ssidPairList.forEach { ssidPair -> results.add(connectNetworkAndroidQ(enterpriseConfig, ssidPair.first)) }

        } else {
            ssidPairList.forEach { ssidPair ->
                val result =
                    if (hasEduroamConfiguration()) {
                        // TODO: check for removal
                        val networkId = getNetworkIdForSsid(ssidPair.first)
                        connectNetworkBelowQ(
                            enterpriseConfig, ssidPair.first, ssidPair.second,
//                            update = true, networkId
                        )
                    } else {
                        connectNetworkBelowQ(enterpriseConfig, ssidPair.first, ssidPair.second)
                    }
                results.add(result)
            }
        }
        return results
    }

    private fun getNetworkIdForSsid(ssid: String): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            activity.checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val network = wifiManager.configuredNetworks.first { it.SSID == "\"$ssid\"" } as WifiConfiguration
                return network.networkId
            } catch (e: NoSuchElementException) {
                Log.e(logTag, "No configured network with SSID $ssid found.")
            }
        }
        return -1
    }


    @Deprecated("Deprecated for API >= Android Q", replaceWith = ReplaceWith("connectNetworkAndroidQ()"))
    private fun connectNetworkBelowQ(
        enterpriseConfig: WifiEnterpriseConfig, ssid: String, securityProtocol: String,
        update: Boolean = false, existingNetworkId: Int = -1,
    ): WifiConfigResult {
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
            "TKIP" ->
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
        }

        val networkId = if (update) {
            // TODO: check for removal
            // update existing network, identified by networkId
            // only works, if existing network has been created by this application
            wifiConfig.networkId = existingNetworkId
            wifiManager.updateNetwork(wifiConfig)
        } else {
            wifiManager.addNetwork(wifiConfig)
        }

        // finally enable network and return result
        return if (wifiManager.enableNetwork(networkId, true))
            WifiConfigResult.ANDROID_BELOW_Q_SUCCESS
        else WifiConfigResult.ANDROID_BELOW_Q_FAIL
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun connectNetworkAndroidQ(enterpriseConfig: WifiEnterpriseConfig, ssid: String): WifiConfigResult {
        // TODO: check for renaming to connectNetwork(..)

        val suggestions: ArrayList<WifiNetworkSuggestion> = ArrayList()
        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .setWpa2EnterpriseConfig(enterpriseConfig)
            .build()
        suggestions.add(suggestion)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val addNetworkIntent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS)
            addNetworkIntent.putParcelableArrayListExtra(Settings.EXTRA_WIFI_NETWORK_LIST, suggestions)
            activity.startActivityForResult(addNetworkIntent, ADD_WIFI_NETWORK_SUGGESTION_REQUEST_CODE)
            //
            WifiConfigResult.ANDROID_R_NO_RESULT
        } else {
            val addStatus = wifiManager.addNetworkSuggestions(suggestions)

            if (addStatus == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS)
                WifiConfigResult.ANDROID_Q_SUCCESS
            else WifiConfigResult.ANDROID_Q_FAIL
        }
    }

    enum class WifiConfigResult {
        ANDROID_BELOW_Q_SUCCESS, // add/enable network success
        ANDROID_BELOW_Q_FAIL, // add/enable network fail
        ANDROID_Q_SUCCESS, // adding suggestion success
        ANDROID_Q_FAIL, // adding suggestion failed
        ANDROID_R_NO_RESULT // activity has to check for intent result
    }
}