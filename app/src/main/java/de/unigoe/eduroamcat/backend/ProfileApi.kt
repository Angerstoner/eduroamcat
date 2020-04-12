package de.unigoe.eduroamcat.backend

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import java.util.*

// TEST CODE START -> TODO: remove after initial testing
const val ORGANIZATION_ID = 5042
const val LANG = "en"
// TEST CODE END


class ProfileApi(private val activityContext: Context) {
    private val tag = "ProfileApi"


    private var onProfileDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            Log.i(tag, "Download complete")
        }
    }


    /**
     * Downloads the eap-config/profile for the given organization identified by its ID
     *
     */
    fun downloadProfile(organizationId: Int) {
        val downloadManager =
            activityContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val lang = Locale.getDefault().language
        val profileDownloadUri = Uri.parse(
            "https://cat.eduroam.org/user/API.php?action=downloadInstaller" +
                    "&id=${AndroidId.getAndroidId()}" +
                    "&profile=$organizationId" +
                    "&lang=$lang"
        )
        activityContext.registerReceiver(
            onProfileDownloadComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
        activityContext.getExternalFilesDir(null)!!.mkdirs()
        downloadManager.enqueue(
            DownloadManager.Request(profileDownloadUri).setDestinationInExternalFilesDir(
                activityContext, null, "eduroam-${organizationId}_${lang}.eap-config"
            )
        )
    }

}
