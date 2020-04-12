package de.unigoe.eduroamcat.frontend.activities

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager
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

        requestAppPermissions()
        ProfileDownloader(this).downloadProfile()
//        connectToOpenWifi()
    }


    /**
     * Requests needed permissions if Android M or higher is used
     * Android L and lower use only the AndroidManifest to grant permissions
     */
    private fun requestAppPermissions() {
        val permissionsNeeded = arrayOf(ACCESS_FINE_LOCATION)
        val permissionsRequestCode = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionsNeeded, permissionsRequestCode)
        }

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

            val networkList =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                ) {
                    wifiManager.configuredNetworks
                } else arrayListOf()

            wifiManager.enableNetwork(networkList.first { networkSsid == it.SSID }.networkId, true)
        }

    }
}


