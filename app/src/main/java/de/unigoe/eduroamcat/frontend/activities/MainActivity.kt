package de.unigoe.eduroamcat.frontend.activities

import android.Manifest.permission.INTERNET
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.unigoe.eduroamcat.R
import de.unigoe.eduroamcat.backend.ProfileDownloader


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ProfileDownloader(this).downloadProfile()
//        connectToOpenWifi()
    }


    private fun connectToOpenWifi() {
        // testing a connection to my "freifunk" router here
        val networkSsid = "\"Freifunk\""
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val wifiNetworkSuggestion = WifiNetworkSuggestion.Builder().setSsid(networkSsid).build()
            wifiManager.addNetworkSuggestions(listOf(wifiNetworkSuggestion))
        } else {
            val wifiConf = WifiConfiguration()
            wifiConf.SSID = networkSsid
            wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            wifiManager.addNetwork(wifiConf)
            val networkList = wifiManager.configuredNetworks
            wifiManager.enableNetwork(networkList.first { networkSsid == it.SSID }.networkId, true)
        }

    }
}


