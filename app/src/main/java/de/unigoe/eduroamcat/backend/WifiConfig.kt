package de.unigoe.eduroamcat.backend

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build

class WifiConfig(private val activityContext: Context) {

    // TEST CODE START -> TODO: remove after initial testing
    private fun connectToOpenWifi() {
        // testing a connection to my "freifunk" router here
        val networkSsid = "\"Freifunk\""
        val wifiManager =
            activityContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val wifiNetworkSuggestion = WifiNetworkSuggestion.Builder().setSsid(networkSsid).build()
            wifiManager.addNetworkSuggestions(listOf(wifiNetworkSuggestion))
        } else {
            val wifiConf = WifiConfiguration()
            wifiConf.SSID = networkSsid
            wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            wifiManager.addNetwork(wifiConf)

            val networkList =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    activityContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) {
                    wifiManager.configuredNetworks
                } else arrayListOf()

            wifiManager.enableNetwork(networkList.first { networkSsid == it.SSID }.networkId, true)
        }
    }
    // TEST CODE END
}